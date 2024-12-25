package content

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.SYSTEM
import okio.buffer
import okio.use
import kotlin.test.Test


class ContentTests {

    private val fs: FileSystem
        get() = FileSystem.SYSTEM

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun checkLessons_allSentencesMustHaveTranslationHints() {
        val missingWords = mutableListOf<Pair<String, Int>>()
        getAllTopics().forEach { topic ->
            val words = topic.lessons
                ?.flatMap { it.toNative.orEmpty() }
                ?.map { it.mokshan.orEmpty() }
                ?.map { it.splitToWords() }
                ?.flatten()
                .orEmpty()
                .toSet()

            val translations = topic.translations?.mapNotNull { it.mokshan }.orEmpty()
            val translationsFromSentences =
                topic.lessons?.flatMap { it.dictionary.orEmpty() }.orEmpty()
            val allTranslations = (translations + translationsFromSentences)
                .flatMap { it.splitToWords() }
                .toSet()

            words.forEach { word ->
                if (word !in allTranslations) {
                    missingWords.add(word to topic.id!!)
                }
            }
        }

        if (missingWords.isNotEmpty()) {
            val message = "Missing words:\n" +
                    missingWords
                        .distinct()
                        .sortedBy { it.second }
                        .joinToString("\n") { "id=${it.second}, word=${it.first}" }
            throw AssertionError(message)
        }
    }

    private fun getAllTopics(): List<TopicResponse> {
        val contentDir = getContentDir()
        val lessonsDirs = fs.list(contentDir).filter { "lesson" in it.name }
        val lessonFiles = lessonsDirs.flatMap { it.getAllNestedFilePaths() }
            .filter { it.name.endsWith("lessons.json") }

        return lessonFiles.map { lessonFile ->
            json.decodeFromString<TopicResponse>(lessonFile.read())
        }
    }

    private fun Path.read(): String {
        val result = StringBuilder()
        fs.source(this).use { fileSource ->
            fileSource.buffer().use { bufferedFileSource ->
                while (true) {
                    val line = bufferedFileSource.readUtf8Line() ?: break
                    result.append(line).append('\n')
                }
            }
        }
        return result.toString()
    }

    private fun Path.getAllNestedFilePaths(): List<Path> {
        return if (fs.metadataOrNull(this)?.isRegularFile == true) {
            listOf(this)
        } else {
            fs.list(this).flatMap { it.getAllNestedFilePaths() }
        }
    }

    private fun String.splitToWords(): List<String> {
        return cleanUp()
            .split(' ')
    }

    private fun String.cleanUp(): String {
        return replace("?", "")
            .replace("!", "")
            .replace(".", "")
            .replace(",", "")
            .replace("–", "")
            .replace("  ", " ")
            .lowercase()
    }

    @Serializable
    internal data class TopicResponse(
        val id: Int?,
        val lessons: List<LessonResponse>?,
        val translations: List<TranslationResponse>?,
    )

    @Serializable
    data class LessonResponse(
        val toNative: List<LessonPairResponse>?,
        val dictionary: List<String>?,
    )

    @Serializable
    data class LessonPairResponse(
        val mokshan: String?,
        val native: List<String>?,
    )

    @Serializable
    data class TranslationResponse(
        val mokshan: String?,
        val native: String?,
    )
}