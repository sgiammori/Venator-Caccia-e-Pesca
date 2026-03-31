package it.mygroup.org.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import it.mygroup.org.ui.HomeSubView

@Composable
fun SecondaryToolbar(
    currentView: HomeSubView,
    onRssClick: () -> Unit,
    onMapClick: () -> Unit,
    onDatabaseClick: () -> Unit,
    onEventsClick: () -> Unit,
    onAiManagerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SecondaryToolbarItem(
                icon = Icons.Default.RssFeed,
                contentDescription = "RSS Feed",
                isSelected = currentView == HomeSubView.RSS,
                onClick = onRssClick
            )
            SecondaryToolbarItem(
                icon = Icons.Default.Map,
                contentDescription = "Laws Map",
                isSelected = currentView == HomeSubView.MAP,
                onClick = onMapClick
            )
            SecondaryToolbarItem(
                icon = Icons.Default.Storage,
                contentDescription = "Personal Database",
                isSelected = currentView == HomeSubView.DATABASE,
                onClick = onDatabaseClick
            )
            SecondaryToolbarItem(
                icon = Icons.Default.DateRange,
                contentDescription = "Scheduled Events",
                isSelected = currentView == HomeSubView.EVENTS,
                onClick = onEventsClick
            )
            SecondaryToolbarItem(
                icon = Icons.Default.AutoAwesome,
                contentDescription = "AI Manager",
                isSelected = currentView == HomeSubView.AI_MANAGER,
                onClick = onAiManagerClick
            )
        }
    }
}

@Composable
private fun SecondaryToolbarItem(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = modifier
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint
            )
        }
    }
}
