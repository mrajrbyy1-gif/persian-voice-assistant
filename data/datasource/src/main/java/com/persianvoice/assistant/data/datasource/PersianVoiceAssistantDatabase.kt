package com.persianvoice.assistant.data.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration as RoomMigration
import com.persianvoice.assistant.data.model.ConversationEntity
import com.persianvoice.assistant.data.model.MessageEntity
import com.persianvoice.assistant.data.model.MemoryEntity
import com.persianvoice.assistant.data.model.TaskEntity
import com.persianvoice.assistant.data.model.ToolExecutionEntity
import com.persianvoice.assistant.data.model.UserPreferenceEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        MemoryEntity::class,
        UserPreferenceEntity::class,
        TaskEntity::class,
        ToolExecutionEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class PersianVoiceAssistantDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun memoryDao(): MemoryDao
    abstract fun userPreferenceDao(): UserPreferenceDao
    abstract fun taskDao(): TaskDao
    abstract fun toolExecutionDao(): ToolExecutionDao

    companion object {
        const val DATABASE_NAME = "persian_voice_assistant.db"

        /**
         * Migration ها از اینجا اضافه می‌شوند.
         * وقتی Schema تغییر کرد، version را افزایش داده و migration اضافه کنید.
         *
         * مثال:
         * val MIGRATION_1_2 = object : RoomMigration(1, 2) {
         *     override fun migrate(db: SupportSQLiteDatabase) {
         *         db.execSQL("ALTER TABLE conversations ADD COLUMN summary TEXT")
         *     }
         * }
         */
    }
}