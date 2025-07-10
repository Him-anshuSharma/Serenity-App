package com.serenity.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.serenity.data.model.AiPerson
import com.serenity.data.local.entities.ChatSession
import com.serenity.data.dao.ChatSessionDao
import com.serenity.data.local.entities.Journal
import com.serenity.data.model.ChatMessage
import com.serenity.data.service.BackupService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import com.serenity.data.dao.JournalDao
import himanshu.com.apikeymanager.AiManager
import javax.inject.Inject

// Data class for people mentioned in journals


@HiltViewModel
class JournalViewModel @Inject constructor(
    private val journalDao: JournalDao,
    private val chatSessionDao: ChatSessionDao,
    private val backupService: BackupService,
    @ApplicationContext private val context: Context // Use ApplicationContext for Hilt
) : ViewModel() {
    val journals: StateFlow<List<Journal>> =
        journalDao.getAll().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val aiManager = AiManager(context)

    private val _analysisResult = MutableStateFlow<JournalAnalysisResult?>(null)
    val analysisResult: StateFlow<JournalAnalysisResult?> = _analysisResult

    // State for people mentioned
    private val _peopleMentioned = MutableStateFlow<List<AiPerson>>(emptyList())
    val peopleMentioned: StateFlow<List<AiPerson>> = _peopleMentioned

    fun addJournal(title: String, content: String, analyze: Boolean = true) {
        viewModelScope.launch {
            try {
                val journal = Journal(title = title, content = content)
                val id = journalDao.insertReturnId(journal)
                val savedJournal = journal.copy(id = id.toInt())
                Timber.d("Journal saved: $savedJournal")
                if (analyze) {
                    analyzeJournal(savedJournal)
                }
                // Update people mentioned after adding journal
                updatePeopleMentioned()
            } catch (e: Exception) {
                Timber.e(e, "Failed to add journal entry")
            }
        }
    }

    fun analyzeJournal(journal: Journal) {
        viewModelScope.launch {
            try {
                val prompt = """
                    Analyze the following journal entry and provide a comprehensive psychological analysis in JSON format with the following structure:

                    {
                      "summary": "A concise summary (2-3 sentences) of the journal entry's main themes and emotional content.",
                      "mood": {
                        "emotion_name": intensity_0_to_100,
                        "emotion_name": intensity_0_to_100,
                        ...
                      },
                      "insights": [
                        "Actionable insight 1",
                        "Actionable insight 2",
                        "Actionable insight 3"
                      ],
                      "activities": [
                        "Suggested activity 1",
                        "Suggested activity 2", 
                        "Suggested activity 3"
                      ],
                      "people": [
                        {
                          "name": "Person name",
                          "relationship": "positive/negative/neutral",
                          "description": "Brief description of their role or relationship"
                        }
                      ],
                      "moments": [
                        {
                          "context": "Description of the moment or situation",
                          "emotion": "positive/negative/neutral",
                          "description": "Brief description of the emotional impact"
                        }
                      ],
                      "places": [
                        "Place 1",
                        "Place 2",
                        ...
                      ],
                      "emotions_psychological": [
                        "Specific psychological emotion 1",
                        "Specific psychological emotion 2",
                        ...
                      ],
                      "reasons_behind_emotions": {
                        "Emotion Name": "Detailed explanation of why this emotion is present, considering both explicit events and underlying psychological factors",
                        "Emotion Name": "Detailed explanation..."
                      },
                      "triggers": {
                        "Emotion Name": "Specific actions, events, or thoughts that triggered this emotion",
                        "Emotion Name": "Specific triggers..."
                      },
                      "psychological_interpretation": "A brief psychological interpretation of the overall emotional state and patterns observed, including potential underlying issues or patterns."
                    }

                    Guidelines:
                    - Use psychological terminology for emotions (e.g., sadness, anxiety, hope, frustration, relief, etc.)
                    - Consider both explicit events and underlying psychological factors (unmet needs, cognitive distortions, past experiences, beliefs)
                    - Provide specific, actionable insights and activities
                    - Be thorough in identifying people, moments, and places mentioned
                    - Give detailed explanations for emotions and their triggers
                    - Provide a comprehensive psychological interpretation

                    Journal Entry:
                    Title: ${journal.title}
                    Content: ${journal.content}
                """.trimIndent()
                val rawResponse = withContext(Dispatchers.IO) {
                    aiManager.postRequest(prompt)
                }
                val analysisJson = extractJsonRobust(rawResponse) ?: rawResponse
                _analysisResult.value = JournalAnalysisResult.Success(analysisJson)
                Timber.d("AI analysis result: $analysisJson")
                // Save analysis to DB
                journalDao.updateAnalysis(journal.id, analysisJson)
                // Parse and save people, moments, places (still save these to DB for backward compatibility)
                try {
                    val json = JsonParser.parseString(analysisJson).asJsonObject
                    val peopleJson = json.get("people")?.toString() ?: "[]"
                    val momentsJson = json.get("moments")?.toString() ?: "[]"
                    val placesJson = json.get("places")?.toString() ?: "[]"
                    journalDao.updatePeople(journal.id, peopleJson)
                    journalDao.updateMoments(journal.id, momentsJson)
                    journalDao.updatePlaces(journal.id, placesJson)
                    Timber.d("Saved people: $peopleJson, moments: $momentsJson, places: $placesJson")
                } catch (e: Exception) {
                    Timber.e(
                        e,
                        "Failed to parse and save people/moments/places from AI analysis"
                    )
                }
                // Update people mentioned after analysis
                updatePeopleMentioned()
            } catch (e: Exception) {
                Timber.e(e, "AI analysis failed")
                _analysisResult.value =
                    JournalAnalysisResult.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    // Robust JSON extraction utility
    private fun extractJsonRobust(response: String): String? {
        // 1. Try to extract between ```json ... ```
        val jsonBlock = Regex("```json\\s*([\\s\\S]*?)\\s*```", RegexOption.IGNORE_CASE)
            .find(response)?.groups?.get(1)?.value
        if (!jsonBlock.isNullOrBlank()) return jsonBlock

        // 2. Try to extract between any triple backticks
        val anyBlock = Regex("```\\s*([\\s\\S]*?)\\s*```").find(response)?.groups?.get(1)?.value
        if (!anyBlock.isNullOrBlank()) return anyBlock

        // 3. Try to extract the first JSON object or array
        val jsonObject = Regex("\\{[\\s\\S]*?\\}").find(response)?.value
        if (!jsonObject.isNullOrBlank()) return jsonObject

        val jsonArray = Regex("\\[[\\s\\S]*?\\]").find(response)?.value
        if (!jsonArray.isNullOrBlank()) return jsonArray

        // 4. Nothing found
        return null
    }

    // Function to get all people mentioned across all journals
    private fun updatePeopleMentioned() {
        viewModelScope.launch {
            try {
                val allJournals = journals.value
                val allPeople = mutableListOf<AiPerson>()
                val peopleMap = mutableMapOf<String, AiPerson>()

                allJournals.forEach { journal ->
                    journal.peopleJson?.let { peopleJson ->
                        try {
                            val type = object : TypeToken<List<AiPerson>>() {}.type
                            val people: List<AiPerson> = Gson().fromJson(peopleJson, type)
                            people.forEach { person ->
                                // Use name as key to avoid duplicates
                                if (!peopleMap.containsKey(person.name)) {
                                    peopleMap[person.name.toString()] = person
                                    allPeople.add(person)
                                }
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to parse people JSON for journal ${journal.id}")
                        }
                    }
                }
                _peopleMentioned.value = allPeople
            } catch (e: Exception) {
                Timber.e(e, "Failed to update people mentioned")
            }
        }
    }

    fun getLastJournals(limit: Int = 10): List<Journal> {
        return journals.value.takeLast(limit)
    }

    // For chat session messages
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    fun addChatMessage(message: ChatMessage) {
        val updated = (_chatMessages.value + message).takeLast(100)
        _chatMessages.value = updated
    }

    fun clearChatMessages() {
        _chatMessages.value = emptyList()
    }

    fun generateChatBotReply(prompt: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                // Build context from journals and people
                val contextStr = buildChatContext()
                val enhancedPrompt = """
                    You are Serenity, a close friend and confidant who helps with mental wellness through casual, supportive conversation. You're warm, empathetic, and genuinely care about the person you're talking to.

                    Your personality:
                    - You're like a caring friend who listens without judgment
                    - You give insights only when they're truly helpful and requested
                    - You keep conversations light and supportive, not preachy
                    - Don't write extra long texts than required
                    - You know when to end conversations gracefully if the person seems done
                    - You use casual, friendly language, not formal therapy speak
                    - You share from your "knowledge" of their journal entries naturally and previous messages like a friend would

                    $contextStr

                    User's message: $prompt

                    Respond as a caring friend would. If they seem to want to end the conversation or are just saying goodbye, acknowledge it warmly and let them go. If they're asking for insights, share them gently and conversationally. Keep it natural and supportive.
                """.trimIndent()
                val reply = withContext(Dispatchers.IO) {
                    aiManager.postRequest(enhancedPrompt)
                }
                onResult(reply)
            } catch (e: Exception) {
                Timber.d("Error AI: " + e.message.toString())
                onResult("Sorry, I couldn't process your request right now.")
            }
        }
    }

    // Build context for chatbot from journals, people, and recent chat messages
    private fun buildChatContext(): String {
        val allJournals = journals.value
        val allPeople = peopleMentioned.value
        val recentChatMessages = _chatMessages.value.takeLast(30) // Last 10 chat messages

        val recentJournals = allJournals.takeLast(10) // Last 10 journal entries
        val journalContext = if (recentJournals.isNotEmpty()) {
            "From users recent journal entries, you can see:\n" + recentJournals.joinToString("\n\n") { journal ->
                "• ${journal.title ?: "Untitled"}: ${(journal.content ?: "")}${if ((journal.content?.length ?: 0) > 150) "..." else ""}"
            }
        } else {
            "users haven't written any journal entries yet."
        }

        val peopleContext = if (allPeople.isNotEmpty()) {
            "People user have mentioned in their journals:\n" + allPeople.joinToString("\n") { person ->
                "• ${person.name ?: "Unknown"} (${person.relationship ?: "neutral"}): ${person.description ?: ""}"
            }
        } else {
            "No people mentioned in user journals yet."
        }

        val chatContext = if (recentChatMessages.isNotEmpty()) {
            "Recent conversation context:\n" + recentChatMessages.joinToString("\n") { message ->
                "${if (message.isUser) "User" else "You"}: ${message.text ?: ""}"
            }
        } else {
            "This is the start of our conversation."
        }

        return """
        $journalContext

        $peopleContext

        $chatContext

        User have written ${allJournals.size} journal entries total.
        """.trimIndent()
    }

    // Save current chat session
    fun saveCurrentChatSession() {
        viewModelScope.launch {
            val messages = _chatMessages.value
            val messagesJson = Gson().toJson(messages)
            val session = ChatSession(messagesJson = messagesJson)
            chatSessionDao.insert(session)
        }
    }

    // Load a previous chat session by id
    fun loadChatSession(sessionId: Int) {
        viewModelScope.launch {
            val session = chatSessionDao.getById(sessionId)
            session?.let {
                try {
                    val type = object : TypeToken<List<ChatMessage>>() {}.type
                    val messages: List<ChatMessage> = Gson().fromJson(it.messagesJson, type)
                    // Filter out any messages with null text
                    val validMessages = messages.filter { message -> message.text != null }
                    _chatMessages.value = validMessages
                } catch (e: Exception) {
                    Timber.e(e, "Failed to load chat session")
                    _chatMessages.value = emptyList()
                }
            }
        }
    }

    // Start a new chat session
    fun startNewChatSession() {
        clearChatMessages()
    }

    // Expose all chat sessions
    val chatSessions = chatSessionDao.getAll()

    // Initialize people mentioned when ViewModel is created
    init {
        updatePeopleMentioned()
    }

}


sealed class JournalAnalysisResult {
    object Loading : JournalAnalysisResult()
    data class Success(val json: String) : JournalAnalysisResult()
    data class Error(val message: String) : JournalAnalysisResult()
}

