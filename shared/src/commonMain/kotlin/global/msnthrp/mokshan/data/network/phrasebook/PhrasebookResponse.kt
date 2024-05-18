package global.msnthrp.mokshan.data.network.phrasebook

import kotlinx.serialization.Serializable

@Serializable
internal data class PhrasebookResponse(
    val version: Int? = null,
    val categories: List<Category>? = null,
    val phrasebook: List<Phrase>? = null,
) {

    @Serializable
    data class Category(
        val category: String? = null,
        val translations: List<Translation>? = null,
    )

    @Serializable
    data class Translation(
        val value: String? = null,
        val note: String? = null,
        val language: String? = null,
    )

    @Serializable
    data class Phrase(
        val mokshan: String? = null,
        val category: String? = null,
        val translations: List<Translation>? = null,
    )
}