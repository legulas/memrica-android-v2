package app.enjoylearning504.core.database

import app.enjoylearning504.core.database.content.ContentDatabase
import app.enjoylearning504.core.database.user.UserDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { ContentDatabase.build(androidContext()) }
    single { UserDatabase.build(androidContext()) }
    single { get<ContentDatabase>().contentDao() }
    single { get<UserDatabase>().userDao() }
}
