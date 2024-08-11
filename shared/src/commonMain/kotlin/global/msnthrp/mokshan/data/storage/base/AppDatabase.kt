package global.msnthrp.mokshan.data.storage.base

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import global.msnthrp.mokshan.data.storage.dictionary.DictionaryDao
import global.msnthrp.mokshan.data.storage.dictionary.DictionaryEntity

@Database(entities = [DictionaryEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getDictionary(): DictionaryDao
}



object Converters {

    @TypeConverter
    fun fromListOfStrings(strings: List<String>): String {
        return strings.joinToString(separator = "|")
    }

    @TypeConverter
    fun toListOfStrings(string: String): List<String> {
        return string.split("|")
    }
}