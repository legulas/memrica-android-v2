package app.enjoylearning504.core.database.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A word's progress. Level and the difficult heart each carry the time they last changed (0 = never),
 * which decides conflicts when progress is synced. [pending] marks a row the server hasn't seen yet.
 */
@Entity(tableName = "word_progress")
data class WordProgressEntity(
    @PrimaryKey @ColumnInfo(name = "word_id") val wordId: Int,
    val level: Int,
    @ColumnInfo(name = "is_difficult") val isDifficult: Boolean,
    @ColumnInfo(name = "level_changed_at") val levelChangedAt: Long,
    @ColumnInfo(name = "difficult_changed_at") val difficultChangedAt: Long,
    @ColumnInfo(defaultValue = "1") val pending: Boolean = true,
)

@Entity(tableName = "class_stats")
data class ClassStatsEntity(
    @PrimaryKey @ColumnInfo(name = "class_id") val classId: Int,
    @ColumnInfo(name = "high_score") val highScore: Int,
    @ColumnInfo(defaultValue = "1") val pending: Boolean = true,
)
