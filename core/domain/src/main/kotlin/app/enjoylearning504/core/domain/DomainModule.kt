package app.enjoylearning504.core.domain

import app.enjoylearning504.core.domain.ads.AdTurnPolicy
import app.enjoylearning504.core.domain.learning.LearningSessionEngine
import app.enjoylearning504.core.domain.quiz.QuizBuilder
import app.enjoylearning504.core.domain.usecase.BuildQuizUseCase
import app.enjoylearning504.core.domain.usecase.CompleteAuthPromptUseCase
import app.enjoylearning504.core.domain.usecase.FinishQuizUseCase
import app.enjoylearning504.core.domain.usecase.ObserveDifficultWordsUseCase
import app.enjoylearning504.core.domain.usecase.ObserveHomeProgressUseCase
import app.enjoylearning504.core.domain.usecase.ObserveLessonUseCase
import app.enjoylearning504.core.domain.usecase.ObserveWordUseCase
import app.enjoylearning504.core.domain.usecase.ObserveWordsUseCase
import app.enjoylearning504.core.domain.usecase.PrepareAppStartUseCase
import app.enjoylearning504.core.domain.usecase.SaveLevelUseCase
import app.enjoylearning504.core.domain.usecase.SetWordDifficultUseCase
import app.enjoylearning504.core.domain.usecase.SetWordKnownUseCase
import app.enjoylearning504.core.domain.usecase.ShouldShowInterstitialUseCase
import app.enjoylearning504.core.domain.usecase.StartLearningSessionUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Name of the application-wide [kotlinx.coroutines.CoroutineScope] binding. */
val AppScope = named("appScope")

val domainModule = module {
    singleOf(::AdTurnPolicy)
    factory { LearningSessionEngine() }
    factory { QuizBuilder() }

    factoryOf(::ObserveHomeProgressUseCase)
    factoryOf(::ObserveLessonUseCase)
    factoryOf(::ObserveDifficultWordsUseCase)
    factoryOf(::ObserveWordUseCase)
    factoryOf(::ObserveWordsUseCase)
    factoryOf(::SetWordKnownUseCase)
    factoryOf(::SetWordDifficultUseCase)
    factoryOf(::StartLearningSessionUseCase)
    factoryOf(::SaveLevelUseCase)
    factoryOf(::BuildQuizUseCase)
    factoryOf(::FinishQuizUseCase)
    factoryOf(::CompleteAuthPromptUseCase)
    factoryOf(::ShouldShowInterstitialUseCase)
    factory { PrepareAppStartUseCase(get(), get(), get(), get(), get(AppScope)) }
}
