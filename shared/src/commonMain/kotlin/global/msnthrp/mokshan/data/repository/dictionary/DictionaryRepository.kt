package global.msnthrp.mokshan.data.repository.dictionary

import global.msnthrp.mokshan.data.storage.base.AppDatabase
import global.msnthrp.mokshan.domain.dictionary.Dictionary
import global.msnthrp.mokshan.domain.dictionary.DictionaryEntry
import global.msnthrp.mokshan.utils.NowProvider

class DictionaryRepository(
    private val appDatabase: AppDatabase,
    private val nowProvider: NowProvider,
) {

    suspend fun getAllWords(): Result<Dictionary> {
        return kotlin.runCatching {
            val entities = appDatabase.getDictionary().getAll()
            val dictionary = entities
                .toDomain()
                .distinctBy { it.mokshan }
                .sortedBy { it.mokshan }
            dictionary
        }
    }

    suspend fun addNewWords(entries: List<DictionaryEntry>): Result<Unit> {
        val addedTs = nowProvider.getNow()
        return kotlin.runCatching {
            entries.forEach { entry ->
                val mokshan = entry.mokshan
                val existingEntity = appDatabase.getDictionary().findByMokshan(mokshan).firstOrNull()
                if (existingEntity != null) {
                    val updatedNative = existingEntity.native.plus(entry.native).distinct()
                    appDatabase.getDictionary().updateByMokshan(mokshan, updatedNative, addedTs)
                } else {
                    appDatabase.getDictionary().insert(entry.copy(addedTs = addedTs).toEntity())
                }
            }
            appDatabase.getDictionary().insertMany(entries.map { it.copy(addedTs = addedTs) }.toEntities())
        }
    }
}