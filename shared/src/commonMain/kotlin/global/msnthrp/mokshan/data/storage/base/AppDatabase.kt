package global.msnthrp.mokshan.data.storage.base

import androidx.room3.ColumnTypeConverter
import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import global.msnthrp.mokshan.data.storage.dictionary.DictionaryDao
import global.msnthrp.mokshan.data.storage.dictionary.DictionaryEntity

@Database(entities = [DictionaryEntity::class], version = 1)
@ColumnTypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getDictionary(): DictionaryDao
}

object Converters {

    @ColumnTypeConverter
    fun fromListOfStrings(strings: List<String>): String {
        return strings.joinToString(separator = "|")
    }

    @ColumnTypeConverter
    fun toListOfStrings(string: String): List<String> {
        return string.split("|")
    }
}
