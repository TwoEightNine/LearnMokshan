package global.msnthrp.mokshan.android.features.articles.list

import androidx.lifecycle.viewModelScope
import global.msnthrp.mokshan.android.core.arch.BaseViewModel
import global.msnthrp.mokshan.data.repository.article.ArticlesRepository
import kotlinx.coroutines.launch

class ArticlesListViewModel(
    private val articlesRepository: ArticlesRepository,
) : BaseViewModel<ArticlesListState>() {

    override fun getInitialState() = ArticlesListState()

    fun load() {
        updateState { copy(isLoading = true) }
        viewModelScope.launch {
            val result = articlesRepository.loadArticles()
            result.getOrNull()?.also { println(it) }
            result.exceptionOrNull()?.printStackTrace()
            updateState {
                copy(
                    isLoading = false,
                    articles = result.getOrNull(),
                )
            }
        }
    }
}