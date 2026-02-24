package com.example.reciclapp.views

import android.Manifest
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.reciclapp.components.ReciclappBottomBar
import com.example.reciclapp.components.RouteTypeSelector
import com.example.reciclapp.components.StationDetailCard
import com.example.reciclapp.components.StationSelector
import com.example.reciclapp.network.RetrofitClient
import com.example.reciclapp.network.Station
import com.example.reciclapp.repository.MapsRepository
import com.example.reciclapp.ui.theme.DarkerPrimary
import com.example.reciclapp.ui.theme.LighterPrimary
import com.example.reciclapp.viewmodels.MapsViewModel
import com.example.reciclapp.viewmodels.MapsViewModelFactory
import com.example.reciclapp.viewmodels.RouteType
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import android.graphics.DashPathEffect

@Composable
fun MapsScreen(navController: NavController) {
    val context = LocalContext.current

    Configuration.getInstance()
        .load(context, PreferenceManager.getDefaultSharedPreferences(context))
    Configuration.getInstance().userAgentValue = context.packageName

    val viewModel: MapsViewModel = viewModel(
        factory = MapsViewModelFactory(MapsRepository(RetrofitClient.getApi(context), context))
    )

    val state by viewModel.uiState.collectAsState()

    var selectedStation by remember { mutableStateOf<Station?>(null) }

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

                        marker.setOnMarkerClickListener { _, _ ->
                            if (selectedStation != station) {
                                viewModel.clearRoute()
                            }
                            selectedStation = station
                            val location = GeoPoint(station.latitude, station.longitude)
                            viewModel.resolveAddressForLocation(location)

                            map.controller.animateTo(location)
                            true
                        }

                        map.overlays.add(marker)
                    }

                    // Renderizado de la ruta
                    state.routePoints?.let { points ->
                        val routeLine = Polyline(map)

                        routeLine.setPoints(points)

                        routeLine.outlinePaint.color = DarkerPrimary.toArgb()
                        routeLine.outlinePaint.strokeWidth = 16f

                        if (state.selectedRouteType == RouteType.WALKING) {
                            routeLine.outlinePaint.pathEffect = DashPathEffect(floatArrayOf(30f, 20f), 0f)
                        } else {
                            routeLine.outlinePaint.pathEffect = null
                        }

                        routeLine.setOnClickListener { _, _, _ -> false }

                        map.overlays.add(routeLine)
                    }

                    map.invalidate()
                }
            )

            Box(modifier = Modifier.align(Alignment.TopCenter)) {
                StationSelector(
                    stations = state.stations,
                    onStationSelected = { station ->
                        if (selectedStation != station) {
                            viewModel.clearRoute()
                        }
                        
                        val targetPoint = GeoPoint(station.latitude, station.longitude)
                        mapView?.controller?.animateTo(targetPoint)
                        mapView?.controller?.setZoom(18.0)
                        selectedStation = station
                    }
                )
            }

            AnimatedVisibility(
                visible = selectedStation != null,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    RouteTypeSelector(
                        selectedType = state.selectedRouteType,
                        onTypeSelected = { newType -> viewModel.updateRouteType(newType) },
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth(0.9f) // Ajusta el ancho según tu diseño
                    )

                    selectedStation?.let { station ->
                        StationDetailCard(
                            station = station,
                            onClose = { selectedStation = null },
                            resolvedAddress = state.currentAddress,
                            onCalculateRoute = {
                                val myLocation = locationOverlay?.myLocation

                                if (myLocation != null) {
                                    viewModel.drawRouteToStation(myLocation, station)
                                    selectedStation = null
                                }
                            }
                        )
                    }
                }
            }

            if (selectedStation == null) {
                FloatingActionButton(
                    containerColor = LighterPrimary,
                    contentColor = DarkerPrimary,
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
                    Icon(imageVector = Icons.Default.MyLocation, contentDescription = null)
                }
            }
        }
        if (state.isLoading) {
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
}