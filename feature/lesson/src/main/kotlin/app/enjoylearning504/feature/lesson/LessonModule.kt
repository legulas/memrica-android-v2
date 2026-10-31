package app.enjoylearning504.feature.lesson

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val lessonModule = module {
    viewModelOf(::LessonViewModel)
}
