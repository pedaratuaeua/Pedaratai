package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.ChatMessageItem
import com.example.api.ChatRequest
import com.example.api.ImageRequest
import com.example.api.RetrofitClient
import com.example.data.AppDatabase
import com.example.data.ChatRepository
import com.example.data.SavedChatMessage
import com.example.data.SavedChatSession
import com.example.preferences.SettingsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class Agent(
    val name: String,
    val faName: String,
    val description: String,
    val faDescription: String,
    val icon: String, // Emoji
    val mode: String, // "text" or "image"
    val modelId: String, // e.g. "gpt-4o-mini", "flux-schnell", etc.
    val systemPrompt: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsManager = SettingsManager(application)
    private val database = AppDatabase.getDatabase(application)
    private val repository = ChatRepository(database.chatDao())

    // Settings Flows
    val apiKey: StateFlow<String?> = settingsManager.apiKeyFlow.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val baseUrl: StateFlow<String> = settingsManager.baseUrlFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "https://api.llm7.io/v1/")
    val language: StateFlow<String> = settingsManager.languageFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "en")
    val theme: StateFlow<String> = settingsManager.themeFlow.stateIn(viewModelScope, SharingStarted.Eagerly, "dark")

    // Saved Sessions
    val savedSessions: StateFlow<List<SavedChatSession>> = repository.allSessions.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Active Chat Sessions
    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

    private val _activeSession = MutableStateFlow<SavedChatSession?>(null)
    val activeSession: StateFlow<SavedChatSession?> = _activeSession.asStateFlow()

    private val _messages = MutableStateFlow<List<SavedChatMessage>>(emptyList())
    val messages: StateFlow<List<SavedChatMessage>> = _messages.asStateFlow()

    // Loading & Error States
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Selected Agent
    private val _selectedAgent = MutableStateFlow<Agent?>(null)
    val selectedAgent: StateFlow<Agent?> = _selectedAgent.asStateFlow()

    // Available Agents List
    val textAgents = listOf(
        Agent(
            "Pedarat AI Friend",
            "دستیار صمیمی پدرت",
            "A humorous Iranian father agent. Playful, slightly strict, and highly witty.",
            "یک دستیار با لحن طنزآمیز پدران ایرانی. شوخ‌طبع، دلسوز و بسیار باهوش.",
            "👴",
            "text",
            "gpt-4o-mini",
            "You are 'Pedarat AI', a witty, humorous, warm, and playful assistant acting as a typical loving Iranian father. You mix deep wisdom, slightly strict humorous dad jokes, and genuine affection. Answer in the same language as the user (English or Persian/Farsi). Use typical Persian dad expressions when responding in Persian."
        ),
        Agent(
            "Deep Code Architect",
            "معمار و تولیدکننده کد",
            "Expert coding assistant that writes beautifully formatted programs.",
            "دستیار برنامه‌نویسی حرفه‌ای که کدهای تمیز و اصولی تولید می‌کند.",
            "💻",
            "text",
            "gpt-4o",
            "You are a Senior Software Architect and expert Programmer. Help the user write, debug, and understand code in any programming language. Format all code blocks perfectly in markdown."
        ),
        Agent(
            "English-Persian Translator",
            "مترجم تخصصی دو زبانه",
            "Instantly converts English to Persian and Persian to English with context.",
            "ترجمه فوری انگلیسی به فارسی و بلعکس با حفظ تکیه کلام‌ها و بافت معنایی.",
            "🌍",
            "text",
            "gpt-4o-mini",
            "You are an expert dual translator fluent in English and Persian (Farsi). Translate input messages instantly to the other language with absolute context accuracy and natural phrasing."
        ),
        Agent(
            "Creative Storywriter",
            "قصه‌گو و داستان‌نویس",
            "Brings ideas to life with gorgeous fictional descriptions.",
            "ایده‌های شما را با توصیف‌های داستانی خلاقانه و جذاب زنده می‌کند.",
            "✍️",
            "text",
            "gpt-4o-mini",
            "You are a master creative writer. Write deep, engaging stories, poems, and creative paragraphs based on the user's prompt."
        )
    )

    val imageAgents = listOf(
        Agent(
            "Cinematic Photo Art",
            "عکاسی سینمایی واقعی",
            "Generates photo-realistic images with professional studio lighting.",
            "تولید تصاویر کاملاً واقعی و طبیعی با نورپردازی استودیویی حرفه‌ای.",
            "📸",
            "image",
            "flux-schnell",
            "photorealistic, dynamic studio lighting, hyper detailed, 4k"
        ),
        Agent(
            "Anime & Pixel illustrator",
            "تصویرساز انیمه و پیکسل",
            "Creates striking anime vectors or cute pixel art designs.",
            "خلق تصاویر انیمه‌ای بسیار زیبا یا آثار پیکسل‌آرت نمادین.",
            "🎨",
            "image",
            "flux-schnell",
            "beautiful anime illustration style, highly detailed coloring, vector art"
        ),
        Agent(
            "Cyberpunk World Builder",
            "سازنده جهان سایبرپانک",
            "Generates dark, neon-soaked cyberpunk concept sketches.",
            "تولید تصاویر تخیلی تاریک با نورهای نئونی سایبرپانک صخره‌ای.",
            "🌃",
            "image",
            "flux-schnell",
            "cyberpunk aesthetic, glowing neon lights, futuristic high-tech city, moody atmosphere"
        )
    )

    init {
        // Automatically select the first agent
        _selectedAgent.value = textAgents[0]

        // Load messages reactively when active session changes
        viewModelScope.launch {
            activeSessionId.collectLatest { id ->
                if (id != null) {
                    repository.getMessagesForSession(id).collect { list ->
                        _messages.value = list
                    }
                } else {
                    _messages.value = emptyList()
                }
            }
        }
    }

    fun selectAgent(agent: Agent) {
        _selectedAgent.value = agent
        // If we switch agents, we can automatically create a new session of this type
        createNewSession(agent)
    }

    fun createNewSession(agent: Agent) {
        val sessionId = UUID.randomUUID().toString()
        val title = if (language.value == "fa") {
            "${Translations.getString("new_chat", "fa")} (${agent.faName})"
        } else {
            "${Translations.getString("new_chat", "en")} (${agent.name})"
        }
        val session = SavedChatSession(
            id = sessionId,
            title = title,
            agentName = agent.name,
            agentMode = agent.mode
        )
        viewModelScope.launch {
            repository.insertSession(session)
            _activeSessionId.value = sessionId
            _activeSession.value = session
        }
    }

    fun loadSession(session: SavedChatSession) {
        _activeSessionId.value = session.id
        _activeSession.value = session
        // Match the selected agent from database
        val matchedText = textAgents.find { it.name == session.agentName }
        val matchedImg = imageAgents.find { it.name == session.agentName }
        _selectedAgent.value = matchedText ?: matchedImg ?: textAgents[0]
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_activeSessionId.value == sessionId) {
                _activeSessionId.value = null
                _activeSession.value = null
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun saveTheme(newTheme: String) {
        viewModelScope.launch {
            settingsManager.saveTheme(newTheme)
        }
    }

    fun saveLanguage(newLang: String) {
        viewModelScope.launch {
            settingsManager.saveLanguage(newLang)
        }
    }

    fun saveApiKeySettings(key: String, url: String) {
        viewModelScope.launch {
            settingsManager.saveApiKey(key)
            settingsManager.saveBaseUrl(url)
        }
    }

    fun sendMessage(content: String) {
        if (content.trim().isEmpty()) return

        val sId = _activeSessionId.value
        val agent = _selectedAgent.value ?: textAgents[0]

        // Ensure session exists
        if (sId == null) {
            val sessionId = UUID.randomUUID().toString()
            val session = SavedChatSession(
                id = sessionId,
                title = if (content.length > 20) content.take(20) + "..." else content,
                agentName = agent.name,
                agentMode = agent.mode
            )
            viewModelScope.launch {
                repository.insertSession(session)
                _activeSessionId.value = sessionId
                _activeSession.value = session
                executeMessageFlow(sessionId, content, agent)
            }
        } else {
            viewModelScope.launch {
                // If it's the first actual message, update session title to the user query
                if (_messages.value.isEmpty()) {
                    val rawTitle = if (content.length > 25) content.take(25) + "..." else content
                    repository.updateSessionTitle(sId, rawTitle)
                    _activeSession.value = _activeSession.value?.copy(title = rawTitle)
                }
                executeMessageFlow(sId, content, agent)
            }
        }
    }

    private suspend fun executeMessageFlow(sessionId: String, userMessage: String, agent: Agent) {
        val userSaved = SavedChatMessage(
            sessionId = sessionId,
            role = "user",
            content = userMessage
        )
        repository.insertMessage(userSaved)

        val key = apiKey.value
        if (key.isNullOrBlank()) {
            val errorLabel = if (language.value == "fa") {
                Translations.getString("no_key_warning", "fa")
            } else {
                Translations.getString("no_key_warning", "en")
            }
            val errorSaved = SavedChatMessage(
                sessionId = sessionId,
                role = "error",
                content = errorLabel
            )
            repository.insertMessage(errorSaved)
            return
        }

        _isLoading.value = true
        _error.value = null

        try {
            val okHttpClient = RetrofitClient.createOkHttpClient()
            val apiService = RetrofitClient.createService(baseUrl.value, okHttpClient)

            if (agent.mode == "image") {
                // Image Generation Flow
                val fullPrompt = if (agent.systemPrompt != null) {
                    "${userMessage}, ${agent.systemPrompt}"
                } else {
                    userMessage
                }

                val imageRequest = ImageRequest(
                    model = agent.modelId,
                    prompt = fullPrompt,
                    n = 1,
                    size = "1024x1024"
                )

                val response = apiService.generateImage("Bearer $key", imageRequest)
                val imageUrl = response.data.firstOrNull()?.url

                if (imageUrl != null) {
                    val assistantSaved = SavedChatMessage(
                        sessionId = sessionId,
                        role = "assistant",
                        content = if (language.value == "fa") "تصویر شما با موفقیت تولید شد!" else "Your image has been generated successfully!",
                        imageUrl = imageUrl
                    )
                    repository.insertMessage(assistantSaved)
                } else {
                    throw Exception("No image URL returned from API")
                }
            } else {
                // Text Completions Flow
                // Gather contextual messages
                val contextMessages = mutableListOf<ChatMessageItem>()
                if (agent.systemPrompt != null) {
                    contextMessages.add(ChatMessageItem("system", agent.systemPrompt))
                }

                // Add past 10 messages for simple context
                val dbMessages = _messages.value.takeLast(10)
                dbMessages.forEach { msg ->
                    if (msg.role == "user" || msg.role == "assistant") {
                        contextMessages.add(ChatMessageItem(msg.role, msg.content))
                    }
                }

                // Append active input message
                contextMessages.add(ChatMessageItem("user", userMessage))

                val chatRequest = ChatRequest(
                    model = agent.modelId,
                    messages = contextMessages
                )

                val response = apiService.chatCompletions("Bearer $key", chatRequest)
                val assistantResponse = response.choices.firstOrNull()?.message?.content

                if (assistantResponse != null) {
                    val assistantSaved = SavedChatMessage(
                        sessionId = sessionId,
                        role = "assistant",
                        content = assistantResponse
                    )
                    repository.insertMessage(assistantSaved)
                } else {
                    throw Exception("Empty response choice")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _error.value = e.localizedMessage ?: "Unknown Error"
            val errorSaved = SavedChatMessage(
                sessionId = sessionId,
                role = "error",
                content = "${Translations.getString("model_error", language.value)}: ${e.localizedMessage ?: ""}"
            )
            repository.insertMessage(errorSaved)
        } finally {
            _isLoading.value = false
        }
    }

    // Share / Export Chat History as Markdown (.md format)
    fun exportChatHistory(context: Context) {
        val sId = _activeSessionId.value ?: return
        val session = _activeSession.value ?: return
        val msgs = _messages.value

        if (msgs.isEmpty()) return

        val lang = language.value
        val markdownBuilder = StringBuilder()
        markdownBuilder.append("# ${session.title}\n")
        markdownBuilder.append("### Agent: ${if (lang == "fa") (textAgents.find { it.name == session.agentName }?.faName ?: session.agentName) else session.agentName}\n")
        markdownBuilder.append("Generated via Pedarat AI\n\n---\n\n")

        for (msg in msgs) {
            val roleLabel = when (msg.role) {
                "user" -> if (lang == "fa") "کاربر" else "User"
                "assistant" -> if (lang == "fa") "دستیار هوش مصنوعی" else "AI Assistant"
                else -> if (lang == "fa") "خطا" else "Error"
            }
            markdownBuilder.append("## $roleLabel:\n")
            markdownBuilder.append("${msg.content}\n")
            if (msg.imageUrl != null) {
                markdownBuilder.append("Image URL: ${msg.imageUrl}\n")
            }
            markdownBuilder.append("\n---\n\n")
        }

        val shareContent = markdownBuilder.toString()
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareContent)
            type = "text/markdown"
        }
        val chooser = Intent.createChooser(sendIntent, Translations.getString("export_history", lang))
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
