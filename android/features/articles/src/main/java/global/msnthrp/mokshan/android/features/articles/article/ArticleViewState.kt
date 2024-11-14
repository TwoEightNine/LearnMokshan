package global.msnthrp.mokshan.android.features.articles.article

import global.msnthrp.mokshan.domain.jart.Jart

sealed interface ArticleViewState {
    data class Loaded(val jart: Jart) : ArticleViewState
    data object Loading : ArticleViewState
    data object Failed : ArticleViewState
}
