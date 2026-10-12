package global.msnthrp.mokshan.android.features.lessons

import global.msnthrp.mokshan.android.features.lessons.lesson.LessonViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val lessonsModules = module {
    viewModelOf(::LessonViewModel)
}
