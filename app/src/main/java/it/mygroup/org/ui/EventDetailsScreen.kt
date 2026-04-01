package it.mygroup.org.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.mygroup.org.R
import it.mygroup.org.ui.navigation.NavigationDestination
import it.mygroup.org.ui.theme.rememberResponsiveUiSpec

object EventDetailsDestination : NavigationDestination {
    override val route = "event_details"
    override val titleRes = R.string.app_name // Or a specific string
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    event: CalendarEvent,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiSpec = rememberResponsiveUiSpec()
    val sectionGap = if (uiSpec.isLargeText) 18.dp else 16.dp

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dettagli Evento") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = uiSpec.screenHorizontalPadding, vertical = sectionGap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = if (uiSpec.isLargeText) 5.dp else 4.dp)
            ) {
                Column(modifier = Modifier.padding(if (uiSpec.isLargeText) 18.dp else 16.dp)) {
                    Text(text = event.title, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(if (uiSpec.isLargeText) 10.dp else 8.dp))
                    Text(text = "Data: ${event.date}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(if (uiSpec.isLargeText) 10.dp else 8.dp))
                    Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(sectionGap))
                    Text(
                        text = "Organizzatore: ${event.ownerId}", 
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(sectionGap)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = if (uiSpec.isLargeText) 50.dp else 46.dp)
                ) {
                    Text("Annulla")
                }
                
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = if (uiSpec.isLargeText) 50.dp else 46.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Elimina")
                }
            }
        }
    }
}
