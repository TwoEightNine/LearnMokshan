package global.msnthrp.mokshan.data.network.jart

import global.msnthrp.mokshan.domain.jart.Jart
import global.msnthrp.mokshan.domain.jart.JartEntry
import global.msnthrp.mokshan.domain.jart.JartEntryType
import global.msnthrp.mokshan.domain.jart.JartMeta

fun JartResponse.toDomain(): Jart? {
    return Jart(
        meta = this.meta?.toDomain() ?: return null,
        content = this.content?.mapNotNull { it.toDomain() } ?: emptyList(),
    )
}

private fun JartMetaResponse.toDomain(): JartMeta? {
    return JartMeta(
        version = this.version ?: return null
    )
}

private fun JartEntryResponse.toDomain(): JartEntry? {
    return JartEntry(
        type = this.type?.asJartEntryType() ?: JartEntryType.UNKNOWN,
        value = this.value.orEmpty().takeIf { it.isNotBlank() } ?: return null
    )
}

private fun String.asJartEntryType(): JartEntryType {
    return runCatching { JartEntryType.valueOf(this) }.getOrNull() ?: JartEntryType.UNKNOWN
}