package com.persianvoice.assistant.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.persianvoice.assistant.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Voice Settings
            SettingsCard(title = stringResource(R.string.settings_voice_language)) {
                Text(
                    text = "زبان: فارسی (fa-IR)",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.settings_speech_speed),
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = settings.speechSpeed,
                    onValueChange = viewModel::setSpeechSpeed,
                    valueRange = 0.5f..2.0f,
                    steps = 6
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.settings_speech_pitch),
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = settings.speechPitch,
                    onValueChange = viewModel::setSpeechPitch,
                    valueRange = 0.5f..2.0f,
                    steps = 6
                )
            }

            // Auto Speak
            SettingsCard(title = stringResource(R.string.settings_auto_speak)) {
                SwitchRow(
                    label = stringResource(R.string.settings_auto_speak),
                    checked = settings.autoSpeak,
                    onCheckedChange = viewModel::setAutoSpeak
                )
            }

            // Wake Word
            SettingsCard(title = stringResource(R.string.settings_wake_word)) {
                SwitchRow(
                    label = stringResource(R.string.settings_wake_word),
                    checked = settings.wakeWord,
                    onCheckedChange = viewModel::setWakeWord
                )
                Text(
                    text = "⚠️ فعلاً غیرفعال است. در نسخه‌های بعدی اضافه می‌شود.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Theme
            SettingsCard(title = stringResource(R.string.settings_dark_mode)) {
                SwitchRow(
                    label = stringResource(R.string.settings_dark_mode),
                    checked = settings.darkMode,
                    onCheckedChange = viewModel::setDarkMode
                )
            }

            // Confirmation Level
            SettingsCard(title = "سطح تأیید") {
                Column {
                    listOf("Always" to "همیشه", "Smart" to "هوشمند", "Never" to "هرگز").forEach { (level, label) ->
                        SwitchRow(
                            label = "$label ($level)",
                            checked = settings.confirmationLevel == level,
                            onCheckedChange = { viewModel.setConfirmationLevel(level) }
                        )
                    }
                    Text(
                        text = "⚠️ برای عملیات بسیار حساس (تماس، پیام، حذف)، حتی در حالت 'هرگز' تأیید گرفته می‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Privacy shortcut
            SettingsCard(title = stringResource(R.string.settings_privacy)) {
                Text("تنظیمات حریم خصوصی و پاک کردن داده‌ها")
            }

            // About
            SettingsCard(title = stringResource(R.string.settings_about)) {
                Text("دستیار فارسی - نسخه 1.0.0")
                Text("یک دستیار صوتی هوشمند برای فارسی‌زبانان")
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}