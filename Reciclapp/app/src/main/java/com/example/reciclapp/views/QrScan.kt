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
import org.json.JSONObject
import java.util.concurrent.Executors
import com.example.reciclapp.engine.QrCodeAnalyzer
// Importamos Retrofit y Modelos
import com.example.reciclapp.network.RetrofitClient
import com.example.reciclapp.network.WasteClaimRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.reciclapp.components.LocalPopupState
import com.example.reciclapp.ui.theme.DarkerPrimary
import com.example.reciclapp.ui.theme.Primary

@Composable
fun ScanQrScreen() {
    val context = LocalContext.current
    // Nota: TokenManager ya no lo instanciamos acá manualmente para la red,
    // porque RetrofitClient ya lo usa internamente en el Interceptor.

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

            HorizontalDivider(thickness = 2.dp, color = DarkerPrimary)

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
                            // 2. No escanear si procesando O si hay popup abierto
                            if (!isProcessing && popupController.currentResult == null) {
                                isProcessing = true

                                scope.launch(Dispatchers.IO) {
                                    try {
                                        println("QR Detectado...")

                                        // 1. Parseamos el String del QR (Sigue siendo un JSON String)
                                        val jsonQr = JSONObject(result)
                                        val idResiduo = jsonQr.getString("ID Residuo")
                                        val puntos = jsonQr.getInt("Puntos")

                                        // 2. RETROFIT: Preparamos la llamada
                                        val api = RetrofitClient.getApi(context)
                                        val request = WasteClaimRequest(idWaste = idResiduo)

                                        // 3. Ejecutamos la llamada (Suspend function)
                                        val response = api.claimWaste(request)

                                        withContext(Dispatchers.Main) {
                                            if (response.isSuccessful) {
                                                // --- ÉXITO (200) ---
                                                try {
                                                    val mp = MediaPlayer.create(context, R.raw.neo_geo_coin)
                                                    mp.start()
                                                    mp.setOnCompletionListener { it.release() }
                                                } catch (e: Exception) { e.printStackTrace() }

                                                popupController.showSuccess("Sumaste $puntos puntos!")

                                            } else {
                                                // --- ERROR (400, 401, 404, 500) ---
                                                // Retrofit lee el stream de error automáticamente
                                                val errorBodyString = response.errorBody()?.string()
                                                var errorMsg = "Error al procesar el código QR"

                                                if (!errorBodyString.isNullOrEmpty()) {
                                                    try {
                                                        Log.e("RETROFIT_ERROR", errorBodyString)
                                                        val jsonError = JSONObject(errorBodyString)

                                                        if (jsonError.has("error")) {
                                                            errorMsg = jsonError.getString("error")
                                                        } else if (jsonError.has("detail")) {
                                                            errorMsg = jsonError.getString("detail")
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.e("RETROFIT_PARSE", "No se pudo parsear el error JSON")
                                                    }
                                                }

                                                popupController.showError(errorMsg)
                                            }
                                        }

                                    } catch (e: Exception) {
                                        // Errores de red (timeout, sin internet)
                                        e.printStackTrace()
                                        withContext(Dispatchers.Main) {
                                            popupController.showError("Error de conexión: ${e.localizedMessage}")
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

// ... El resto del archivo (CameraPreview, QrOverlay, ReciclappBottomBar) queda IGUAL ...
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