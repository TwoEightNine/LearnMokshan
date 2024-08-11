package global.msnthrp.mokshan.data.storage.dictionary

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DictionaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mokshan: String,
    val native: List<String>,
    val addedTs: Long,
)