package global.msnthrp.mokshan.data.network.jart

import global.msnthrp.mokshan.domain.jart.Jart
import global.msnthrp.mokshan.domain.jart.JartEntry
import global.msnthrp.mokshan.domain.jart.JartMeta
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PrelinarConvertersTest {

    private val url = "https://test/article.prelinar"

    private fun parse(vararg lines: String): Jart =
        lines.joinToString("\n").asPrelinarToJart(url)

    private fun content(vararg contentLines: String): List<JartEntry> =
        parse("version: 1", "CONTENT", *contentLines).content

    // region metadata

    @Test
    fun metadata_versionAndUrlAreTaken() {
        val jart = parse("version: 12", "CONTENT")

        assertEquals(JartMeta(version = 12, url = url), jart.meta)
        assertEquals(emptyList(), jart.content)
    }

    @Test
    fun metadata_otherKeysAreAllowedAndIgnored() {
        val jart = parse("title: Hello", "version: 2", "author: me", "CONTENT", "<h1> A")

        assertEquals(2, jart.meta.version)
        assertEquals(listOf<JartEntry>(JartEntry.Header1("A")), jart.content)
    }

    @Test
    fun metadata_valuesAreTrimmed() {
        assertEquals(7, parse("version:   7  ", "CONTENT").meta.version)
    }

    @Test
    fun metadata_withoutContentMarker_givesEmptyContent() {
        // pins current behaviour: a file with only metadata is still accepted
        val jart = parse("version: 3")

        assertEquals(3, jart.meta.version)
        assertEquals(emptyList(), jart.content)
    }

    @Test
    fun metadata_missingVersion_failsWithDescriptiveError() {
        val error = assertFailsWith<IllegalStateException> { parse("title: Hello", "CONTENT") }

        assertEquals("Prelinar metadata has no version", error.message)
    }

    @Test
    fun metadata_nonNumericVersion_failsWithDescriptiveError() {
        val error = assertFailsWith<IllegalStateException> { parse("version: abc", "CONTENT") }

        assertEquals("Malformed Prelinar version: abc", error.message)
    }

    @Test
    fun metadata_valueMayContainColonSpace() {
        val jart = parse("title: Cases: nominative", "version: 4", "CONTENT")

        assertEquals(4, jart.meta.version)
        assertFailsWith<IllegalStateException> { parse("version: 1: 2", "CONTENT") }
    }

    @Test
    fun metadata_laterDuplicateKeyWins() {
        assertEquals(2, parse("version: 1", "version: 2", "CONTENT").meta.version)
    }

    @Test
    fun metadata_lineWithoutColonSpace_isMalformed() {
        assertFailsWith<IllegalStateException> { parse("version:1", "CONTENT") }
        assertFailsWith<IllegalStateException> { parse("just text", "CONTENT") }
    }

    @Test
    fun metadata_blankLinesAreSkipped() {
        val jart = parse("", "version: 1", "   ", "CONTENT", "<h1> A")

        assertEquals(1, jart.meta.version)
        assertEquals(listOf<JartEntry>(JartEntry.Header1("A")), jart.content)
    }

    @Test
    fun windowsAndOldMacLineEndings_areSupported() {
        val expected = listOf<JartEntry>(JartEntry.Header1("A"), JartEntry.Body("text"))

        assertEquals(expected, "version: 1\r\nCONTENT\r\n<h1> A\r\ntext".asPrelinarToJart(url).content)
        assertEquals(expected, "version: 1\rCONTENT\r<h1> A\rtext".asPrelinarToJart(url).content)
    }

    @Test
    fun contentMarker_mustBeExactAndOnlyFirstOneCounts() {
        val jart = parse("version: 1", "CONTENT", "CONTENT", "text")

        assertEquals(listOf<JartEntry>(JartEntry.Body("CONTENT"), JartEntry.Body("text")), jart.content)
    }

    // endregion

    // region entries

    @Test
    fun typedEntries_areMappedByPrefix() {
        val entries = content(
            "<title> The title",
            "<h1> One",
            "<h2> Two",
            "<h3> Three",
            "<hint> Remember",
            "<list> item",
            "<sublist> sub item",
        )

        assertEquals(
            listOf(
                JartEntry.Title("The title"),
                JartEntry.Header1("One"),
                JartEntry.Header2("Two"),
                JartEntry.Header3("Three"),
                JartEntry.Hint("Remember"),
                JartEntry.ListItem("item"),
                JartEntry.SubListItem("sub item"),
            ),
            entries,
        )
    }

    @Test
    fun lineWithoutPrefix_isBody() {
        assertEquals(listOf<JartEntry>(JartEntry.Body("Plain paragraph, with: punctuation.")), content("Plain paragraph, with: punctuation."))
    }

    @Test
    fun unknownPrefix_becomesUnknownEntry() {
        assertEquals(listOf<JartEntry>(JartEntry.Unknown("text")), content("<video> text"))
    }

    @Test
    fun prefixIsCaseSensitive() {
        assertEquals(listOf<JartEntry>(JartEntry.Unknown("text")), content("<H1> text"))
    }

    @Test
    fun prefixWithoutValue_givesEmptyValue() {
        assertEquals(listOf<JartEntry>(JartEntry.Header2("")), content("<h2>"))
    }

    @Test
    fun cyrillicAndInnerAngleBrackets_inValueArePreserved() {
        assertEquals(
            listOf<JartEntry>(JartEntry.Body("Шумбрат < привет > ")),
            content("Шумбрат < привет > "),
        )
    }

    @Test
    fun tagLikeTextThatIsNotAPrefixLine_staysPlainBody() {
        assertEquals(listOf<JartEntry>(JartEntry.Body("<h1>glued")), content("<h1>glued"))
        assertEquals(listOf<JartEntry>(JartEntry.Body("<b>bold</b> text")), content("<b>bold</b> text"))
        assertEquals(listOf<JartEntry>(JartEntry.Body("<3 you")), content("<3 you"))
        assertEquals(listOf<JartEntry>(JartEntry.Body("<my tag> text")), content("<my tag> text"))
    }

    @Test
    fun prefixLine_keepsRestOfLineVerbatimAfterOneSpace() {
        assertEquals(listOf<JartEntry>(JartEntry.Header1(" x ")), content("<h1>  x "))
        assertEquals(listOf<JartEntry>(JartEntry.Header1("")), content("<h1> "))
    }

    @Test
    fun blankLinesInContent_becomeEmptyBodyEntries_usedAsSpacingByArticles() {
        // intended: several articles rely on blank lines as vertical spacing
        assertEquals(
            listOf<JartEntry>(JartEntry.Body("a"), JartEntry.Body(""), JartEntry.Body("b"), JartEntry.Body("")),
            content("a", "", "b", ""),
        )
    }

    @Test
    fun contentOrderIsPreserved() {
        val entries = content("<h1> A", "text", "<h2> B")

        assertEquals(listOf("A", "text", "B"), entries.map { it.value })
    }

    // endregion

    // region images

    @Test
    fun image_hasLinkAndFooterSeparatedByPipe() {
        assertEquals(
            listOf<JartEntry>(JartEntry.Image(link = "pic.png", footer = "Nice picture")),
            content("<image> pic.png | Nice picture"),
        )
    }

    @Test
    fun image_footerKeepsPipesAfterTheFirstOne() {
        assertEquals(
            listOf<JartEntry>(JartEntry.Image(link = "pic.png", footer = "a | b")),
            content("<image> pic.png | a | b"),
        )
    }

    @Test
    fun image_withoutFooter_hasEmptyFooter() {
        assertEquals(listOf<JartEntry>(JartEntry.Image("pic.png", "")), content("<image> pic.png"))
        assertEquals(listOf<JartEntry>(JartEntry.Image("pic.png", "")), content("<image> pic.png |"))
    }

    @Test
    fun image_withoutLink_isDropped() {
        assertEquals(emptyList(), content("<image>", "<image> | footer"))
    }

    // endregion

    // region tables

    @Test
    fun table_isBuiltFromRowsAndHasNoHeader() {
        val entries = content("<table>", "<row> a | b", "<row> c | d", "<endtable>")

        assertEquals(
            listOf<JartEntry>(JartEntry.Table(cells = listOf("a", "b", "c", "d"), size = 2 to 2, header = false)),
            entries,
        )
    }

    @Test
    fun table_cellsAreTrimmed() {
        val table = content("<table>", "<row>   a   |   b ", "<endtable>").single() as JartEntry.Table

        assertEquals(listOf("a", "b"), table.cells)
        assertEquals(2 to 1, table.size)
    }

    @Test
    fun table_betweenOtherEntries_keepsOrder() {
        val entries = content("<h1> A", "<table>", "<row> a | b", "<endtable>", "after")

        assertEquals(3, entries.size)
        assertEquals(JartEntry.Header1("A"), entries[0])
        assertEquals(JartEntry.Body("after"), entries[2])
    }

    @Test
    fun twoTablesInOneDocument_areIndependent() {
        val entries = content(
            "<table>", "<row> a | b", "<endtable>",
            "<table>", "<row> c", "<endtable>",
        )

        assertEquals(
            listOf<JartEntry>(
                JartEntry.Table(listOf("a", "b"), 2 to 1, false),
                JartEntry.Table(listOf("c"), 1 to 1, false),
            ),
            entries,
        )
    }

    @Test
    fun table_withJaggedRows_isPaddedToTheWidestRow() {
        val table = content("<table>", "<row> a | b | c", "<row> d | e", "<endtable>").single() as JartEntry.Table

        assertEquals(3 to 2, table.size)
        assertEquals(listOf("a", "b", "c", "d", "e", ""), table.cells)
    }

    @Test
    fun emptyTable_producesNoEntry() {
        assertEquals(emptyList(), content("<table>", "<endtable>"))
        assertEquals(listOf<JartEntry>(JartEntry.Body("x")), content("<table>", "<endtable>", "x"))
    }

    @Test
    fun unclosedTable_keepsWrittenRows() {
        assertEquals(
            listOf<JartEntry>(JartEntry.Table(listOf("a", "b"), 2 to 1, false)),
            content("<table>", "<row> a | b"),
        )
    }

    @Test
    fun newTableBeforeEndOfPreviousOne_closesThePreviousOne() {
        assertEquals(
            listOf<JartEntry>(
                JartEntry.Table(listOf("a"), 1 to 1, false),
                JartEntry.Table(listOf("b"), 1 to 1, false),
            ),
            content("<table>", "<row> a", "<table>", "<row> b", "<endtable>"),
        )
    }

    @Test
    fun rowAndEndTableOutsideTable_becomeUnknownEntries() {
        assertEquals(
            listOf<JartEntry>(JartEntry.Unknown("a | b"), JartEntry.Unknown("")),
            content("<row> a | b", "<endtable>"),
        )
    }

    // endregion
}
