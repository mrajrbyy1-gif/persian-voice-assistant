package com.persianvoice.assistant.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.persianvoice.assistant.R
import com.persianvoice.assistant.domain.model.AssistantState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.assistantState.collectAsState()
    val response by viewModel.currentResponse.collectAsState()
    val transcript by viewModel.liveTranscript.collectAsState()
    val lastConversation by viewModel.lastConversation.collectAsState()

    var hasMicPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasMicPermission = granted }

    LaunchedEffect(Unit) {
        // بررسی Permission در شروع - فقط Microphone
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        HomeContent(
            padding = padding,
            state = state,
            response = response,
            transcript = transcript,
            lastConversationTitle = lastConversation?.title,
            onMicClick = viewModel::onMicButtonClick,
            hasMicPermission = hasMicPermission
        )
    }
}

@Composable
private fun HomeContent(
    padding: PaddingValues,
    state: AssistantState,
    response: String,
    transcript: String,
    lastConversationTitle: String?,
    onMicClick: () -> Unit,
    hasMicPermission: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // پیام خوش‌آمدگویی
        Text(
            text = stringResource(R.string.home_greeting),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        // کارت وضعیت
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = getStateText(state),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )

                if (transcript.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = transcript,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                if (response.isNotBlank() && state != AssistantState.IDLE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = response,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // آخرین مکالمه
        if (lastConversationTitle != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_last_conversation),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = lastConversationTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // دکمه میکروفون با انیمیشن
        MicButton(
            state = state,
            enabled = hasMicPermission,
            onClick = onMicClick
        )
    }
}

@Composable
private fun MicButton(
    state: AssistantState,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val isListening = state == AssistantState.LISTENING
    val isProcessing = state == AssistantState.PROCESSING || state == AssistantState.EXECUTING_TOOL
    val isSpeaking = state == AssistantState.SPEAKING

    // انیمیشن Pulse برای حالت Listening
    val infiniteTransition = rememberInfiniteTransition(label = "mic")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val buttonColor = when {
        !enabled -> MaterialTheme.colorScheme.surface
        isSpeaking -> MaterialTheme.colorScheme.tertiary
        isProcessing -> MaterialTheme.colorScheme.secondary
        isListening -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(scale)
            .clip(CircleShape)
            .semantics {
                contentDescription = when (state) {
                    AssistantState.LISTENING -> "در حال گوش دادن، برای توقف ضربه بزنید"
                    AssistantState.SPEAKING -> "در حال پاسخ، برای توقف ضربه بزنید"
                    else -> "برای شروع صحبت ضربه بزنید"
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            color = buttonColor,
            tonalElevation = 8.dp,
            onClick = onClick
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }
        }
    }
}

@Composable
private fun getStateText(state: AssistantState): String = when (state) {
    AssistantState.IDLE -> stringResource(R.string.state_idle)
    AssistantState.LISTENING -> stringResource(R.string.state_listening)
    AssistantState.PROCESSING -> stringResource(R.string.state_processing)
    AssistantState.EXECUTING_TOOL -> stringResource(R.string.state_executing_tool)
    AssistantState.SPEAKING -> stringResource(R.string.state_speaking)
    AssistantState.ERROR -> stringResource(R.string.state_error)
}