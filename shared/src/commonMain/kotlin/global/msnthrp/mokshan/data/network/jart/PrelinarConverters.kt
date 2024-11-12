package global.msnthrp.mokshan.data.network.jart

import global.msnthrp.mokshan.domain.jart.Jart
import global.msnthrp.mokshan.domain.jart.JartEntry
import global.msnthrp.mokshan.domain.jart.JartMeta

fun String.asPrelinarToJart(url: String): Jart {
    val lines = split("\n")
    val meta = mutableMapOf<String, String>()
    val content = mutableListOf<JartEntry>()

    val table = mutableListOf<List<String>>()
    var isContentStarted = false
    var isInTable = false
    lines.forEach { line ->
        if (!isContentStarted && line == "CONTENT") {
            isContentStarted = true
        } else if (!isContentStarted) {
            val metaKeyValue = line.split(": ").map { it.trim() }
            if (metaKeyValue.size == 2) {
                meta[metaKeyValue[0]] = metaKeyValue[1]
            } else {
                throw IllegalStateException("Malformed meta key-value: $line")
            }
        } else {
            var prefix: String? = null
            var lineValueStart = 0
            if (line.startsWith("<")) {
                val end = line.indexOf('>')
                if (end > 0) {
                    prefix = line.substring(0, end).replace("<", "").replace(">", "")
                    lineValueStart = end + 2
                }
            }
            val value = when {
                lineValueStart in line.indices -> line.substring(lineValueStart)
                else -> ""
            }
            if (prefix == "table") {
                isInTable = true
            } else if (isInTable && prefix == "row") {
                table.add(value.split("|").map { it.trim() })
            } else if (isInTable && prefix == "endtable") {
                isInTable = false
                val width = table.maxOf { it.size }
                val height = table.size
                content.add(
                    JartEntry.Table(
                        cells = table.flatten(),
                        size = width to height,
                        header = false,
                    )
                )
                table.clear()
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
                        val values = value.split("|").map { it.trim() }
                        JartEntry.Image(link = values[0], footer = values[1])
                    }

                    else -> JartEntry.Unknown(value)
                }
                content.add(jartEntry)
            }
        }
    }

    return Jart(
        meta = JartMeta(
            version = meta["version"]!!.toInt(),
            url = url,
        ),
        content = content.toList()
    )
}