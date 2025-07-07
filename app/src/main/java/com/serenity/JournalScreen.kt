package com.serenity

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.gson.Gson
import com.serenity.data.Journal
import java.com.serenity.R
import java.com.serenity.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun JournalScreen(
    journalViewModel: JournalViewModel = hiltViewModel(),
    signInViewModel: SignInViewModel = hiltViewModel()
) {
    val journals by journalViewModel.journals.collectAsState()
    val analysisResult by journalViewModel.analysisResult.collectAsState()
    val user by signInViewModel.user.collectAsState()
    
    var showHistory by remember { mutableStateOf(false) }
    var showChat by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    var showDashboard by remember { mutableStateOf(false) }
    var showDetail by remember { mutableStateOf(false) }
    var selectedJournal by remember { mutableStateOf<Journal?>(null) }
    
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }

    // Animated background
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "background"
    )

    if (showHistory) {
        JournalHistoryScreen(
            journals = journalViewModel.journals.collectAsState().value,
            onBack = { showHistory = false },
            _onJournalSelected = { /* handled in history screen */ }
        )
        return
    }

    if (showChat) {
        ChatScreen(
            journalViewModel = journalViewModel,
            onBack = { showChat = false }
        )
        return
    }

    if (showProfile) {
        ProfileScreen(
            journalViewModel = journalViewModel,
            signInViewModel = signInViewModel,
            onBack = { showProfile = false }
        )
        return
    }

    if (showDashboard) {
        DashboardScreen(
            journals = journalViewModel.journals.collectAsState().value,
            onBack = { showDashboard = false }
        )
        return
    }

    if (showDetail && selectedJournal != null) {
        JournalDetailScreen(
            journal = selectedJournal!!,
            onBack = { showDetail = false }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        // Animated background elements
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100 + animatedProgress * 50).dp, y = (-150 + animatedProgress * 30).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(150.dp)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            // Beautiful Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = PrimaryGradient,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "Welcome back!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "How are you feeling today?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NavigationButton(
                    icon = painterResource(R.drawable.book),
                    text = "History",
                    onClick = { showHistory = true },
                    color = SerenityAccent1
                )
                NavigationButton(
                    icon = painterResource(R.drawable.chat),
                    text = "Chat",
                    onClick = { showChat = true },
                    color = SerenityAccent2
                )
                NavigationButton(
                    icon = painterResource(R.drawable.dashboard),
                    text = "Dashboard",
                    onClick = { showDashboard = true },
                    color = SerenityQuaternary
                )
                NavigationButton(
                    icon = painterResource(R.drawable.profile),
                    text = "Profile",
                    onClick = { showProfile = true },
                    color = SerenityAccent3
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Journal Entry Form
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "New Journal Entry",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("What's on your mind?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (title.isNotBlank() && content.isNotBlank()) {
                                    journalViewModel.addJournal(title, content, analyze = true)
                                    title = ""
                                    content = ""
                                    isAnalyzing = true
                                }
                            },
                            enabled = title.isNotBlank() && content.isNotBlank() && !isAnalyzing,
                            modifier = Modifier.weight(1f).background(
                                brush = if (isAnalyzing) {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                        )
                                    )
                                } else {
                                    PrimaryGradient
                                },
                                shape = RoundedCornerShape(12.dp)
                            ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier,
                                contentAlignment = Alignment.Center
                            ) {
                                if (isAnalyzing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        "Save & Analyze",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        
                        Button(
                            onClick = {
                                if (title.isNotBlank() && content.isNotBlank()) {
                                    journalViewModel.addJournal(title, content, analyze = false)
                                    title = ""
                                    content = ""
                                }
                            },
                            enabled = title.isNotBlank() && content.isNotBlank() && !isAnalyzing,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Save Only",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Recent Journals
            if (journals.isNotEmpty()) {
                Text(
                    text = "Recent Entries",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(journals.take(5)) { journal ->
                        JournalCard(
                            journal = journal,
                            onClick = {
                                selectedJournal = journal
                                showDetail = true
                            }
                        )
                    }
                }
            } else {
                // Empty State
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Start Writing",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Start your journaling journey",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Write your first entry above to begin reflecting",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Analysis Result Overlay
        analysisResult?.let { result ->
            when (result) {
                is JournalAnalysisResult.Success -> {
                    LaunchedEffect(result) {
                        isAnalyzing = false
                    }
                }
                is JournalAnalysisResult.Error -> {
                    LaunchedEffect(result) {
                        isAnalyzing = false
                    }
                }
                JournalAnalysisResult.Loading -> {
                    // Loading state is handled in the button
                }
            }
        }
    }
}

@Composable
private fun NavigationButton(
    icon: Painter,
    text: String,
    onClick: () -> Unit,
    color: Color
) {
    Card(
        modifier = Modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = color.copy(alpha = 0.2f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .clickable { onClick() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = icon,
                contentDescription = text,
                modifier = Modifier.size(32.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun JournalCard(
    journal: Journal,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = journal.title ?: "Untitled",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = journal.content ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(Date(journal.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AnalysisResultScreen(result: JournalAnalysisResult?, onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AI Journal Analysis",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            when (result) {
                is JournalAnalysisResult.Loading -> {
                    CircularProgressIndicator()
                    Text("Analyzing your journal...")
                }
                is JournalAnalysisResult.Error -> {
                    Text("Error: ${result.message}", color = MaterialTheme.colorScheme.error)
                }
                is JournalAnalysisResult.Success -> {
                    val analysis = remember(result.json) {
                        try {
                            Gson().fromJson(result.json, AiAnalysis::class.java)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (analysis != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(analysis.summary ?: "", modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Mood Landscape", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                MoodChart(analysis.mood ?: emptyMap())
                            }
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                analysis.insights?.forEach {
                                    Text("• ${it ?: ""}", modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Suggested Activities", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                analysis.activities?.forEach {
                                    Text("• ${it ?: ""}", modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                        }
                    } else {
                        Text("Could not parse analysis result.")
                    }
                }
                else -> {}
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onBack) { Text("Back to Journal") }
        }
    }
}

@Composable
fun MoodChart(mood: Map<String, Int>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        mood.forEach { (key, value) ->
            Text(key, modifier = Modifier.width(80.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { value / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .padding(horizontal = 8.dp)
                )
                Text("$value", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun ChatBotScreen(journalViewModel: JournalViewModel = hiltViewModel(), onBack: () -> Unit) {
    val chatMessages by journalViewModel.chatMessages.collectAsState()
    val journals by journalViewModel.journals.collectAsState()
    val chatSessions by journalViewModel.chatSessions.collectAsState(initial = emptyList())
    var input by remember { mutableStateOf(TextFieldValue("")) }
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isLoading by remember { mutableStateOf(false) }
    var selectedSessionId by remember { mutableStateOf<Int?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Empathetic ChatBot", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(8.dp))
            // Session controls
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = { journalViewModel.saveCurrentChatSession() }) { Text("Save Session") }
                Button(onClick = { journalViewModel.startNewChatSession() }) { Text("New Session") }
            }
            Spacer(Modifier.height(8.dp))
            // List previous sessions
            if (chatSessions.isNotEmpty()) {
                Text("Previous Sessions:", style = MaterialTheme.typography.titleMedium)
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    items(chatSessions) { session ->
                        Button(
                            onClick = { journalViewModel.loadChatSession(session.id); selectedSessionId = session.id },
                            colors = if (selectedSessionId == session.id) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) else ButtonDefaults.buttonColors()
                        ) {
                            Text("Session #${session.id}")
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            Card(modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(scrollState)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    chatMessages.forEach { msg ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (msg.isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                                    .widthIn(max = 280.dp)
                            ) {
                                Text(msg.text.toString(), textAlign = if (msg.isUser) TextAlign.End else TextAlign.Start)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    if (isLoading) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type your message...") },
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        val userMsg = JournalViewModel.ChatMessage(input.text, true)
                        journalViewModel.addChatMessage(userMsg)
                        isLoading = true
                        // Compose context for AiAnalysis
                        val chatHistory = journalViewModel.chatMessages.value.takeLast(100).joinToString("\n") { if (it.isUser) "User: ${it.text}" else "Bot: ${it.text}" }
                        val lastJournals = journals.takeLast(10).joinToString("\n\n") { "Title: ${it.title}\nContent: ${it.content}" }
                        val prompt = """
                        You are an empathetic mental health chatbot. Use the user's last 10 journal entries and the current chat session to provide supportive, thoughtful, and helpful responses. Be kind, non-judgmental, and offer practical advice or a listening ear. If appropriate, reference themes or emotions from the journal entries.
                        
                        Respond like a caring friend: be warm, relatable, and supportive. Use friendly, conversational language. It's okay to share a little more if it helps, but keep it natural and not too formal. Limit your reply to 4-5 sentences, focusing on empathy and encouragement.
                        
                        Last 10 Journal Entries:
                        $lastJournals
                        
                        Chat History:
                        $chatHistory
                        
                        User: ${input.text}
                        Bot:
                        """.trimIndent()
                        journalViewModel.generateChatBotReply(prompt) { reply ->
                            journalViewModel.addChatMessage(JournalViewModel.ChatMessage(reply, false))
                            isLoading = false
                        }
                        input = TextFieldValue("")
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    },
                    enabled = input.text.isNotBlank() && !isLoading
                ) {
                    Text("Send")
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Back") }
        }
    }
}

// Add a button in the header row to open history
@Composable
fun HeaderRow(showHistory: Boolean, showProfile: Boolean, onHistoryClick: () -> Unit, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Serenity Journal",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Row {
            IconButton(onClick = { showHistory }) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = "History", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(28.dp))
            }
            IconButton(onClick = { showProfile }) {
                Icon(Icons.Filled.AccountCircle, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(32.dp))
            }
        }
    }
} 