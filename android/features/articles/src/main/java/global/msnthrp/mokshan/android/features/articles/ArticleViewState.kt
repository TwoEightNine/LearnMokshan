package global.msnthrp.mokshan.android.features.articles

import global.msnthrp.mokshan.domain.jart.Jart

data class ArticleViewState(
    val isLoading: Boolean = false,
    val jart: Jart? = null,
)
