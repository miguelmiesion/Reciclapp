package com.example.reciclapp.views

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.reciclapp.R
import org.json.JSONObject
import java.util.concurrent.Executors
import com.example.reciclapp.engine.QrCodeAnalyzer
import com.example.reciclapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.reciclapp.components.LocalPopupState
import com.example.reciclapp.network.NetworkResult
import com.example.reciclapp.repository.WasteRepository
import com.example.reciclapp.ui.theme.DarkerPrimary
import com.example.reciclapp.ui.theme.Primary
import androidx.navigation.NavController
import com.example.reciclapp.components.ReciclappBottomBar


import com.example.reciclapp.components.ProfileDropdown
import com.example.reciclapp.network.TokenManager

@Composable
fun ScanQrScreen(navController: NavController, tokenManager: TokenManager) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val popupController = LocalPopupState.current

    val wasteRepository = remember { WasteRepository(RetrofitClient.getApi(context)) }

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
        bottomBar = { ReciclappBottomBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Escaneá el QR",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                ProfileDropdown(navController, tokenManager, Modifier.align(Alignment.CenterEnd))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Reciclaste! Ahora escaneá el código qr del cesto para obtener tus puntos!",
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(thickness = 2.dp, color = DarkerPrimary)

            Spacer(modifier = Modifier.height(30.dp))

            if (hasCamPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    CameraPreview(
                        onQrScanned = { resultString ->
                            if (!isProcessing && popupController.currentResult == null) {
                                isProcessing = true

                                scope.launch(Dispatchers.IO) {
                                    try {

                                        val jsonQr = JSONObject(resultString)
                                        val wasteId = jsonQr.getString("ID Residuo")
                                        val points = jsonQr.optInt("Puntos", 0)

                                        val result = wasteRepository.claimWaste(wasteId)

                                        withContext(Dispatchers.Main) {
                                            when (result) {
                                                is NetworkResult.Success -> {

                                                    playSound(context)
                                                    popupController.showSuccess("Sumaste $points puntos!")
                                                }
                                                is NetworkResult.Error -> {

                                                    popupController.showError(result.message ?: "Error desconocido")
                                                }
                                            }
                                        }

                                    } catch (_: Exception) {
                                        withContext(Dispatchers.Main) {
                                            popupController.showError("Código QR inválido")
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
    LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
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


fun playSound(context: android.content.Context) {
    try {
        val mp = MediaPlayer.create(context, R.raw.neo_geo_coin)
        mp.start()
        mp.setOnCompletionListener { it.release() }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
