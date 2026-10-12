package content

import global.msnthrp.mokshan.data.network.article.ArticlesResponse
import global.msnthrp.mokshan.data.network.article.toDomain
import global.msnthrp.mokshan.data.network.base.defaultJson
import global.msnthrp.mokshan.data.network.jart.JartResponse
import global.msnthrp.mokshan.data.network.jart.asPrelinarToJart
import global.msnthrp.mokshan.data.network.jart.toDomain
import global.msnthrp.mokshan.data.network.lessons.response.TopicResponse
import global.msnthrp.mokshan.data.network.lessons.response.TopicsSummaryResponse
import global.msnthrp.mokshan.data.network.lessons.response.toDomain
import global.msnthrp.mokshan.data.network.phrasebook.PhrasebookResponse
import global.msnthrp.mokshan.data.network.phrasebook.toDomain
import global.msnthrp.mokshan.domain.jart.Jart
import global.msnthrp.mokshan.domain.jart.JartEntry
import global.msnthrp.mokshan.domain.lessons.Topic
import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import okio.FileSystem
import okio.Path
import okio.SYSTEM
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Checks the invariants the client relies on, using the real production parsers on `content/`.
 *
 * Tests named `*_knownIssue*` pin a defect that is currently present in the content, so the suite
 * stays green. When the content is fixed they fail on purpose: update the expectation to empty.
 */
class ContentContractTests {

    private val fs = FileSystem.SYSTEM
    private val contentDir: Path = getContentDir()
    private val articlesDir = contentDir / "articles"

    private val languages = listOf("en", "ru")
    private val indexNames = listOf("index", "index-sandbox")

    // region helpers

    private fun Path.readText(): String = fs.read(this) { readUtf8() }

    private fun lessonsDir(lang: String) = contentDir / "lessons-$lang"

    private fun summary(lang: String, indexName: String): TopicsSummaryResponse =
        defaultJson.decodeFromString((lessonsDir(lang) / "$indexName.json").readText())

    private fun rawTopic(lang: String, id: Int): TopicResponse =
        defaultJson.decodeFromString((lessonsDir(lang) / "$id" / "lessons.json").readText())

    private fun topic(lang: String, id: Int): Topic = rawTopic(lang, id).toDomain()
        ?: throw AssertionError("topic $lang/$id is rejected by the client mapper")

    private fun topicIds(lang: String, indexName: String = "index"): List<Int> =
        summary(lang, indexName).topicsInfo.orEmpty().map { it.id!! }

    private fun articleIndex(lang: String, indexName: String): ArticlesResponse {
        val file = if (indexName == "index") "index-$lang.json" else "index-sandbox-$lang.json"
        return defaultJson.decodeFromString((articlesDir / file).readText())
    }

    private fun String.cleanUp(): String = replace("?", "").replace("!", "").replace(".", "")
        .replace(",", "").replace("–", "").replace("  ", " ").lowercase().trim()

    private fun parseArticle(file: String): Jart {
        val path = articlesDir / file
        val text = path.readText()
        val url = "https://test/articles/$file"
        return if (file.endsWith(".json")) {
            defaultJson.decodeFromString<JartResponse>(text).toDomain(url)
                ?: throw AssertionError("$file is rejected by the JART mapper")
        } else {
            text.asPrelinarToJart(url)
        }
    }

    // endregion

    // region lessons

    @Test
    fun lessonIndexes_areAcceptedByTheClient() {
        for (lang in languages) for (name in indexNames) {
            val response = summary(lang, name)
            val domain = assertNotNull(response.toDomain(), "$lang/$name")

            assertEquals(response.topicsInfo!!.size, domain.topicsInfo.size, "$lang/$name drops topics")
        }
    }

    @Test
    fun lessonIndexes_haveUniqueAndAscendingTopicIds() {
        for (lang in languages) for (name in indexNames) {
            val ids = topicIds(lang, name)

            assertEquals(ids.sorted().distinct(), ids, "$lang/$name")
            assertEquals((1..ids.size).toList(), ids, "$lang/$name ids must be 1..n (progress assumes it)")
        }
    }

    @Test
    fun lessonIndexes_haveSameTopicIdsInBothLanguages() {
        for (name in indexNames) {
            assertEquals(topicIds("en", name), topicIds("ru", name), name)
        }
    }

    @Test
    fun lessonIndexes_haveNonBlankTitlesSafeForNavigationRoutes() {
        val forbidden = "&=#%{}/\\+".toSet()
        for (lang in languages) for (name in indexNames) {
            summary(lang, name).topicsInfo!!.forEach { info ->
                assertTrue(!info.title.isNullOrBlank(), "$lang/$name topic ${info.id}")
                val text = info.title.orEmpty() + info.description.orEmpty()
                assertTrue(text.none { it in forbidden }, "$lang/$name topic ${info.id}: $text")
            }
        }
    }

