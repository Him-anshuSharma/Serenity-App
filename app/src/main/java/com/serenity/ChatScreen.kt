package com.serenity

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.serenity.data.ChatSession
import java.com.serenity.ui.theme.*

@Composable
fun ChatScreen(
    journalViewModel: JournalViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val chatMessages by journalViewModel.chatMessages.collectAsState()
    val journals by journalViewModel.journals.collectAsState()
    val chatSessions by journalViewModel.chatSessions.collectAsState(initial = emptyList())
    val peopleMentioned by journalViewModel.peopleMentioned.collectAsState()
    var input by remember { mutableStateOf(TextFieldValue("")) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isLoading by remember { mutableStateOf(false) }
    var selectedSessionId by remember { mutableStateOf<Int?>(null) }
    var showSessionsScreen by remember { mutableStateOf(false) }
    var showPeopleSection by remember { mutableStateOf(false) }
    var showTyping by remember { mutableStateOf(false) }
    
    if (showSessionsScreen) {
        ChatSessionsScreen(
            journalViewModel = journalViewModel,
            onBack = { showSessionsScreen = false },
            onSessionSelected = { session: ChatSession ->
                journalViewModel.loadChatSession(session.id)
                selectedSessionId = session.id
                showSessionsScreen = false
            }
        )
        return
    }

    // Animated background
    val infiniteTransition = rememberInfiniteTransition(label = "chat-background")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chat-background"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SerenityBackground,
                        SerenitySurfaceVariant.copy(alpha = 0.2f)
                    )
                )
            )
    ) {
        // Animated background elements
        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = (-50 + animatedProgress * 30).dp, y = (-100 + animatedProgress * 20).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SerenityAccent2.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(125.dp)
                )
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Beautiful Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(0.dp, 0.dp, 20.dp, 20.dp),
                        spotColor = SerenitySecondary.copy(alpha = 0.2f)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(0.dp, 0.dp, 20.dp, 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = SecondaryGradient,
                            shape = RoundedCornerShape(0.dp, 0.dp, 20.dp, 20.dp)
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .background(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = "Chat with Serenity",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Your friendly AI companion",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // Session controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { journalViewModel.saveCurrentChatSession() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Save Session")
                }
                Button(
                    onClick = { journalViewModel.startNewChatSession() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("New Session")
                }
            }

            // Previous sessions
            if (chatSessions.isNotEmpty()) {
                Text(
                    "Previous Sessions:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chatSessions) { session ->
                        Button(
                            onClick = {
                                journalViewModel.loadChatSession(session.id)
                                selectedSessionId = session.id
                            },
                            colors = if (selectedSessionId == session.id) {
                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            } else {
                                ButtonDefaults.buttonColors()
                            }
                        ) {
                            Text("Session #${session.id}")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Chat Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                reverseLayout = true
            ) {
                items(chatMessages.reversed()) { message ->
                    ChatBubble(
                        message = message,
                        isUser = message.isUser
                    )
                }
                if (showTyping) {
                    item {
                        ChatBubble(
                            message = JournalViewModel.ChatMessage("Serenity is typing…", isUser = false),
                            isUser = false
                        )
                    }
                }
                if (chatMessages.isEmpty() && !showTyping) {
                    item {
                        EmptyChatState()
                    }
                }
            }

            // Input Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(20.dp, 20.dp, 0.dp, 0.dp),
                        spotColor = SerenityPrimary.copy(alpha = 0.2f)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp, 20.dp, 0.dp, 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Quick Suggestions
                    if (chatMessages.isEmpty() && journals.isNotEmpty()) {
                        Text(
                            text = "Quick suggestions:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = SerenityOnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                listOf(
                                    "How am I feeling lately?",
                                    "What patterns do you see?",
                                    "Give me some insights",
                                    "Suggest activities for me",
                                    "Tell me about people in my life",
                                    "What should I focus on?"
                                )
                            ) { suggestion ->
                                SuggestionChip(
                                    text = suggestion,
                                    onClick = {
                                        input = TextFieldValue(suggestion)
                                        journalViewModel.generateChatBotReply(suggestion) { reply ->
                                            journalViewModel.addChatMessage(
                                                JournalViewModel.ChatMessage(suggestion, true)
                                            )
                                            journalViewModel.addChatMessage(
                                                JournalViewModel.ChatMessage(reply, false)
                                            )
                                        }
                                        input = TextFieldValue("")
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Input Field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            placeholder = { Text("Type your message…") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SerenityPrimary,
                                unfocusedBorderColor = SerenityOnSurfaceVariant.copy(alpha = 0.3f),
                                focusedPlaceholderColor = SerenityOnSurfaceVariant,
                                unfocusedPlaceholderColor = SerenityOnSurfaceVariant
                            ),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = {
                                if (input.text.isNotBlank()) {
                                    val userMessage = input.text
                                    journalViewModel.addChatMessage(
                                        JournalViewModel.ChatMessage(userMessage, true)
                                    )
                                    input = TextFieldValue("")
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    showTyping = true
                                    journalViewModel.generateChatBotReply(userMessage) { reply ->
                                        showTyping = false
                                        journalViewModel.addChatMessage(
                                            JournalViewModel.ChatMessage(reply ?: "Sorry, I couldn't process your request.", false)
                                        )
                                    }
                                }
                            },
                            enabled = input.text.isNotBlank(),
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = if (input.text.isNotBlank()) SerenityPrimary else SerenityOnSurfaceVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(24.dp)
                                ),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Transparent
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (input.text.isNotBlank()) Color.White else SerenityOnSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: JournalViewModel.ChatMessage,
    isUser: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    ),
                    spotColor = if (isUser) {
                        SerenityPrimary.copy(alpha = 0.2f)
                    } else {
                        SerenitySecondary.copy(alpha = 0.2f)
                    }
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) {
                    Color.White
                } else {
                    SerenitySurfaceVariant.copy(alpha = 0.5f)
                }
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = if (isUser) {
                            PrimaryGradient
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    SerenitySurfaceVariant.copy(alpha = 0.8f),
                                    SerenitySurfaceVariant.copy(alpha = 0.6f)
                                )
                            )
                        },
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = message.text ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else SerenityOnSurface,
                    textAlign = if (isUser) TextAlign.End else TextAlign.Start
                )
            }
        }
    }
}

@Composable
private fun EmptyChatState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Email,
            contentDescription = "Start Chat",
            modifier = Modifier.size(80.dp),
            tint = SerenityOnSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Start a conversation",
            style = MaterialTheme.typography.headlineSmall,
            color = SerenityOnSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Ask me about your thoughts, feelings, or anything on your mind",
            style = MaterialTheme.typography.bodyMedium,
            color = SerenityOnSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun SuggestionChip(
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = SerenityPrimary.copy(alpha = 0.1f)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = SerenityPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}
