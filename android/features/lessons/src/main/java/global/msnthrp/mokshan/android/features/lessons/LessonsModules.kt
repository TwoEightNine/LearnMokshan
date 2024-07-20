package global.msnthrp.mokshan.android.features.lessons

import global.msnthrp.mokshan.android.features.lessons.lesson.LessonViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val lessonsModules = module {
    viewModelOf(::LessonViewModel)
}