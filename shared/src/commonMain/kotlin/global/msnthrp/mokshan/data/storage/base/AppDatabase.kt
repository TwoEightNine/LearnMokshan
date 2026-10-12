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

/**
 * Stores a list of strings in one text column. Items are joined by `|`; a `|` or `\` inside an
 * item is escaped with a backslash. Rows written before the escaping existed (no escapes at all)
 * are read unchanged, and an empty list is stored as an empty string.
 */
object Converters {

    private const val SEPARATOR = '|'
    private const val ESCAPE = '\\'

    @ColumnTypeConverter
    fun fromListOfStrings(strings: List<String>): String {
        return strings.joinToString(separator = SEPARATOR.toString()) { item ->
            buildString {
                item.forEach { char ->
                    if (char == SEPARATOR || char == ESCAPE) append(ESCAPE)
                    append(char)
                }
            }
        }
    }

    @ColumnTypeConverter
    fun toListOfStrings(string: String): List<String> {
        if (string.isEmpty()) return emptyList()
        val items = mutableListOf<String>()
        val current = StringBuilder()
        var index = 0
        while (index < string.length) {
            val char = string[index]
            val next = string.getOrNull(index + 1)
            when {
                char == ESCAPE && (next == SEPARATOR || next == ESCAPE) -> {
                    current.append(next)
                    index++
                }

                char == SEPARATOR -> {
                    items += current.toString()
                    current.clear()
                }

                else -> current.append(char)
            }
            index++
        }
        items += current.toString()
        return items
    }
}
