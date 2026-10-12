package global.msnthrp.mokshan.data.network.jart

import global.msnthrp.mokshan.domain.jart.Jart
import global.msnthrp.mokshan.domain.jart.JartEntry
import global.msnthrp.mokshan.domain.jart.JartMeta

/**
 * `<name>` at the very start of a line, followed by the end of the line or one space and the value.
 * A line that only starts with something tag-like (`<b>bold</b> text`, `<h1>glued`) is plain text.
 */
private val PREFIX_LINE = Regex("""<([A-Za-z0-9_]+)>(?: (.*))?""")

fun String.asPrelinarToJart(url: String): Jart {
    val meta = mutableMapOf<String, String>()
    val content = mutableListOf<JartEntry>()

    val tableRows = mutableListOf<List<String>>()
    var isContentStarted = false
    var isInTable = false

    fun finishTable() {
        if (tableRows.isNotEmpty()) {
            // short rows are padded so that every row has the width of the widest one
            val width = tableRows.maxOf { it.size }
            content.add(
                JartEntry.Table(
                    cells = tableRows.flatMap { row -> row + List(width - row.size) { "" } },
                    size = width to tableRows.size,
                    header = false,
                )
            )
        }
        tableRows.clear()
        isInTable = false
    }

    // lines() understands \n, \r\n and \r
    lines().forEach { line ->
        if (!isContentStarted && line == "CONTENT") {
            isContentStarted = true
        } else if (!isContentStarted) {
            if (line.isBlank()) return@forEach
            val separator = line.indexOf(": ")
            if (separator < 0) {
                throw IllegalStateException("Malformed meta key-value: $line")
            }
            meta[line.substring(0, separator).trim()] = line.substring(separator + 2).trim()
        } else {
            val match = PREFIX_LINE.matchEntire(line)
            val prefix = match?.groupValues?.get(1)
            val value = match?.groupValues?.get(2) ?: line
            if (prefix == "table") {
                finishTable()
                isInTable = true
            } else if (isInTable && prefix == "row") {
                tableRows.add(value.split("|").map { it.trim() })
            } else if (isInTable && prefix == "endtable") {
                finishTable()
            } else {
                val jartEntry = when (prefix) {
                    null -> JartEntry.Body(value)
                    "h1" -> JartEntry.Header1(value)
                    "h2" -> JartEntry.Header2(value)
                    "h3" -> JartEntry.Header3(value)
                    "title" -> JartEntry.Title(value)
                    "hint" -> JartEntry.Hint(value)
                    "list" -> JartEntry.ListItem(value)
                    "sublist" -> JartEntry.SubListItem(value)
                    "image" -> {
                        val values = value.split("|", limit = 2).map { it.trim() }
                        val link = values[0]
                        if (link.isEmpty()) null else JartEntry.Image(link = link, footer = values.getOrElse(1) { "" })
                    }

                    else -> JartEntry.Unknown(value)
                }
                if (jartEntry != null) content.add(jartEntry)
            }
        }
    }
    // a table that is not closed at the end of the file still shows what has been written
    if (isInTable) finishTable()

    val version = meta["version"] ?: throw IllegalStateException("Prelinar metadata has no version")
    return Jart(
        meta = JartMeta(
            version = version.toIntOrNull()
                ?: throw IllegalStateException("Malformed Prelinar version: $version"),
            url = url,
        ),
        content = content.toList()
    )
}
