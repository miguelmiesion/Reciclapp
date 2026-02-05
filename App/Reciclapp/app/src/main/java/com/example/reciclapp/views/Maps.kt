package com.example.reciclapp.views

import android.Manifest
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.reciclapp.components.ReciclappBottomBar
import com.example.reciclapp.network.RetrofitClient
import com.example.reciclapp.repository.MapsRepository
import com.example.reciclapp.viewmodels.MapsViewModel
import com.example.reciclapp.viewmodels.MapsViewModelFactory
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MapsScreen(navController: NavController) {
    val context = LocalContext.current

    Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
    Configuration.getInstance().userAgentValue = context.packageName

    val viewModel: MapsViewModel = viewModel(
        factory = MapsViewModelFactory(MapsRepository(RetrofitClient.getApi(context)))
    )

    val state by viewModel.uiState.collectAsState()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasLocationPermission = isGranted }
    )

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var locationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        viewModel.fetchStations()
    }

    Scaffold(
        bottomBar = { ReciclappBottomBar(navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)
                        controller.setCenter(GeoPoint(-34.9214, -57.9545))

                        val overlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                        overlay.enableMyLocation()
                        overlay.enableFollowLocation()
                        overlays.add(overlay)

                        locationOverlay = overlay
                        mapView = this
                    }
                },
                update = { map ->
                    map.overlays.removeAll { it !is MyLocationNewOverlay }

                    if (hasLocationPermission) {
                        locationOverlay?.enableMyLocation()
                    }

                    state.stations.forEach { station ->
                        val marker = Marker(map)
                        marker.position = GeoPoint(station.latitude, station.longitude)
                        marker.title = station.name
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        map.overlays.add(marker)
                    }

                    map.invalidate()
                }   
            )

            FloatingActionButton(
                onClick = {
                    if (hasLocationPermission) {
                        val location = locationOverlay?.myLocation
                        if (location != null) {
                            mapView?.controller?.animateTo(location)
                            mapView?.controller?.setZoom(18.0)
                        }
                    } else {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null
                )
            }
        }
    }
}