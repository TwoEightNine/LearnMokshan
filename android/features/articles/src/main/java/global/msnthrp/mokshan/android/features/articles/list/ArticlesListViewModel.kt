package global.msnthrp.mokshan.android.features.articles.list

import androidx.lifecycle.viewModelScope
import global.msnthrp.mokshan.android.core.arch.BaseViewModel
import global.msnthrp.mokshan.data.repository.article.ArticlesRepository
import kotlinx.coroutines.launch

class ArticlesListViewModel(
    private val articlesRepository: ArticlesRepository,
) : BaseViewModel<ArticlesListState>() {

    override fun getInitialState(): ArticlesListState = ArticlesListState.Loading

    fun load() {
        if (currentState !is ArticlesListState.Loaded) {
            updateState { ArticlesListState.Loading }
        }
        viewModelScope.launch {
            val result = articlesRepository.loadArticles()
            val articles = result.getOrNull()

            articles?.also { println(it) }
            result.exceptionOrNull()?.printStackTrace()

            if (articles == null) {
                updateState { ArticlesListState.Failed }
            } else {
                updateState { ArticlesListState.Loaded(articles) }
            }
        }
    }
}