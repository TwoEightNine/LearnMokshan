package global.msnthrp.mokshan.domain.jart

data class Jart(
    val meta: JartMeta,
    val content: JartContent,
)

data class JartMeta(
    val version: Int,
)

typealias JartContent = List<JartEntry>

enum class JartEntryType {
    H1,
    H2,
    H3,
    BODY,
    HINT,
    TITLE,
    TABLE,
    LIST,
    UNKNOWN
}

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