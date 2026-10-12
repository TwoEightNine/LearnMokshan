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
    fun metadata_missingVersion_failsWithNullPointer_knownBugB13() {
        assertFailsWith<NullPointerException> { parse("title: Hello", "CONTENT") }
    }

    @Test
    fun metadata_nonNumericVersion_failsWithNumberFormat_knownBugB13() {
        assertFailsWith<NumberFormatException> { parse("version: abc", "CONTENT") }
    }

    @Test
    fun metadata_lineWithoutColonSpace_isMalformed() {
        assertFailsWith<IllegalStateException> { parse("version:1", "CONTENT") }
        assertFailsWith<IllegalStateException> { parse("just text", "CONTENT") }
    }

    @Test
    fun metadata_blankLineBeforeContent_isMalformed_knownBugB13() {
        assertFailsWith<IllegalStateException> { parse("version: 1", "", "CONTENT") }
    }

    @Test
    fun metadata_valueWithColonSpace_isMalformed_knownBugB13() {
        assertFailsWith<IllegalStateException> { parse("version: 1", "title: Cases: nominative", "CONTENT") }
    }

    @Test
    fun windowsLineEndings_breakContentMarker_knownBugB13() {
        val text = "version: 1\r\nCONTENT\r\n<h1> A\r\n"

        assertFailsWith<IllegalStateException> { text.asPrelinarToJart(url) }
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
    fun valueDirectlyAfterPrefix_losesItsFirstCharacter_knownBugB13() {
        assertEquals(listOf<JartEntry>(JartEntry.Header1("")), content("<h1>x"))
        assertEquals(listOf<JartEntry>(JartEntry.Header1("y")), content("<h1>xy"))
    }

    @Test
    fun bodyStartingWithAngleBracket_isMisparsedAsPrefix_knownBugB13() {
        assertEquals(listOf<JartEntry>(JartEntry.Unknown("old</b> text")), content("<b>bold</b> text"))
    }

    @Test
    fun blankLinesInContent_becomeEmptyBodyEntries_knownBugB13() {
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
    fun image_withExtraPipes_dropsTheRest() {
        assertEquals(
            listOf<JartEntry>(JartEntry.Image(link = "pic.png", footer = "a")),
            content("<image> pic.png | a | b"),
        )
    }

    @Test
    fun image_withoutFooterSeparator_fails_knownBugB13() {
        assertFailsWith<IndexOutOfBoundsException> { content("<image> pic.png") }
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
    fun table_withJaggedRows_isFlattenedWithoutPadding_knownBugB13() {
        val table = content("<table>", "<row> a | b | c", "<row> d | e", "<endtable>").single() as JartEntry.Table

        assertEquals(3 to 2, table.size)
        assertEquals(listOf("a", "b", "c", "d", "e"), table.cells)
    }

    @Test
    fun emptyTable_fails_knownBugB13() {
        assertFailsWith<NoSuchElementException> { content("<table>", "<endtable>") }
    }

    @Test
    fun unclosedTable_isDroppedSilently_knownBugB13() {
        assertEquals(emptyList(), content("<table>", "<row> a | b"))
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
