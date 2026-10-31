package app.enjoylearning504.feature.difficult

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val difficultModule = module {
    viewModelOf(::DifficultWordsViewModel)
}