    @Test
    fun everyIndexedTopic_hasTopicFileAcceptedByTheClient() {
        for (lang in languages) for (name in indexNames) {
            topicIds(lang, name).forEach { id ->
                val raw = rawTopic(lang, id)
                val domain = topic(lang, id)

                assertEquals(id, domain.id, "$lang topic file id")
                assertEquals(raw.lessons!!.size, domain.lessons.size, "$lang/$id: mapper drops lessons")
            }
        }
    }

    @Test
    fun lessonOrders_areContiguousFromOne() {
        for (lang in languages) for (id in topicIds(lang)) {
            val orders = topic(lang, id).lessons.map { it.order }

            assertEquals((1..orders.size).toList(), orders, "$lang/$id")
        }
    }

    @Test
    fun indexLessonsCount_matchesTopicFiles() {
        // the topics list takes the topic length from the index, lessons from the topic file;
        // a mismatch makes a topic impossible to complete
        val mismatched = sortedSetOf<String>()
        for (lang in languages) for (name in indexNames) {
            summary(lang, name).topicsInfo!!.forEach { info ->
                val actual = topic(lang, info.id!!).lessons.size
                if (info.lessonsCount != actual) {
                    mismatched += "$lang/$name/topic${info.id}: index=${info.lessonsCount} actual=$actual"
                }
            }
        }

        assertEquals(emptySet<String>(), mismatched)
    }

    @Test
    fun grammarArticlesReferencedByIndexes_exist() {
        for (lang in languages) for (name in indexNames) {
            summary(lang, name).topicsInfo!!.mapNotNull { it.grammar }.filter { it.isNotBlank() }.forEach { file ->
                assertTrue(fs.exists(articlesDir / file), "$lang/$name references missing $file")
                assertTrue(file.contains("-$lang."), "$lang/$name references wrong language file $file")
            }
        }
    }

    @Test
    fun lessonPairs_areWellFormed() {
        for (lang in languages) for (id in topicIds(lang)) {
            topic(lang, id).lessons.forEach { lesson ->
                assertTrue(lesson.toNative.isNotEmpty(), "$lang/$id/${lesson.order}")
                lesson.toNative.forEach { pair ->
                    val where = "$lang/$id/${lesson.order}: ${pair.mokshan}"
                    assertTrue(pair.mokshan.isNotBlank(), where)
                    assertTrue(pair.native.isNotEmpty(), "$where has no translation (lesson crashes)")
                    pair.native.forEach { native -> assertTrue(native.isNotBlank(), where) }
                    assertEquals(pair.mokshan.trim(), pair.mokshan, where)
                    pair.native.forEach { assertEquals(it.trim(), it, where) }
                    assertTrue("  " !in pair.mokshan && pair.native.none { "  " in it }, "$where has double spaces")
                }
            }
        }
    }

    @Test
    fun lessonTexts_doNotContainTheDictionaryDelimiter() {
        for (lang in languages) for (id in topicIds(lang)) {
            val topic = topic(lang, id)
            val texts = topic.lessons.flatMap { lesson ->
                lesson.toNative.flatMap { listOf(it.mokshan) + it.native } + lesson.dictionary
            } + topic.translations.flatMap { listOf(it.mokshan, it.native) }

            texts.forEach { assertTrue('|' !in it, "$lang/$id: $it") }
        }
    }

    @Test
    fun lessonStructure_isTheSameInBothLanguages_knownIssueB18() {
        // KNOWN ISSUE B18: the Russian tree has one pair less in lesson 1 of topic 1
        val differing = sortedSetOf<Int>()
        for (id in topicIds("en")) {
            val en = topic("en", id)
            val ru = topic("ru", id)
            val enShape = en.lessons.map { it.order to it.toNative.size to it.dictionary.size }
            val ruShape = ru.lessons.map { it.order to it.toNative.size to it.dictionary.size }
            if (enShape != ruShape) differing += id
        }

        assertEquals(sortedSetOf(1), differing)
    }

    @Test
    fun dictionaryWords_areKnownToTheTopic_knownIssueB18() {
        // every word that completing a lesson should add must exist in pairs or translations
        val unresolved = sortedSetOf<String>()
        for (lang in languages) for (id in topicIds(lang)) {
            val topic = topic(lang, id)
            val known = topic.lessons.flatMap { l -> l.toNative.map { it.mokshan.cleanUp() } }.toSet() +
                topic.translations.map { it.mokshan.cleanUp() }
            topic.lessons.flatMap { it.dictionary }.forEach { word ->
                if (word.cleanUp() !in known) unresolved += "$lang/$id: $word"
            }
        }

        assertEquals(sortedSetOf("en/9: аньцек", "ru/1: Кода тевонте?", "ru/9: аньцек"), unresolved)
    }

    // endregion

    // region articles

