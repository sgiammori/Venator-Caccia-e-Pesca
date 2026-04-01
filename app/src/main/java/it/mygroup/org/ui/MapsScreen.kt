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
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
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
import it.mygroup.org.ui.theme.WidthClass
import it.mygroup.org.ui.theme.rememberResponsiveUiSpec
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
    // LISTA AGGIUNTIVA CHE TI HO DATO
    Reserve("ATC TO1 Torino", LatLng(45.0703, 7.6869), ReserveType.HUNTING, "Piemonte"),
    Reserve("ATC CN4 Mondovì", LatLng(44.3833, 7.8167), ReserveType.HUNTING, "Piemonte"),
    Reserve("Comprensorio Alpino VC2 Valsesia", LatLng(45.8500, 8.0833), ReserveType.HUNTING, "Piemonte"),
    Reserve("AFV La Baraggia", LatLng(45.5167, 8.1667), ReserveType.HUNTING, "Piemonte"),
    Reserve("AFV Tenuta Bonicelli", LatLng(45.4333, 7.8833), ReserveType.HUNTING, "Piemonte"),
    // ATC COMPLETI PIEMONTE
    Reserve("ATC AL2", LatLng(44.8167, 8.6167), ReserveType.HUNTING, "Piemonte"),
    Reserve("ATC AL3", LatLng(44.7000, 8.5667), ReserveType.HUNTING, "Piemonte"),
    Reserve("ATC AT1", LatLng(44.9000, 8.2000), ReserveType.HUNTING, "Piemonte"),
    Reserve("ATC AT2", LatLng(44.8500, 8.1500), ReserveType.HUNTING, "Piemonte"),
    Reserve("ATC BI1", LatLng(45.5667, 8.0500), ReserveType.HUNTING, "Piemonte"),
    Reserve("ATC CN1", LatLng(44.4500, 7.5500), ReserveType.HUNTING, "Piemonte"),
    Reserve("ATC CN2", LatLng(44.5500, 7.7000), ReserveType.HUNTING, "Piemonte"),
    Reserve("ATC CN3", LatLng(44.6000, 7.9000), ReserveType.HUNTING, "Piemonte"),
    Reserve("ATC NO1", LatLng(45.4500, 8.6167), ReserveType.HUNTING, "Piemonte"),
    Reserve("ATC NO2", LatLng(45.5500, 8.4500), ReserveType.HUNTING, "Piemonte"),
    Reserve("ATC TO2", LatLng(45.1000, 7.5000), ReserveType.HUNTING, "Piemonte"),
    Reserve("ATC TO3", LatLng(45.2000, 7.6000), ReserveType.HUNTING, "Piemonte"),
    Reserve("ATC VB1", LatLng(46.0000, 8.4500), ReserveType.HUNTING, "Piemonte"),
    Reserve("ATC VC1", LatLng(45.3167, 8.4167), ReserveType.HUNTING, "Piemonte"),
    Reserve("ATC VC2", LatLng(45.4500, 8.3000), ReserveType.HUNTING, "Piemonte"),
    // COMPRENSORI ALPINI COMPLETI PIEMONTE
    Reserve("CA TO1 Alta Val Susa", LatLng(45.1000, 6.9667), ReserveType.HUNTING, "Piemonte"),
    Reserve("CA TO2 Bassa Val Susa", LatLng(45.0667, 7.2000), ReserveType.HUNTING, "Piemonte"),
    Reserve("CA TO3 Valli di Lanzo", LatLng(45.3333, 7.3333), ReserveType.HUNTING, "Piemonte"),
    Reserve("CA CN1 Valle Gesso", LatLng(44.2500, 7.3500), ReserveType.HUNTING, "Piemonte"),
    Reserve("CA CN2 Valle Stura", LatLng(44.3500, 7.1833), ReserveType.HUNTING, "Piemonte"),
    Reserve("CA CN3 Valle Maira", LatLng(44.4667, 7.1667), ReserveType.HUNTING, "Piemonte"),
    Reserve("CA CN4 Valle Varaita", LatLng(44.5833, 7.0167), ReserveType.HUNTING, "Piemonte"),
    Reserve("CA VC1 Alta Valsesia", LatLng(45.8500, 8.0833), ReserveType.HUNTING, "Piemonte"),
    Reserve("CA VB1 Ossola", LatLng(46.1333, 8.2833), ReserveType.HUNTING, "Piemonte"),
    // AFV PRINCIPALI PIEMONTE
    Reserve("AFV Tenuta della Mandria", LatLng(45.1500, 7.5833), ReserveType.HUNTING, "Piemonte"),
    Reserve("AFV Valcasotto", LatLng(44.2667, 7.8833), ReserveType.HUNTING, "Piemonte"),
    Reserve("AFV Rocca de’ Baldi", LatLng(44.3833, 7.7333), ReserveType.HUNTING, "Piemonte"),
    Reserve("AFV Castelmagno", LatLng(44.4333, 7.2333), ReserveType.HUNTING, "Piemonte"),
    Reserve("AFV Valle Pesio", LatLng(44.3167, 7.6167), ReserveType.HUNTING, "Piemonte"),
    //--- VALLE D'AOSTA ---
    Reserve("Riserva di Caccia Valle di Rhemes", LatLng(45.5667, 7.1167), ReserveType.HUNTING, "Valle d'Aosta"),
    Reserve("Lago di Place-Moulin", LatLng(45.8933, 7.4700), ReserveType.FISHING, "Valle d'Aosta"),
    // LISTA AGGIUNTIVA CHE TI HO DATO
    Reserve("Comprensorio Alpino Valgrisenche", LatLng(45.5833, 7.0333), ReserveType.HUNTING, "Valle d'Aosta"),
    Reserve("Comprensorio Alpino Cervinia", LatLng(45.9333, 7.6333), ReserveType.HUNTING, "Valle d'Aosta"),
    // COMPRENSORI ALPINI COMPLETI VALLE D’AOSTA
    Reserve("CA 01 Monte Bianco", LatLng(45.8333, 6.8667), ReserveType.HUNTING, "Valle d'Aosta"),
    Reserve("CA 02 Valdigne", LatLng(45.7500, 7.0500), ReserveType.HUNTING, "Valle d'Aosta"),
    Reserve("CA 03 Valgrisenche", LatLng(45.5833, 7.0333), ReserveType.HUNTING, "Valle d'Aosta"),
    Reserve("CA 04 La Thuile", LatLng(45.7167, 6.9500), ReserveType.HUNTING, "Valle d'Aosta"),
    Reserve("CA 05 Valpelline", LatLng(45.8500, 7.3333), ReserveType.HUNTING, "Valle d'Aosta"),
    Reserve("CA 06 Gran San Bernardo", LatLng(45.8833, 7.1667), ReserveType.HUNTING, "Valle d'Aosta"),
    Reserve("CA 07 Valtournenche", LatLng(45.8833, 7.6000), ReserveType.HUNTING, "Valle d'Aosta"),
    Reserve("CA 08 Ayas", LatLng(45.8000, 7.7000), ReserveType.HUNTING, "Valle d'Aosta"),
    Reserve("CA 09 Gressoney", LatLng(45.7833, 7.8167), ReserveType.HUNTING, "Valle d'Aosta"),
    Reserve("CA 10 Champorcher", LatLng(45.6333, 7.6500), ReserveType.HUNTING, "Valle d'Aosta"),
    Reserve("CA 11 Cogne", LatLng(45.6000, 7.3500), ReserveType.HUNTING, "Valle d'Aosta"),
    Reserve("CA 12 Valsavarenche", LatLng(45.5667, 7.2000), ReserveType.HUNTING, "Valle d'Aosta"),
    Reserve("CA 13 Rhêmes", LatLng(45.5667, 7.1167), ReserveType.HUNTING, "Valle d'Aosta"),
    Reserve("CA 14 Valgrisenche", LatLng(45.5833, 7.0333), ReserveType.HUNTING, "Valle d'Aosta"),
    // AFV PRINCIPALI VALLE D’AOSTA
    Reserve("AFV Mont Avic", LatLng(45.6500, 7.6500), ReserveType.HUNTING, "Valle d'Aosta"),
    Reserve("AFV Saint-Marcel", LatLng(45.7333, 7.4167), ReserveType.HUNTING, "Valle d'Aosta"),
    Reserve("AFV Champdepraz", LatLng(45.6833, 7.6500), ReserveType.HUNTING, "Valle d'Aosta"),
    Reserve("AFV Brusson", LatLng(45.7500, 7.7333), ReserveType.HUNTING, "Valle d'Aosta"),
    Reserve("AFV Gressoney-Saint-Jean", LatLng(45.7833, 7.8167), ReserveType.HUNTING, "Valle d'Aosta"),
    // --- LOMBARDIA ---
    Reserve("Riserva Naturale Pian di Spagna", LatLng(46.1833, 9.4000), ReserveType.FISHING, "Lombardia"),
    Reserve("Lago di Como (Ramo di Lecco)", LatLng(45.8500, 9.3833), ReserveType.FISHING, "Lombardia"),
    Reserve("Fiume Ticino (Parco Lombardo)", LatLng(45.3000, 8.9000), ReserveType.BOTH, "Lombardia"),
    Reserve("Lago d'Iseo", LatLng(45.7167, 10.0833), ReserveType.FISHING, "Lombardia"),
    Reserve("ATC Oltrepò Pavese", LatLng(44.9000, 9.1000), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC Brescia 1", LatLng(45.5500, 10.2167), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC Bergamo 2", LatLng(45.7000, 9.6667), ReserveType.HUNTING, "Lombardia"),
    Reserve("Comprensorio Alpino Sondrio CA1", LatLng(46.1667, 9.8667), ReserveType.HUNTING, "Lombardia"),
    Reserve("AFV Valvestino", LatLng(45.7667, 10.6333), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC BG1", LatLng(45.7167, 9.6667), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC BG2", LatLng(45.7000, 9.6667), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC BG3", LatLng(45.7833, 9.7167), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC BS1", LatLng(45.5500, 10.2167), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC BS2", LatLng(45.4667, 10.3000), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC BS3", LatLng(45.6000, 10.4500), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC CO1", LatLng(45.8000, 9.0833), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC CO2", LatLng(45.7667, 9.1667), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC CR1", LatLng(45.1500, 10.0333), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC LC1", LatLng(45.8500, 9.3833), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC LO1", LatLng(45.3167, 9.5000), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC MN1", LatLng(45.1500, 10.7833), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC MN2", LatLng(45.0000, 10.7000), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC MI1", LatLng(45.4667, 9.1833), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC MI2", LatLng(45.4500, 9.3000), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC PV1", LatLng(45.2000, 9.1500), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC PV2", LatLng(45.0500, 9.2000), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC SO1", LatLng(46.1667, 9.8667), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC SO2", LatLng(46.2500, 10.0833), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC VA1", LatLng(45.8167, 8.8333), ReserveType.HUNTING, "Lombardia"),
    Reserve("ATC VA2", LatLng(45.9000, 8.8000), ReserveType.HUNTING, "Lombardia"),
    Reserve("CA BG1 Valle Brembana", LatLng(45.9500, 9.6667), ReserveType.HUNTING, "Lombardia"),
    Reserve("CA BG2 Valle Seriana", LatLng(45.9000, 9.9000), ReserveType.HUNTING, "Lombardia"),
    Reserve("CA BS1 Valle Camonica", LatLng(46.0333, 10.3000), ReserveType.HUNTING, "Lombardia"),
    Reserve("CA BS2 Alto Garda", LatLng(45.7333, 10.7000), ReserveType.HUNTING, "Lombardia"),
    Reserve("CA SO1 Alta Valtellina", LatLng(46.5000, 10.3667), ReserveType.HUNTING, "Lombardia"),
    Reserve("CA SO2 Media Valtellina", LatLng(46.1667, 9.8667), ReserveType.HUNTING, "Lombardia"),
    Reserve("CA SO3 Valchiavenna", LatLng(46.3167, 9.4000), ReserveType.HUNTING, "Lombardia"),
    Reserve("AFV Valvestino", LatLng(45.7667, 10.6333), ReserveType.HUNTING, "Lombardia"),
    Reserve("AFV Pian di Spagna", LatLng(46.1833, 9.4000), ReserveType.HUNTING, "Lombardia"),
    Reserve("AFV Monte Muggio", LatLng(46.0167, 9.3333), ReserveType.HUNTING, "Lombardia"),
    Reserve("AFV Alpe Giumello", LatLng(46.0333, 9.3667), ReserveType.HUNTING, "Lombardia"),
    Reserve("AFV Val d’Intelvi", LatLng(45.9667, 9.0667), ReserveType.HUNTING, "Lombardia"),
    Reserve("AFV Monte Generoso", LatLng(45.9167, 9.0167), ReserveType.HUNTING, "Lombardia"),
    // --- TRENTINO-ALTO ADIGE ---
    Reserve("Lago di Caldonazzo", LatLng(46.0000, 11.2000), ReserveType.FISHING, "Trentino"),
    Reserve("Riserva di Caccia Val di Non", LatLng(46.3667, 11.0667), ReserveType.HUNTING, "Trentino"),
    Reserve("Riserva di Caccia Paneveggio", LatLng(46.3000, 11.7833), ReserveType.HUNTING, "Trentino"),
    Reserve("CA TN1 Alta Val di Sole", LatLng(46.3333, 10.7667), ReserveType.HUNTING, "Trentino"),
    Reserve("CA TN2 Bassa Val di Sole", LatLng(46.3000, 10.8500), ReserveType.HUNTING, "Trentino"),
    Reserve("CA TN3 Val di Non", LatLng(46.3667, 11.0667), ReserveType.HUNTING, "Trentino"),
    Reserve("CA TN4 Paganella", LatLng(46.1667, 11.0333), ReserveType.HUNTING, "Trentino"),
    Reserve("CA TN5 Val di Cembra", LatLng(46.2167, 11.2333), ReserveType.HUNTING, "Trentino"),
    Reserve("CA TN6 Valsugana", LatLng(46.0500, 11.4500), ReserveType.HUNTING, "Trentino"),
    Reserve("CA TN7 Primiero", LatLng(46.1667, 11.8333), ReserveType.HUNTING, "Trentino"),
    Reserve("CA TN8 Fiemme", LatLng(46.3000, 11.5000), ReserveType.HUNTING, "Trentino"),
    Reserve("CA TN9 Fassa", LatLng(46.4333, 11.7000), ReserveType.HUNTING, "Trentino"),
    Reserve("CA TN10 Giudicarie", LatLng(46.0333, 10.7333), ReserveType.HUNTING, "Trentino"),
    Reserve("CA TN11 Alto Garda e Ledro", LatLng(45.8833, 10.8500), ReserveType.HUNTING, "Trentino"),
    Reserve("CA TN12 Vallagarina", LatLng(45.8333, 10.9833), ReserveType.HUNTING, "Trentino"),
    Reserve("AFV Val di Fiemme", LatLng(46.3000, 11.5000), ReserveType.HUNTING, "Trentino"),
    Reserve("AFV Monte Bondone", LatLng(46.0167, 11.0667), ReserveType.HUNTING, "Trentino"),
    Reserve("AFV Val di Rabbi", LatLng(46.3833, 10.8333), ReserveType.HUNTING, "Trentino"),
    Reserve("AFV Val di Peio", LatLng(46.3500, 10.6500), ReserveType.HUNTING, "Trentino"),
    Reserve("AFV Alpe di Folgaria", LatLng(45.9167, 11.1667), ReserveType.HUNTING, "Trentino"),
    // --- ALTO ADIGE / SÜDTIROL ---
    Reserve("Fiume Adige (Zona Bolzano)", LatLng(46.4833, 11.3333), ReserveType.FISHING, "Alto Adige"),
    Reserve("Riserva di Caccia Val di Funes", LatLng(46.6400, 11.6800), ReserveType.HUNTING, "Alto Adige"),
    Reserve("AFV Anterselva", LatLng(46.8500, 12.1667), ReserveType.HUNTING, "Alto Adige"),
    Reserve("DV 01 Alta Val Venosta", LatLng(46.6500, 10.5500), ReserveType.HUNTING, "Alto Adige"),
    Reserve("DV 02 Media Val Venosta", LatLng(46.6333, 10.8333), ReserveType.HUNTING, "Alto Adige"),
    Reserve("DV 03 Bassa Val Venosta", LatLng(46.6167, 11.0333), ReserveType.HUNTING, "Alto Adige"),
    Reserve("DV 04 Merano e dintorni", LatLng(46.6667, 11.1667), ReserveType.HUNTING, "Alto Adige"),
    Reserve("DV 05 Val Passiria", LatLng(46.8333, 11.2500), ReserveType.HUNTING, "Alto Adige"),
    Reserve("DV 06 Val d’Ultimo", LatLng(46.5500, 11.0000), ReserveType.HUNTING, "Alto Adige"),
    Reserve("DV 07 Bolzano", LatLng(46.5000, 11.3500), ReserveType.HUNTING, "Alto Adige"),
    Reserve("DV 08 Bassa Atesina", LatLng(46.3333, 11.2833), ReserveType.HUNTING, "Alto Adige"),
    Reserve("DV 09 Val d’Isarco", LatLng(46.7167, 11.6500), ReserveType.HUNTING, "Alto Adige"),
    Reserve("DV 10 Alta Val d’Isarco", LatLng(46.9333, 11.4333), ReserveType.HUNTING, "Alto Adige"),
    Reserve("DV 11 Val Gardena", LatLng(46.5667, 11.7333), ReserveType.HUNTING, "Alto Adige"),
    Reserve("DV 12 Val Badia", LatLng(46.6500, 11.9333), ReserveType.HUNTING, "Alto Adige"),
    Reserve("DV 13 Alta Pusteria", LatLng(46.7333, 12.2167), ReserveType.HUNTING, "Alto Adige"),
    Reserve("DV 14 Val Pusteria", LatLng(46.7833, 11.8833), ReserveType.HUNTING, "Alto Adige"),
    Reserve("CA BZ1 Sciliar", LatLng(46.5167, 11.5667), ReserveType.HUNTING, "Alto Adige"),
    Reserve("CA BZ2 Renon", LatLng(46.5500, 11.4500), ReserveType.HUNTING, "Alto Adige"),
    Reserve("CA BZ3 Sarentino", LatLng(46.6500, 11.3500), ReserveType.HUNTING, "Alto Adige"),
    Reserve("CA BZ4 Val d’Ega", LatLng(46.4000, 11.5500), ReserveType.HUNTING, "Alto Adige"),
    Reserve("CA BZ5 Alta Badia", LatLng(46.6000, 11.9000), ReserveType.HUNTING, "Alto Adige"),
    Reserve("AFV Anterselva", LatLng(46.8500, 12.1667), ReserveType.HUNTING, "Alto Adige"),
    Reserve("AFV Sesto", LatLng(46.7000, 12.3500), ReserveType.HUNTING, "Alto Adige"),
    Reserve("AFV Dobbiaco", LatLng(46.7333, 12.2167), ReserveType.HUNTING, "Alto Adige"),
    Reserve("AFV Braies", LatLng(46.7000, 12.0833), ReserveType.HUNTING, "Alto Adige"),
    Reserve("AFV Funes", LatLng(46.6500, 11.7000), ReserveType.HUNTING, "Alto Adige"),
    Reserve("AFV Tures", LatLng(46.9333, 11.9500), ReserveType.HUNTING, "Alto Adige"),
    // --- VENETO ---
    Reserve("Laguna di Caorle", LatLng(45.6000, 12.9000), ReserveType.FISHING, "Veneto"),
    Reserve("Altopiano di Asiago", LatLng(45.8758, 11.5097), ReserveType.HUNTING, "Veneto"),
    Reserve("Delta del Po Veneto", LatLng(44.9667, 12.4167), ReserveType.BOTH, "Veneto"),
    Reserve("Lago di Santa Croce", LatLng(46.1167, 12.3333), ReserveType.FISHING, "Veneto"),
    Reserve("ATC Venezia 1", LatLng(45.5000, 12.3000), ReserveType.HUNTING, "Veneto"),
    Reserve("ATC Verona 3", LatLng(45.4167, 10.9833), ReserveType.HUNTING, "Veneto"),
    Reserve("AFV Valle Averto", LatLng(45.3833, 12.2000), ReserveType.HUNTING, "Veneto"),
    Reserve("ATC VE1", LatLng(45.5000, 12.3000), ReserveType.HUNTING, "Veneto"),
    Reserve("ATC VE2", LatLng(45.6000, 12.3500), ReserveType.HUNTING, "Veneto"),
    Reserve("ATC TV1", LatLng(45.6667, 12.3000), ReserveType.HUNTING, "Veneto"),
    Reserve("ATC TV2", LatLng(45.7333, 12.3500), ReserveType.HUNTING, "Veneto"),
    Reserve("ATC PD1", LatLng(45.4167, 11.8833), ReserveType.HUNTING, "Veneto"),
    Reserve("ATC PD2", LatLng(45.3500, 11.9500), ReserveType.HUNTING, "Veneto"),
    Reserve("ATC RO1", LatLng(45.0667, 11.7833), ReserveType.HUNTING, "Veneto"),
    Reserve("ATC RO2", LatLng(45.0333, 11.8500), ReserveType.HUNTING, "Veneto"),
    Reserve("ATC VR1", LatLng(45.4500, 10.9833), ReserveType.HUNTING, "Veneto"),
    Reserve("ATC VR2", LatLng(45.5000, 11.0000), ReserveType.HUNTING, "Veneto"),
    Reserve("ATC VR3", LatLng(45.4167, 10.9833), ReserveType.HUNTING, "Veneto"),
    Reserve("ATC VI1", LatLng(45.5500, 11.5500), ReserveType.HUNTING, "Veneto"),
    Reserve("ATC VI2", LatLng(45.6000, 11.6000), ReserveType.HUNTING, "Veneto"),
    Reserve("ATC BL1", LatLng(46.1500, 12.2167), ReserveType.HUNTING, "Veneto"),
    Reserve("ATC BL2", LatLng(46.2500, 12.1333), ReserveType.HUNTING, "Veneto"),
    Reserve("CA BL1 Cadore", LatLng(46.4333, 12.3000), ReserveType.HUNTING, "Veneto"),
    Reserve("CA BL2 Comelico", LatLng(46.6000, 12.4667), ReserveType.HUNTING, "Veneto"),
    Reserve("CA BL3 Agordino", LatLng(46.2833, 12.0333), ReserveType.HUNTING, "Veneto"),
    Reserve("CA BL4 Zoldo", LatLng(46.3500, 12.1167), ReserveType.HUNTING, "Veneto"),
    Reserve("CA BL5 Alpago", LatLng(46.1667, 12.3667), ReserveType.HUNTING, "Veneto"),
    Reserve("CA VI1 Asiago", LatLng(45.8758, 11.5097), ReserveType.HUNTING, "Veneto"),
    Reserve("CA VR1 Lessinia", LatLng(45.6333, 11.0333), ReserveType.HUNTING, "Veneto"),
    Reserve("AFV Valle Averto", LatLng(45.3833, 12.2000), ReserveType.HUNTING, "Veneto"),
    Reserve("AFV Cansiglio", LatLng(46.0667, 12.4000), ReserveType.HUNTING, "Veneto"),
    Reserve("AFV Monte Grappa", LatLng(45.8500, 11.8000), ReserveType.HUNTING, "Veneto"),
    Reserve("AFV Altopiano dei Sette Comuni", LatLng(45.8833, 11.5167), ReserveType.HUNTING, "Veneto"),
    Reserve("AFV Val d’Illasi", LatLng(45.5167, 11.1167), ReserveType.HUNTING, "Veneto"),
    Reserve("AFV Valpolicella", LatLng(45.5167, 10.9000), ReserveType.HUNTING, "Veneto"),
    Reserve("Valle Bagliona", LatLng(45.0667, 12.2667), ReserveType.HUNTING, "Veneto"),
    Reserve("Valle Morosina", LatLng(45.0333, 12.3000), ReserveType.HUNTING, "Veneto"),
    Reserve("Valle Ca’ Pisani", LatLng(45.0500, 12.2167), ReserveType.HUNTING, "Veneto"),
    Reserve("Valle Millecampi", LatLng(45.2667, 12.2667), ReserveType.HUNTING, "Veneto"),
    Reserve("Valle Averto Sud", LatLng(45.3500, 12.2000), ReserveType.HUNTING, "Veneto"),
    // --- FRIULI-VENEZIA GIULIA ---
    Reserve("Riserva Naturale Valle Canal Novo", LatLng(45.7167, 13.1333), ReserveType.FISHING, "Friuli-Venezia Giulia"),
    Reserve("Lago di Cavazzo", LatLng(46.3333, 13.0667), ReserveType.FISHING, "Friuli-Venezia Giulia"),
    Reserve("Riserva di Caccia Tarvisio", LatLng(46.5000, 13.5833), ReserveType.HUNTING, "Friuli-Venezia Giulia"),
    Reserve("AFV Fusine", LatLng(46.5000, 13.7000), ReserveType.HUNTING, "Friuli-Venezia Giulia"),
    Reserve("ATC Udine 2", LatLng(46.0833, 13.2333), ReserveType.HUNTING, "Friuli-Venezia Giulia"),
    Reserve("ATC UD1", LatLng(46.0667, 13.2333), ReserveType.HUNTING, "Friuli-Venezia Giulia"),
    Reserve("ATC UD2", LatLng(46.0833, 13.2333), ReserveType.HUNTING, "Friuli-Venezia Giulia"),
    Reserve("ATC UD3", LatLng(46.1500, 13.2000), ReserveType.HUNTING, "Friuli-Venezia Giulia"),
    Reserve("ATC PN1", LatLng(46.0333, 12.6500), ReserveType.HUNTING, "Friuli-Venezia Giulia"),
    Reserve("ATC PN2", LatLng(46.0833, 12.7000), ReserveType.HUNTING, "Friuli-Venezia Giulia"),
    Reserve("ATC GO1", LatLng(45.9333, 13.6167), ReserveType.HUNTING, "Friuli-Venezia Giulia"),
    Reserve("ATC TS1", LatLng(45.6500, 13.7667), ReserveType.HUNTING, "Friuli-Venezia Giulia"),
    Reserve("CA FVG1 Tarvisiano", LatLng(46.5000, 13.5833), ReserveType.HUNTING, "Friuli-Venezia Giulia"),
    Reserve("CA FVG2 Carnia", LatLng(46.5000, 12.9000), ReserveType.HUNTING, "Friuli-Venezia Giulia"),
    Reserve("CA FVG3 Canal del Ferro", LatLng(46.4167, 13.2667), ReserveType.HUNTING, "Friuli-Venezia Giulia"),
    Reserve("CA FVG4 Valcellina", LatLng(46.2667, 12.5667), ReserveType.HUNTING, "Friuli-Venezia Giulia"),
    Reserve("CA FVG5 Prealpi Giulie", LatLng(46.3000, 13.3333), ReserveType.HUNTING, "Friuli-Venezia Giulia"),
    Reserve("AFV Fusine", LatLng(46.5000, 13.7000), ReserveType.HUNTING, "Friuli-Venezia Giulia"),
    Reserve("AFV Val Alba", LatLng(46.4000, 13.0833), ReserveType.HUNTING, "Friuli-Venezia Giulia"),
    Reserve("AFV Sauris", LatLng(46.4667, 12.7000), ReserveType.HUNTING, "Friuli-Venezia Giulia"),
    Reserve("AFV Forni di Sopra", LatLng(46.4167, 12.5833), ReserveType.HUNTING, "Friuli-Venezia Giulia"),
    Reserve("AFV Resia", LatLng(46.3667, 13.3167), ReserveType.HUNTING, "Friuli-Venezia Giulia"),
    Reserve("AFV Val Tramontina", LatLng(46.2667, 12.7667), ReserveType.HUNTING, "Friuli-Venezia Giulia"),
    // --- LIGURIA ---
    Reserve("Parco dell'Antola", LatLng(44.5667, 9.1333), ReserveType.HUNTING, "Liguria"),
    Reserve("Lago di Brugneto", LatLng(44.5333, 9.2000), ReserveType.FISHING, "Liguria"),
    Reserve("ATC Genova 1", LatLng(44.4167, 8.9500), ReserveType.HUNTING, "Liguria"),
    Reserve("AFV Monte Zatta", LatLng(44.3833, 9.4500), ReserveType.HUNTING, "Liguria"),
    Reserve("ATC GE1", LatLng(44.4167, 8.9500), ReserveType.HUNTING, "Liguria"),
    Reserve("ATC GE2", LatLng(44.4500, 9.0500), ReserveType.HUNTING, "Liguria"),
    Reserve("ATC SV1", LatLng(44.3000, 8.4667), ReserveType.HUNTING, "Liguria"),
    Reserve("ATC SV2", LatLng(44.2333, 8.3167), ReserveType.HUNTING, "Liguria"),
    Reserve("ATC IM1", LatLng(43.8833, 7.9500), ReserveType.HUNTING, "Liguria"),
    Reserve("ATC IM2", LatLng(43.9000, 7.8500), ReserveType.HUNTING, "Liguria"),
    Reserve("ATC SP1", LatLng(44.1000, 9.8167), ReserveType.HUNTING, "Liguria"),
    Reserve("ATC SP2", LatLng(44.1500, 9.8500), ReserveType.HUNTING, "Liguria"),
    Reserve("CA GE1 Alta Val Trebbia", LatLng(44.5667, 9.3333), ReserveType.HUNTING, "Liguria"),
    Reserve("CA GE2 Aveto", LatLng(44.5167, 9.4500), ReserveType.HUNTING, "Liguria"),
    Reserve("CA SV1 Val Bormida", LatLng(44.3500, 8.2667), ReserveType.HUNTING, "Liguria"),
    Reserve("CA IM1 Valle Argentina", LatLng(43.9333, 7.8000), ReserveType.HUNTING, "Liguria"),
    Reserve("CA IM2 Valle Nervia", LatLng(43.8833, 7.6667), ReserveType.HUNTING, "Liguria"),
    Reserve("AFV Monte Zatta", LatLng(44.3833, 9.4500), ReserveType.HUNTING, "Liguria"),
    Reserve("AFV Rezzo", LatLng(44.0500, 7.8500), ReserveType.HUNTING, "Liguria"),
    Reserve("AFV Monte Gottero", LatLng(44.2667, 9.7167), ReserveType.HUNTING, "Liguria"),
    Reserve("AFV Vara", LatLng(44.2667, 9.7167), ReserveType.HUNTING, "Liguria"),
    Reserve("AFV Val d’Aveto", LatLng(44.5333, 9.4500), ReserveType.HUNTING, "Liguria"),
    // --- EMILIA-ROMAGNA ---
    Reserve("ATC BO2 (Bologna)", LatLng(44.5500, 11.3500), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("Valli di Comacchio", LatLng(44.6167, 12.1667), ReserveType.BOTH, "Emilia-Romagna"),
    Reserve("Parco Regionale del Delta del Po", LatLng(44.8000, 12.2333), ReserveType.BOTH, "Emilia-Romagna"),
    Reserve("Fiume Po (Tratto Piacentino)", LatLng(45.0500, 9.7000), ReserveType.FISHING, "Emilia-Romagna"),
    Reserve("Lago di Suviana", LatLng(44.1167, 11.0500), ReserveType.FISHING, "Emilia-Romagna"),
    Reserve("ATC PC1 Piacenza", LatLng(45.0500, 9.7000), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("ATC PR4 Parma Ovest", LatLng(44.8000, 10.3333), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("AFV San Vitale", LatLng(44.4667, 11.8833), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("AFV Campotto", LatLng(44.6333, 11.9500), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("ATC PC1", LatLng(45.0500, 9.7000), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("ATC PC2", LatLng(44.9500, 9.6000), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("ATC PR1", LatLng(44.8000, 10.3333), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("ATC PR2", LatLng(44.7333, 10.2667), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("ATC PR3", LatLng(44.6667, 10.2000), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("ATC PR4", LatLng(44.8000, 10.3333), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("ATC RE1", LatLng(44.7000, 10.6333), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("ATC RE2", LatLng(44.6167, 10.5500), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("ATC MO1", LatLng(44.6500, 10.9167), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("ATC MO2", LatLng(44.5333, 10.8500), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("ATC BO1", LatLng(44.5000, 11.3500), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("ATC BO2", LatLng(44.5500, 11.3500), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("ATC FE1", LatLng(44.8333, 11.6167), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("ATC FE2", LatLng(44.7667, 11.7167), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("ATC RA1", LatLng(44.3667, 12.2000), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("ATC RA2", LatLng(44.3167, 12.1500), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("ATC FC1", LatLng(44.1333, 12.0500), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("ATC FC2", LatLng(44.0833, 12.1000), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("ATC RN1", LatLng(44.0667, 12.5667), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("ATC RN2", LatLng(43.9833, 12.6000), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("CA ER1 Alto Appennino Piacentino", LatLng(44.7167, 9.4500), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("CA ER2 Appennino Parmense", LatLng(44.5333, 10.0833), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("CA ER3 Appennino Reggiano", LatLng(44.4167, 10.4500), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("CA ER4 Appennino Modenese", LatLng(44.2833, 10.7000), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("CA ER5 Appennino Bolognese", LatLng(44.2167, 11.2167), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("CA ER6 Appennino Romagnolo", LatLng(44.0667, 11.8000), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("AFV San Vitale", LatLng(44.4667, 11.8833), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("AFV Campotto", LatLng(44.6333, 11.9500), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("AFV Valle Santa", LatLng(44.7000, 12.1000), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("AFV Ostellato", LatLng(44.7667, 11.9000), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("AFV San Bartolo", LatLng(44.0333, 12.6333), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("AFV Monte Fumaiolo", LatLng(43.8500, 12.0333), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("Valle Bertuzzi", LatLng(44.7667, 12.2667), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("Valle Nuova", LatLng(44.7333, 12.3000), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("Valle Fattibello", LatLng(44.6667, 12.2333), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("Valle Campo", LatLng(44.7000, 12.2667), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("Valle Spavola", LatLng(44.7167, 12.2833), ReserveType.HUNTING, "Emilia-Romagna"),
    Reserve("Valle Magnavacca", LatLng(44.6500, 12.2333), ReserveType.HUNTING, "Emilia-Romagna"),
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
    Reserve("AFV La Marsiliana", LatLng(42.5833, 11.2833), ReserveType.HUNTING, "Toscana"),
    Reserve("AFV Montebamboli", LatLng(43.0333, 10.8000), ReserveType.HUNTING, "Toscana"),
    Reserve("AFV Poggio all’Olmo", LatLng(42.9000, 11.4500), ReserveType.HUNTING, "Toscana"),
    Reserve("AFV Santa Luce", LatLng(43.4833, 10.5833), ReserveType.HUNTING, "Toscana"),
    Reserve("AFV Monterotondo", LatLng(43.1333, 10.8333), ReserveType.HUNTING, "Toscana"),
    Reserve("ATC AR1", LatLng(43.4667, 11.8833), ReserveType.HUNTING, "Toscana"),
    Reserve("ATC AR2", LatLng(43.4500, 11.7500), ReserveType.HUNTING, "Toscana"),
    Reserve("ATC FI1", LatLng(43.7667, 11.2500), ReserveType.HUNTING, "Toscana"),
    Reserve("ATC FI2", LatLng(43.7000, 11.3000), ReserveType.HUNTING, "Toscana"),
    Reserve("ATC GR5", LatLng(42.7833, 11.2167), ReserveType.HUNTING, "Toscana"),
    Reserve("ATC GR6", LatLng(42.7600, 11.1100), ReserveType.HUNTING, "Toscana"),
    Reserve("ATC LI1", LatLng(43.5500, 10.3167), ReserveType.HUNTING, "Toscana"),
    Reserve("ATC LI2", LatLng(43.3000, 10.4500), ReserveType.HUNTING, "Toscana"),
    Reserve("ATC LU1", LatLng(43.8500, 10.5167), ReserveType.HUNTING, "Toscana"),
    Reserve("ATC LU2", LatLng(44.0333, 10.3333), ReserveType.HUNTING, "Toscana"),
    Reserve("ATC MS1", LatLng(44.0333, 10.1333), ReserveType.HUNTING, "Toscana"),
    Reserve("ATC MS2", LatLng(44.0833, 10.1667), ReserveType.HUNTING, "Toscana"),
    Reserve("ATC PI1", LatLng(43.7167, 10.4000), ReserveType.HUNTING, "Toscana"),
    Reserve("ATC PI2", LatLng(43.6167, 10.5500), ReserveType.HUNTING, "Toscana"),
    Reserve("ATC PT1", LatLng(43.8833, 10.9167), ReserveType.HUNTING, "Toscana"),
    Reserve("ATC PT2", LatLng(43.9500, 10.8333), ReserveType.HUNTING, "Toscana"),
    Reserve("ATC PO1", LatLng(43.8833, 11.1000), ReserveType.HUNTING, "Toscana"),
    Reserve("ATC SI1", LatLng(43.3167, 11.3333), ReserveType.HUNTING, "Toscana"),
    Reserve("ATC SI2", LatLng(43.2500, 11.4500), ReserveType.HUNTING, "Toscana"),
    Reserve("ATC SI3", LatLng(43.1000, 11.7000), ReserveType.HUNTING, "Toscana"),
    Reserve("CA TO1 Appennino Pistoiese", LatLng(44.0500, 10.8333), ReserveType.HUNTING, "Toscana"),
    Reserve("CA TO2 Appennino Pratese", LatLng(44.0000, 11.0833), ReserveType.HUNTING, "Toscana"),
    Reserve("CA TO3 Appennino Fiorentino", LatLng(43.9000, 11.4500), ReserveType.HUNTING, "Toscana"),
    Reserve("CA TO4 Casentino", LatLng(43.7333, 11.7833), ReserveType.HUNTING, "Toscana"),
    Reserve("CA TO5 Amiata", LatLng(42.8833, 11.6167), ReserveType.HUNTING, "Toscana"),
    Reserve("AFV La Marsiliana", LatLng(42.5833, 11.2833), ReserveType.HUNTING, "Toscana"),
    Reserve("AFV Montebamboli", LatLng(43.0333, 10.8000), ReserveType.HUNTING, "Toscana"),
    Reserve("AFV Poggio all’Olmo", LatLng(42.9000, 11.4500), ReserveType.HUNTING, "Toscana"),
    Reserve("AFV Santa Luce", LatLng(43.4833, 10.5833), ReserveType.HUNTING, "Toscana"),
    Reserve("AFV Monterotondo", LatLng(43.1333, 10.8333), ReserveType.HUNTING, "Toscana"),
    Reserve("AFV Le Corniole", LatLng(43.3939687, 11.6716462), ReserveType.HUNTING, "Toscana"),
    Reserve("AFV Le Forane", LatLng(42.4893860, 11.3519838), ReserveType.HUNTING, "Toscana"),
    Reserve("AFV San Gimignano", LatLng(43.4364301, 11.0007402), ReserveType.HUNTING, "Toscana"),
    Reserve("AAV Montecatini Val di Cecina", LatLng(43.3500, 10.7333), ReserveType.HUNTING, "Toscana"),
    Reserve("AAV Pomarance", LatLng(43.2833, 10.8833), ReserveType.HUNTING, "Toscana"),
    Reserve("AAV Radicondoli", LatLng(43.2500, 11.0333), ReserveType.HUNTING, "Toscana"),
    Reserve("AAV Castellina Marittima", LatLng(43.4333, 10.5833), ReserveType.HUNTING, "Toscana"),
    Reserve("AAV Montieri", LatLng(43.1167, 10.9667), ReserveType.HUNTING, "Toscana"),
    // --- UMBRIA ---
    Reserve("Lago Trasimeno", LatLng(43.1300, 12.1000), ReserveType.FISHING, "Umbria"),
    Reserve("Lago di Corbara", LatLng(42.7167, 12.2833), ReserveType.FISHING, "Umbria"),
    Reserve("ATC Perugia PG1", LatLng(43.1167, 12.3833), ReserveType.HUNTING, "Umbria"),
    Reserve("AFV Colfiorito", LatLng(43.0333, 12.8833), ReserveType.HUNTING, "Umbria"),
    Reserve("AFV Monte Peglia", LatLng(42.8333, 12.2000), ReserveType.HUNTING, "Umbria"),
    Reserve("ATC PG1", LatLng(43.1167, 12.3833), ReserveType.HUNTING, "Umbria"),
    Reserve("ATC PG2", LatLng(43.0500, 12.5500), ReserveType.HUNTING, "Umbria"),
    Reserve("ATC TR1", LatLng(42.5667, 12.6500), ReserveType.HUNTING, "Umbria"),
    Reserve("ATC TR2", LatLng(42.7000, 12.4500), ReserveType.HUNTING, "Umbria"),
    Reserve("CA UM1 Valnerina", LatLng(42.7333, 12.8833), ReserveType.HUNTING, "Umbria"),
    Reserve("CA UM2 Monti Martani", LatLng(42.8333, 12.5667), ReserveType.HUNTING, "Umbria"),
    Reserve("CA UM3 Monte Peglia", LatLng(42.8333, 12.2000), ReserveType.HUNTING, "Umbria"),
    Reserve("CA UM4 Alta Umbria", LatLng(43.3500, 12.4500), ReserveType.HUNTING, "Umbria"),
    Reserve("AFV Colfiorito", LatLng(43.0333, 12.8833), ReserveType.HUNTING, "Umbria"),
    Reserve("AFV Monte Peglia", LatLng(42.8333, 12.2000), ReserveType.HUNTING, "Umbria"),
    Reserve("AFV Monte Subasio", LatLng(43.0667, 12.7000), ReserveType.HUNTING, "Umbria"),
    Reserve("AFV Nocera Umbra", LatLng(43.1167, 12.7833), ReserveType.HUNTING, "Umbria"),
    Reserve("AFV Amelia", LatLng(42.5500, 12.4167), ReserveType.HUNTING, "Umbria"),
    // --- MARCHE ---
    Reserve("Riserva Naturale Gola della Rossa", LatLng(43.4000, 13.0000), ReserveType.BOTH, "Marche"),
    Reserve("Lago di Gerosa", LatLng(42.9500, 13.3833), ReserveType.FISHING, "Marche"),
    Reserve("ATC Macerata MC1", LatLng(43.3000, 13.4500), ReserveType.HUNTING, "Marche"),
    Reserve("AFV Montefeltro", LatLng(43.7000, 12.5000), ReserveType.HUNTING, "Marche"),
    Reserve("ATC AN1", LatLng(43.6167, 13.5167), ReserveType.HUNTING, "Marche"),
    Reserve("ATC AN2", LatLng(43.4500, 13.3500), ReserveType.HUNTING, "Marche"),
    Reserve("ATC PU1", LatLng(43.9000, 12.9167), ReserveType.HUNTING, "Marche"),
    Reserve("ATC PU2", LatLng(43.7667, 12.6500), ReserveType.HUNTING, "Marche"),
    Reserve("ATC MC1", LatLng(43.3000, 13.4500), ReserveType.HUNTING, "Marche"),
    Reserve("ATC MC2", LatLng(43.2000, 13.2167), ReserveType.HUNTING, "Marche"),
    Reserve("ATC FM1", LatLng(43.1500, 13.7167), ReserveType.HUNTING, "Marche"),
    Reserve("ATC AP1", LatLng(42.8500, 13.5833), ReserveType.HUNTING, "Marche"),
    Reserve("CA MA1 Monti Sibillini Nord", LatLng(43.0000, 13.2667), ReserveType.HUNTING, "Marche"),
    Reserve("CA MA2 Monti Sibillini Sud", LatLng(42.8833, 13.2667), ReserveType.HUNTING, "Marche"),
    Reserve("CA MA3 Monte Catria", LatLng(43.4833, 12.7000), ReserveType.HUNTING, "Marche"),
    Reserve("CA MA4 Monte Nerone", LatLng(43.5667, 12.5167), ReserveType.HUNTING, "Marche"),
    Reserve("CA MA5 Monte San Vicino", LatLng(43.3500, 13.0333), ReserveType.HUNTING, "Marche"),
    Reserve("AFV Montefeltro", LatLng(43.7000, 12.5000), ReserveType.HUNTING, "Marche"),
    Reserve("AFV Carpegna", LatLng(43.7833, 12.5167), ReserveType.HUNTING, "Marche"),
    Reserve("AFV Frontino", LatLng(43.7667, 12.4500), ReserveType.HUNTING, "Marche"),
    Reserve("AFV Amandola", LatLng(42.9667, 13.3500), ReserveType.HUNTING, "Marche"),
    Reserve("AFV Sarnano", LatLng(43.0333, 13.3000), ReserveType.HUNTING, "Marche"),
    Reserve("AFV Monte Priora", LatLng(42.9500, 13.3167), ReserveType.HUNTING, "Marche"),
    Reserve("AFV Montefortino", LatLng(42.9333, 13.3333), ReserveType.HUNTING, "Marche"),
    // --- LAZIO ---
    Reserve("Riserva Naturale del Litorale Romano", LatLng(41.7456, 12.2747), ReserveType.BOTH, "Lazio"),
    Reserve("Riserva Naturale della Marcigliana", LatLng(42.0006, 12.5350), ReserveType.HUNTING, "Lazio"),
    Reserve("Lago di Bracciano", LatLng(42.1208, 12.2333), ReserveType.FISHING, "Lazio"),
    Reserve("Lago di Bolsena", LatLng(42.5933, 11.9358), ReserveType.FISHING, "Lazio"),
    Reserve("ATC Rieti RI1", LatLng(42.4013, 12.8622), ReserveType.HUNTING, "Lazio"),
    Reserve("AFV Tenuta di Castelporziano", LatLng(41.7333, 12.4000), ReserveType.HUNTING, "Lazio"),
    Reserve("ATC Viterbo VT2", LatLng(42.4167, 12.1000), ReserveType.HUNTING, "Lazio"),
    Reserve("ATC RM1", LatLng(41.9000, 12.5000), ReserveType.HUNTING, "Lazio"),
    Reserve("ATC RM2", LatLng(41.8500, 12.6000), ReserveType.HUNTING, "Lazio"),
    Reserve("ATC RM3", LatLng(41.9500, 12.7000), ReserveType.HUNTING, "Lazio"),
    Reserve("ATC RI1", LatLng(42.4013, 12.8622), ReserveType.HUNTING, "Lazio"),
    Reserve("ATC RI2", LatLng(42.4167, 12.9500), ReserveType.HUNTING, "Lazio"),
    Reserve("ATC VT1", LatLng(42.4167, 12.1000), ReserveType.HUNTING, "Lazio"),
    Reserve("ATC VT2", LatLng(42.4500, 12.2000), ReserveType.HUNTING, "Lazio"),
    Reserve("ATC FR1", LatLng(41.6333, 13.3500), ReserveType.HUNTING, "Lazio"),
    Reserve("ATC FR2", LatLng(41.5667, 13.4500), ReserveType.HUNTING, "Lazio"),
    Reserve("ATC LT1", LatLng(41.4667, 12.9000), ReserveType.HUNTING, "Lazio"),
    Reserve("ATC LT2", LatLng(41.4000, 13.0333), ReserveType.HUNTING, "Lazio"),
    Reserve("CA LA1 Monti Simbruini", LatLng(41.9500, 13.1333), ReserveType.HUNTING, "Lazio"),
    Reserve("CA LA2 Monti Ernici", LatLng(41.7000, 13.4500), ReserveType.HUNTING, "Lazio"),
    Reserve("CA LA3 Monti Lepini", LatLng(41.5833, 13.0833), ReserveType.HUNTING, "Lazio"),
    Reserve("CA LA4 Monti Ausoni", LatLng(41.3833, 13.2667), ReserveType.HUNTING, "Lazio"),
    Reserve("CA LA5 Monti Aurunci", LatLng(41.3167, 13.6667), ReserveType.HUNTING, "Lazio"),
    Reserve("AFV Castelporziano", LatLng(41.7333, 12.4000), ReserveType.HUNTING, "Lazio"),
    Reserve("AFV Tenuta Presidenziale di Castelporziano", LatLng(41.7333, 12.4000), ReserveType.HUNTING, "Lazio"),
    Reserve("AFV Tenuta di San Vittorino", LatLng(42.0333, 12.6500), ReserveType.HUNTING, "Lazio"),
    Reserve("AFV Tenuta di Baccano", LatLng(42.1333, 12.3333), ReserveType.HUNTING, "Lazio"),
    Reserve("AFV Tolfa", LatLng(42.1500, 11.9333), ReserveType.HUNTING, "Lazio"),
    Reserve("AFV Mazzano Romano", LatLng(42.2000, 12.4000), ReserveType.HUNTING, "Lazio"),
    Reserve("AFV Valle del Treja", LatLng(42.2333, 12.4167), ReserveType.HUNTING, "Lazio"),
    Reserve("AFV Monti della Tolfa", LatLng(42.1500, 11.9333), ReserveType.HUNTING, "Lazio"),
    // --- ABRUZZO ---
    Reserve("Lago di Campotosto", LatLng(42.5333, 13.3833), ReserveType.FISHING, "Abruzzo"),
    Reserve("Parco Nazionale del Gran Sasso", LatLng(42.5000, 13.5500), ReserveType.BOTH, "Abruzzo"),
    Reserve("Lago di Scanno", LatLng(41.9167, 13.8667), ReserveType.FISHING, "Abruzzo"),
    Reserve("AFV Valle del Giovenco", LatLng(42.0833, 13.7167), ReserveType.HUNTING, "Abruzzo"),
    Reserve("ATC Chieti CH2", LatLng(42.3500, 14.1667), ReserveType.HUNTING, "Abruzzo"),
    Reserve("ATC AQ1", LatLng(42.3500, 13.4000), ReserveType.HUNTING, "Abruzzo"),
    Reserve("ATC AQ2", LatLng(42.2167, 13.5667), ReserveType.HUNTING, "Abruzzo"),
    Reserve("ATC AQ3", LatLng(42.0833, 13.7167), ReserveType.HUNTING, "Abruzzo"),
    Reserve("ATC CH1", LatLng(42.3333, 14.1667), ReserveType.HUNTING, "Abruzzo"),
    Reserve("ATC CH2", LatLng(42.3500, 14.1667), ReserveType.HUNTING, "Abruzzo"),
    Reserve("ATC PE1", LatLng(42.4500, 14.2167), ReserveType.HUNTING, "Abruzzo"),
    Reserve("ATC PE2", LatLng(42.3667, 14.0833), ReserveType.HUNTING, "Abruzzo"),
    Reserve("ATC TE1", LatLng(42.6500, 13.7000), ReserveType.HUNTING, "Abruzzo"),
    Reserve("ATC TE2", LatLng(42.7167, 13.7833), ReserveType.HUNTING, "Abruzzo"),
    Reserve("CA AB1 Gran Sasso", LatLng(42.5000, 13.5500), ReserveType.HUNTING, "Abruzzo"),
    Reserve("CA AB2 Monti della Laga", LatLng(42.6500, 13.3500), ReserveType.HUNTING, "Abruzzo"),
    Reserve("CA AB3 Majella", LatLng(42.1000, 14.0000), ReserveType.HUNTING, "Abruzzo"),
    Reserve("CA AB4 Sirente-Velino", LatLng(42.1667, 13.5167), ReserveType.HUNTING, "Abruzzo"),
    Reserve("CA AB5 Monti Marsicani", LatLng(41.9000, 13.8333), ReserveType.HUNTING, "Abruzzo"),
    Reserve("AFV Valle del Giovenco", LatLng(42.0833, 13.7167), ReserveType.HUNTING, "Abruzzo"),
    Reserve("AFV Pescocostanzo", LatLng(41.8833, 14.0667), ReserveType.HUNTING, "Abruzzo"),
    Reserve("AFV Rocca di Mezzo", LatLng(42.1667, 13.5167), ReserveType.HUNTING, "Abruzzo"),
    Reserve("AFV Campo Imperatore", LatLng(42.4500, 13.5667), ReserveType.HUNTING, "Abruzzo"),
    Reserve("AFV Valle del Sagittario", LatLng(41.9500, 13.8667), ReserveType.HUNTING, "Abruzzo"),
    // --- MOLISE ---
    Reserve("Lago di Guardialfiera", LatLng(41.7167, 14.7833), ReserveType.FISHING, "Molise"),
    Reserve("Oasi di Colle Meluccio", LatLng(41.5833, 14.4667), ReserveType.HUNTING, "Molise"),
    Reserve("AFV Monte Cesima", LatLng(41.3667, 14.0667), ReserveType.HUNTING, "Molise"),
    Reserve("ATC Campobasso CB2", LatLng(41.5667, 14.6500), ReserveType.HUNTING, "Molise"),
    Reserve("ATC CB1", LatLng(41.5667, 14.6500), ReserveType.HUNTING, "Molise"),
    Reserve("ATC CB2", LatLng(41.5667, 14.6500), ReserveType.HUNTING, "Molise"),
    Reserve("ATC IS1", LatLng(41.6000, 14.2333), ReserveType.HUNTING, "Molise"),
    Reserve("ATC IS2", LatLng(41.7000, 14.1500), ReserveType.HUNTING, "Molise"),
    Reserve("CA MO1 Matese", LatLng(41.4500, 14.3667), ReserveType.HUNTING, "Molise"),
    Reserve("CA MO2 Alto Molise", LatLng(41.8000, 14.2500), ReserveType.HUNTING, "Molise"),
    Reserve("CA MO3 Mainarde", LatLng(41.7000, 14.0000), ReserveType.HUNTING, "Molise"),
    Reserve("AFV Monte Cesima", LatLng(41.3667, 14.0667), ReserveType.HUNTING, "Molise"),
    Reserve("AFV Valle Fiorita", LatLng(41.7167, 14.0333), ReserveType.HUNTING, "Molise"),
    Reserve("AFV Montagnola Molisana", LatLng(41.6333, 14.4500), ReserveType.HUNTING, "Molise"),
    Reserve("AFV Pesche", LatLng(41.6167, 14.2333), ReserveType.HUNTING, "Molise"),
    Reserve("AFV Filignano", LatLng(41.5667, 14.0833), ReserveType.HUNTING, "Molise"),
    // --- CAMPANIA ---
    Reserve("Lago di Patria", LatLng(40.9500, 14.0500), ReserveType.FISHING, "Campania"),
    Reserve("Oasi dei Variconi", LatLng(40.9833, 13.9833), ReserveType.BOTH, "Campania"),
    Reserve("Lago Laceno", LatLng(40.8500, 15.0667), ReserveType.FISHING, "Campania"),
    Reserve("AFV Monte Faito", LatLng(40.6833, 14.4833), ReserveType.HUNTING, "Campania"),
    Reserve("ATC Benevento BN2", LatLng(41.1333, 14.7833), ReserveType.HUNTING, "Campania"),
    Reserve("ATC NA1", LatLng(40.8500, 14.2667), ReserveType.HUNTING, "Campania"),
    Reserve("ATC NA2", LatLng(40.9000, 14.3500), ReserveType.HUNTING, "Campania"),
    Reserve("ATC CE1", LatLng(41.0833, 14.3333), ReserveType.HUNTING, "Campania"),
    Reserve("ATC CE2", LatLng(41.1500, 14.2000), ReserveType.HUNTING, "Campania"),
    Reserve("ATC BN1", LatLng(41.1333, 14.7833), ReserveType.HUNTING, "Campania"),
    Reserve("ATC BN2", LatLng(41.1333, 14.7833), ReserveType.HUNTING, "Campania"),
    Reserve("ATC AV1", LatLng(40.9167, 14.7833), ReserveType.HUNTING, "Campania"),
    Reserve("ATC AV2", LatLng(40.9000, 14.9500), ReserveType.HUNTING, "Campania"),
    Reserve("ATC SA1", LatLng(40.6833, 14.8000), ReserveType.HUNTING, "Campania"),
    Reserve("ATC SA2", LatLng(40.5500, 15.0000), ReserveType.HUNTING, "Campania"),
    Reserve("CA CA1 Matese Sud", LatLng(41.3500, 14.4000), ReserveType.HUNTING, "Campania"),
    Reserve("CA CA2 Monti del Partenio", LatLng(40.9500, 14.7000), ReserveType.HUNTING, "Campania"),
    Reserve("CA CA3 Monti Picentini", LatLng(40.7833, 14.9667), ReserveType.HUNTING, "Campania"),
    Reserve("CA CA4 Cilento Nord", LatLng(40.3333, 15.1667), ReserveType.HUNTING, "Campania"),
    Reserve("CA CA5 Cilento Sud", LatLng(40.1500, 15.3333), ReserveType.HUNTING, "Campania"),
    Reserve("AFV Monte Faito", LatLng(40.6833, 14.4833), ReserveType.HUNTING, "Campania"),
    Reserve("AFV Monte Taburno", LatLng(41.0833, 14.6000), ReserveType.HUNTING, "Campania"),
    Reserve("AFV Monti Lattari", LatLng(40.6500, 14.5500), ReserveType.HUNTING, "Campania"),
    Reserve("AFV Cilento", LatLng(40.2500, 15.2500), ReserveType.HUNTING, "Campania"),
    Reserve("AFV Monte Cervialto", LatLng(40.8500, 15.1667), ReserveType.HUNTING, "Campania"),
    Reserve("AFV Alburni", LatLng(40.4833, 15.3167), ReserveType.HUNTING, "Campania"),
    // --- PUGLIA ---
    Reserve("Lago di Lesina", LatLng(41.8667, 15.3500), ReserveType.FISHING, "Puglia"),
    Reserve("Lago di Varano", LatLng(41.9000, 15.7500), ReserveType.FISHING, "Puglia"),
    Reserve("Oasi Lago Salso", LatLng(41.4667, 15.9667), ReserveType.BOTH, "Puglia"),
    Reserve("AFV Foresta Umbra", LatLng(41.8333, 16.0000), ReserveType.HUNTING, "Puglia"),
    Reserve("ATC Bari BA2", LatLng(41.0833, 16.8667), ReserveType.HUNTING, "Puglia"),
    Reserve("ATC FG1", LatLng(41.6333, 15.5667), ReserveType.HUNTING, "Puglia"),
    Reserve("ATC FG2", LatLng(41.7000, 15.8000), ReserveType.HUNTING, "Puglia"),
    Reserve("ATC BA1", LatLng(41.1167, 16.8667), ReserveType.HUNTING, "Puglia"),
    Reserve("ATC BA2", LatLng(41.0833, 16.8667), ReserveType.HUNTING, "Puglia"),
    Reserve("ATC TA1", LatLng(40.4667, 17.2333), ReserveType.HUNTING, "Puglia"),
    Reserve("ATC BR1", LatLng(40.6333, 17.8167), ReserveType.HUNTING, "Puglia"),
    Reserve("ATC LE1", LatLng(40.3500, 18.1667), ReserveType.HUNTING, "Puglia"),
    Reserve("ATC LE2", LatLng(40.2000, 18.3000), ReserveType.HUNTING, "Puglia"),
    Reserve("CA PU1 Gargano Nord", LatLng(41.9000, 16.0000), ReserveType.HUNTING, "Puglia"),
    Reserve("CA PU2 Gargano Sud", LatLng(41.7000, 15.9000), ReserveType.HUNTING, "Puglia"),
    Reserve("CA PU3 Alta Murgia", LatLng(40.9000, 16.4000), ReserveType.HUNTING, "Puglia"),
    Reserve("CA PU4 Bassa Murgia", LatLng(40.7000, 16.8000), ReserveType.HUNTING, "Puglia"),
    Reserve("AFV Foresta Umbra", LatLng(41.8333, 16.0000), ReserveType.HUNTING, "Puglia"),
    Reserve("AFV San Marco in Lamis", LatLng(41.7000, 15.7000), ReserveType.HUNTING, "Puglia"),
    Reserve("AFV Monte Sant’Angelo", LatLng(41.7000, 15.9500), ReserveType.HUNTING, "Puglia"),
    Reserve("AFV Alta Murgia", LatLng(40.9000, 16.4000), ReserveType.HUNTING, "Puglia"),
    Reserve("AFV Gravina", LatLng(40.8167, 16.4167), ReserveType.HUNTING, "Puglia"),
    Reserve("AFV Laterza", LatLng(40.6333, 16.8000), ReserveType.HUNTING, "Puglia"),
    // --- BASILICATA ---
    Reserve("Lago di Pietra del Pertusillo", LatLng(40.2500, 15.9333), ReserveType.FISHING, "Basilicata"),
    Reserve("Lago di Senise (Monte Cotugno)", LatLng(40.1333, 16.3333), ReserveType.FISHING, "Basilicata"),
    Reserve("Parco Nazionale del Pollino (Area Lucana)", LatLng(39.9500, 16.1667), ReserveType.BOTH, "Basilicata"),
    Reserve("AFV San Giorgio Lucano", LatLng(40.1167, 16.3667), ReserveType.HUNTING, "Basilicata"),
    Reserve("ATC Potenza PZ2", LatLng(40.6333, 15.8000), ReserveType.HUNTING, "Basilicata"),
    Reserve("ATC PZ1", LatLng(40.6333, 15.8000), ReserveType.HUNTING, "Basilicata"),
    Reserve("ATC PZ2", LatLng(40.6333, 15.8000), ReserveType.HUNTING, "Basilicata"),
    Reserve("ATC MT1", LatLng(40.6667, 16.6000), ReserveType.HUNTING, "Basilicata"),
    Reserve("ATC MT2", LatLng(40.5000, 16.6500), ReserveType.HUNTING, "Basilicata"),
    Reserve("CA BA1 Appennino Lucano Nord", LatLng(40.7000, 15.8000), ReserveType.HUNTING, "Basilicata"),
    Reserve("CA BA2 Appennino Lucano Sud", LatLng(40.3500, 15.8500), ReserveType.HUNTING, "Basilicata"),
    Reserve("CA BA3 Massiccio del Sirino", LatLng(40.0833, 15.8167), ReserveType.HUNTING, "Basilicata"),
    Reserve("CA BA4 Monte Alpi", LatLng(40.0667, 16.0667), ReserveType.HUNTING, "Basilicata"),
    Reserve("CA BA5 Pollino Lucano", LatLng(39.9500, 16.1667), ReserveType.HUNTING, "Basilicata"),
    Reserve("AFV San Giorgio Lucano", LatLng(40.1167, 16.3667), ReserveType.HUNTING, "Basilicata"),
    Reserve("AFV Rotonda", LatLng(40.0500, 16.0500), ReserveType.HUNTING, "Basilicata"),
    Reserve("AFV Terranova del Pollino", LatLng(40.0333, 16.2167), ReserveType.HUNTING, "Basilicata"),
    Reserve("AFV Viggianello", LatLng(40.0667, 16.0833), ReserveType.HUNTING, "Basilicata"),
    Reserve("AFV San Severino Lucano", LatLng(40.0667, 16.1333), ReserveType.HUNTING, "Basilicata"),
    Reserve("AFV Lauria", LatLng(40.0500, 15.8333), ReserveType.HUNTING, "Basilicata"),
    // --- CALABRIA ---
    Reserve("Lago Ampollino", LatLng(39.2333, 16.6500), ReserveType.FISHING, "Calabria"),
    Reserve("Lago Arvo", LatLng(39.2500, 16.5500), ReserveType.FISHING, "Calabria"),
    Reserve("Parco Nazionale dell’Aspromonte", LatLng(38.2000, 16.0833), ReserveType.BOTH, "Calabria"),
    Reserve("AFV Serra San Bruno", LatLng(38.5833, 16.3333), ReserveType.HUNTING, "Calabria"),
    Reserve("ATC Cosenza CS3", LatLng(39.3000, 16.2500), ReserveType.HUNTING, "Calabria"),
    Reserve("ATC CS1", LatLng(39.3000, 16.2500), ReserveType.HUNTING, "Calabria"),
    Reserve("ATC CS2", LatLng(39.4500, 16.3500), ReserveType.HUNTING, "Calabria"),
    Reserve("ATC CS3", LatLng(39.3000, 16.2500), ReserveType.HUNTING, "Calabria"),
    Reserve("ATC KR1", LatLng(39.0833, 17.1167), ReserveType.HUNTING, "Calabria"),
    Reserve("ATC KR2", LatLng(39.0000, 17.0000), ReserveType.HUNTING, "Calabria"),
    Reserve("ATC CZ1", LatLng(38.9000, 16.6000), ReserveType.HUNTING, "Calabria"),
    Reserve("ATC CZ2", LatLng(38.8167, 16.5500), ReserveType.HUNTING, "Calabria"),
    Reserve("ATC VV1", LatLng(38.6833, 16.1000), ReserveType.HUNTING, "Calabria"),
    Reserve("ATC VV2", LatLng(38.6500, 16.2000), ReserveType.HUNTING, "Calabria"),
    Reserve("ATC RC1", LatLng(38.3333, 16.4167), ReserveType.HUNTING, "Calabria"),
    Reserve("ATC RC2", LatLng(38.2000, 16.0833), ReserveType.HUNTING, "Calabria"),
    Reserve("CA CL1 Pollino Sud", LatLng(39.9000, 16.1667), ReserveType.HUNTING, "Calabria"),
    Reserve("CA CL2 Sila Grande", LatLng(39.3333, 16.5667), ReserveType.HUNTING, "Calabria"),
    Reserve("CA CL3 Sila Piccola", LatLng(39.0833, 16.6500), ReserveType.HUNTING, "Calabria"),
    Reserve("CA CL4 Serre Vibonesi", LatLng(38.6000, 16.2500), ReserveType.HUNTING, "Calabria"),
    Reserve("CA CL5 Aspromonte", LatLng(38.2000, 16.0833), ReserveType.HUNTING, "Calabria"),
    Reserve("AFV Serra San Bruno", LatLng(38.5833, 16.3333), ReserveType.HUNTING, "Calabria"),
    Reserve("AFV Mongiana", LatLng(38.5667, 16.3333), ReserveType.HUNTING, "Calabria"),
    Reserve("AFV Zagarise", LatLng(39.0167, 16.6333), ReserveType.HUNTING, "Calabria"),
    Reserve("AFV Cotronei", LatLng(39.1500, 16.7833), ReserveType.HUNTING, "Calabria"),
    Reserve("AFV San Luca", LatLng(38.2000, 16.0833), ReserveType.HUNTING, "Calabria"),
    Reserve("AFV Montalto", LatLng(39.8833, 16.0833), ReserveType.HUNTING, "Calabria"),
    // --- SICILIA ---
    Reserve("Lago di Pergusa", LatLng(37.5167, 14.3000), ReserveType.FISHING, "Sicilia"),
    Reserve("Riserva Naturale dello Zingaro", LatLng(38.1167, 12.8333), ReserveType.BOTH, "Sicilia"),
    Reserve("Lago di Piana degli Albanesi", LatLng(37.9667, 13.2833), ReserveType.FISHING, "Sicilia"),
    Reserve("AFV Etna Nord", LatLng(37.8000, 15.0500), ReserveType.HUNTING, "Sicilia"),
    Reserve("ATC Catania CT2", LatLng(37.5000, 15.0833), ReserveType.HUNTING, "Sicilia"),
    Reserve("ATC PA1", LatLng(38.1167, 13.3667), ReserveType.HUNTING, "Sicilia"),
    Reserve("ATC PA2", LatLng(37.9500, 13.4500), ReserveType.HUNTING, "Sicilia"),
    Reserve("ATC ME1", LatLng(38.1833, 15.5500), ReserveType.HUNTING, "Sicilia"),
    Reserve("ATC ME2", LatLng(38.0333, 15.3000), ReserveType.HUNTING, "Sicilia"),
    Reserve("ATC CT1", LatLng(37.5000, 15.0833), ReserveType.HUNTING, "Sicilia"),
    Reserve("ATC CT2", LatLng(37.5000, 15.0833), ReserveType.HUNTING, "Sicilia"),
    Reserve("ATC SR1", LatLng(37.0667, 15.2833), ReserveType.HUNTING, "Sicilia"),
    Reserve("ATC SR2", LatLng(36.9667, 15.1000), ReserveType.HUNTING, "Sicilia"),
    Reserve("ATC RG1", LatLng(36.9333, 14.7333), ReserveType.HUNTING, "Sicilia"),
    Reserve("ATC RG2", LatLng(36.8833, 14.7000), ReserveType.HUNTING, "Sicilia"),
    Reserve("ATC CL1", LatLng(37.4833, 14.0667), ReserveType.HUNTING, "Sicilia"),
    Reserve("ATC EN1", LatLng(37.5667, 14.2833), ReserveType.HUNTING, "Sicilia"),
    Reserve("ATC AG1", LatLng(37.3167, 13.5833), ReserveType.HUNTING, "Sicilia"),
    Reserve("ATC TP1", LatLng(38.0167, 12.5167), ReserveType.HUNTING, "Sicilia"),
    Reserve("CA SI1 Etna Nord", LatLng(37.8000, 15.0500), ReserveType.HUNTING, "Sicilia"),
    Reserve("CA SI2 Etna Sud", LatLng(37.6500, 15.0000), ReserveType.HUNTING, "Sicilia"),
    Reserve("CA SI3 Nebrodi", LatLng(38.0000, 14.7000), ReserveType.HUNTING, "Sicilia"),
    Reserve("CA SI4 Madonie", LatLng(37.8500, 14.0000), ReserveType.HUNTING, "Sicilia"),
    Reserve("CA SI5 Monti Iblei", LatLng(36.9500, 14.8500), ReserveType.HUNTING, "Sicilia"),
    Reserve("AFV Etna Nord", LatLng(37.8000, 15.0500), ReserveType.HUNTING, "Sicilia"),
    Reserve("AFV Etna Sud", LatLng(37.6500, 15.0000), ReserveType.HUNTING, "Sicilia"),
    Reserve("AFV Nebrodi", LatLng(38.0000, 14.7000), ReserveType.HUNTING, "Sicilia"),
    Reserve("AFV Madonie", LatLng(37.8500, 14.0000), ReserveType.HUNTING, "Sicilia"),
    Reserve("AFV Ficuzza", LatLng(37.8833, 13.3833), ReserveType.HUNTING, "Sicilia"),
    Reserve("AFV Monti Sicani", LatLng(37.5667, 13.4500), ReserveType.HUNTING, "Sicilia"),
    Reserve("AFV Iblei", LatLng(36.9500, 14.8500), ReserveType.HUNTING, "Sicilia"),
    Reserve("AFV Bosco della Favara", LatLng(37.6667, 13.6000), ReserveType.HUNTING, "Sicilia"),
    // --- SARDEGNA ---
    Reserve("Lago Omodeo", LatLng(40.0500, 8.9000), ReserveType.FISHING, "Sardegna"),
    Reserve("Stagno di Cabras", LatLng(39.9333, 8.5167), ReserveType.FISHING, "Sardegna"),
    Reserve("Parco del Gennargentu", LatLng(40.0000, 9.3000), ReserveType.BOTH, "Sardegna"),
    Reserve("AFV Monte Lerno", LatLng(40.6000, 9.0000), ReserveType.HUNTING, "Sardegna"),
    Reserve("Comprensorio Gallura Nord", LatLng(41.0000, 9.2000), ReserveType.HUNTING, "Sardegna"),
    Reserve("CT Sardegna Nord", LatLng(40.8000, 9.0000), ReserveType.HUNTING, "Sardegna"),
    Reserve("CT Sardegna Centro", LatLng(40.0000, 9.0000), ReserveType.HUNTING, "Sardegna"),
    Reserve("CT Sardegna Sud", LatLng(39.3000, 9.0000), ReserveType.HUNTING, "Sardegna"),
    Reserve("ZO Gallura", LatLng(40.9000, 9.3000), ReserveType.HUNTING, "Sardegna"),
    Reserve("ZO Barbagia", LatLng(40.1500, 9.2000), ReserveType.HUNTING, "Sardegna"),
    Reserve("ZO Ogliastra", LatLng(39.8500, 9.5500), ReserveType.HUNTING, "Sardegna"),
    Reserve("ZO Sulcis", LatLng(39.2000, 8.5000), ReserveType.HUNTING, "Sardegna"),
    Reserve("ZO Nurra", LatLng(40.6500, 8.3500), ReserveType.HUNTING, "Sardegna"),
    Reserve("AFV Monte Lerno", LatLng(40.6000, 9.0000), ReserveType.HUNTING, "Sardegna"),
    Reserve("AFV Monti di Alà", LatLng(40.6500, 9.2500), ReserveType.HUNTING, "Sardegna"),
    Reserve("AFV Supramonte", LatLng(40.2000, 9.5000), ReserveType.HUNTING, "Sardegna"),
    Reserve("AFV Barbagia di Seulo", LatLng(39.9000, 9.2500), ReserveType.HUNTING, "Sardegna"),
    Reserve("AFV Capoterra", LatLng(39.2000, 8.9667), ReserveType.HUNTING, "Sardegna"),
    Reserve("AFV Sette Fratelli", LatLng(39.3500, 9.3667), ReserveType.HUNTING, "Sardegna"),
    Reserve("AFV Limbara", LatLng(40.8000, 9.1500), ReserveType.HUNTING, "Sardegna"),
    Reserve("AFV Monte Arci", LatLng(39.8000, 8.7000), ReserveType.HUNTING, "Sardegna"),
    Reserve("AFV Monte Albo", LatLng(40.4500, 9.6000), ReserveType.HUNTING, "Sardegna"),
    Reserve("AFV Sulcis", LatLng(39.2000, 8.5000), ReserveType.HUNTING, "Sardegna"),
    // --- SAN MARINO ---
    Reserve("Riserva Faunistica di Montecerreto", LatLng(43.9500, 12.4500), ReserveType.HUNTING, "San Marino"),
    Reserve("Riserva Faunistica di Montegiardino", LatLng(43.9167, 12.4667), ReserveType.HUNTING, "San Marino"),
    Reserve("Zona Naturalistica del Monte Titano", LatLng(43.9333, 12.4500), ReserveType.BOTH, "San Marino"),
    Reserve("Lago di Faetano", LatLng(43.9333, 12.5000), ReserveType.FISHING, "San Marino"),
    Reserve("Torrente Marano (Tratto Sammarinese)", LatLng(43.9500, 12.5167), ReserveType.FISHING, "San Marino"),
    Reserve("Lago di Endine", LatLng(45.7833, 9.9500), ReserveType.FISHING, "Lombardia"),
    Reserve("Lago di Monate", LatLng(45.8000, 8.6667), ReserveType.FISHING, "Lombardia"),
    Reserve("Lago di Mergozzo", LatLng(45.9667, 8.4500), ReserveType.FISHING, "Piemonte"),
    Reserve("Lago del Turano", LatLng(42.2167, 12.9833), ReserveType.FISHING, "Lazio"),
    Reserve("Lago del Salto", LatLng(42.2667, 13.0333), ReserveType.FISHING, "Lazio"),
    Reserve("Lago di Alserio", LatLng(45.8000, 9.2333), ReserveType.FISHING, "Lombardia"),
    Reserve("Lago di Piano", LatLng(46.0333, 9.2167), ReserveType.FISHING, "Lombardia"),
    Reserve("Lago di Barrea", LatLng(41.7667, 13.9833), ReserveType.FISHING, "Abruzzo"),
    Reserve("Lago di Campolattaro", LatLng(41.2667, 14.7833), ReserveType.FISHING, "Campania"),
    Reserve("Lago di Occhito", LatLng(41.5833, 15.0333), ReserveType.FISHING, "Molise"),
    Reserve("Fiume Adda (Valtellina)", LatLng(46.1667, 10.1667), ReserveType.FISHING, "Lombardia"),
    Reserve("Fiume Mera", LatLng(46.3333, 9.4500), ReserveType.FISHING, "Lombardia"),
    Reserve("Fiume Sesia (Valsesia)", LatLng(45.8167, 8.2667), ReserveType.FISHING, "Piemonte"),
    Reserve("Fiume Vara", LatLng(44.2500, 9.7167), ReserveType.FISHING, "Liguria"),
    Reserve("Fiume Nera (Valnerina)", LatLng(42.7333, 12.8833), ReserveType.FISHING, "Umbria"),
    Reserve("Fiume Sangro", LatLng(41.9000, 14.1667), ReserveType.FISHING, "Abruzzo"),
    Reserve("Fiume Volturno (Alto corso)", LatLng(41.4500, 14.2333), ReserveType.FISHING, "Molise"),
    Reserve("Fiume Brenta (Valsugana)", LatLng(46.0167, 11.4667), ReserveType.FISHING, "Trentino"),
    Reserve("Fiume Noce", LatLng(46.3333, 10.8833), ReserveType.FISHING, "Trentino"),
    Reserve("Fiume Tagliamento (Alto corso)", LatLng(46.4167, 12.9833), ReserveType.FISHING, "Friuli-Venezia Giulia"),
    Reserve("Cava di Cinto Caomaggiore", LatLng(45.8167, 12.7333), ReserveType.FISHING, "Veneto"),
    Reserve("Cave di Onara", LatLng(45.5667, 11.8333), ReserveType.FISHING, "Veneto"),
    Reserve("Cava di San Polo", LatLng(45.3833, 10.7000), ReserveType.FISHING, "Lombardia"),
    Reserve("Cave di Spilamberto", LatLng(44.5333, 11.0167), ReserveType.FISHING, "Emilia-Romagna"),
    Reserve("Cava di Ostellato", LatLng(44.7667, 11.9000), ReserveType.FISHING, "Emilia-Romagna"),
    Reserve("Cave di Piadena", LatLng(45.1167, 10.3833), ReserveType.FISHING, "Lombardia"),
    Reserve("Cava di Porto Viro", LatLng(45.0333, 12.3000), ReserveType.FISHING, "Veneto"),
    Reserve("Cava di Montichiari", LatLng(45.4167, 10.3833), ReserveType.FISHING, "Lombardia"),
    Reserve("Cava di Fucecchio", LatLng(43.7333, 10.8000), ReserveType.FISHING, "Toscana"),
    Reserve("Cave di Aprilia", LatLng(41.5833, 12.6500), ReserveType.FISHING, "Lazio"),
    Reserve("Cava di San Giovanni in Persiceto", LatLng(44.4500, 11.1000), ReserveType.FISHING, "Emilia-Romagna"),
    Reserve("Cava di San Martino", LatLng(45.2000, 10.5000), ReserveType.FISHING, "Lombardia"),
    Reserve("Cava di San Zeno", LatLng(45.3500, 10.7000), ReserveType.FISHING, "Lombardia"),
    Reserve("Cava di San Giorgio", LatLng(45.2500, 10.6000), ReserveType.FISHING, "Lombardia"),
    Reserve("Cava di San Pietro in Guarano", LatLng(39.5000, 16.0000), ReserveType.FISHING, "Calabria"),
    Reserve("Cava di San Nicola Arcella", LatLng(40.0000, 15.8000), ReserveType.FISHING, "Calabria"),
    Reserve("Cava di San Giovanni in Fiore", LatLng(39.5000, 16.0000), ReserveType.FISHING, "Calabria"),
    Reserve("Cava di San Marco Argentano", LatLng(39.5000, 16.0000), ReserveType.FISHING, "Calabria"),
    Reserve("Cava di San Sosti", LatLng(39.5000, 16.0000), ReserveType.FISHING, "Calabria"),
    Reserve("Canale Brian", LatLng(45.5833, 12.7333), ReserveType.FISHING, "Veneto"),
    Reserve("Canale Fissero-Tartaro", LatLng(45.1667, 11.2000), ReserveType.FISHING, "Veneto"),
    Reserve("Canale Naviglio Pavese", LatLng(45.3000, 9.1500), ReserveType.FISHING, "Lombardia"),
    Reserve("Canale Villoresi", LatLng(45.5500, 9.1167), ReserveType.FISHING, "Lombardia"),
    Reserve("Canale Emiliano Romagnolo (CER)", LatLng(44.5500, 11.8500), ReserveType.FISHING, "Emilia-Romagna"),
    Reserve("Canale Scolmatore", LatLng(43.5833, 10.3167), ReserveType.FISHING, "Toscana"),
    Reserve("Canale dei Navicelli", LatLng(43.6833, 10.3833), ReserveType.FISHING, "Toscana"),
    Reserve("Canale Reale", LatLng(40.6500, 17.7167), ReserveType.FISHING, "Puglia"),
    Reserve("Canale Litoraneo Tarantino", LatLng(40.4500, 17.2500), ReserveType.FISHING, "Puglia"),
    Reserve("Canale di Calich", LatLng(40.6000, 8.3000), ReserveType.FISHING, "Sardegna"),
    // --- SPOT TROTA & MOSCA (ITALIA) ---
    Reserve("Fiume Sarca (Alto Sarca No-Kill)", LatLng(46.1333, 10.7667), ReserveType.FISHING, "Trentino"),
    Reserve("Fiume Avisio (Val di Fassa)", LatLng(46.4333, 11.6833), ReserveType.FISHING, "Trentino"),
    Reserve("Torrente Rabbies", LatLng(46.3833, 10.8333), ReserveType.FISHING, "Trentino"),
    Reserve("Fiume Noce (Val di Sole)", LatLng(46.3333, 10.8833), ReserveType.FISHING, "Trentino"),
    Reserve("Fiume Brenta (Valstagna No-Kill)", LatLng(45.8667, 11.6333), ReserveType.FISHING, "Veneto"),
    Reserve("Torrente Astico", LatLng(45.7667, 11.3500), ReserveType.FISHING, "Veneto"),
    Reserve("Fiume Piave (Alto Piave)", LatLng(46.5667, 12.3000), ReserveType.FISHING, "Veneto"),
    Reserve("Fiume Sesia (Piode No-Kill)", LatLng(45.8000, 8.0667), ReserveType.FISHING, "Piemonte"),
    Reserve("Torrente Sermenza", LatLng(45.8500, 8.1500), ReserveType.FISHING, "Piemonte"),
    Reserve("Torrente Mastallone", LatLng(45.8167, 8.2667), ReserveType.FISHING, "Piemonte"),
    Reserve("Fiume Adda (Valtellina)", LatLng(46.1667, 10.1667), ReserveType.FISHING, "Lombardia"),
    Reserve("Torrente Mallero", LatLng(46.1667, 9.8667), ReserveType.FISHING, "Lombardia"),
    Reserve("Torrente Toce (Val Formazza)", LatLng(46.3833, 8.3500), ReserveType.FISHING, "Piemonte"),
    Reserve("Fiume Nera (No-Kill Borgo Cerreto)", LatLng(42.7667, 12.9000), ReserveType.FISHING, "Umbria"),
    Reserve("Fiume Corno", LatLng(42.7333, 12.9667), ReserveType.FISHING, "Umbria"),
    Reserve("Fiume Sordo", LatLng(42.7333, 12.8833), ReserveType.FISHING, "Umbria"),
    Reserve("Fiume Lima (Bagni di Lucca)", LatLng(44.0167, 10.5833), ReserveType.FISHING, "Toscana"),
    Reserve("Torrente Orfento", LatLng(42.1500, 14.0000), ReserveType.FISHING, "Abruzzo"),
    Reserve("Fiume Sagittario (Anversa No-Kill)", LatLng(41.9833, 13.8333), ReserveType.FISHING, "Abruzzo"),
    Reserve("Fiume Tirino", LatLng(42.2833, 13.8000), ReserveType.FISHING, "Abruzzo"),
    Reserve("Fiume Velino (No-Kill)", LatLng(42.4167, 12.9500), ReserveType.FISHING, "Lazio"),
    Reserve("Fiume Aniene (Subiaco)", LatLng(41.9500, 13.1000), ReserveType.FISHING, "Lazio"),
    Reserve("Fiume Sele (Alto Sele)", LatLng(40.6500, 15.2333), ReserveType.FISHING, "Campania"),
    Reserve("Fiume Calore Irpino", LatLng(40.9333, 15.0000), ReserveType.FISHING, "Campania"),
    Reserve("Fiume Lao (Castrovillari)", LatLng(39.8500, 15.9500), ReserveType.FISHING, "Calabria"),
    Reserve("Fiume Argentino", LatLng(39.9000, 15.9500), ReserveType.FISHING, "Calabria"),
    Reserve("Torrente Flumendosa (Alto corso)", LatLng(39.9000, 9.3000), ReserveType.FISHING, "Sardegna"),
    Reserve("Torrente Riu Cannisoni", LatLng(39.4500, 9.3500), ReserveType.FISHING, "Sardegna"),
    Reserve("Torrente San Leonardo", LatLng(40.0000, 9.0000), ReserveType.FISHING, "Sardegna"),
    Reserve("Torrente Alcantara (Gole)", LatLng(37.8667, 15.1500), ReserveType.FISHING, "Sicilia"),
    Reserve("Torrente San Paolo", LatLng(37.9500, 14.8000), ReserveType.FISHING, "Sicilia"),
    Reserve("Riserva di San Pietro (Sarca)", LatLng(46.0833, 10.7667), ReserveType.FISHING, "Trentino"),
    Reserve("Riserva di Sporminore (Noce)", LatLng(46.2667, 11.0333), ReserveType.FISHING, "Trentino"),
    Reserve("Riserva di Sostegno (Sesia)", LatLng(45.6500, 8.2667), ReserveType.FISHING, "Piemonte"),
    Reserve("Riserva di Castel di Sangro", LatLng(41.7833, 14.0833), ReserveType.FISHING, "Abruzzo"),
    Reserve("Riserva di Leonessa (Velino)", LatLng(42.4833, 12.9667), ReserveType.FISHING, "Lazio")
    )

@Composable
fun MapsScreen(
    onConsultAI: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiSpec = rememberResponsiveUiSpec()

    // Spacing responsive per card (compatta ma leggibile con testo grande)
    val cardOuterHorizontalPadding = when {
        uiSpec.widthClass == WidthClass.Compact -> 10.dp
        uiSpec.isLargeText -> 14.dp
        else -> 12.dp
    }
    val cardOuterVerticalPadding = if (uiSpec.isLargeText) 76.dp else 72.dp
    val cardInnerHorizontalPadding = if (uiSpec.isLargeText) 14.dp else 12.dp
    val cardInnerVerticalPadding = if (uiSpec.isLargeText) 8.dp else 6.dp
    val sectionSpacer = if (uiSpec.isLargeText) 8.dp else 6.dp
    val buttonHeight = uiSpec.actionButtonSize
    val closeButtonSize = (uiSpec.actionButtonSize - 4.dp).coerceAtLeast(44.dp)
    val cardMaxHeight = if (uiSpec.isLargeText) 250.dp else 230.dp

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
    var selectedReserve by remember { mutableStateOf<Reserve?>(null) }
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
                modifier = Modifier.padding(bottom = 15.dp, end = 45.dp) // Alzato a 32.dp dal fondo
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Aggiungi Località")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = properties,
                uiSettings = uiSettings
            ) {
                (staticReserves + customReserves).forEach { reserve ->
                    Marker(
                        state = MarkerState(position = reserve.location),
                        title = reserve.name,
                        onClick = {
                            selectedReserve = reserve
                            true
                        }
                    )
                }
            }

            // Overlay card riserva selezionata
            selectedReserve?.let { reserve ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = cardOuterHorizontalPadding, vertical = cardOuterVerticalPadding)
                        .heightIn(max = cardMaxHeight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = cardInnerHorizontalPadding, vertical = cardInnerVerticalPadding)
                    ) {
                        // Intestazione: nome + chiudi
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = reserve.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = if (uiSpec.isLargeText) 14.sp else 13.sp),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { selectedReserve = null },
                                modifier = Modifier.size(closeButtonSize)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Chiudi",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        val typeText = when (reserve.type) {
                            ReserveType.HUNTING -> "Caccia"
                            ReserveType.FISHING -> "Pesca"
                            ReserveType.BOTH -> "Caccia & Pesca"
                        }
                        Text(
                            text = buildString {
                                append(typeText)
                                if (!reserve.region.isNullOrBlank()) append(" · ${reserve.region}")
                                if (!reserve.notes.isNullOrBlank()) append(" · ${reserve.notes}")
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = if (uiSpec.isLargeText) 12.sp else 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(sectionSpacer))

                        // Link AI: iscrizione / libero ingresso
                        TextButton(
                            onClick = {
                                val query = "Dammi informazioni sulla riserva \"${reserve.name}\": è a libero ingresso o è necessario iscriversi? Come ci si iscrive?"
                                onConsultAI(query)
                                selectedReserve = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(buttonHeight),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "🤖 Iscrizione / Libero ingresso — Consulta AI →",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = uiSpec.chipTextSize,
                                    textDecoration = TextDecoration.Underline
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Navigazione
                        OutlinedButton(
                            onClick = {
                                val gmmIntentUri = Uri.parse("google.navigation:q=${reserve.location.latitude},${reserve.location.longitude}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                if (mapIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(mapIntent)
                                } else {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
                                }
                                selectedReserve = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(buttonHeight),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                "Indicazioni stradali",
                                fontSize = uiSpec.chipTextSize,
                                maxLines = 1
                            )
                        }

                        Spacer(Modifier.height(if (uiSpec.isLargeText) 6.dp else 4.dp))
                    }
                }
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
