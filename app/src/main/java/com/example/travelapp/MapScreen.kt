package com.example.travelapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.example.travelapp.data.model.Trip
import com.example.travelapp.viewmodel.AppViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState

@Composable
fun MapScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }

    // Richiesta permessi
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            permissionGranted = granted
            if (!granted) {
                Toast.makeText(context, "Permesso necessario per vedere la posizione", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    if (permissionGranted) {
        val trips by viewModel.getTripsForCurrentUser().observeAsState(emptyList())
        TrackableMapViewWithTrips(trips)
    }
}


@Composable
fun TrackableMapViewWithMarker(trips: List<Trip>) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
    }

    val map = remember { MapView(context) }

    AndroidView(
        factory = { map },
        modifier = Modifier.fillMaxSize()
    ) { mapView ->
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        // Marker viaggi
        trips.forEach { trip ->
            val tripMarker = Marker(mapView)
            tripMarker.position = GeoPoint(trip.latitude!!, trip.longitude!!)
            tripMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            tripMarker.title = trip.name // o qualsiasi campo descrittivo
            mapView.overlays.add(tripMarker)
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Centra sulla posizione iniziale
            val lastLocation: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            lastLocation?.let {
                val startPoint = GeoPoint(it.latitude, it.longitude)
                mapView.controller.setZoom(15.0)
                mapView.controller.setCenter(startPoint)

                // Marker iniziale
                val userMarker = Marker(mapView)
                userMarker.position = startPoint
                userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                userMarker.title = "Tu sei qui"
                mapView.overlays.add(userMarker)

                // Listener per tracking in tempo reale
                val locationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        val geoPoint = GeoPoint(location.latitude, location.longitude)
                        mapView.controller.setCenter(geoPoint)
                        userMarker.position = geoPoint
                        mapView.invalidate() // aggiorna la mappa
                    }
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000L, // ogni 2 secondi
                    2f,    // o ogni 2 metri
                    locationListener
                )
            }
        }
    }
}

@Composable
fun TrackableMapViewWithTrips(trips: List<Trip>) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
    }

    val map = remember { MapView(context) }

    AndroidView(
        factory = { map },
        modifier = Modifier.fillMaxSize()
    ) { mapView ->
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        // Centra sulla posizione iniziale
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            lastLocation?.let {
                val startPoint = GeoPoint(it.latitude, it.longitude)
                mapView.controller.setZoom(15.0)
                mapView.controller.setCenter(startPoint)

                // Marker per la posizione dell'utente
                val userMarker = Marker(mapView).apply {
                    position = startPoint
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Tu sei qui"
                }
                mapView.overlays.add(userMarker)

                // Listener per tracking in tempo reale
                val locationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        val geoPoint = GeoPoint(location.latitude, location.longitude)
                        mapView.controller.setCenter(geoPoint)
                        userMarker.position = geoPoint
                        mapView.invalidate()
                    }
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 2f, locationListener)
            }
        }

        // Marker per i viaggi dell'utente
        trips.forEach { trip ->
            val lat = trip.latitude
            val lon = trip.longitude
            if (lat != null && lon != null) {
                val tripMarker = Marker(mapView).apply {
                    position = GeoPoint(lat, lon)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = trip.name
                }
                mapView.overlays.add(tripMarker)
            }
        }

        mapView.invalidate()
    }
}
