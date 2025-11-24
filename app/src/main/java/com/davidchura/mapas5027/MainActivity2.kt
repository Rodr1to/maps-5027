package com.davidchura.mapas5027

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.davidchura.mapas5027.ui.theme.Mapas5027Theme
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.MarkerState.Companion.invoke
import com.google.maps.android.compose.rememberCameraPositionState
import com.davidchura.mapas5027.BuildConfig

class MainActivity2 : ComponentActivity() {
    private lateinit var placesClient: PlacesClient

    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Places.initializeWithNewPlacesApiEnabled(applicationContext,
            BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(this)

        setContent {
            Mapas5027Theme {
                var consulta by remember { mutableStateOf("") }
                var predicciones by remember { mutableStateOf(listOf<HashMap<String,String>>()) }
                var ubicacionSeleccionada by remember { mutableStateOf<LatLng?>(null) }

                val ubicacion = LatLng(-12.093711795939, -77.05290448442663)
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
                    )
                }
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .background(color = Color.White)) {

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Buscar un lugar") },
                        value = consulta,
                        onValueChange = { nuevaConsulta ->
                            consulta = nuevaConsulta
                            if (nuevaConsulta.length > 4) {
                                val areaCircular: CircularBounds = CircularBounds.newInstance(
                                ubicacion, 2000.0)
                                val request = FindAutocompletePredictionsRequest.builder()
                                    .setQuery(nuevaConsulta)
                                    .setCountries("PE", "CL", "BO")
                                    .setLocationRestriction(areaCircular)
                                    //.setTypesFilter(listOf(PlaceTypes.RESTAURANT, PlaceTypes.BAR, PlaceTypes.CAFE))
                                    .build()
                                placesClient.findAutocompletePredictions(request)
                                    .addOnSuccessListener { response ->
                                        predicciones =
                                            response.autocompletePredictions.map { prediccion ->
                                                hashMapOf(
                                                    "place_id" to prediccion.placeId,
                                                    "descripcion" to prediccion.getFullText(null)
                                                        .toString()
                                                )
                                            }
                                    }.addOnFailureListener {
                                        Toast.makeText(
                                            this@MainActivity2,
                                            "No hay resultados",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                    )//OutlinedTextField
                    Column(modifier = Modifier.fillMaxWidth()) {
                        predicciones.forEach { prediccion ->
                            Text(text = prediccion["descripcion"] ?: "",
                                modifier = Modifier.padding(16.dp, 4.dp)
                                    .clickable{
                                        val placeId = prediccion["place_id"] ?: ""
                                        val placeLocation = listOf(Place.Field.LOCATION)
                                        val placeRequest = FetchPlaceRequest.newInstance(
                                            placeId, placeLocation)
                                        placesClient.fetchPlace(placeRequest)
                                            .addOnSuccessListener { response ->
                                                ubicacionSeleccionada = response.place.location
                                                cameraPositionState.position =
                                                    CameraPosition.fromLatLngZoom(
                                                        ubicacionSeleccionada!!, 19f)
                                                predicciones = emptyList()
                                                consulta = prediccion["descripcion"].toString()
                                            }
                                    })
                        }
                    }
                }
            }
        }
    }
}

