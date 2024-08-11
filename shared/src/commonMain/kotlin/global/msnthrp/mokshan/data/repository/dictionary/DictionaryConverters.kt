package global.msnthrp.mokshan.data.repository.dictionary

import global.msnthrp.mokshan.data.storage.dictionary.DictionaryEntity
import global.msnthrp.mokshan.domain.dictionary.Dictionary
import global.msnthrp.mokshan.domain.dictionary.DictionaryEntry


internal fun List<DictionaryEntity>.toDomain(): Dictionary {
    return map { it.toDomain() }
}

internal fun List<DictionaryEntry>.toEntities(): List<DictionaryEntity> {
    return map { it.toEntity() }
}

private fun DictionaryEntity.toDomain(): DictionaryEntry {
    return DictionaryEntry(
        mokshan = mokshan,
        native = native,
        addedTs = addedTs,
    )
}

private fun DictionaryEntry.toEntity(): DictionaryEntity {
    return DictionaryEntity(
        mokshan = mokshan,
        native = native,
        addedTs = addedTs,
    )
}