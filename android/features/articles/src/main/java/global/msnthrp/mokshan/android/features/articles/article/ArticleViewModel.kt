package global.msnthrp.mokshan.android.features.articles.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import global.msnthrp.mokshan.data.repository.jart.JartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class ArticleViewModel : ViewModel() {

    private val repo by inject<JartRepository>(JartRepository::class.java)

    private val _state = MutableStateFlow<ArticleViewState>(ArticleViewState.Loading)
    val state: StateFlow<ArticleViewState>
        get() = _state.asStateFlow()

    fun load(url: String) {
        _state.value = ArticleViewState.Loading
        viewModelScope.launch {
            val result = repo.loadJart(url)
            val jart = result.getOrNull()
            when {
                jart == null -> _state.value = ArticleViewState.Failed
                else -> _state.value = ArticleViewState.Loaded(jart)
            }
        }
    }
}