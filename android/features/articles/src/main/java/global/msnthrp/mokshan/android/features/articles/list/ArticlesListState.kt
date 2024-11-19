package global.msnthrp.mokshan.android.features.articles.list

import global.msnthrp.mokshan.domain.articles.Articles

sealed interface ArticlesListState {
    data class Loaded(val articles: Articles) : ArticlesListState
    data object Loading : ArticlesListState
    data object Failed : ArticlesListState
}