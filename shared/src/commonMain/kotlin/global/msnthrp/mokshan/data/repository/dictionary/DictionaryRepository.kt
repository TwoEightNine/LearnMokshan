package global.msnthrp.mokshan.data.repository.dictionary

import global.msnthrp.mokshan.data.storage.dictionary.DictionaryDao
import global.msnthrp.mokshan.domain.dictionary.Dictionary
import global.msnthrp.mokshan.domain.dictionary.DictionaryEntry
import global.msnthrp.mokshan.utils.NowProvider

class DictionaryRepository(
    private val dictionaryDao: DictionaryDao,
    private val nowProvider: NowProvider,
) {

    suspend fun getAllWords(): Result<Dictionary> {
        return kotlin.runCatching {
            val entities = dictionaryDao.getAll()
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
                val existingEntity = dictionaryDao.findByMokshan(mokshan).firstOrNull()
                if (existingEntity != null) {
                    val updatedNative = existingEntity.native.plus(entry.native).distinct()
                    dictionaryDao.updateByMokshan(mokshan, updatedNative, addedTs)
                } else {
                    dictionaryDao.insert(entry.copy(addedTs = addedTs).toEntity())
                }
            }
            dictionaryDao.insertMany(entries.map { it.copy(addedTs = addedTs) }.toEntities())
        }
    }
}