    @Test
    fun articleIndexes_areAcceptedByTheClientWithoutDroppingItems() {
        for (lang in languages) for (name in indexNames) {
            val response = articleIndex(lang, name)
            val domain = response.toDomain("https://test/articles/")

            assertEquals(response.articles!!.size, domain.articles.size, "$lang/$name drops articles")
            assertTrue(domain.articles.all { it.added > 0 }, "$lang/$name has an unparsable date")
        }
    }

    @Test
    fun articleIndexes_referenceExistingFilesWithValidMetadata() {
        val isoDate = Regex("""\d{4}-\d{2}-\d{2}""")
        for (lang in languages) for (name in indexNames) {
            val response = articleIndex(lang, name)
            val urls = response.articles!!.map { it.url!! }

            assertEquals(urls.distinct(), urls, "$lang/$name has duplicated articles")
            response.articles.forEach { article ->
                val where = "$lang/$name: ${article.url}"
                assertTrue(fs.exists(articlesDir / article.url!!), "$where is missing")
                assertTrue(!article.title.isNullOrBlank(), where)
                assertTrue(isoDate.matches(article.added.orEmpty()), "$where has date ${article.added}")
                assertTrue(response.categories!!.containsAll(article.categories.orEmpty()), "$where uses unknown category")
            }
        }
    }

    @Test
    fun articleIndexes_listTheSameArticlesInBothLanguages() {
        for (name in indexNames) {
            val en = articleIndex("en", name).articles!!.map { it.url!!.replace("-en.", "-XX.") }
            val ru = articleIndex("ru", name).articles!!.map { it.url!!.replace("-ru.", "-XX.") }

            assertEquals(en, ru, name)
            assertEquals(articleIndex("en", name).categories!!.size, articleIndex("ru", name).categories!!.size)
        }
    }

    @Test
    fun everyArticleFile_isParsedByTheClient() {
        val articleFiles = fs.list(articlesDir).map { it.name }
            .filter { !it.startsWith("index") && (it.endsWith(".json") || it.endsWith(".prelinar")) }

        assertTrue(articleFiles.size >= 10)
        articleFiles.forEach { file ->
            val jart = parseArticle(file)

            assertTrue(jart.content.isNotEmpty(), "$file is empty")
            assertTrue(jart.content.none { it is JartEntry.Unknown }, "$file has entries of unknown type")
        }
    }

    @Test
    fun articleImages_existNextToArticles() {
        fs.list(articlesDir).map { it.name }
            .filter { !it.startsWith("index") && (it.endsWith(".json") || it.endsWith(".prelinar")) }
            .forEach { file ->
                parseArticle(file).content.filterIsInstance<JartEntry.Image>().forEach { image ->
                    assertTrue(fs.exists(articlesDir / image.link), "$file references missing image ${image.link}")
                }
            }
    }

    @Test
    fun pronunciationArticles_usedByHardCodedLinkExistForBothLanguages() {
        languages.forEach { lang ->
            assertTrue(fs.exists(articlesDir / "pronunciation-$lang.json"), lang)
        }
    }

    // endregion

    // region phrasebook and legal

    @Test
    fun phrasebook_isAcceptedByTheClientWithoutDroppingItems() {
        val raw = defaultJson.decodeFromString<PhrasebookResponse>((contentDir / "phrasebook" / "phrasebook.json").readText())
        val domain = assertNotNull(raw.toDomain())

        assertEquals(raw.categories!!.size, domain.categories.size)
        assertEquals(raw.phrasebook!!.size, domain.phrases.size, "phrases with unknown category are dropped")
    }

    @Test
    fun phrasebook_isTranslatedToBothLanguages() {
        val raw = defaultJson.decodeFromString<PhrasebookResponse>((contentDir / "phrasebook" / "phrasebook.json").readText())
        val domain = raw.toDomain()!!
        val expected = ForeignLanguage.entries.toSet()

        domain.categories.forEach { category ->
            assertEquals(expected, category.translations.map { it.foreignLanguage }.toSet(), category.id)
        }
        domain.phrases.forEach { phrase ->
            assertEquals(expected, phrase.translations.map { it.foreignLanguage }.toSet(), phrase.mokshanPhrase)
        }
        assertEquals(raw.phrasebook!!.sumOf { it.translations!!.size }, domain.phrases.sumOf { it.translations.size })
    }

    @Test
    fun legalDocuments_existInEveryLanguageAndFormat() {
        for (doc in listOf("pp", "tos")) for (lang in languages) for (ext in listOf("md", "json")) {
            val path = contentDir / "legal" / "$doc-$lang.$ext"

            assertTrue(fs.exists(path), "$path")
            assertTrue(path.readText().isNotBlank(), "$path")
            if (ext == "json") Json.parseToJsonElement(path.readText()).jsonObject
        }
    }

    // endregion
}
