package com.persianvoice.assistant.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.persianvoice.assistant.core.ai.ConversationEngine
import com.persianvoice.assistant.domain.model.AssistantState
import com.persianvoice.assistant.domain.model.Conversation
import com.persianvoice.assistant.domain.model.Message
import com.persianvoice.assistant.domain.model.MessageRole
import com.persianvoice.assistant.domain.repository.ConversationRepository
import com.persianvoice.assistant.domain.repository.MemoryRepository
import com.persianvoice.assistant.domain.repository.VoiceRepository
import com.persianvoice.assistant.domain.usecase.PersianNormalizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel اصلی Home Screen.
 * State را از ConversationEngine می‌گیرد و به UI expose می‌کند.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val conversationEngine: ConversationEngine,
    private val voiceRepository: VoiceRepository,
    private val conversationRepository: ConversationRepository,
    private val memoryRepository: MemoryRepository,
    private val normalizer: PersianNormalizer
) : ViewModel() {

    val assistantState: StateFlow<AssistantState> = conversationEngine.state
    val currentResponse: StateFlow<String> = conversationEngine.currentResponse

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    val lastConversation: StateFlow<Conversation?> = conversationRepository
        .observeConversations()
        .let { flow ->
            kotlinx.coroutines.flow.flow {
                flow.collect { list -> emit(list.firstOrNull()) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val recentMemories: StateFlow<List<com.persianvoice.assistant.domain.model.Memory>> =
        memoryRepository.observeMemories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            conversationEngine.initialize()
            observeSttResults()
        }
    }

    private fun observeSttResults() {
        viewModelScope.launch {
            voiceRepository.observeSpeechResults().collect { result ->
                if (result.isFinal) {
                    _liveTranscript.value = ""
                    val normalized = normalizer.normalize(result.text)
                    onUserSpeech(normalized)
                } else {
                    _liveTranscript.value = result.text
                }
            }
        }
    }

    fun onMicButtonClick() {
        viewModelScope.launch {
            when (assistantState.value) {
                AssistantState.IDLE, AssistantState.ERROR -> startListening()
                AssistantState.SPEAKING -> conversationEngine.stopSpeaking()
                AssistantState.LISTENING -> stopListening()
                else -> { /* ignore */ }
            }
        }
    }

    private suspend fun startListening() {
        if (!voiceRepository.hasMicrophonePermission()) {
            return // نیاز به درخواست Permission در UI
        }
        voiceRepository.startListening("fa-IR")
    }

    private suspend fun stopListening() {
        voiceRepository.stopListening()
    }

    fun onUserSpeech(text: String) {
        viewModelScope.launch {
            conversationEngine.processUserInput(text)
        }
    }
}