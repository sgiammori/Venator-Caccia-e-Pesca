package it.mygroup.org.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import it.mygroup.org.network.GoogleSearchApi
import it.mygroup.org.ui.components.AdBannerController
import it.mygroup.org.ui.theme.rememberResponsiveUiSpec
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
    initialQuery: String = "",
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var isLoading by remember { mutableStateOf(false) }
    var isSearchingWeb by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val uiSpec = rememberResponsiveUiSpec()
    val view = LocalView.current

    var isInputFocused by remember { mutableStateOf(false) }
    var isKeyboardOpen by remember { mutableStateOf(false) }

    // Rilevamento tastiera affidabile tramite ridimensionamento della finestra
    DisposableEffect(view) {
        val listener = android.view.ViewTreeObserver.OnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            // Soglia empirica del 15% dell'altezza dello schermo per rilevare la tastiera
            val isOpen = keypadHeight > screenHeight * 0.15
            if (isKeyboardOpen != isOpen) {
                isKeyboardOpen = isOpen
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    // Imposta il ridimensionamento della finestra per questo schermo
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val window = activity?.window
        val previousSoftInputMode = window?.attributes?.softInputMode

        @Suppress("DEPRECATION")
        window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        )

        onDispose {
            // Ripristina la modalità precedente e assicura che il banner sia visibile uscendo
            previousSoftInputMode?.let { window.setSoftInputMode(it) }
            AdBannerController.isVisible = true
        }
    }

    // Initialize Gemini model
    val model = remember {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-3-flash-preview")
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
        focusManager.clearFocus()
        isInputFocused = false
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
                
                // Fallback to web search if AI fails
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

    // Handle initial query
    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank() && messages.isEmpty()) {
            sendMessage(initialQuery)
        }
    }

    // Sincronizzazione Banner e Focus: il banner scompare se c'è focus O tastiera aperta
    LaunchedEffect(isKeyboardOpen, isInputFocused) {
        AdBannerController.isVisible = !isKeyboardOpen && !isInputFocused
    }

    // Quando la tastiera si chiude (es. primo back), resettiamo il focus immediatamente
    LaunchedEffect(isKeyboardOpen) {
        if (!isKeyboardOpen) {
            focusManager.clearFocus()
            isInputFocused = false
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
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            // No .imePadding() to avoid extra space with ADJUST_RESIZE
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(uiSpec.screenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(uiSpec.listItemSpacing)
        ) {
            items(items = messages, key = { it.timestamp }) { message ->
                ChatBubble(
                    message = message,
                    onLinkClick = { link ->
                        try {
                            uriHandler.openUri(link)
                        } catch (e: Exception) {
                            Log.e("IAManager", "Error opening URI: $link", e)
                        }
                    },
                    bubbleMaxWidth = uiSpec.chatBubbleMaxWidth,
                    resultCardMaxWidth = uiSpec.resultCardMaxWidth
                )
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

        Surface(
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 8.dp,
                    end = 8.dp,
                    top = if (isKeyboardOpen || isInputFocused) 0.dp else 4.dp,
                    bottom = if (isKeyboardOpen || isInputFocused) 8.dp else 4.dp
                )
            ) {
                if (!isKeyboardOpen && !isInputFocused) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { messages.clear() },
                            enabled = messages.isNotEmpty(),
                            modifier = Modifier.heightIn(min = uiSpec.actionButtonSize)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Pulisci",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pulisci", fontSize = uiSpec.chipTextSize)
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 4.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(suggestions) { suggestion ->
                            FilterChip(
                                selected = false,
                                onClick = { sendMessage(suggestion) },
                                label = {
                                    Text(
                                        suggestion,
                                        fontSize = uiSpec.chipTextSize,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                leadingIcon = { Icon(Icons.Default.Public, null, Modifier.size(16.dp)) },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.height(uiSpec.chipHeight)
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { isInputFocused = it.isFocused }
                            .heightIn(min = uiSpec.actionButtonSize, max = 120.dp),
                        placeholder = { Text("Chiedi all'AI...", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        shape = RoundedCornerShape(24.dp),
                        trailingIcon = {
                            IconButton(
                                onClick = { startVoiceInput() },
                                modifier = Modifier.size(uiSpec.actionButtonSize)
                            ) {
                                Icon(
                                    Icons.Default.Mic,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendMessage(inputText) }),
                        textStyle = LocalTextStyle.current.copy(fontSize = uiSpec.inputTextSize)
                    )
                    Spacer(modifier = Modifier.width(if (uiSpec.isLargeText) 10.dp else 8.dp))
                    IconButton(
                        onClick = { sendMessage(inputText) },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(uiSpec.actionButtonSize)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

private val messageTokenRegex = "(?i)\\b((?:https?://|www\\.)[^\\s<>()]+|[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,})".toRegex()

private fun trimTrailingPunctuation(raw: String): String =
    raw.trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '}')

private fun normalizeOutgoingUrl(raw: String): String {
    val trimmed = trimTrailingPunctuation(raw)
    return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
        trimmed
    } else {
        "https://$trimmed"
    }
}

private fun normalizeOutgoingEmail(raw: String): String {
    val trimmed = trimTrailingPunctuation(raw)
    return if (trimmed.startsWith("mailto:", ignoreCase = true)) trimmed else "mailto:$trimmed"
}

@Composable
private fun LinkifiedMessageText(
    text: String,
    modifier: Modifier = Modifier
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val baseStyle = MaterialTheme.typography.bodyMedium

    val annotated = remember(text, linkColor) {
        buildAnnotatedString {
            var lastIndex = 0
            messageTokenRegex.findAll(text).forEach { match ->
                val start = match.range.first
                val endExclusive = match.range.last + 1

                if (start > lastIndex) {
                    append(text.substring(lastIndex, start))
                }

                val displayedToken = trimTrailingPunctuation(match.value)
                val normalizedTarget = if (displayedToken.contains("@") &&
                    !displayedToken.startsWith("http://", ignoreCase = true) &&
                    !displayedToken.startsWith("https://", ignoreCase = true)
                ) {
                    normalizeOutgoingEmail(displayedToken)
                } else {
                    normalizeOutgoingUrl(displayedToken)
                }

                withLink(
                    LinkAnnotation.Url(
                        url = normalizedTarget,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    )
                ) {
                    append(displayedToken)
                }

                lastIndex = endExclusive
            }

            if (lastIndex < text.length) {
                append(text.substring(lastIndex))
            }
        }
    }

    Text(
        text = annotated,
        style = baseStyle,
        modifier = modifier
    )
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    onLinkClick: (String) -> Unit,
    bubbleMaxWidth: Dp,
    resultCardMaxWidth: Dp
) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val color = if (message.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Box(
            modifier = Modifier
                .widthIn(max = bubbleMaxWidth)
                .clip(RoundedCornerShape(12.dp))
                .background(color)
                .padding(12.dp)
        ) {
            LinkifiedMessageText(
                text = message.text
            )
        }

        if (message.results.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            message.results.forEach { result ->
                ResultCard(result = result, onLinkClick = onLinkClick, maxWidth = resultCardMaxWidth)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun ResultCard(result: SearchResult, onLinkClick: (String) -> Unit, maxWidth: Dp) {
    val isPdf = result.link.lowercase().endsWith(".pdf")
    Card(
        modifier = Modifier.widthIn(max = maxWidth).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .clickable { onLinkClick(result.link) }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isPdf) Icons.Default.Description else Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (result.snippet.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = result.snippet,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = result.link,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
