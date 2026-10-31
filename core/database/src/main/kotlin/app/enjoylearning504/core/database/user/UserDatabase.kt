package app.enjoylearning504.core.database.user

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** The user's own data. Never destructive: every schema change needs a migration. */
@Database(
    entities = [WordProgressEntity::class, ClassStatsEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        const val NAME = "user.db"

        fun build(context: Context): UserDatabase =
            Room.databaseBuilder(context, UserDatabase::class.java, NAME).build()
    }
}
