package com.serenity.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.serenity.data.local.entities.ChatSession
import com.serenity.data.model.ChatMessage
import com.serenity.data.dao.ChatSessionDao
import com.serenity.data.local.entities.Journal
import com.serenity.data.model.AiPerson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import javax.inject.Inject
import himanshu.com.apikeymanager.AiManager
import com.serenity.data.dao.JournalDao

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatSessionDao: ChatSessionDao,
    private val journalDao: JournalDao,
    @ApplicationContext private val context: Context
) : ViewModel() {
    // Chat messages state
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    // All chat sessions
    val chatSessions = chatSessionDao.getAll()

    // People mentioned (from journals)
    private val _peopleMentioned = MutableStateFlow<List<AiPerson>>(emptyList())
    val peopleMentioned: StateFlow<List<AiPerson>> = _peopleMentioned

    // Journals (for chat context)
    val journals: StateFlow<List<Journal>> =
        journalDao.getAll().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val aiManager = AiManager(context)

    init {
        updatePeopleMentioned()
    }

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

    private fun buildChatContext(): String {
        val allJournals = journals.value
        val allPeople = peopleMentioned.value
        val recentChatMessages = _chatMessages.value.takeLast(30)

        val recentJournals = allJournals.takeLast(10)
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

    fun saveCurrentChatSession() {
        viewModelScope.launch {
            val messages = _chatMessages.value
            val messagesJson = Gson().toJson(messages)
            val session = ChatSession(messagesJson = messagesJson)
            chatSessionDao.insert(session)
        }
    }

    fun loadChatSession(sessionId: Int) {
        viewModelScope.launch {
            val session = chatSessionDao.getById(sessionId)
            session?.let {
                try {
                    val type = object : TypeToken<List<ChatMessage>>() {}.type
                    val messages: List<ChatMessage> = Gson().fromJson(it.messagesJson, type)
                    val validMessages = messages.filter { message -> message.text != null }
                    _chatMessages.value = validMessages
                } catch (e: Exception) {
                    Timber.e(e, "Failed to load chat session")
                    _chatMessages.value = emptyList()
                }
            }
        }
    }

    fun startNewChatSession() {
        clearChatMessages()
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
} 