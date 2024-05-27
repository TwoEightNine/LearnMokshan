package global.msnthrp.mokshan.data.network.jart

data class JartResponse(
    val meta: JartMetaResponse?,
    val content: JartContentResponse?,
)

data class JartMetaResponse(
    val version: Int?,
)

typealias JartContentResponse = List<JartEntryResponse>

data class JartEntryResponse(
    val type: String?,
    val value: String?,
)