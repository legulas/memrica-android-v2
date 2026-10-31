package app.enjoylearning504.core.audio

import app.enjoylearning504.core.domain.platform.PronunciationPlayer
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val audioModule = module {
    single<PronunciationPlayer> { ExoPronunciationPlayer(androidContext()) }
}
