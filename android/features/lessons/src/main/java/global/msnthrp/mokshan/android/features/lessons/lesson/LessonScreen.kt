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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import asPxToDp
import global.msnthrp.mokshan.android.core.designsystem.theme.Icons
import global.msnthrp.mokshan.android.core.designsystem.theme.LeMokTheme
import global.msnthrp.mokshan.android.core.designsystem.theme.SpecialColors
import global.msnthrp.mokshan.android.core.utils.stringResource
import global.msnthrp.mokshan.android.features.lessons.R
import global.msnthrp.mokshan.domain.lessons.BankWord
import global.msnthrp.mokshan.domain.lessons.LessonStepType
import global.msnthrp.mokshan.domain.lessons.TopicInfo
import global.msnthrp.mokshan.domain.lessons.UserInput
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun LessonScreen(
    topicInfo: TopicInfo,
    lessonNumber: Int,
    lessonViewModel: LessonViewModel = koinViewModel(parameters = {
        parametersOf(
            topicInfo,
            lessonNumber
        )
    }),
    onBackPressed: () -> Unit,
) {
    val viewState by lessonViewModel.state.collectAsState()
    if (viewState.exit) {
        lessonViewModel.onCloseInvoked()
        onBackPressed()
    }

    if (viewState.showCompleted) {
        CompletedSurface(onContinueClicked = lessonViewModel::onCompletedClosed)
        return
    }

    val preparedLesson = viewState.preparedLesson ?: return
    val currentStep = preparedLesson.lessonSteps.getOrNull(viewState.currentStepIndex) ?: return
    if (viewState.showExitAlert) {
        ExitAlert(
            onDismissed = lessonViewModel::onCloseDismissed,
            onConfirmed = lessonViewModel::onCloseConfirmed,
        )
    }

    var sentenceBounds by remember { mutableStateOf(Rect.Zero) }
    val hint = viewState.translationHint
    if (hint != null) {
        val topLeft = sentenceBounds.topLeft
        val sentence = currentStep.sentence
        val wordRelativeOffset =
            if (sentence.isNotEmpty()) hint.pos.toFloat() / sentence.length else 0f
        val wordOffsetX = sentenceBounds.width * wordRelativeOffset
        DropdownMenu(
            expanded = true,
            onDismissRequest = lessonViewModel::onHintDismissed,
            offset = DpOffset((topLeft.x + wordOffsetX).asPxToDp(), topLeft.y.asPxToDp())
        ) {
            hint.translations.forEach { hint ->
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp),
                    text = hint,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
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
                                contentDescription = stringResource(id = R.string.lesson_back_accessibility),
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
                    trackColor = MaterialTheme.colorScheme.surface,
                    strokeCap = StrokeCap.Round,
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val focusManager = LocalFocusManager.current
            val onInputDone: () -> Unit = {
                focusManager.clearFocus()
                lessonViewModel.onCheckClicked()
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(padding.calculateTopPadding()))
                Text(
                    text = stringResource(id = R.string.lesson_translate_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (preparedLesson.withHints) {
                    HintedText(
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                sentenceBounds = coordinates.boundsInWindow()
                            },
                        text = currentStep.sentence,
                        style = MaterialTheme.typography.titleLarge,
                        onClicked = lessonViewModel::onWordInSentenceClicked,
                    )
                } else {
                    Text(
                        text = currentStep.sentence,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

                val userInput = viewState.userInput
                when (userInput) {
                    is UserInput.Bank -> {
                        WordBank(
                            selectedWords = userInput.words,
                            allWords = (currentStep.type as? LessonStepType.WordBank)?.availableWords
                                ?: emptyList(),
                            onWordAdded = lessonViewModel::onWordAdded,
                            onWordRemoved = lessonViewModel::onWordRemoved,
                        )
                    }

                    is UserInput.Input -> {
                        Input(
                            value = userInput.text,
                            uniqueStepId = currentStep.sentence,
                            onInputUpdated = lessonViewModel::onInputUpdated,
                            onDoneEntered = { onInputDone() },
                        )
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
                onClick = onInputDone
            ) {
                Text(
                    text = stringResource(id = R.string.lesson_button_check),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (viewState.showCorrectCheck) {
                val otherPossibleVariant = when (currentStep.answers.size) {
                    0, 1 -> ""
                    else -> currentStep.answers[1]
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
                    correctAnswer = currentStep.answers.first(),
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
        title = { Text(text = stringResource(id = R.string.lesson_quit_title)) },
        text = { Text(text = stringResource(id = R.string.lesson_quit_message)) },
        onDismissRequest = onDismissed,
        confirmButton = {
            TextButton(
                onClick = onConfirmed,
            ) {
                Text(text = stringResource(id = R.string.lesson_quit_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissed,
            ) {
                Text(text = stringResource(id = R.string.lesson_quit_cancel))
            }
        },
    )
}

@Composable
private fun Input(
    value: String,
    uniqueStepId: String,
    onInputUpdated: (String) -> Unit,
    onDoneEntered: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    Column {
        Spacer(modifier = Modifier.height(32.dp))
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 128.dp)
                .focusRequester(focusRequester),
            value = value,
            keyboardActions = KeyboardActions(
                onDone = { onDoneEntered() },
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onValueChange = onInputUpdated,
            shape = RoundedCornerShape(size = 16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
    }
    LaunchedEffect(key1 = "input_focus_$uniqueStepId") {
        focusRequester.requestFocus()
    }
}

@Composable
@Preview
private fun InputPreview() {
    LeMokTheme {
        Input(
            value = "hello",
            uniqueStepId = "1",
            onInputUpdated = {},
            onDoneEntered = {},
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
        stringResource(
            id = R.string.lesson_translate_title,
            "possibleVariant" to otherPossibleVariant
        )
    } else {
        otherPossibleVariant
    }
    CommonSheet(
        padding = padding,
        title = stringResource(id = R.string.lesson_answer_correct),
        message = message,
        backgroundColor = SpecialColors.correctGreenSurface,
        buttonColor = SpecialColors.correctGreen,
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
        title = stringResource(id = R.string.lesson_answer_incorrect),
        message = stringResource(
            id = R.string.lesson_answer_correct_answer,
            "correctAnswer" to correctAnswer
        ),
        backgroundColor = SpecialColors.incorrectRedSurface,
        buttonColor = SpecialColors.incorrectRed,
        onButtonClicked = onContinueClicked,
    )
}

@Composable
private fun BoxScope.CommonSheet(
    padding: PaddingValues,
    title: String,
    message: String,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    buttonColor: Color = MaterialTheme.colorScheme.primary,
    onButtonClicked: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = backgroundColor)
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
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor
            ),
            onClick = { onButtonClicked?.invoke() }
        ) {
            Text(
                text = stringResource(id = R.string.lesson_continue),
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
                    text = stringResource(id = R.string.lesson_completed),
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
                    text = stringResource(id = R.string.lesson_continue),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun HintedText(
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    text: String,
    onClicked: (word: String, pos: Int) -> Unit,
) {
    val words = text.split(" ")
    val string = buildAnnotatedString {
        words.forEach { word ->
            withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                append(word)
            }
            append(" ")
        }
    }
    ClickableText(
        modifier = modifier,
        style = style,
        text = string,
        onClick = { offset ->
            var startIndex = 0
            var fired = false
            for (word in words) {
                if (offset in startIndex..(startIndex + word.length)) {
                    onClicked(word, startIndex)
                    fired = true
                    break
                } else {
                    startIndex += word.length.inc()
                }
            }
            if (!fired) {
                words.lastOrNull()?.also { word -> onClicked(word, text.length - word.length) }
            }
        }
    )
}

@Preview
@Composable
private fun HintedTextPreview() {
    LeMokTheme {
        HintedText(
            text = "Hello! Is it great?",
            onClicked = { _, _ -> },
        )
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
            topicInfo = TopicInfo(
                id = 1,
                lessonsCount = 2,
                title = "Greetings",
                description = "Common phrases and genitive case"
            ),
            lessonNumber = 2,
            onBackPressed = {},
        )
    }
}