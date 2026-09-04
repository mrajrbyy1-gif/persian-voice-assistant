package com.persianvoice.assistant.core.common.di

import android.content.Context
import androidx.room.Room
import com.persianvoice.assistant.core.ai.LlmProvider
import com.persianvoice.assistant.core.ai.OpenAiCompatibleProvider
import com.persianvoice.assistant.core.common.AndroidLogger
import com.persianvoice.assistant.core.common.AppLogger
import com.persianvoice.assistant.core.common.BuildConfigBridge
import com.persianvoice.assistant.core.network.ApiClient
import com.persianvoice.assistant.core.voice.SpeechToTextService
import com.persianvoice.assistant.core.voice.TextToSpeechService
import com.persianvoice.assistant.data.datasource.ConversationDao
import com.persianvoice.assistant.data.datasource.MemoryDao
import com.persianvoice.assistant.data.datasource.MessageDao
import com.persianvoice.assistant.data.datasource.PersianVoiceAssistantDatabase
import com.persianvoice.assistant.data.datasource.TaskDao
import com.persianvoice.assistant.data.datasource.ToolExecutionDao
import com.persianvoice.assistant.data.datasource.UserPreferenceDao
import com.persianvoice.assistant.data.repository.ConversationRepositoryImpl
import com.persianvoice.assistant.data.repository.MemoryRepositoryImpl
import com.persianvoice.assistant.data.repository.SettingsRepositoryImpl
import com.persianvoice.assistant.data.repository.ToolRepositoryImpl
import com.persianvoice.assistant.domain.repository.ConversationRepository
import com.persianvoice.assistant.domain.repository.MemoryRepository
import com.persianvoice.assistant.domain.repository.SettingsRepository
import com.persianvoice.assistant.domain.repository.ToolRepository
import com.persianvoice.assistant.domain.repository.VoiceRepository
import com.persianvoice.assistant.domain.usecase.DefaultConfirmationManager
import com.persianvoice.assistant.domain.usecase.ConfirmationManager
import com.persianvoice.assistant.domain.usecase.KeywordIntentDetector
import com.persianvoice.assistant.domain.usecase.DetectIntentUseCase
import com.persianvoice.assistant.domain.usecase.PersianNormalizer
import com.persianvoice.assistant.tools.AgentEngine
import com.persianvoice.assistant.tools.AssistantTool
import com.persianvoice.assistant.tools.ToolRegistry
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PersianVoiceAssistantDatabase {
        BuildConfigBridge.isDebug = true
        return Room.databaseBuilder(
            context,
            PersianVoiceAssistantDatabase::class.java,
            PersianVoiceAssistantDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun provideConversationDao(db: PersianVoiceAssistantDatabase): ConversationDao = db.conversationDao()
    @Provides fun provideMessageDao(db: PersianVoiceAssistantDatabase): MessageDao = db.messageDao()
    @Provides fun provideMemoryDao(db: PersianVoiceAssistantDatabase): MemoryDao = db.memoryDao()
    @Provides fun provideUserPreferenceDao(db: PersianVoiceAssistantDatabase): UserPreferenceDao = db.userPreferenceDao()
    @Provides fun provideTaskDao(db: PersianVoiceAssistantDatabase): TaskDao = db.taskDao()
    @Provides fun provideToolExecutionDao(db: PersianVoiceAssistantDatabase): ToolExecutionDao = db.toolExecutionDao()

    /**
     * ساخت ToolRegistry با تمام Tool های Inject شده.
     */
    @Provides
    @Singleton
    fun provideToolRegistry(tools: Set<@JvmSuppressWildcards AssistantTool>): ToolRegistry {
        val registry = ToolRegistry()
        tools.forEach { registry.register(it) }
        return registry
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindConversationRepository(impl: ConversationRepositoryImpl): ConversationRepository

    @Binds @Singleton
    abstract fun bindMemoryRepository(impl: MemoryRepositoryImpl): MemoryRepository

    @Binds @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds @Singleton
    abstract fun bindToolRepository(impl: ToolRepositoryImpl): ToolRepository

    @Binds @Singleton
    abstract fun bindVoiceRepository(impl: com.persianvoice.assistant.core.voice.VoiceRepositoryImpl): VoiceRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class VoiceModule {

    @Binds @Singleton
    abstract fun bindSttService(impl: com.persianvoice.assistant.core.voice.AndroidSpeechToTextService): SpeechToTextService

    @Binds @Singleton
    abstract fun bindTtsService(impl: com.persianvoice.assistant.core.voice.AndroidTextToSpeechService): TextToSpeechService
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ToolModule {

    @Binds @IntoSet
    abstract fun bindCalculator(impl: com.persianvoice.assistant.tools.calculator.CalculatorTool): AssistantTool

    @Binds @IntoSet
    abstract fun bindBrowser(impl: com.persianvoice.assistant.tools.browser.BrowserTool): AssistantTool

    @Binds @IntoSet
    abstract fun bindWebSearch(impl: com.persianvoice.assistant.tools.search.WebSearchTool): AssistantTool

    @Binds @IntoSet
    abstract fun bindContacts(impl: com.persianvoice.assistant.tools.contacts.ContactsTool): AssistantTool

    @Binds @IntoSet
    abstract fun bindPhoneCall(impl: com.persianvoice.assistant.tools.phone.PhoneCallTool): AssistantTool

    @Binds @IntoSet
    abstract fun bindPhoneDial(impl: com.persianvoice.assistant.tools.phone.PhoneDialTool): AssistantTool

    @Binds @IntoSet
    abstract fun bindSmsSend(impl: com.persianvoice.assistant.tools.sms.SmsSendTool): AssistantTool

    @Binds @IntoSet
    abstract fun bindAlarmCreate(impl: com.persianvoice.assistant.tools.alarm.AlarmCreateTool): AssistantTool

    @Binds @IntoSet
    abstract fun bindCalendarCreate(impl: com.persianvoice.assistant.tools.calendar.CalendarCreateTool): AssistantTool

    @Binds @IntoSet
    abstract fun bindWeather(impl: com.persianvoice.assistant.tools.weather.WeatherCurrentTool): AssistantTool

    @Binds @IntoSet
    abstract fun bindBattery(impl: com.persianvoice.assistant.tools.device.BatteryTool): AssistantTool

    @Binds @IntoSet
    abstract fun bindTime(impl: com.persianvoice.assistant.tools.device.TimeTool): AssistantTool

    @Binds @IntoSet
    abstract fun bindAppLaunch(impl: com.persianvoice.assistant.tools.device.AppLaunchTool): AssistantTool
}

@Module
@InstallIn(SingletonComponent::class)
object LoggerModule {
    @Provides @Singleton
    fun provideLogger(): AppLogger = AndroidLogger()
}

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides @Singleton
    fun provideApiClient(): ApiClient = ApiClient()

    @Provides @Singleton
    fun provideLlmProviders(apiClient: ApiClient): Map<String, LlmProvider> {
        // در production، Provider ها از Settings بارگذاری می‌شوند.
        // فعلاً empty map - کاربر باید Provider را در Settings اضافه کند.
        return emptyMap()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides @Singleton
    fun provideNormalizer(): PersianNormalizer = PersianNormalizer()

    @Provides @Singleton
    fun provideIntentDetector(normalizer: PersianNormalizer): DetectIntentUseCase =
        KeywordIntentDetector(normalizer)

    @Provides @Singleton
    fun provideConfirmationManager(): ConfirmationManager = DefaultConfirmationManager()
}