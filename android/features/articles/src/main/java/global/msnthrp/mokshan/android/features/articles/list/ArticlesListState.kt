package global.msnthrp.mokshan.android.features.articles.list

import global.msnthrp.mokshan.domain.articles.Articles

data class ArticlesListState(
    val isLoading: Boolean = false,
    val articles: Articles? = null,
)