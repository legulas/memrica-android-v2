package app.enjoylearning504

import android.app.Application
import app.enjoylearning504.core.ads.AdsController
import app.enjoylearning504.core.ads.adsModule
import app.enjoylearning504.core.audio.audioModule
import app.enjoylearning504.core.data.dataModule
import app.enjoylearning504.core.domain.AppScope
import app.enjoylearning504.core.domain.domainModule
import app.enjoylearning504.feature.auth.authModule
import app.enjoylearning504.feature.difficult.difficultModule
import app.enjoylearning504.feature.home.homeModule
import app.enjoylearning504.feature.learning.learningModule
import app.enjoylearning504.feature.lesson.lessonModule
import app.enjoylearning504.feature.quiz.quizModule
import app.enjoylearning504.feature.word.wordModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single<CoroutineScope>(AppScope) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    viewModelOf(::MainViewModel)
}

val allModules = listOf(
    appModule,
    domainModule,
    dataModule,
    audioModule,
    adsModule,
    authModule,
    homeModule,
    lessonModule,
    wordModule,
    learningModule,
    quizModule,
    difficultModule,
)

class MemricaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MemricaApplication)
            modules(allModules)
        }
        get<AdsController>().initialize(this)
    }
}
