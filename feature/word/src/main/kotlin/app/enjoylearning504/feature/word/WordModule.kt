package app.enjoylearning504.feature.word

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val wordModule = module {
    viewModelOf(::WordViewModel)
}
