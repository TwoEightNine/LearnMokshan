package global.msnthrp.mokshan.data.repository.dictionary

import global.msnthrp.mokshan.data.storage.dictionary.DictionaryDao
import global.msnthrp.mokshan.domain.dictionary.Dictionary
import global.msnthrp.mokshan.domain.dictionary.DictionaryEntry
import global.msnthrp.mokshan.utils.NowProvider
import global.msnthrp.mokshan.utils.runCatchingCancellable

class DictionaryRepository(
    private val dictionaryDao: DictionaryDao,
    private val nowProvider: NowProvider,
) {

    suspend fun getAllWords(): Result<Dictionary> {
        return runCatchingCancellable {
            val entities = dictionaryDao.getAll()
            val dictionary = entities
                .toDomain()
                .distinctBy { it.mokshan }
                .sortedBy { it.mokshan }
            dictionary
        }
    }

    /**
     * Saves each word once: an unknown word is inserted, a known one gets the new translations
     * merged in and a fresh timestamp. It is safe to repeat with the same entries.
     */
    suspend fun addNewWords(entries: List<DictionaryEntry>): Result<Unit> {
        val addedTs = nowProvider.getNow()
        return runCatchingCancellable {
            entries.groupBy { it.mokshan }.forEach { (mokshan, sameWordEntries) ->
                val natives = sameWordEntries.flatMap { it.native }.distinct()
                val existingEntity = dictionaryDao.findByMokshan(mokshan).firstOrNull()
                if (existingEntity != null) {
                    val updatedNative = existingEntity.native.plus(natives).distinct()
                    dictionaryDao.updateByMokshan(mokshan, updatedNative, addedTs)
                } else {
                    dictionaryDao.insert(DictionaryEntry(mokshan, natives, addedTs).toEntity())
                }
            }
        }
    }
}