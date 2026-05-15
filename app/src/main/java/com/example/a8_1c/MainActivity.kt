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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a8_1c.ui.theme._81CTheme
import java.text.SimpleDateFormat
import java.util.Date
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

enum class SenderType {
    USER,
    BOT
}

data class ChatMessage(
    val id: String,
    val username: String,
    val messageText: String,
    val senderType: SenderType,
    val timestamp: String
)

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
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    id = "welcome",
                    username = "ChatBot",
                    messageText = "Hi $username, how can I help you today?",
                    senderType = SenderType.BOT,
                    timestamp = currentTimeLabel()
                )
            )
        )
    }
    val listState = rememberLazyListState()

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
                shape = RoundedCornerShape(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val trimmed = inputText.trim()
                    if (trimmed.isNotEmpty()) {
                        messages = messages + ChatMessage(
                            id = "user-${System.currentTimeMillis()}",
                            username = username,
                            messageText = trimmed,
                            senderType = SenderType.USER,
                            timestamp = currentTimeLabel()
                        )
                        inputText = ""
                    }
                },
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Send")
            }
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
                    text = message.timestamp,
                    color = if (isUser) Color(0xFFBBDEFB) else Color(0xFF607D8B),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun currentTimeLabel(): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    _81CTheme {
        LoginScreen(username = "", onUsernameChanged = {}, onLogin = {})
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    _81CTheme {
        ChatScreen(username = "Alex")
    }
}
