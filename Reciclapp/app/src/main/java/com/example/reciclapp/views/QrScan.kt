package com.example.reciclapp.views

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import android.widget.Toast
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
import org.json.JSONObject
import java.util.concurrent.Executors
import com.example.reciclapp.engine.QrCodeAnalyzer

@Composable
fun ScanQrScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
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
        onResult = { granted ->
            hasCamPermission = granted
        }
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

            // Título
            Text(
                text = "Escaneá el QR",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Descripción
            Text(
                text = "Reciclaste! Ahora escaneá el código qr del cesto para obtener tus puntos!",
                fontSize = 14.sp,
                color = Color.DarkGray,
                lineHeight = 20.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Línea divisoria Verde
            HorizontalDivider(
                thickness = 2.dp,
                color = ReciclappGreen // Usando tu variable global
            )

            Spacer(modifier = Modifier.height(30.dp))

            // --- AREA DE CÁMARA ---
            if (hasCamPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f) // Cuadrado
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    CameraPreview(
                        onQrScanned = { result ->
                            // PROCESAMIENTO DEL JSON
                            try {
                                val json = JSONObject(result)
                                val idResiduo = json.getString("ID Residuo")
                                val puntos = json.getInt("Puntos")
                                val tipo = json.getString("Tipo Residuo")



                            } catch (e: Exception) {
                                // El QR no tenía el formato JSON esperado
                            }
                        }
                    )

                    // Overlay Oscuro con Hueco
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

            Text(
                text = "Centrá el código QR en el cuadrado",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun CameraPreview(onQrScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Evitamos escanear multiples veces el mismo codigo muy rapido
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

            // Executor para el análisis en background
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
                            // Simple debounce de 2 segundos
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastScannedTime > 3000) {
                                lastScannedTime = currentTime
                                // Volvemos al hilo principal para UI
                                previewView.post {
                                    onQrScanned(qrContent)
                                }
                            }
                        })
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )
                } catch (exc: Exception) {
                    exc.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

// Dibuja el oscurecimiento alrededor del cuadro central
@Composable
fun QrOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val squareSize = canvasWidth * 0.6f // El cuadro central es el 60% del ancho
        val left = (canvasWidth - squareSize) / 2
        val top = (canvasHeight - squareSize) / 2

        // Capa Oscura semi-transparente (Color gris oscuro del diseño)
        drawRect(
            color = Color(0xFF333333).copy(alpha = 0.85f),
        )

        // "Borramos" el centro usando BlendMode.Clear para que se vea la cámara
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(squareSize, squareSize),
            cornerRadius = CornerRadius(16.dp.toPx()), // Bordes redondeados
            blendMode = BlendMode.Clear
        )

        // (Opcional) Dibujar un borde blanco fino alrededor del hueco para que se vea mejor
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
    // Barra flotante estilo "Isla" como en la imagen
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp) // Margen para que flote
            .height(70.dp)
            .clip(RoundedCornerShape(35.dp)) // Muy redondeado
            .background(Color(0xFFA5D6A7)) // Un verde claro (Similar al de la imagen)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono 1: Hash
            Icon(
                imageVector = Icons.Default.Tag, // #
                contentDescription = "Menu",
                tint = Color(0xFF424242),
                modifier = Modifier.size(28.dp)
            )

            // Icono 2: Calendario
            Icon(
                imageVector = Icons.Outlined.CalendarToday,
                contentDescription = "Calendario",
                tint = Color(0xFF424242),
                modifier = Modifier.size(28.dp)
            )

            // Icono 3: Cámara (Activo)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp)) // Forma cuadrada redondeada del botón central
                    .background(ReciclappGreen), // Verde oscuro activo
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = "Escanear",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Icono 4: Regalo
            Icon(
                imageVector = Icons.Outlined.CardGiftcard,
                contentDescription = "Premios",
                tint = Color(0xFF424242),
                modifier = Modifier.size(28.dp)
            )

            // Icono 5: Perfil
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = "Perfil",
                tint = Color(0xFF424242),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}