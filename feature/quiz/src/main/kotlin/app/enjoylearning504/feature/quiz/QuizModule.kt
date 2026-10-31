package app.enjoylearning504.feature.quiz

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val quizModule = module {
    viewModelOf(::QuizViewModel)
}
