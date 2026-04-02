package it.mygroup.org.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import it.mygroup.org.ui.theme.rememberResponsiveUiSpec

@Composable
fun SecondaryToolbar(
    currentView: HomeSubView,
    onRssClick: () -> Unit,
    onMapClick: () -> Unit,
    onDatabaseClick: () -> Unit,
    onAiManagerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiSpec = rememberResponsiveUiSpec()
    val toolbarHorizontalPadding = (uiSpec.screenHorizontalPadding - 4.dp).coerceAtLeast(6.dp)
    val toolbarVerticalPadding = if (uiSpec.isLargeText) 6.dp else 4.dp

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = toolbarHorizontalPadding, vertical = toolbarVerticalPadding),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SecondaryToolbarItem(
                icon = Icons.Default.RssFeed,
                contentDescription = "RSS Feed",
                isSelected = currentView == HomeSubView.RSS,
                onClick = onRssClick,
                iconContainerSize = uiSpec.actionButtonSize + 2.dp
            )
            SecondaryToolbarItem(
                icon = Icons.Default.Map,
                contentDescription = "Laws Map",
                isSelected = currentView == HomeSubView.MAP,
                onClick = onMapClick,
                iconContainerSize = uiSpec.actionButtonSize + 2.dp
            )
            SecondaryToolbarItem(
                icon = Icons.Default.Storage,
                contentDescription = "Personal Database",
                isSelected = currentView == HomeSubView.DATABASE,
                onClick = onDatabaseClick,
                iconContainerSize = uiSpec.actionButtonSize + 2.dp
            )
            SecondaryToolbarItem(
                icon = Icons.Default.AutoAwesome,
                contentDescription = "AI Manager",
                isSelected = currentView == HomeSubView.AI_MANAGER,
                onClick = onAiManagerClick,
                iconContainerSize = uiSpec.actionButtonSize + 2.dp
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
    iconContainerSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val background = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .size(iconContainerSize)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
