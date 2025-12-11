package com.example.reciclapp.views

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.reciclapp.R
import com.example.reciclapp.BuildConfig
import org.json.JSONObject
import java.util.concurrent.Executors
import com.example.reciclapp.engine.QrCodeAnalyzer
import com.example.reciclapp.network.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI
import com.example.reciclapp.components.LocalPopupState // Importante
import com.example.reciclapp.ui.theme.Primary

@Composable
fun ScanQrScreen() {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val scope = rememberCoroutineScope()

    // 1. OBTENEMOS EL CONTROLADOR DEL POPUP GLOBAL
    val popupController = LocalPopupState.current

    var isProcessing by remember { mutableStateOf(false) }
    var hasCamPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCamPermission = granted }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCamPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        bottomBar = { ReciclappBottomBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Escaneá el QR",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Reciclaste! Ahora escaneá el código qr del cesto para obtener tus puntos!",
                fontSize = 14.sp,
                color = Color.DarkGray,
                lineHeight = 20.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(thickness = 2.dp, color = ReciclappGreen)

            Spacer(modifier = Modifier.height(30.dp))

            // --- AREA DE CÁMARA ---
            if (hasCamPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    CameraPreview(
                        onQrScanned = { result ->
                            // 2. MODIFICADO: No escanear si procesando O si hay popup abierto
                            if (!isProcessing && popupController.currentResult == null) {
                                isProcessing = true

                                scope.launch(Dispatchers.IO) {
                                    try {
                                        println("QR Detectado...")

                                        val json = JSONObject(result)
                                        val idResiduo = json.getString("ID Residuo")
                                        val puntos = json.getInt("Puntos")

                                        val url = URI.create(BuildConfig.API_URL + "api/residuo/reclamar/").toURL()
                                        val connection = url.openConnection() as HttpURLConnection

                                        connection.setRequestProperty("Authorization", "Bearer ${tokenManager.getAccessToken()}")
                                        connection.requestMethod = "POST"
                                        connection.setRequestProperty("Content-Type", "application/json; utf-8")
                                        connection.doOutput = true
                                        connection.connectTimeout = 5000

                                        val jsonObject = JSONObject()
                                        jsonObject.put("id_residuo", idResiduo)

                                        connection.outputStream.use { os ->
                                            val input = jsonObject.toString().toByteArray(Charsets.UTF_8)
                                            os.write(input, 0, input.size)
                                        }

                                        val responseCode = connection.responseCode
                                        println("API Response: $responseCode")

                                        withContext(Dispatchers.Main) {
                                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                                // --- CASO 1: ÉXITO (200) ---
                                                try {
                                                    val mp = MediaPlayer.create(context, R.raw.neo_geo_coin)
                                                    mp.start()
                                                    mp.setOnCompletionListener { it.release() }
                                                } catch (e: Exception) { e.printStackTrace() }

                                                popupController.showSuccess(puntos)

                                            } else if (responseCode >= 400) { // Atrapa 400, 401, 403, 404, 500...

                                            var errorMsg = "Error al procesar el código QR" // Mensaje por defecto

                                            try {
                                                // 1. Verificamos si existe el canal de error
                                                val stream = connection.errorStream
                                                Log.e("ErrorStream", stream.bufferedReader().use { it.readText() })

                                                if (stream != null) {
                                                    // 2. Leemos el texto crudo
                                                    val errorRaw = stream.bufferedReader().use { it.readText() }

                                                    // --- ¡MIRA ESTO EN TU LOGCAT! ---
                                                    println("DEBUG_SERVER_ERROR: $errorRaw")

                                                    if (errorRaw.isNotEmpty()) {
                                                        // 3. Intentamos parsear
                                                        val json = JSONObject(errorRaw)

                                                        // Tu servidor usa "error" según me mostraste
                                                        if (json.has("error")) {
                                                            errorMsg = json.getString("error")
                                                        }
                                                        // Por si acaso usa otro formato
                                                        else if (json.has("detail")) {
                                                            errorMsg = json.getString("detail")
                                                        }
                                                    }
                                                } else {
                                                    println("DEBUG_SERVER_ERROR: El stream llegó NULO (El servidor mandó 400 pero sin cuerpo JSON)")
                                                }

                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                Log.e("DEBUG_EXCEPTION:", "Falló el parseo del error")
                                            }

                                            // Mostramos el mensaje final (sea el del JSON o el por defecto)
                                            popupController.showError(errorMsg)

                                            } else {
                                                popupController.showError("Ha ocurrido un error inesperado.")
                                            }
                                        }
                                        connection.disconnect()

                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        withContext(Dispatchers.Main) {
                                            popupController.showError("Error de lectura: ${e.localizedMessage}")
                                        }
                                    } finally {

                                        isProcessing = false
                                    }
                                }
                            }
                        }
                    )
                    QrOverlay()
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Se requiere permiso de cámara")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Centrá el código QR en el cuadrado", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

// ... CameraPreview, QrOverlay y ReciclappBottomBar quedan igual ...
@Composable
fun CameraPreview(onQrScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var lastScannedTime by remember { mutableLongStateOf(0L) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                this.scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            val cameraExecutor = Executors.newSingleThreadExecutor()
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, QrCodeAnalyzer { qrContent ->
                            val currentTime = System.currentTimeMillis()
                            // Debounce de 3s para evitar spam a la API
                            if (currentTime - lastScannedTime > 3000) {
                                lastScannedTime = currentTime
                                previewView.post { onQrScanned(qrContent) }
                            }
                        })
                    }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalyzer)
                } catch (exc: Exception) { exc.printStackTrace() }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun QrOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val squareSize = canvasWidth * 0.6f
        val left = (canvasWidth - squareSize) / 2
        val top = (canvasHeight - squareSize) / 2

        drawRect(color = Color(0xFF333333).copy(alpha = 0.85f))
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(squareSize, squareSize),
            cornerRadius = CornerRadius(16.dp.toPx()),
            blendMode = BlendMode.Clear
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(squareSize, squareSize),
            cornerRadius = CornerRadius(16.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun ReciclappBottomBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .height(70.dp)
            .clip(RoundedCornerShape(35.dp))
            .background(Color(0xFFA5D6A7))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Tag, "Menu", tint = Color(0xFF424242), modifier = Modifier.size(28.dp))
            Icon(Icons.Outlined.CalendarToday, "Calendario", tint = Color(0xFF424242), modifier = Modifier.size(28.dp))
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.CameraAlt, "Escanear", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Icon(Icons.Outlined.CardGiftcard, "Premios", tint = Color(0xFF424242), modifier = Modifier.size(28.dp))
            Icon(Icons.Outlined.Person, "Perfil", tint = Color(0xFF424242), modifier = Modifier.size(28.dp))
        }
    }
}