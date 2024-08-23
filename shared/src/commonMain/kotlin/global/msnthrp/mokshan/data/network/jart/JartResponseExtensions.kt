package global.msnthrp.mokshan.data.network.jart

import global.msnthrp.mokshan.domain.jart.Jart
import global.msnthrp.mokshan.domain.jart.JartEntry
import global.msnthrp.mokshan.domain.jart.JartMeta

fun JartResponse.toDomain(articleUrl: String): Jart? {
    return Jart(
        meta = this.meta?.toDomain(articleUrl) ?: return null,
        content = this.content?.mapNotNull { it.toDomain() } ?: emptyList(),
    )
}

private fun JartMetaResponse.toDomain(articleUrl: String): JartMeta? {
    return JartMeta(
        version = this.version ?: return null,
        url = articleUrl,
    )
}

private fun JartEntryResponse.toDomain(): JartEntry? {
    val type = this.type?.lowercase()
    val value = this.value.orEmpty()
    return when (type) {
        "h1" -> JartEntry.Header1(value = value.takeIf { it.isNotBlank() } ?: return null)
        "h2" -> JartEntry.Header2(value = value.takeIf { it.isNotBlank() } ?: return null)
        "h3" -> JartEntry.Header3(value = value.takeIf { it.isNotBlank() } ?: return null)
        "title" -> JartEntry.Title(value = value.takeIf { it.isNotBlank() } ?: return null)
        "body" -> JartEntry.Body(value = value.takeIf { it.isNotBlank() } ?: return null)
        "hint" -> JartEntry.Hint(value = value.takeIf { it.isNotBlank() } ?: return null)
        "list" -> JartEntry.ListItem(value = value.takeIf { it.isNotBlank() } ?: return null)
        "sublist" -> JartEntry.SubListItem(value = value.takeIf { it.isNotBlank() } ?: return null)
        "image" -> JartEntry.Image(
            link = value.takeIf { it.isNotBlank() } ?: return null,
            footer = this.footer.orEmpty(),
        )
        "table" -> JartEntry.Table(
            cells = this.cells ?: return null,
            size = this.size?.run { first() to last() } ?: return null,
            header = this.header ?: false,
        )
        else -> JartEntry.Unknown(value = value.takeIf { it.isNotBlank() } ?: return null)
    }
}