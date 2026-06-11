package com.example.travelapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.example.travelapp.data.model.Trip
import com.example.travelapp.data.model.TripStatus
import com.example.travelapp.viewmodel.AppViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.runtime.livedata.observeAsState

@Composable
fun MapScreen(viewModel: AppViewModel, navController: NavController) {
    val context = LocalContext.current
    var permissionGranted by remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

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
        if (!permissionGranted) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val trips by viewModel.getTripsForCurrentUser().observeAsState(emptyList())
    val currentUser by viewModel.currentUser.collectAsState()
    TrackableMapViewWithTrips(trips, navController, currentUser?.profileImageUri)
}

@Composable
fun TrackableMapViewWithTrips(trips: List<Trip>, navController: NavController, profileImageUri: String? = null) {
    val context = LocalContext.current

    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

    val userMarkerRef = remember { mutableStateOf<Marker?>(null) }

    val map = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setBuiltInZoomControls(false)
            setMultiTouchControls(true)
            isHorizontalMapRepetitionEnabled = false
            isVerticalMapRepetitionEnabled = false
            minZoomLevel = 4.0
            controller.setZoom(5.0)
            setScrollableAreaLimitLatitude(80.0, -80.0, 0)
            setScrollableAreaLimitLongitude(
                MapView.getTileSystem().minLongitude,
                MapView.getTileSystem().maxLongitude,
                0
            )
            controller.setCenter(GeoPoint(41.9028, 12.4964)) // Italia come default
        }
    }

    DisposableEffect(Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var locationListener: LocationListener? = null

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            lastLocation?.let {
                val startPoint = GeoPoint(it.latitude, it.longitude)
                map.controller.setZoom(15.0)
                map.controller.setCenter(startPoint)

                val userMarker = Marker(map).apply {
                    position = startPoint
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = userProfileMarkerIcon(context, profileImageUri)
                    setOnMarkerClickListener { _, _ ->
                        navController.navigate("profile") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        true
                    }
                }
                userMarkerRef.value = userMarker
                map.overlays.add(userMarker)

                locationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        val geoPoint = GeoPoint(location.latitude, location.longitude)
                        map.controller.setCenter(geoPoint)
                        userMarker.position = geoPoint
                        map.invalidate()
                    }
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 2f, locationListener!!)
            }
        }

        onDispose {
            locationListener?.let { locationManager.removeUpdates(it) }
            map.onDetach()
        }
    }

    AndroidView(
        factory = { map },
        modifier = Modifier.fillMaxSize()
    ) { mapView ->
        // Rimuovi i marker dei viaggi precedenti e riaggiungi quelli aggiornati
        mapView.overlays.removeAll(
            mapView.overlays.filterIsInstance<Marker>().filter { it != userMarkerRef.value }
        )
        trips.forEach { trip ->
            val lat = trip.latitude ?: return@forEach
            val lon = trip.longitude ?: return@forEach
            val tripMarker = Marker(mapView).apply {
                position = GeoPoint(lat, lon)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = trip.name
                icon = tripStatusMarkerIcon(mapView.context, trip.status)
                setOnMarkerClickListener { _, _ ->
                    navController.navigate("tripDetail/${trip.id}")
                    true
                }
            }
            mapView.overlays.add(tripMarker)
        }
        mapView.invalidate()
    }
}

private fun userProfileMarkerIcon(context: Context, profileImageUri: String?): Drawable {
    val size = 64
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val r = size / 2f - 4f

    if (profileImageUri != null) {
        val source = runCatching {
            context.contentResolver.openInputStream(Uri.parse(profileImageUri))?.use {
                android.graphics.BitmapFactory.decodeStream(it)
            }
        }.getOrNull()

        if (source != null) {
            val scaled = Bitmap.createScaledBitmap(source, size, size, true)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            }
            canvas.drawCircle(size / 2f, size / 2f, r, paint)
        } else {
            drawFallbackCircle(canvas, size, r)
        }
    } else {
        drawFallbackCircle(canvas, size, r)
    }

    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    canvas.drawCircle(size / 2f, size / 2f, r, strokePaint)
    return BitmapDrawable(context.resources, bitmap)
}

private fun drawFallbackCircle(canvas: Canvas, size: Int, r: Float) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#2196F3")
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, r, paint)
}

// Crea un'icona circolare colorata per il marker in base allo status del viaggio.
// OSMDroid wiki:  https://github.com/osmdroid/osmdroid/wiki/Markers,-Lines-and-Polygons-(Java)
// Canvas drawing: https://medium.com/over-engineering/getting-started-with-drawing-on-the-android-canvas-621cf512f4c7
private fun tripStatusMarkerIcon(context: Context, status: TripStatus): Drawable {
    val color = when (status) {
        TripStatus.DRAFT       -> android.graphics.Color.parseColor("#9E9E9E") // grigio
        TripStatus.PLANNED     -> android.graphics.Color.parseColor("#FF9800") // arancione
        TripStatus.IN_PROGRESS -> android.graphics.Color.parseColor("#2196F3") // blu
        TripStatus.COMPLETED   -> android.graphics.Color.parseColor("#4CAF50") // verde
    }
    val size = 64
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    val r = size / 2f - 4
    canvas.drawCircle(size / 2f, size / 2f, r, fillPaint)
    canvas.drawCircle(size / 2f, size / 2f, r, strokePaint)
    return BitmapDrawable(context.resources, bitmap)
}
