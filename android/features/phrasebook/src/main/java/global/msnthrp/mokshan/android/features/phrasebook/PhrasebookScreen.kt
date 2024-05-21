package global.msnthrp.mokshan.android.features.phrasebook

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import asPxToDp
import global.msnthrp.mokshan.android.core.designsystem.theme.Icons
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.domain.phrasebook.Category
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage
import global.msnthrp.mokshan.domain.phrasebook.Phrase
import global.msnthrp.mokshan.domain.phrasebook.Translation
import kotlinx.coroutines.launch

@Composable
fun PhrasebookScreen(
    appName: String,
    phrasebookViewModel: PhrasebookViewModel = viewModel()
) {
    val state by phrasebookViewModel.state.collectAsState()
    LifecycleResumeEffect(key1 = "huy") {
        phrasebookViewModel.load()
        onPauseOrDispose {}
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            (CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = appName,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            ))
        },
    ) { padding ->
        if (state.isLoading && state.phrasebook == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(40.dp),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(top = padding.calculateTopPadding())
            ) {
                state.phrasebook?.phrases?.forEach { phrasesByCategory ->
                    val category = phrasesByCategory.category
                    val isExpanded = category.id == state.visibleCategory
                    item {
                        CategoryHeaderView(
                            category = phrasesByCategory.category,
                            isExpanded = isExpanded,
                            onClicked = phrasebookViewModel::onCategoryClicked
                        )
                    }
                    item {
                        AnimatedVisibility(
                            visible = category.id == state.visibleCategory,
                            enter = expandVertically(),
                            exit = shrinkVertically(),
                        ) {
                            Column {
                                phrasesByCategory.phrases.forEach { phrase ->
                                    PhraseView(phrase = phrase)
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(
                        modifier = Modifier.height(
                            padding.calculateBottomPadding()
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryHeaderView(
    category: Category,
    isExpanded: Boolean,
    onClicked: (category: Category) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClicked(category)
            },
    ) {
        Box {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 32.dp, bottom = 16.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                text = category.translations.first().value
            )
            val rotation by animateFloatAsState(
                targetValue = if (isExpanded) 180f else 0f,
                animationSpec = tween(500)
            )
            Icon(
                imageVector = Icons.chevronDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(all = 16.dp)
                    .rotate(rotation),
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 16.dp),
            thickness = 1.asPxToDp()
        )
    }
}

@Composable
private fun PhraseView(phrase: Phrase) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
    ) {
        Text(
            text = phrase.mokshanPhrase,
            style = MaterialTheme.typography.titleMedium,
        )
        phrase.translations.forEach { translation ->
            val translationText = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.secondary,
                    )
                ) {
                    append(translation.value)
                }
                if (!translation.note.isNullOrBlank()) {
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    ) {
                        append(" (${translation.note})")
                    }
                }
            }
            Text(
                modifier = Modifier.padding(top = 2.dp),
                text = translationText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(thickness = 1.asPxToDp())
    }
}

@Preview
@Composable
fun ScreenPreview() {
    LeMokTheme {
        PhrasebookScreen(appName = "App name")
    }
}

@Preview
@Composable
fun PhrasePreview() {
    LeMokTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            PhraseView(
                Phrase(
                    mokshanPhrase = "Mokshan phrase",
                    translations = listOf(
                        Translation(
                            value = "English phrase",
                            foreignLanguage = ForeignLanguage.ENGLISH,
                            note = "note"
                        )
                    ),
                    category = Category(
                        id = "category",
                        translations = emptyList()
                    )
                )
            )
        }
    }
}

@Preview
@Composable
fun CategoryHeaderPreview() {
    LeMokTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            CategoryHeaderView(
                category = Category(
                    id = "id",
                    translations = listOf(
                        Translation(
                            value = "Category name",
                            foreignLanguage = ForeignLanguage.ENGLISH,
                        )
                    )
                ),
                isExpanded = false,
                onClicked = {},
            )
        }
    }
}