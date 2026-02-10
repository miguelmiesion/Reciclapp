package com.example.reciclapp.views


import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.reciclapp.components.LocalPopupState
import com.example.reciclapp.components.ProfileDropdown
import com.example.reciclapp.components.ReciclappBottomBar
import com.example.reciclapp.database.ReciclappDatabase
import com.example.reciclapp.engine.QrCodeAnalyzer
import com.example.reciclapp.network.RetrofitClient
import com.example.reciclapp.network.TokenManager
import com.example.reciclapp.repository.WasteRepository
import com.example.reciclapp.ui.theme.DarkerPrimary
import com.example.reciclapp.utils.playSound
import com.example.reciclapp.viewmodels.QrScanViewModel
import com.example.reciclapp.viewmodels.QrScanViewModelFactory
import kotlinx.coroutines.MainScope
import java.util.concurrent.Executors

@Composable
fun ScanQrScreen(navController: NavController, tokenManager: TokenManager) {
    val context = LocalContext.current
    val popupController = LocalPopupState.current

    val wasteRepository = remember { WasteRepository(RetrofitClient.getApi(context),
        ReciclappDatabase.getDatabase(context, MainScope())) }

    val viewModel: QrScanViewModel = viewModel(
        factory = QrScanViewModelFactory(wasteRepository)
    )

    var isProcessing by remember { mutableStateOf(false) }

    val state by viewModel.uiState.collectAsState()

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

    LaunchedEffect(state) {
        if (state.success) {
            playSound(context)
            popupController.showSuccess(state.message ?: "Felicidades, reciclaste con éxito!")
            viewModel.resetState()
            isProcessing = false
        } else if (state.error) {
            popupController.showError(state.message ?: "Ocurrió un error desconocido")
            viewModel.resetState()
            isProcessing = false
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
                                viewModel.claimWaste(resultString)
                            }
                        }
                    )
                    QrOverlay()
                    if (isProcessing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = DarkerPrimary
                            )
                        }
                    }
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