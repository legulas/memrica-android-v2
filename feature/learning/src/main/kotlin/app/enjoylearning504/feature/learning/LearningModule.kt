package app.enjoylearning504.feature.learning

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val learningModule = module {
    viewModelOf(::LearningViewModel)
}
