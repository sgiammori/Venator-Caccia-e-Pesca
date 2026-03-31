package it.mygroup.org.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import it.mygroup.org.network.GoogleSearchApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.Locale

data class SearchResult(
    val title: String,
    val link: String,
    val snippet: String
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isWebResult: Boolean = false,
    val results: List<SearchResult> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IAManagerScreen(
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var isLoading by remember { mutableStateOf(false) }
    var isSearchingWeb by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val uriHandler = LocalUriHandler.current

    // Initialize Gemini model
    val model = remember {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.0-flash")
    }

    val suggestions = listOf(
        "Calendario Venatorio Toscana",
        "Regolamento Pesca Sardegna",
        "Misure Minime Pesci",
        "Zone No-Kill",
        "Licenza di caccia rinnovo"
    )

    fun cleanUrl(url: String): String {
        return when {
            url.startsWith("//") -> "https:$url"
            url.startsWith("/") -> "https://duckduckgo.com$url"
            !url.startsWith("http") -> "https://$url"
            else -> url
        }
    }

    suspend fun searchWebFallback(query: String): List<SearchResult> {
        return withContext(Dispatchers.IO) {
            try {
                val searchUrl = "https://html.duckduckgo.com/html/?q=" + query.replace(" ", "+")
                val doc = Jsoup.connect(searchUrl).userAgent("Mozilla/5.0").get()
                doc.select("div.result__body").take(3).map { element ->
                    val rawLink = element.select("a.result__a").attr("href")
                    SearchResult(
                        title = element.select("a.result__a").text(),
                        link = cleanUrl(rawLink),
                        snippet = element.select("a.result__snippet").text()
                    )
                }
            } catch (e: Exception) { emptyList() }
        }
    }

    suspend fun searchWeb(query: String): List<SearchResult> {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = "AIzaSyDyNq6kYjvLnhAwckRtj5wnXcYZoxNisVs"
                val cx = "873a4bf543bb64bd2"
                
                val jsonResponse = GoogleSearchApi.retrofitService.search(
                    apiKey = apiKey,
                    searchEngineId = cx,
                    query = query,
                    packageName = null,
                    fingerprint = null
                )
                
                val results = mutableListOf<SearchResult>()
                val itemsPattern = "\\\"items\\\": \\[(.*)\\]".toRegex(RegexOption.DOT_MATCHES_ALL)
                val itemsMatch = itemsPattern.find(jsonResponse)
                
                if (itemsMatch != null) {
                    val itemsJson = itemsMatch.groupValues[1]
                    val individualItemPattern = "\\{.*?\\\"title\\\": \\\"(.*?)\\\".*?\\\"link\\\": \\\"(.*?)\\\".*?\\\"snippet\\\": \\\"(.*?)\\\".*?\\}".toRegex(RegexOption.DOT_MATCHES_ALL)
                    individualItemPattern.findAll(itemsJson).take(3).forEach { match ->
                        results.add(SearchResult(
                            title = match.groupValues[1],
                            link = cleanUrl(match.groupValues[2]),
                            snippet = match.groupValues[3].replace("\\n", " ").replace("\\\"", "\"")
                        ))
                    }
                }

                if (results.isEmpty()) searchWebFallback(query) else results
            } catch (e: Exception) {
                searchWebFallback(query)
            }
        }
    }

    fun sendMessage(text: String, useWeb: Boolean = false) {
        if (text.isBlank()) return
        messages.add(ChatMessage(text, true))
        inputText = ""
        isLoading = true
        
        coroutineScope.launch {
            try {
                val response = model.generateContent(text)
                val aiText = response.text ?: "Spiacente, non ho ricevuto una risposta valida."
                messages.add(ChatMessage(aiText, false))
            } catch (e: Exception) {
                Log.e("IAManager", "Error generating content", e)
                
                // Fallback to web search if AI fails or for specific queries
                isSearchingWeb = true
                val webResults = searchWeb(text)
                
                if (webResults.isNotEmpty()) {
                    val preamble = "Ho trovato queste informazioni sul web per la tua richiesta:"
                    messages.add(ChatMessage(preamble, false, isWebResult = true, results = webResults))
                } else {
                    messages.add(ChatMessage("Spiacente, si è verificato un errore e non ho trovato informazioni utili.", false))
                }
            }
            isLoading = false
            isSearchingWeb = false
        }
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
                spokenText?.let { sendMessage(it) }
            }
        }
    )

    fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "it-IT")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Cosa vuoi cercare?")
        }
        speechRecognizerLauncher.launch(intent)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "AI Manager", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(text = "Assistente Intelligente Venator", style = MaterialTheme.typography.bodySmall)
                }
                
                IconButton(
                    onClick = { messages.clear() },
                    enabled = messages.isNotEmpty()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Pulisci",
                            tint = if (messages.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                        )
                        Text("Clean", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = if (messages.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(messages) { message ->
                ChatBubble(message, onLinkClick = { link ->
                    try {
                        uriHandler.openUri(link)
                    } catch (e: Exception) {
                        Log.e("IAManager", "Error opening URI: $link", e)
                    }
                })
            }
            if (isLoading) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSearchingWeb) "Ricerca in corso..." else "Pensando...", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 4.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(suggestions) { suggestion ->
                        FilterChip(
                            selected = false,
                            onClick = { sendMessage(suggestion) },
                            label = { Text(suggestion, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            leadingIcon = { Icon(Icons.Default.Public, null, Modifier.size(12.dp)) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(max = 120.dp),
                        placeholder = { Text("Chiedi all'AI...", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        shape = RoundedCornerShape(24.dp),
                        trailingIcon = { IconButton(onClick = { startVoiceInput() }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Mic, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) } },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendMessage(inputText) }),
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { sendMessage(inputText) },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, onLinkClick: (String) -> Unit) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val color = if (message.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Box(modifier = Modifier.widthIn(max = 320.dp).clip(RoundedCornerShape(12.dp)).background(color).padding(12.dp)) {
            Text(text = message.text, style = MaterialTheme.typography.bodyMedium)
        }
        
        if (message.results.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            message.results.forEach { result ->
                ResultCard(result, onLinkClick)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun ResultCard(result: SearchResult, onLinkClick: (String) -> Unit) {
    val isPdf = result.link.lowercase().endsWith(".pdf")
    Card(
        modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isPdf) Icons.Default.Description else Icons.Default.Public,
                    contentDescription = null,
                    tint = if (isPdf) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = result.title, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = result.snippet, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onLinkClick(result.link) },
                modifier = Modifier.fillMaxWidth().height(36.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isPdf) "Scarica PDF" else "Apri Sito", fontSize = 12.sp)
            }
        }
    }
}
