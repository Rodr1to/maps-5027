package com.davidchura.mapas5027

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import com.davidchura.mapas5027.ui.theme.Mapas5027Theme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var latitud by mutableStateOf(0.0)
    var longitud by mutableStateOf(0.0)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        obtenerUbicacionActual()
        enableEdgeToEdge()
    }

    @SuppressLint("UnrememberedMutableState")
    fun dibujarMapa() {
        setContent {
            Mapas5027Theme {
                //val ubicacion = LatLng(-12.093711795939, -77.05290448442663)
                val ubicacion = LatLng(latitud, longitud)
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(ubicacion, 18f)
                }
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = MarkerState(position = ubicacion),
                        title = "ISIL",
                        snippet = "Estamos aquí",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                        onClick = { marker ->
                            Toast.makeText(this@MainActivity, marker.title, Toast.LENGTH_SHORT).show()
                            true
                        }

                    )
                    Circle(
                        //center = ubicacion,
                        center = LatLng(-12.125384385934547, -77.02482493062013),
                        radius = 200.0,
                        fillColor = Color(0x30FF0000),
                        strokeColor = Color(0x10FF0000),
                        clickable = true,
                        onClick = {
                            Toast.makeText(this@MainActivity, "Zona de cobertura", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Polygon(
                        points = listOf(
                            LatLng(-12.041398403103493, -77.03409014746201),
                            LatLng(-12.0487400338798, -77.0391061291173),
                            LatLng(-12.054492235162652, -77.03023863591336),
                            LatLng(-12.043608357166883, -77.02450844856838),
                            LatLng(-12.043496152005906, -77.02919097058644)
                        ),
                        fillColor = Color(0x30FFFF00),
                        strokeColor = Color(0x10FFFF00),
                        clickable = true,
                        onClick = {
                            Toast.makeText(this@MainActivity, "Area restringida", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Polyline(
                        points = listOf(
                            LatLng(-12.06509052905659, -77.03363452886627),
                            LatLng(-12.063728712254036, -77.03411151246458),
                            LatLng(-12.064472014495104, -77.03919757581274),
                            LatLng(-12.06064094025216, -77.04127163363805)
                        ),
                        width = 5f,
                        color = Color(0xFF0000FF),
                        clickable = true,
                        onClick = {
                            Toast.makeText(this@MainActivity, "Ruta del desfile", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
    fun obtenerUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            solicitarPermisos()
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                latitud = location?.latitude ?: 0.0
                longitud = location?.longitude ?: 0.0
                dibujarMapa()
            }
    }
    fun solicitarPermisos() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when{
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    obtenerUbicacionActual()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    obtenerUbicacionActual()
                }
            }
        }
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}
