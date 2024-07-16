package global.msnthrp.mokshan.android.features.lessons.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import global.msnthrp.mokshan.android.core.designsystem.theme.Icons
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.domain.lessons.BankWord
import global.msnthrp.mokshan.domain.lessons.LessonStepType
import global.msnthrp.mokshan.domain.lessons.TopicInfo
import global.msnthrp.mokshan.domain.lessons.UserInput

@Composable
fun LessonScreen(
    topicInfo: TopicInfo,
    lessonNumber: Int,
    lessonViewModel: LessonViewModel = viewModel(),
    onBackPressed: () -> Unit,
) {
    val viewState by lessonViewModel.state.collectAsState()
    if (viewState.showCompleted) {
        CompletedSurface(onContinueClicked = lessonViewModel::onCompletedClosed)
        if (viewState.exit) {
            lessonViewModel.onCloseInvoked()
            onBackPressed()
        }
        return
    }

    val currentLesson = viewState.preparedLesson?.lessonSteps?.getOrNull(viewState.currentStepIndex) ?: return
    if (viewState.showExitAlert) {
        ExitAlert(
            onDismissed = lessonViewModel::onCloseDismissed,
            onConfirmed = lessonViewModel::onCloseConfirmed,
        )
    }

    Scaffold(
        topBar = {
            Column {
                @OptIn(ExperimentalMaterial3Api::class)
                (CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "${topicInfo.title} #$lessonNumber",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = lessonViewModel::onCloseClicked) {
                            Icon(
                                imageVector = Icons.close,
                                contentDescription = "Go back",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                    colors = TopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.primary,
                    ),
                ))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = { viewState.progress },
                    strokeCap = StrokeCap.Round,
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(padding.calculateTopPadding()))
                Text(
                    text = "Translate this sentence",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = currentLesson.sentence,
                    style = MaterialTheme.typography.titleLarge,
                )

                val userInput = viewState.userInput
                when (userInput) {
                    is UserInput.Bank -> {
                        WordBank(
                            selectedWords = userInput.words,
                            allWords = (currentLesson.type as? LessonStepType.WordBank)?.availableWords
                                ?: emptyList(),
                            onWordAdded = lessonViewModel::onWordAdded,
                            onWordRemoved = lessonViewModel::onWordRemoved,
                        )
                    }

                    is UserInput.Input -> {
                        Input(value = userInput.text)
                    }

                    null -> Unit
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = padding.calculateBottomPadding() + 16.dp)
                    .align(Alignment.BottomCenter),
                onClick = { lessonViewModel.onCheckClicked() }
            ) {
                Text(
                    text = "Check",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (viewState.showCorrectCheck) {
                val otherPossibleVariant = when (currentLesson.answers.size) {
                    0, 1 -> ""
                    else -> currentLesson.answers[1]
                }
                CorrectSheet(
                    padding = padding,
                    otherPossibleVariant = otherPossibleVariant,
                    onContinueClicked = lessonViewModel::onCheckSheetClosed,
                )
                LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            if (viewState.showIncorrectCheck) {
                IncorrectSheet(
                    padding = padding,
                    correctAnswer = currentLesson.answers.first(),
                    onContinueClicked = lessonViewModel::onCheckSheetClosed,
                )
                LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordBank(
    selectedWords: List<BankWord>,
    allWords: List<BankWord>,
    onWordAdded: (BankWord) -> Unit,
    onWordRemoved: (BankWord) -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(top = 32.dp, bottom = 128.dp)
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopStart)
        ) {
            selectedWords.forEach { word ->
                Text(
                    text = word.word,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(size = 16.dp)
                        )
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .clickable { onWordRemoved(word) }
                )
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.BottomStart)
        ) {
            val selectedIndices = selectedWords.map { it.index }
            allWords.forEachIndexed { index, word ->
                val isSelected = index in selectedIndices
                val textColor = when {
                    isSelected -> MaterialTheme.colorScheme.surfaceContainer
                    else -> MaterialTheme.colorScheme.onBackground
                }
                Text(
                    text = word.word,
                    color = textColor,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(size = 16.dp)
                        )
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .run {
                            if (!isSelected) {
                                clickable { onWordAdded(word) }
                            } else {
                                this
                            }
                        }
                )
            }
        }
    }
}

