package global.msnthrp.learmokshan.android.features.dictionary

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val dictionaryModule = module {
    viewModel { DictionaryViewModel(dictionaryRepository = get()) }
}