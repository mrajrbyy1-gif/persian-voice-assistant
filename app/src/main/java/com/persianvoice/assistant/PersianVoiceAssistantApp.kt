package com.persianvoice.assistant

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * کلاس Application اصلی - نقطه شروع DI با Hilt
 * تمام ماژول‌های Inject از اینجا شروع می‌شوند.
 */
@HiltAndroidApp
class PersianVoiceAssistantApp : Application()