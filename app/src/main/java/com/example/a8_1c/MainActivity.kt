package com.example.a8_1c

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a8_1c.ui.theme._81CTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _81CTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppRoot(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AppRoot(modifier: Modifier = Modifier) {
    var username by remember { mutableStateOf("") }
    var showChat by remember { mutableStateOf(false) }

    if (showChat) {
        ChatScreen(username = username.trim(), modifier = modifier)
    } else {
        LoginScreen(
            username = username,
            onUsernameChanged = { username = it },
            onLogin = { if (username.isNotBlank()) showChat = true },
            modifier = modifier
        )
    }
}

@Composable
fun LoginScreen(
    username: String,
    onUsernameChanged: (String) -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryBlue = Color(0xFF1565C0)
    val screenBlue = Color(0xFFEAF4FF)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBlue)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                color = primaryBlue
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Enter your username to start chatting",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF2A4B6A)
        )
        Spacer(modifier = Modifier.height(28.dp))
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChanged,
            label = { Text("Username") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onLogin,
            enabled = username.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Login")
        }
    }
}

@Composable
fun ChatScreen(username: String, modifier: Modifier = Modifier) {
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val chatStorage = remember { ChatStorage(context) }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val savedMessages = chatStorage.readAllMessagesSorted()
        messages.clear()
        messages.addAll(savedMessages)

        if (savedMessages.isEmpty()) {
            val welcomeMessageText = "Hi $username, how can I help you today?"
            val welcomeTimestamp = System.currentTimeMillis()
            chatStorage.insertMessage("ChatBot", welcomeMessageText, SenderType.BOT, welcomeTimestamp)
            messages.addAll(chatStorage.readAllMessagesSorted())
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F9FF))
            .padding(16.dp)
    ) {
        Text(
            text = "Chat with Bot",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0D47A1),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(message = message)
            }
        }

        errorText?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = Color(0xFFB00020), style = MaterialTheme.typography.bodySmall)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Type your message") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val trimmed = inputText.trim()
                    if (trimmed.isNotEmpty()) {
                        val userTimestamp = System.currentTimeMillis()
                        chatStorage.insertMessage(username, trimmed, SenderType.USER, userTimestamp)
                        messages.clear()
                        messages.addAll(chatStorage.readAllMessagesSorted())
                        inputText = ""
                        isLoading = true
                        errorText = null

                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                GeminiApiService.sendUserMessage(trimmed)
                            }

                            val botTimestamp = System.currentTimeMillis()
                            if (result.isSuccess) {
                                val botText = result.getOrDefault("I couldn't generate a response.")
                                chatStorage.insertMessage("ChatBot", botText, SenderType.BOT, botTimestamp)
                            } else {
                                errorText = result.exceptionOrNull()?.message ?: "Failed to call Gemini API."
                                chatStorage.insertMessage(
                                    "ChatBot",
                                    "Sorry, I could not process your request right now.",
                                    SenderType.BOT,
                                    botTimestamp
                                )
                            }
                            messages.clear()
                            messages.addAll(chatStorage.readAllMessagesSorted())
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(20.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(20.dp)
                            .height(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Send")
                }
            }
        }
    }
}

object GeminiApiService {
    private const val MODEL_NAME = "gemini-3-flash-preview"

    fun sendUserMessage(userMessage: String): Result<String> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            return Result.failure(IllegalStateException("GEMINI_API_KEY is missing. Add it in local.properties."))
        }

        return runCatching {
            val endpoint =
                "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }

            val requestBody = JSONObject()
                .put("contents", JSONArray().put(
                    JSONObject().put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
                ))

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val responseBody = BufferedReader(InputStreamReader(stream)).use { it.readText() }

            if (responseCode !in 200..299) {
                throw IllegalStateException("Gemini API failed with code $responseCode: $responseBody")
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            parts?.optJSONObject(0)?.optString("text")
                ?: throw IllegalStateException("Gemini response did not contain text.")
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.senderType == SenderType.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isUser) Color(0xFF1976D2) else Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                Text(
                    text = message.messageText,
                    color = if (isUser) Color.White else Color(0xFF0D1B2A),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(message.timestampMillis),
                    color = if (isUser) Color(0xFFBBDEFB) else Color(0xFF607D8B),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun formatTimestamp(timestampMillis: Long): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(timestampMillis)
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    _81CTheme {
        LoginScreen(username = "", onUsernameChanged = {}, onLogin = {})
    }
}
