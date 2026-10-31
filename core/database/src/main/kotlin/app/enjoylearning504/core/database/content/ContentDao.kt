package app.enjoylearning504.core.database.content

import androidx.room.Dao
import androidx.room.Query

@Dao
interface ContentDao {
    @Query("SELECT * FROM classes ORDER BY id")
    suspend fun classes(): List<ClassEntity>

    @Query("SELECT * FROM lessons ORDER BY id")
    suspend fun lessons(): List<LessonEntity>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun lesson(lessonId: Int): LessonEntity

    @Query("SELECT * FROM words ORDER BY id")
    suspend fun words(): List<WordEntity>

    @Query("SELECT * FROM words WHERE id = :wordId")
    suspend fun word(wordId: Int): WordEntity
}
