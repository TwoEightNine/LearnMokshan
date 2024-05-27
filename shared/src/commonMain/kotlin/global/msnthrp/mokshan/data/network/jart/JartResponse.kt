package global.msnthrp.mokshan.data.network.jart

import kotlinx.serialization.Serializable

@Serializable
data class JartResponse(
    val meta: JartMetaResponse?,
    val content: JartContentResponse?,
)

@Serializable
data class JartMetaResponse(
    val version: Int?,
)

typealias JartContentResponse = List<JartEntryResponse>

@Serializable
data class JartEntryResponse(
    val type: String?,
    val value: String?,
)