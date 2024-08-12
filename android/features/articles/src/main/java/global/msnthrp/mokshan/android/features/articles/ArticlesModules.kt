package global.msnthrp.mokshan.android.features.articles

import global.msnthrp.mokshan.android.features.articles.list.ArticlesListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val articlesModule = module {
    viewModel {
        ArticlesListViewModel(
            articlesRepository = get()
        )
    }
}