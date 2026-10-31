package app.enjoylearning504.feature.auth

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    viewModelOf(::AuthViewModel)
}
