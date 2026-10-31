package app.enjoylearning504.core.database.content

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// These definitions must match tools/build-content-db.sh, which builds the shipped database

@Entity(tableName = "classes")
data class ClassEntity(@PrimaryKey val id: Int)

@Entity(tableName = "lessons", indices = [Index("class_id")])
data class LessonEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "class_id") val classId: Int,
    @ColumnInfo(name = "number_in_class") val numberInClass: Int,
    @ColumnInfo(name = "first_word_id") val firstWordId: Int,
    @ColumnInfo(name = "last_word_id") val lastWordId: Int,
)

@Entity(tableName = "words", indices = [Index("lesson_id"), Index("class_id")])
data class WordEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "lesson_id") val lessonId: Int,
    @ColumnInfo(name = "class_id") val classId: Int,
    val english: String,
    val persian: String,
    val synonyms: String?,
    val phonetic: String?,
    @ColumnInfo(name = "part_of_speech") val partOfSpeech: String?,
    @ColumnInfo(name = "sentence_en_1") val sentenceEn1: String?,
    @ColumnInfo(name = "sentence_fa_1") val sentenceFa1: String?,
    @ColumnInfo(name = "sentence_en_2") val sentenceEn2: String?,
    @ColumnInfo(name = "sentence_fa_2") val sentenceFa2: String?,
    @ColumnInfo(name = "sentence_en_3") val sentenceEn3: String?,
    @ColumnInfo(name = "sentence_fa_3") val sentenceFa3: String?,
    @ColumnInfo(name = "audio_asset") val audioAsset: String?,
)
