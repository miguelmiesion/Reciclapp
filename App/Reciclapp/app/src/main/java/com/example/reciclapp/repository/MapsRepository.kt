package com.example.reciclapp.repository

import android.content.Context
import android.location.Address
import com.example.reciclapp.network.BaseApiResponse
import com.example.reciclapp.network.NetworkResult
import com.example.reciclapp.network.ReciclappApi
import com.example.reciclapp.network.Station
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.location.GeocoderNominatim
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.util.GeoPoint
import java.util.Locale

class MapsRepository(private val api: ReciclappApi, private val context: Context) :
    BaseApiResponse() {

    suspend fun getStations(): NetworkResult<List<Station>> {
        return safeApiCall { api.getStations() }
    }
    suspend fun calculateRoute(
        start: GeoPoint,
        end: GeoPoint,
        apiProfile: String
    ): List<GeoPoint>? = withContext(Dispatchers.IO) {
        val roadManager = OSRMRoadManager(context, "Reciclapp/1.0")

        val routeMean = when(apiProfile) {
            "foot" -> OSRMRoadManager.MEAN_BY_FOOT
            "car" -> OSRMRoadManager.MEAN_BY_CAR
            else -> OSRMRoadManager.MEAN_BY_CAR
        }

        roadManager.setMean(routeMean)

        val waypoints = ArrayList<GeoPoint>()
        waypoints.add(start)
        waypoints.add(end)

        val road = roadManager.getRoad(waypoints)

        if (road.mStatus != Road.STATUS_OK) {
            return@withContext null
        }

        return@withContext road.mRouteHigh
    }

    suspend fun getAddress(geoPoint: GeoPoint): String? = withContext(Dispatchers.IO) {
        try {
            val geocoder = GeocoderNominatim(Locale.getDefault(), "Reciclapp/1.0")

            val addresses: List<Address> = geocoder.getFromLocation(
                geoPoint.latitude,
                geoPoint.longitude,
                1
            )

            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                return@withContext address.getAddressLine(0)
            }

            return@withContext null

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}