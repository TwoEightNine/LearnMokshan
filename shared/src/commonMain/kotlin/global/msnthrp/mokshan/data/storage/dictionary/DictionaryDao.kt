package global.msnthrp.mokshan.data.storage.dictionary

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DictionaryDao {

    @Insert
    suspend fun insert(entity: DictionaryEntity)

    @Insert
    suspend fun insertMany(entities: List<DictionaryEntity>)

    @Query("SELECT * FROM DictionaryEntity")
    suspend fun getAll(): List<DictionaryEntity>

    @Query("SELECT * FROM DictionaryEntity WHERE mokshan = :mokshan")
    suspend fun findByMokshan(mokshan: String): List<DictionaryEntity>

    @Query("UPDATE DictionaryEntity SET native = :native, addedTs = :addedTs WHERE mokshan = :mokshan")
    suspend fun updateByMokshan(mokshan: String, native: List<String>, addedTs: Long)
}