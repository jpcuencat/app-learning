package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.LessonProgressSummary
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey val lessonId: String,
    val practiceTurnIndex: Int = 0,
    val practiceCompletedCount: Int = 0,
    val reviewTurnIndex: Int = 0,
    val reviewCompletedCount: Int = 0,
    val starsEarned: Int = 0,
    val bestXp: Int = 0,
    val nextReviewEpochDay: Long? = null,
    val lastUpdatedMillis: Long = System.currentTimeMillis(),
    val pendingRemoteSync: Boolean = true
) {
    fun toDomain(): LessonProgressSummary = LessonProgressSummary(
        lessonId = lessonId,
        practiceTurnIndex = practiceTurnIndex,
        practiceCompletedCount = practiceCompletedCount,
        reviewTurnIndex = reviewTurnIndex,
        reviewCompletedCount = reviewCompletedCount,
        starsEarned = starsEarned,
        bestXp = bestXp,
        nextReviewEpochDay = nextReviewEpochDay,
        lastUpdatedMillis = lastUpdatedMillis
    )
}

@Dao
interface LessonProgressDao {

    @Query("SELECT * FROM lesson_progress")
    fun observeAllProgress(): Flow<List<LessonProgressEntity>>

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :lessonId LIMIT 1")
    suspend fun getProgressById(lessonId: String): LessonProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(entity: LessonProgressEntity)

    @Query("DELETE FROM lesson_progress")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM lesson_progress WHERE pendingRemoteSync = 1")
    fun observePendingSyncCount(): Flow<Int>
}

@Database(
    entities = [LessonProgressEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonProgressDao(): LessonProgressDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hablaya_local.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
