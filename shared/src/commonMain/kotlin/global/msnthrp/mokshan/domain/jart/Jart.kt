package global.msnthrp.mokshan.domain.jart

data class Jart(
    val meta: JartMeta,
    val content: JartContent,
)

data class JartMeta(
    val version: Int,
)

typealias JartContent = List<JartEntry>

data class JartEntry(
    val type: JartEntryType,
    val value: String,
)

enum class JartEntryType {
    H1,
    H2,
    H3,
    BODY,
    HINT,
    TITLE,
    UNKNOWN
}