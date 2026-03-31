package it.mygroup.org.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import it.mygroup.org.R
import org.json.JSONArray
import org.json.JSONObject

data class Reserve(
    val name: String,
    val location: LatLng,
    val type: ReserveType,
    val region: String? = null,
    val notes: String? = null
)

enum class ReserveType {
    HUNTING, FISHING, BOTH
}

val staticReserves = listOf(
    // --- PIEMONTE ---
    Reserve("Comprensorio Alpino CN1 Valle Gesso", LatLng(44.2500, 7.3500), ReserveType.HUNTING, "Piemonte"),
    Reserve("Lago d'Orta", LatLng(45.8167, 8.4000), ReserveType.FISHING, "Piemonte"),
    Reserve("Lago di Viverone", LatLng(45.4167, 8.0500), ReserveType.FISHING, "Piemonte"),
    Reserve("Fiume Sesia (Zona No-Kill)", LatLng(45.7500, 8.2667), ReserveType.FISHING, "Piemonte", "Acque a regolamentazione speciale"),
    Reserve("ATC AL1 (Alessandria)", LatLng(44.9126, 8.6151), ReserveType.HUNTING, "Piemonte"),

    // --- VALLE D'AOSTA ---
    Reserve("Riserva di Caccia Valle di Rhemes", LatLng(45.5667, 7.1167), ReserveType.HUNTING, "Valle d'Aosta"),
    Reserve("Lago di Place-Moulin", LatLng(45.8933, 7.4700), ReserveType.FISHING, "Valle d'Aosta"),

    // --- LOMBARDIA ---
    Reserve("Riserva Naturale Pian di Spagna", LatLng(46.1833, 9.4000), ReserveType.FISHING, "Lombardia"),
    Reserve("Lago di Como (Ramo di Lecco)", LatLng(45.8500, 9.3833), ReserveType.FISHING, "Lombardia"),
    Reserve("Fiume Ticino (Parco Lombardo)", LatLng(45.3000, 8.9000), ReserveType.BOTH, "Lombardia"),
    Reserve("Lago d'Iseo", LatLng(45.7167, 10.0833), ReserveType.FISHING, "Lombardia"),
    Reserve("ATC Oltrepò Pavese", LatLng(44.9000, 9.1000), ReserveType.HUNTING, "Lombardia"),

    // --- TRENTINO-ALTO ADIGE ---
    Reserve("Lago di Caldonazzo", LatLng(46.0000, 11.2000), ReserveType.FISHING, "Trentino"),
    Reserve("Fiume Adige (Zona Bolzano)", LatLng(46.4833, 11.3333), ReserveType.FISHING, "Alto Adige"),
    Reserve("Riserva di Caccia Val di Funes", LatLng(46.6400, 11.6800), ReserveType.HUNTING, "Alto Adige"),

    // --- VENETO ---
    Reserve("Laguna di Caorle", LatLng(45.6000, 12.9000), ReserveType.FISHING, "Veneto"),
    Reserve("Altopiano di Asiago", LatLng(45.8758, 11.5097), ReserveType.HUNTING, "Veneto"),
    Reserve("Delta del Po Veneto", LatLng(44.9667, 12.4167), ReserveType.BOTH, "Veneto"),
    Reserve("Lago di Santa Croce", LatLng(46.1167, 12.3333), ReserveType.FISHING, "Veneto"),

    // --- FRIULI-VENEZIA GIULIA ---
    Reserve("Riserva Naturale Valle Canal Novo", LatLng(45.7167, 13.1333), ReserveType.FISHING, "Friuli-Venezia Giulia"),
    Reserve("Lago di Cavazzo", LatLng(46.3333, 13.0667), ReserveType.FISHING, "Friuli-Venezia Giulia"),
    Reserve("Riserva di Caccia Tarvisio", LatLng(46.5000, 13.5833), ReserveType.HUNTING, "Friuli-Venezia Giulia"),

    // --- LIGURIA ---
    Reserve("Parco dell'Antola", LatLng(44.5667, 9.1333), ReserveType.HUNTING, "Liguria"),
    Reserve("Lago di Brugneto", LatLng(44.5333, 9.2000), ReserveType.FISHING, "Liguria"),

    // --- EMILIA-ROMAGNA ---
    Reserve("ATC BO2 (Bologna)", LatLng(44.5500, 11.3500), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("Valli di Comacchio", LatLng(44.6167, 12.1667), ReserveType.BOTH, "Emilia-Romagna"),
    Reserve("Parco Regionale del Delta del Po", LatLng(44.8000, 12.2333), ReserveType.BOTH, "Emilia-Romagna"),
    Reserve("Fiume Po (Tratto Piacentino)", LatLng(45.0500, 9.7000), ReserveType.FISHING, "Emilia-Romagna"),
    Reserve("Lago di Suviana", LatLng(44.1167, 11.0500), ReserveType.FISHING, "Emilia-Romagna"),

    // --- TOSCANA ---
    Reserve("Lago di Massaciuccoli", LatLng(43.8333, 10.3333), ReserveType.FISHING, "Toscana"),
    Reserve("Riserva di San Rossore", LatLng(43.7167, 10.3000), ReserveType.HUNTING, "Toscana"),
    Reserve("Lago di Chiusi", LatLng(43.0000, 11.9500), ReserveType.FISHING, "Toscana"),
    Reserve("ATC Arezzo AR1", LatLng(43.4667, 11.8833), ReserveType.HUNTING, "Toscana"),
    Reserve("Padule di Fucecchio", LatLng(43.8000, 10.8000), ReserveType.BOTH, "Toscana"),
    Reserve("Lago di Bilancino", LatLng(43.9800, 11.3300), ReserveType.FISHING, "Toscana"),
    Reserve("ATC Grosseto GR6", LatLng(42.7600, 11.1100), ReserveType.HUNTING, "Toscana"),
    Reserve("Fiume Arno (Tratto Fiorentino)", LatLng(43.7700, 11.2500), ReserveType.FISHING, "Toscana"),
    Reserve("Riserva di Caccia Monterufoli", LatLng(43.3000, 10.7500), ReserveType.HUNTING, "Toscana"),
    Reserve("Lago di Vagli", LatLng(44.1100, 10.2900), ReserveType.FISHING, "Toscana"),
    Reserve("Fiume Ombrone", LatLng(42.8000, 11.2000), ReserveType.FISHING, "Toscana"),
    Reserve("ATC Siena 3", LatLng(43.1000, 11.7000), ReserveType.HUNTING, "Toscana"),
    Reserve("Riserva di Caccia Le Corniole", LatLng(43.3939687, 11.6716462), ReserveType.HUNTING, "Toscana"),
    Reserve("Azienda Faunistica Venatoria Le Forane", LatLng(42.4893860, 11.3519838), ReserveType.HUNTING, "Toscana"),
    Reserve("Caccia Toscana – San Gimignano", LatLng(43.4364301, 11.0007402), ReserveType.HUNTING, "Toscana"),
    Reserve("Laghi di Bellavalle", LatLng(43.9768030, 11.2169887), ReserveType.FISHING, "Toscana"),
    Reserve("Riserva Naturale Ponte a Buriano e Penna", LatLng(43.5011712, 11.8026065), ReserveType.BOTH, "Toscana"),

    // --- UMBRIA ---
    Reserve("Lago Trasimeno", LatLng(43.1300, 12.1000), ReserveType.FISHING, "Umbria"),
    Reserve("Lago di Corbara", LatLng(42.7167, 12.2833), ReserveType.FISHING, "Umbria"),
    Reserve("ATC Perugia PG1", LatLng(43.1167, 12.3833), ReserveType.HUNTING, "Umbria"),

    // --- MARCHE ---
    Reserve("Riserva Naturale Gola della Rossa", LatLng(43.4000, 13.0000), ReserveType.BOTH, "Marche"),
    Reserve("Lago di Gerosa", LatLng(42.9500, 13.3833), ReserveType.FISHING, "Marche"),

    // --- LAZIO ---
    Reserve("Riserva Naturale del Litorale Romano", LatLng(41.7456, 12.2747), ReserveType.BOTH, "Lazio"),
    Reserve("Riserva Naturale della Marcigliana", LatLng(42.0006, 12.5350), ReserveType.HUNTING, "Lazio"),
    Reserve("Lago di Bracciano", LatLng(42.1208, 12.2333), ReserveType.FISHING, "Lazio"),
    Reserve("Lago di Bolsena", LatLng(42.5933, 11.9358), ReserveType.FISHING, "Lazio"),
    Reserve("ATC Rieti RI1", LatLng(42.4013, 12.8622), ReserveType.HUNTING, "Lazio"),

    // --- ABRUZZO ---
    Reserve("Lago di Campotosto", LatLng(42.5333, 13.3833), ReserveType.FISHING, "Abruzzo"),
    Reserve("Parco Nazionale del Gran Sasso", LatLng(42.5000, 13.5500), ReserveType.BOTH, "Abruzzo"),
    Reserve("Lago di Scanno", LatLng(41.9167, 13.8667), ReserveType.FISHING, "Abruzzo"),

    // --- MOLISE ---
    Reserve("Lago di Guardialfiera", LatLng(41.8000, 14.8333), ReserveType.FISHING, "Molise"),
    Reserve("ATC Campobasso", LatLng(41.5667, 14.6667), ReserveType.HUNTING, "Molise"),

    // --- CAMPANIA ---
    Reserve("Lago del Matese", LatLng(41.4167, 14.4333), ReserveType.FISHING, "Campania"),
    Reserve("Oasi di Persano", LatLng(40.5500, 15.1000), ReserveType.FISHING, "Campania"),
    Reserve("ATC Salerno", LatLng(40.6833, 14.7667), ReserveType.HUNTING, "Campania"),

    // --- PUGLIA ---
    Reserve("Laghi Alimini", LatLng(40.2000, 18.4000), ReserveType.FISHING, "Puglia"),
    Reserve("Lago di Varano", LatLng(41.8833, 15.7500), ReserveType.FISHING, "Puglia"),
    Reserve("Parco Nazionale del Gargano", LatLng(41.8000, 15.9000), ReserveType.BOTH, "Puglia"),

    // --- BASILICATA ---
    Reserve("Lago di Pertusillo", LatLng(40.2667, 15.9333), ReserveType.FISHING, "Basilicata"),
    Reserve("Lago San Giuliano", LatLng(40.6333, 16.5000), ReserveType.FISHING, "Basilicata"),

    // --- CALABRIA ---
    Reserve("Lago Arvo", LatLng(39.2167, 16.6333), ReserveType.FISHING, "Calabria"),
    Reserve("Parco Nazionale della Sila", LatLng(39.3000, 16.5000), ReserveType.BOTH, "Calabria"),
    Reserve("Lago di Tarsia", LatLng(39.6333, 16.2667), ReserveType.FISHING, "Calabria"),

    // --- SICILIA ---
    Reserve("Riserva Naturale Vendicari", LatLng(36.8000, 15.1000), ReserveType.BOTH, "Sicilia"),
    Reserve("Lago Pozzillo", LatLng(37.6333, 14.6167), ReserveType.FISHING, "Sicilia"),
    Reserve("Lago di Piana degli Albanesi", LatLng(37.9833, 13.3000), ReserveType.FISHING, "Sicilia"),
    Reserve("ATC Catania", LatLng(37.5000, 15.0833), ReserveType.HUNTING, "Sicilia"),

    // --- SARDEGNA ---
    Reserve("Stagno di Cabras", LatLng(39.9500, 8.4500), ReserveType.FISHING, "Sardegna"),
    Reserve("Lago Omodeo", LatLng(40.1333, 8.9167), ReserveType.FISHING, "Sardegna"),
    Reserve("ATC CA1 (Cagliari)", LatLng(39.3000, 9.0000), ReserveType.HUNTING, "Sardegna"),
    Reserve("Riserva di Caccia Monte Arcosu", LatLng(39.1833, 8.8833), ReserveType.HUNTING, "Sardegna")
)

@Composable
fun MapsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val sharedPrefs = remember { context.getSharedPreferences("maps_prefs", Context.MODE_PRIVATE) }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(42.0, 12.5), 6f)
    }

    // Custom reserves management
    val customReserves = remember { 
        mutableStateListOf<Reserve>().apply {
            val json = sharedPrefs.getString("custom_reserves", "[]") ?: "[]"
            try {
                val arr = JSONArray(json)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(Reserve(
                        name = obj.getString("name"),
                        location = LatLng(obj.getDouble("lat"), obj.getDouble("lng")),
                        type = ReserveType.valueOf(obj.getString("type")),
                        region = obj.optString("region", ""),
                        notes = obj.optString("notes", "")
                    ))
                }
            } catch (e: Exception) { Log.e("MapsScreen", "Error loading custom reserves", e) }
        }
    }

    fun saveCustomReserves() {
        val arr = JSONArray()
        customReserves.forEach { res ->
            arr.put(JSONObject().apply {
                put("name", res.name)
                put("lat", res.location.latitude)
                put("lng", res.location.longitude)
                put("type", res.type.name)
                put("region", res.region ?: "")
                put("notes", res.notes ?: "")
            })
        }
        sharedPrefs.edit().putString("custom_reserves", arr.toString()).apply()
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        locationPermissionGranted = isGranted
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionGranted) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            LatLng(it.latitude, it.longitude), 10f
                        )
                    }
                }
            } catch (e: SecurityException) { }
        }
    }
    
    val uiSettings by remember { mutableStateOf(MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = true)) }
    val properties by remember(locationPermissionGranted) { 
        mutableStateOf(MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = locationPermissionGranted)) 
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                shape = MaterialTheme.shapes.medium,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = 45.dp, end = 60.dp) // Alzato a 32.dp dal fondo
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Aggiungi Località")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        GoogleMap(
            modifier = Modifier.fillMaxSize().padding(padding),
            cameraPositionState = cameraPositionState,
            properties = properties,
            uiSettings = uiSettings
        ) {
            (staticReserves + customReserves).forEach { reserve ->
                val typeText = when(reserve.type) {
                    ReserveType.HUNTING -> "Riserva di Caccia"
                    ReserveType.FISHING -> "Zona di Pesca"
                    ReserveType.BOTH -> "Caccia e Pesca"
                }
                val snippetText = buildString {
                    append(typeText)
                    if (!reserve.region.isNullOrBlank()) append(" - ${reserve.region}")
                    if (!reserve.notes.isNullOrBlank()) append("\n${reserve.notes}")
                    append("\n\nClicca qui per indicazioni stradali")
                }
                Marker(
                    state = MarkerState(position = reserve.location),
                    title = reserve.name,
                    snippet = snippetText,
                    onInfoWindowClick = {
                        val gmmIntentUri = Uri.parse("google.navigation:q=${reserve.location.latitude},${reserve.location.longitude}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(mapIntent)
                        } else {
                            // Fallback if Google Maps app is not installed
                            context.startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
                        }
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddReserveDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newRes ->
                customReserves.add(newRes)
                saveCustomReserves()
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddReserveDialog(onDismiss: () -> Unit, onConfirm: (Reserve) -> Unit) {
    var name by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf("") }
    var lng by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(ReserveType.HUNTING) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuova Località") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxWidth()) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") })
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = lat, onValueChange = { lat = it }, label = { Text("Lat") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    OutlinedTextField(value = lng, onValueChange = { lng = it }, label = { Text("Lng") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }
                Spacer(Modifier.height(8.dp))
                Box {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(when(type) {
                            ReserveType.HUNTING -> "Caccia"
                            ReserveType.FISHING -> "Pesca"
                            ReserveType.BOTH -> "Entrambi"
                        })
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("Caccia") }, onClick = { type = ReserveType.HUNTING; expanded = false })
                        DropdownMenuItem(text = { Text("Pesca") }, onClick = { type = ReserveType.FISHING; expanded = false })
                        DropdownMenuItem(text = { Text("Entrambi") }, onClick = { type = ReserveType.BOTH; expanded = false })
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = region, onValueChange = { region = it }, label = { Text("Regione") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Note") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val l = lat.toDoubleOrNull() ?: 0.0
                    val g = lng.toDoubleOrNull() ?: 0.0
                    onConfirm(Reserve(name, LatLng(l, g), type, region, notes))
                },
                enabled = name.isNotBlank() && lat.isNotBlank() && lng.isNotBlank()
            ) { Text("Aggiungi") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } }
    )
}
