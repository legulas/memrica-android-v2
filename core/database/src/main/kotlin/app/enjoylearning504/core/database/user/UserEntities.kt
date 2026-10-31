package app.enjoylearning504.core.database.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "word_progress")
data class WordProgressEntity(
    @PrimaryKey @ColumnInfo(name = "word_id") val wordId: Int,
    val level: Int,
    @ColumnInfo(name = "is_difficult") val isDifficult: Boolean,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

@Entity(tableName = "class_stats")
data class ClassStatsEntity(
    @PrimaryKey @ColumnInfo(name = "class_id") val classId: Int,
    @ColumnInfo(name = "high_score") val highScore: Int,
)
