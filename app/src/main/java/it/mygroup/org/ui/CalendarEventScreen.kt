package it.mygroup.org.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import it.mygroup.org.network.CacciaPescaApi
import it.mygroup.org.network.UserPresenceManager
import it.mygroup.org.ui.theme.rememberResponsiveUiSpec
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID

data class UserData(val userId: String, val isAttivo: Boolean)

data class CalendarEvent(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val ownerId: String,
    val isAccepted: Boolean = false,
    val isRefused: Boolean = false,
    val isReceived: Boolean = false
)

data class CalendarEventInvitation(
    val event: CalendarEvent,
    val senderId: String
)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CalendarEventScreen(
    modifier: Modifier = Modifier,
    userId: String = "admin123",
    searchQuery: String = ""
) {
    val context = LocalContext.current
    val presenceManager = remember { UserPresenceManager.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    val uiSpec = rememberResponsiveUiSpec()

    val sectionGap = if (uiSpec.isLargeText) 12.dp else 8.dp
    val cardInnerPadding = if (uiSpec.isLargeText) 12.dp else 8.dp
    val contactListMaxHeight = if (uiSpec.isLargeText) 240.dp else 200.dp
    val communityActionSize = uiSpec.actionButtonSize

    val calendar = Calendar.getInstance()
    var selectedDateMillis by remember { mutableLongStateOf(calendar.timeInMillis) }
    var showDatePicker by remember { mutableStateOf(false) }

    val formattedDate = remember(selectedDateMillis) {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}"
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var showFriendsDialog by remember { mutableStateOf(false) }
    var eventTitle by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }

    // State for details view
    var selectedEventForDetails by remember { mutableStateOf<CalendarEvent?>(null) }

    // States for friend management
    var localFriendSearchQuery by remember { mutableStateOf("") }
    val allUsers = remember { mutableStateListOf<UserData>() }

    // Load friends from persistent storage
    val friends = remember { mutableStateListOf<String>().apply { addAll(presenceManager.getFriends()) } }

    val invitations = remember { mutableStateListOf<CalendarEventInvitation>() }

    // Persistence management
    val sharedPrefs = remember { context.getSharedPreferences("calendar_prefs", Context.MODE_PRIVATE) }

    val handledEventIds = remember {
        mutableStateListOf<String>().apply {
            addAll(sharedPrefs.getStringSet("handled_events", emptySet()) ?: emptySet())
        }
    }

    val notifiedEventIds = remember {
        mutableStateListOf<String>().apply {
            addAll(sharedPrefs.getStringSet("notified_events", emptySet()) ?: emptySet())
        }
    }

    val acceptedEvents = remember {
        mutableStateListOf<CalendarEvent>().apply {
            val savedJson = sharedPrefs.getString("accepted_events_json", "[]")
            try {
                val arr = JSONArray(savedJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(CalendarEvent(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        description = obj.getString("description"),
                        date = obj.getString("date"),
                        ownerId = obj.getString("ownerId"),
                        isAccepted = true
                    ))
                }
            } catch (e: Exception) {
                Log.e("CalendarEvent", "Error loading accepted events", e)
            }
        }
    }

    fun saveAcceptedEvents() {
        val arr = JSONArray()
        acceptedEvents.forEach { event ->
            val obj = JSONObject().apply {
                put("id", event.id)
                put("title", event.title)
                put("description", event.description)
                put("date", event.date)
                put("ownerId", event.ownerId)
            }
            arr.put(obj)
        }
        sharedPrefs.edit().putString("accepted_events_json", arr.toString()).apply()
    }

    fun markEventHandled(eventId: String) {
        if (!handledEventIds.contains(eventId)) {
            handledEventIds.add(eventId)
            sharedPrefs.edit().putStringSet("handled_events", handledEventIds.toSet()).apply()
        }
    }

    fun markEventNotified(eventId: String) {
        if (!notifiedEventIds.contains(eventId)) {
            notifiedEventIds.add(eventId)
            sharedPrefs.edit().putStringSet("notified_events", notifiedEventIds.toSet()).apply()
        }
    }

    // Real-time polling effect (like Angular signals/effects for reactive updates)
    LaunchedEffect(userId) {
        while (isActive) {
            try {
                // 1. Fetch Invites
                val invitesResponse = CacciaPescaApi.retrofitService.getInvites(userId)
                Log.d("CalendarEvent", "Invites raw response: $invitesResponse")

                try {
                    val invitesArray = JSONArray(invitesResponse)
                    val newInvites = mutableListOf<CalendarEventInvitation>()
                    for (i in 0 until invitesArray.length()) {
                        val obj = invitesArray.getJSONObject(i)
                        val eventId = obj.getString("_id")

                        // Skip if already accepted or refused
                        if (handledEventIds.contains(eventId)) continue

                        val ownerId = obj.getString("ownerId")
                        val event = CalendarEvent(
                            id = eventId,
                            title = obj.getString("title"),
                            description = obj.getString("description"),
                            date = obj.getString("date"),
                            ownerId = ownerId,
                            isReceived = notifiedEventIds.contains(eventId)
                        )
                        val senderId = if (obj.has("senderId")) obj.getString("senderId") else ownerId

                        val invitation = CalendarEventInvitation(event, senderId)
                        newInvites.add(invitation)

                        // If not notified yet, show notification
                        if (!event.isReceived) {
                            showLocalNotification(context, invitation)
                            markEventNotified(eventId)
                        }
                    }

                    // Reactive update for invitations
                    if (invitations.size != newInvites.size || invitations.zip(newInvites).any { it.first != it.second }) {
                        invitations.clear()
                        invitations.addAll(newInvites)
                    }
                } catch (e: Exception) {
                    Log.e("CalendarEvent", "Error parsing invites", e)
                }

                // 2. Fetch all users status
                val usersResponse = CacciaPescaApi.retrofitService.getAllUsers()
                val usersArray = JSONArray(usersResponse)

                val newList = mutableListOf<UserData>()
                for (i in 0 until usersArray.length()) {
                    val obj = usersArray.getJSONObject(i)
                    val uId = obj.getString("userId")
                    val isAttivo = obj.optBoolean("attivo", false) || obj.optString("attivo") == "true"
                    if (uId != userId) {
                        newList.add(UserData(uId, isAttivo))
                    }
                }

                if (allUsers.size != newList.size || allUsers.zip(newList).any { it.first != it.second }) {
                    allUsers.clear()
                    allUsers.addAll(newList)
                }
            } catch (e: Exception) {
                Log.e("CalendarEvent", "Real-time sync error", e)
            }
            delay(5000) // Poll every 5 seconds
        }
    }

    val filteredUsers = remember(localFriendSearchQuery, allUsers, friends) {
        if (localFriendSearchQuery.isBlank()) emptyList()
        else allUsers.filter {
            it.userId.contains(localFriendSearchQuery, ignoreCase = true) && !friends.contains(it.userId)
        }
    }

    val filteredAcceptedEvents = remember(searchQuery, acceptedEvents) {
        if (searchQuery.isBlank()) acceptedEvents
        else acceptedEvents.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = uiSpec.screenHorizontalPadding, vertical = if (uiSpec.isLargeText) 10.dp else 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Community",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = { showFriendsDialog = true }, modifier = Modifier.size(communityActionSize)) {
                    Icon(Icons.Default.Group, contentDescription = "Amici")
                }
            }
            Text(
                "($userId)",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .combinedClickable(
                        onClick = { },
                        onLongClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Venator User ID", userId)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "ID copiato negli appunti!", Toast.LENGTH_SHORT).show()
                        }
                    )
            )
        }

        Spacer(Modifier.height(if (uiSpec.isLargeText) 6.dp else 4.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(cardInnerPadding)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showDatePicker = true }
                        .padding(if (uiSpec.isLargeText) 14.dp else 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(if (uiSpec.isLargeText) 14.dp else 12.dp))
                    Column {
                        Text("Data Selezionata", style = MaterialTheme.typography.labelSmall)
                        Text(formattedDate, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Spacer(Modifier.height(sectionGap))

                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = uiSpec.actionButtonSize),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Crea Evento")
                }
            }
        }

        Spacer(Modifier.height(if (uiSpec.isLargeText) 18.dp else 16.dp))

        if (invitations.isNotEmpty()) {
            Text("Inviti Ricevuti", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(sectionGap))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(uiSpec.listItemSpacing)
            ) {
                items(invitations) { invitation ->
                    InvitationCard(
                        invitation = invitation,
                        onAccept = {
                            coroutineScope.launch {
                                try {
                                    val response = CacciaPescaApi.retrofitService.respondToInvite(invitation.event.id, userId, "accept")
                                    if (response.contains("success", ignoreCase = true)) {
                                        acceptedEvents.add(invitation.event.copy(isAccepted = true))
                                        saveAcceptedEvents()
                                        markEventHandled(invitation.event.id)
                                        invitations.remove(invitation)
                                        Toast.makeText(context, "Invito accettato!", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Log.e("CalendarEvent", "Error accepting invite", e)
                                }
                            }
                        },
                        onRefuse = {
                            coroutineScope.launch {
                                try {
                                    val response = CacciaPescaApi.retrofitService.respondToInvite(invitation.event.id, userId, "refuse")
                                    if (response.contains("success", ignoreCase = true)) {
                                        markEventHandled(invitation.event.id)
                                        invitations.remove(invitation)
                                        Toast.makeText(context, "Invito rifiutato.", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Log.e("CalendarEvent", "Error refusing invite", e)
                                }
                            }
                        }
                    )
                }
            }
            Spacer(Modifier.height(if (uiSpec.isLargeText) 18.dp else 16.dp))
        }

        Text("I tuoi Eventi", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(sectionGap))

        if (filteredAcceptedEvents.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nessun evento in questa data o corrispondente alla ricerca", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(uiSpec.listItemSpacing)
            ) {
                items(filteredAcceptedEvents) { event ->
                    EventCard(
                        event = event,
                        onClick = { selectedEventForDetails = event }
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            }
        ) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
            DatePicker(state = datePickerState)
            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Nuovo Evento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = eventTitle,
                        onValueChange = { eventTitle = it },
                        label = { Text("Titolo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = eventDescription,
                        onValueChange = { eventDescription = it },
                        label = { Text("Descrizione") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Text("Data: $formattedDate", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val json = JSONObject().apply {
                                    put("title", eventTitle)
                                    put("description", eventDescription)
                                    put("date", formattedDate)
                                    put("ownerId", userId)
                                    put("invitedUserIds", JSONArray(friends))
                                }
                                val response = CacciaPescaApi.retrofitService.createEvent(json.toString())
                                if (response.contains("success", ignoreCase = true)) {
                                    val newEvent = CalendarEvent(
                                        id = UUID.randomUUID().toString(), // Will be updated by polling or we could parse ID from response
                                        title = eventTitle,
                                        description = eventDescription,
                                        date = formattedDate,
                                        ownerId = userId,
                                        isAccepted = true
                                    )
                                    acceptedEvents.add(newEvent)
                                    saveAcceptedEvents()
                                    showCreateDialog = false
                                    eventTitle = ""
                                    eventDescription = ""
                                    Toast.makeText(context, "Evento creato!", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Log.e("CalendarEvent", "Error creating event", e)
                                Toast.makeText(context, "Errore durante la creazione", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = eventTitle.isNotBlank()
                ) {
                    Text("Invia Inviti")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Annulla") }
            }
        )
    }

    if (showFriendsDialog) {
        AlertDialog(
            onDismissRequest = { showFriendsDialog = false },
            title = { Text("Gestione Community") },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(sectionGap)) {
                    OutlinedTextField(
                        value = localFriendSearchQuery,
                        onValueChange = { localFriendSearchQuery = it },
                        label = { Text("Cerca utenti per ID") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )

                    if (localFriendSearchQuery.isNotBlank()) {
                        Text("Risultati Ricerca", style = MaterialTheme.typography.labelSmall)
                        LazyColumn(modifier = Modifier.heightIn(max = contactListMaxHeight)) {
                            items(filteredUsers) { user ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (user.isAttivo) Color.Green else Color.Gray)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(user.userId, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    IconButton(onClick = {
                                        presenceManager.addFriend(user.userId)
                                        friends.add(user.userId)
                                    }, modifier = Modifier.size(uiSpec.actionButtonSize)) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = "Aggiungi", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    Text("I tuoi contatti (${friends.size})", style = MaterialTheme.typography.labelSmall)
                    LazyColumn(modifier = Modifier.heightIn(max = contactListMaxHeight)) {
                        items(friends) { friendId ->
                            val isOnline = allUsers.find { it.userId == friendId }?.isAttivo ?: false
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isOnline) Color.Green else Color.Gray)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(friendId, style = MaterialTheme.typography.bodyMedium)
                                }
                                IconButton(onClick = {
                                    presenceManager.removeFriend(friendId)
                                    friends.remove(friendId)
                                }, modifier = Modifier.size(uiSpec.actionButtonSize)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Rimuovi", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFriendsDialog = false }) { Text("Chiudi") }
            }
        )
    }

    selectedEventForDetails?.let { event ->
        AlertDialog(
            onDismissRequest = { selectedEventForDetails = null },
            title = { Text(event.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Data: ${event.date}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("Organizzatore: ${event.ownerId}", style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider()
                    Text(event.description, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedEventForDetails = null }) { Text("Chiudi") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        acceptedEvents.remove(event)
                        saveAcceptedEvents()
                        selectedEventForDetails = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Rimuovi dal Calendario")
                }
            }
        )
    }
}

@Composable
fun InvitationCard(invitation: CalendarEventInvitation, onAccept: () -> Unit, onRefuse: () -> Unit) {
    val uiSpec = rememberResponsiveUiSpec()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(if (uiSpec.isLargeText) 14.dp else 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Mail, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Invito da: ${invitation.senderId}", style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(if (uiSpec.isLargeText) 10.dp else 8.dp))
            Text(invitation.event.title, style = MaterialTheme.typography.titleMedium)
            Text(invitation.event.date, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(if (uiSpec.isLargeText) 14.dp else 12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = onRefuse,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .heightIn(min = uiSpec.actionButtonSize)
                ) {
                    Text("Rifiuta")
                }
                Button(onClick = onAccept, modifier = Modifier.heightIn(min = uiSpec.actionButtonSize)) {
                    Text("Accetta")
                }
            }
        }
    }
}

@Composable
fun EventCard(event: CalendarEvent, onClick: () -> Unit) {
    val uiSpec = rememberResponsiveUiSpec()
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(if (uiSpec.isLargeText) 18.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (uiSpec.isLargeText) 44.dp else 40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(if (uiSpec.isLargeText) 18.dp else 16.dp))
            Column(Modifier.weight(1f)) {
                Text(event.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${event.date} • ${event.ownerId}", style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

private fun showLocalNotification(context: Context, invitation: CalendarEventInvitation) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "calendar_invites"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Inviti Calendario", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Nuovo invito!")
        .setContentText("${invitation.senderId} ti ha invitato a: ${invitation.event.title}")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(invitation.event.id.hashCode(), notification)
}
