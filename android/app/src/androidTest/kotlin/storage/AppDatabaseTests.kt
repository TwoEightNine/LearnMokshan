package storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import global.msnthrp.mokshan.data.storage.base.AppDatabase
import global.msnthrp.mokshan.data.storage.dictionary.DictionaryEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTests {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private var database: AppDatabase? = null
    private val compatibilityDatabaseName = "room3-compatibility-test.db"

    @After
    fun tearDown() {
        database?.close()
        context.deleteDatabase(compatibilityDatabaseName)
    }

    @Test
    fun readsAndWritesDictionaryEntries() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder<AppDatabase>(context)
            .setDriver(BundledSQLiteDriver())
            .build()
            .also { database = it }

        db.getDictionary().insert(
            DictionaryEntity(
                mokshan = "шумбрат",
                native = listOf("hello"),
                addedTs = 1L,
            )
        )

        assertEquals(listOf("hello"), db.getDictionary().getAll().single().native)
    }

    @Test
    fun opensExistingVersionOneDatabaseWithoutLosingData() = runBlocking {
        val file = context.getDatabasePath(compatibilityDatabaseName)
        file.parentFile?.mkdirs()
        file.delete()

        SQLiteDatabase.openOrCreateDatabase(file, null).use { sqliteDatabase ->
            sqliteDatabase.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `DictionaryEntity` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `mokshan` TEXT NOT NULL,
                    `native` TEXT NOT NULL,
                    `addedTs` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            sqliteDatabase.execSQL(
                "INSERT INTO `DictionaryEntity` (`mokshan`, `native`, `addedTs`) " +
                    "VALUES ('шумбрат', 'hello|hi', 1)"
            )
            sqliteDatabase.version = 1
        }

        val db = Room.databaseBuilder<AppDatabase>(context, file.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .build()
            .also { database = it }

        assertEquals(listOf("hello", "hi"), db.getDictionary().getAll().single().native)
    }
}
