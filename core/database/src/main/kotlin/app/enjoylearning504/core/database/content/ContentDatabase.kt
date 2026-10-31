package app.enjoylearning504.core.database.content

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The course content, shipped prebuilt in assets and never written to. It holds no user data,
 * so a new release can replace it outright: bump [VERSION] and Room copies the new asset.
 */
@Database(
    entities = [ClassEntity::class, LessonEntity::class, WordEntity::class],
    version = ContentDatabase.VERSION,
    exportSchema = true,
)
abstract class ContentDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao

    companion object {
        const val VERSION = 1
        private const val NAME = "content.db"
        private const val ASSET = "database/content.db"

        fun build(context: Context): ContentDatabase =
            Room.databaseBuilder(context, ContentDatabase::class.java, NAME)
                .createFromAsset(ASSET)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