@Composable
private fun ExitAlert(
    onDismissed: () -> Unit,
    onConfirmed: () -> Unit,
) {
    AlertDialog(
        title = { Text(text = "Do you want to quit the lesson?") },
        text = { Text(text = "Lesson progress will not be saved") },
        onDismissRequest = onDismissed,
        confirmButton = {
            TextButton(
                onClick = onConfirmed,
            ) {
                Text(text = "Quit")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissed,
            ) {
                Text(text = "Keep going")
            }
        },
    )
}

@Composable
private fun Input(value: String) {
    Column {
        Spacer(modifier = Modifier.height(32.dp))
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 128.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(size = 16.dp)
                ),
            value = value,
            onValueChange = {},
            colors = TextFieldDefaults.colors(
                disabledTextColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun BoxScope.CorrectSheet(
    padding: PaddingValues,
    otherPossibleVariant: String = "",
    onContinueClicked: () -> Unit,
) {
    val message = if (otherPossibleVariant.isNotBlank()) {
        "Possible variant: $otherPossibleVariant"
    } else {
        otherPossibleVariant
    }
    CommonSheet(
        padding = padding,
        title = "Correct!",
        message = message,
        onButtonClicked = onContinueClicked
    )
}

@Composable
private fun BoxScope.IncorrectSheet(
    padding: PaddingValues,
    correctAnswer: String,
    onContinueClicked: () -> Unit,
) {
    CommonSheet(
        padding = padding,
        title = "Incorrect",
        message = "Correct answer: $correctAnswer",
        onButtonClicked = onContinueClicked,
    )
}

@Composable
private fun BoxScope.CommonSheet(
    padding: PaddingValues,
    title: String,
    message: String,
    onButtonClicked: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
            .align(Alignment.BottomCenter)
    ) {
        Spacer(modifier = Modifier.height(height = 16.dp))
        Text(
            modifier = Modifier
                .padding(start = 16.dp),
            text = title,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            modifier = Modifier
                .padding(start = 16.dp, top = 8.dp),
            text = message,
            style = MaterialTheme.typography.titleMedium,
        )
        val isButtonVisible = onButtonClicked != null
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .alpha(if (isButtonVisible) 1f else 0f),
            onClick = { onButtonClicked?.invoke() }
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.height(height = padding.calculateBottomPadding() + 16.dp))
    }
}

@Composable
private fun CompletedSurface(
    onContinueClicked: () -> Unit
) {
    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            
            Column(
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "\uD83C\uDF89",
                    fontSize = TextUnit(64f, TextUnitType.Sp)
                )
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "Completed!",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Button(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 16.dp + padding.calculateBottomPadding()),
                onClick = onContinueClicked,
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Preview
@Composable
private fun CompletedSurfacePreview() {
    LeMokTheme {
        CompletedSurface(
            onContinueClicked = {},
        )
    }
}

@Composable
@Preview
private fun CommonSheetPreview() {
    LeMokTheme {
        Box(modifier = Modifier.fillMaxWidth()) {
            CommonSheet(
                padding = PaddingValues.Absolute(bottom = 32.dp),
                title = "Title",
                message = "Message message message",
                onButtonClicked = {},
            )
        }
    }
}

@Preview
@Composable
fun LessonScreenPreview() {
    LeMokTheme {
        LessonScreen(
            topicInfo = TopicInfo(id = 1, lessonsCount = 2, title = "Greetings"),
            lessonNumber = 2,
            onBackPressed = {},
        )
    }
}