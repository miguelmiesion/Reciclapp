package com.example.reciclapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.network.NetworkResult
import com.example.reciclapp.network.Station
import com.example.reciclapp.repository.MapsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

data class MapsUiState(
    val stations: List<Station> = emptyList(),
    val routePoints: List<GeoPoint>? = null,
    val currentAddress: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedRouteType: RouteType = RouteType.DRIVING,
    val currentRouteStart: GeoPoint? = null,
    val currentRouteEnd: GeoPoint? = null
)

enum class RouteType(val displayName: String, val apiProfile: String) {
    DRIVING("En auto", "car"),
    WALKING("Caminando", "foot")
}

class MapsViewModel(private val repository: MapsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(MapsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        fetchStations()
    }

    private fun recalculateActiveRoute(start: GeoPoint, end: GeoPoint, routeType: RouteType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val points = repository.calculateRoute(start, end, routeType.apiProfile)
                if (!points.isNullOrEmpty()) {
                    _uiState.update { it.copy(routePoints = points, isLoading = false) }
                } else {
                    _uiState.update { it.copy(error = "No se pudo recalcular la ruta", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al recalcular: ${e.message}", isLoading = false) }
            }
        }
    }

    fun updateRouteType(newType: RouteType) {
        _uiState.update { it.copy(selectedRouteType = newType) }

        val state = _uiState.value
        if (state.currentRouteStart != null && state.currentRouteEnd != null) {
            recalculateActiveRoute(state.currentRouteStart, state.currentRouteEnd, newType)
        }
    }

    fun clearRoute() {
        _uiState.update {
            it.copy(
                routePoints = null,
                currentRouteStart = null,
                currentRouteEnd = null
            )
        }
    }

    fun fetchStations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                when (val result = repository.getStations()) {
                    is NetworkResult.Success -> {
                        _uiState.update {
                            it.copy(
                                stations = result.data ?: emptyList(),
                                isLoading = false
                            )
                        }
                    }

                    is NetworkResult.Error -> {
                        _uiState.update {
                            it.copy(
                                error = result.message ?: "Error al cargar estaciones",
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun drawRouteToStation(userLocation: GeoPoint, station: Station) {
        viewModelScope.launch {
            val endPoint = GeoPoint(station.latitude, station.longitude)

            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    currentRouteStart = userLocation,
                    currentRouteEnd = endPoint
                )
            }

            try {
                val currentRouteType = _uiState.value.selectedRouteType
                val points = repository.calculateRoute(userLocation, endPoint, currentRouteType.apiProfile)

                if (points.isNullOrEmpty()) {
                    _uiState.update {
                        it.copy(error = "No se pudo encontrar una ruta", isLoading = false)
                    }
                } else {
                    _uiState.update {
                        it.copy(routePoints = points, isLoading = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Error de conexión al calcular ruta", isLoading = false)
                }
            }
        }
    }

    fun resolveAddressForLocation(geoPoint: GeoPoint) {
        viewModelScope.launch {
            _uiState.update { it.copy(currentAddress = null) }

            val address = repository.getAddress(geoPoint)

            _uiState.update {
                it.copy(currentAddress = address ?: "Dirección no encontrada")
            }
        }
    }
}

class MapsViewModelFactory(private val repository: MapsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}