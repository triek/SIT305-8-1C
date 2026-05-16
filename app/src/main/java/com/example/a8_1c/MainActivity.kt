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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a8_1c.ui.theme._81CTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _81CTheme {
                AppRoot()
            }
        }
    }
}

private val screenGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF4DE7FF), Color(0xFF1D8DFF))
)

data class UiMessage(
    val text: String,
    val timestamp: String,
    val isUser: Boolean
)

@Composable
fun AppRoot() {
    var username by remember { mutableStateOf("") }
    var showChat by remember { mutableStateOf(false) }

    if (showChat) {
        ChatScreen(
            username = username.ifBlank { "User" },
            onSend = {},
            onMessageChange = {},
            messageInput = ""
        )
    } else {
        LoginScreen(
            username = username,
            onUsernameChanged = { username = it },
            onGo = { if (username.isNotBlank()) showChat = true }
        )
    }
}

@Composable
fun LoginScreen(
    username: String,
    onUsernameChanged: (String) -> Unit,
    onGo: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenGradient)
            .padding(horizontal = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 56.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Welcome,",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 38.sp
            )
            Text(
                text = "Lets Chat!",
                color = Color.Black,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 46.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TextField(
                value = username,
                onValueChange = onUsernameChanged,
                placeholder = { Text("Username", color = Color(0xFF8A8A8A)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onGo,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5BFF4A)),
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .height(52.dp)
            ) {
                Text("Go", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ChatScreen(
    username: String,
    messageInput: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val demoMessages = listOf(
        UiMessage("Welcome $username!", "09:14", false),
        UiMessage("Hi bot", "09:15", true),
        UiMessage("How can I help?", "09:15", false)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenGradient)
            .padding(horizontal = 12.dp, vertical = 18.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(demoMessages) { msg ->
                    ChatBubble(message = msg)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = messageInput,
                    onValueChange = onMessageChange,
                    placeholder = { Text("Type a message", color = Color(0xFF9A9A9A)) },
                    shape = RoundedCornerShape(18.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE6E6E6),
                        unfocusedContainerColor = Color(0xFFE6E6E6),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSend,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8F8F8F)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Text(
                        text = "↑",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: UiMessage) {
    val rowAlignment = if (message.isUser) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = rowAlignment,
        verticalAlignment = Alignment.Top
    ) {
        if (!message.isUser) {
            AvatarBubble(isUser = false)
            Spacer(modifier = Modifier.width(6.dp))
        }

        Column(horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (message.isUser) Color(0xFF3E93FF) else Color(0xFFEDEDED))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (message.isUser) Color.White else Color.Black,
                    fontSize = 15.sp
                )
            }
            Text(
                text = message.timestamp,
                color = Color(0xFF777777),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp, start = 2.dp, end = 2.dp)
            )
        }

        if (message.isUser) {
            Spacer(modifier = Modifier.width(6.dp))
            AvatarBubble(isUser = true)
        }
    }
}

@Composable
fun AvatarBubble(isUser: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (isUser) Color(0xFF6F6F6F) else Color(0xFFFFF28A)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isUser) "U" else "✦",
            color = Color.Black,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    _81CTheme {
        LoginScreen(username = "", onUsernameChanged = {}, onGo = {})
    }
}
