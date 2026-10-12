package global.msnthrp.mokshan.data.storage.dictionary

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class DictionaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mokshan: String,
    val native: List<String>,
    val addedTs: Long,
)
