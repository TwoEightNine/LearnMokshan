package global.msnthrp.mokshan.data.network.jart

import global.msnthrp.mokshan.domain.jart.JartEntry
import global.msnthrp.mokshan.domain.jart.JartMeta
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class JartResponseExtensionsTest {

    private val url = "https://test/article.json"

    private fun entry(
        type: String? = "body",
        value: String? = "text",
        size: List<Int>? = null,
        header: Boolean? = null,
        cells: List<String>? = null,
        footer: String? = null,
    ) = JartEntryResponse(type, value, size, header, cells, footer)

    private fun response(vararg entries: JartEntryResponse) =
        JartResponse(JartMetaResponse(version = 1), entries.toList())

    private fun mapped(vararg entries: JartEntryResponse): List<JartEntry> =
        response(*entries).toDomain(url)!!.content

    // region response

    @Test
    fun response_carriesVersionAndArticleUrl() {
        assertEquals(JartMeta(1, url), response().toDomain(url)!!.meta)
    }

    @Test
    fun response_withoutMeta_isInvalid() {
        assertNull(JartResponse(meta = null, content = emptyList()).toDomain(url))
    }

    @Test
    fun response_withoutVersion_isInvalid() {
        assertNull(JartResponse(JartMetaResponse(version = null), emptyList()).toDomain(url))
    }

    @Test
    fun response_withoutContent_hasEmptyContent() {
        assertEquals(emptyList(), JartResponse(JartMetaResponse(1), null).toDomain(url)!!.content)
    }

    // endregion

    // region text entries

    @Test
    fun textEntries_areMappedByType() {
        val entries = mapped(
            entry("title", "T"), entry("h1", "A"), entry("h2", "B"), entry("h3", "C"),
            entry("body", "D"), entry("hint", "E"), entry("list", "F"), entry("sublist", "G"),
        )

        assertEquals(
            listOf(
                JartEntry.Title("T"), JartEntry.Header1("A"), JartEntry.Header2("B"),
                JartEntry.Header3("C"), JartEntry.Body("D"), JartEntry.Hint("E"),
                JartEntry.ListItem("F"), JartEntry.SubListItem("G"),
            ),
            entries,
        )
    }

    @Test
    fun entryType_isCaseInsensitive() {
        assertEquals(listOf<JartEntry>(JartEntry.Header1("A")), mapped(entry("H1", "A")))
    }

    @Test
    fun textEntriesWithBlankValue_areDropped() {
        assertEquals(
            emptyList(),
            mapped(entry("h1", null), entry("body", ""), entry("hint", "   "), entry("list", "\n")),
        )
    }

    @Test
    fun unknownTypeOrNoType_becomesUnknownEntryWhenHasValue() {
        assertEquals(
            listOf<JartEntry>(JartEntry.Unknown("a"), JartEntry.Unknown("b")),
            mapped(entry("video", "a"), entry(null, "b")),
        )
    }

    @Test
    fun unknownTypeWithBlankValue_isDropped() {
        assertEquals(emptyList(), mapped(entry("video", " ")))
    }

    @Test
    fun invalidEntries_doNotAffectTheValidOnes() {
        val entries = mapped(entry("h1", "A"), entry("body", ""), entry("h2", "B"))

        assertEquals(listOf("A", "B"), entries.map { it.value })
    }

    // endregion

    // region images and tables

    @Test
    fun image_hasLinkAndFooter() {
        assertEquals(
            listOf<JartEntry>(JartEntry.Image("pic.png", "Footer")),
            mapped(entry("image", "pic.png", footer = "Footer")),
        )
    }

    @Test
    fun image_withoutFooter_hasEmptyFooter() {
        assertEquals(listOf<JartEntry>(JartEntry.Image("pic.png", "")), mapped(entry("image", "pic.png")))
    }

    @Test
    fun image_withoutLink_isDropped() {
        assertEquals(emptyList(), mapped(entry("image", " ", footer = "Footer")))
    }

    @Test
    fun table_hasCellsSizeAndHeaderFlag() {
        assertEquals(
            listOf<JartEntry>(JartEntry.Table(listOf("a", "b", "c", "d"), 2 to 2, header = true)),
            mapped(entry("table", null, size = listOf(2, 2), header = true, cells = listOf("a", "b", "c", "d"))),
        )
    }

    @Test
    fun table_headerDefaultsToFalse() {
        val table = mapped(entry("table", null, size = listOf(1, 1), cells = listOf("a"))).single() as JartEntry.Table

        assertEquals(false, table.header)
    }

    @Test
    fun table_withoutCellsOrSize_isDropped() {
        assertEquals(
            emptyList(),
            mapped(
                entry("table", null, size = listOf(1, 1), cells = null),
                entry("table", null, size = null, cells = listOf("a")),
            ),
        )
    }

    @Test
    fun table_sizeUsesFirstAndLastItems_knownBugB14() {
        val single = mapped(entry("table", null, size = listOf(4), cells = listOf("a"))).single() as JartEntry.Table
        val triple = mapped(entry("table", null, size = listOf(2, 9, 3), cells = listOf("a"))).single() as JartEntry.Table

        assertEquals(4 to 4, single.size)
        assertEquals(2 to 3, triple.size)
    }

    @Test
    fun table_withEmptySize_fails_knownBugB14() {
        assertFailsWith<NoSuchElementException> {
            mapped(entry("table", null, size = emptyList(), cells = listOf("a")))
        }
    }

    // endregion

    // region domain entries

    @Test
    fun image_valueContainsLinkAndFooter() {
        assertEquals("pic.png\nFooter", JartEntry.Image("pic.png", "Footer").value)
    }

    @Test
    fun table_valueIsRenderedRowByRow() {
        val table = JartEntry.Table(listOf("a", "b", "c", "d", "e"), 2 to 3, header = false)

        assertEquals("a | b\nc | d\ne", table.value)
    }

    @Test
    fun table_withZeroWidth_failsWhenRendered_knownBugB14() {
        val table = JartEntry.Table(listOf("a"), 0 to 1, header = false)

        assertFailsWith<IllegalArgumentException> { table.value }
    }

    // endregion
}
