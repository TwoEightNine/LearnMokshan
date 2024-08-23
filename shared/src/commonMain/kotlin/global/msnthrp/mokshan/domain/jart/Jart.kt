package global.msnthrp.mokshan.domain.jart

data class Jart(
    val meta: JartMeta,
    val content: JartContent,
)

data class JartMeta(
    val version: Int,
    val url: String,
)

typealias JartContent = List<JartEntry>

sealed interface JartEntry {
    val value: String

    data class Header1(override val value: String) : JartEntry
    data class Header2(override val value: String) : JartEntry
    data class Header3(override val value: String) : JartEntry
    data class Body(override val value: String) : JartEntry
    data class Hint(override val value: String) : JartEntry
    data class Title(override val value: String) : JartEntry
    data class ListItem(override val value: String) : JartEntry
    data class SubListItem(override val value: String) : JartEntry
    data class Image(
        val link: String,
        val footer: String,
    ) : JartEntry {
        override val value: String by lazy { "$link\n$footer" }
    }

    data class Table(
        val cells: List<String>,
        val size: Pair<Int, Int>,
        val header: Boolean,
    ) : JartEntry {
        override val value: String by lazy {
            cells.chunked(size.first)
                .joinToString(separator = "\n") {
                    it.joinToString(separator = " | ")
                }
        }
    }

    data class Unknown(override val value: String) : JartEntry
}