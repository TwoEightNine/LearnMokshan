package global.msnthrp.mokshan.data.storage.base

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import global.msnthrp.mokshan.data.storage.dictionary.DictionaryDao
import global.msnthrp.mokshan.data.storage.dictionary.DictionaryEntity

@Database(entities = [DictionaryEntity::class], version = 1)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getDictionary(): DictionaryDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
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