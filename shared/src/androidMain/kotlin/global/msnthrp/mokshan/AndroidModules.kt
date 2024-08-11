package global.msnthrp.mokshan

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import global.msnthrp.mokshan.data.network.base.AndroidCacheStorage
import global.msnthrp.mokshan.data.network.base.NetworkClient
import global.msnthrp.mokshan.data.storage.base.AppDatabase
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val androidModules = module {
    single<NetworkClient.CacheStorage> {
        AndroidCacheStorage(
            applicationContext = get()
        )
    }
    single<AppDatabase> {
        val applicationContext: Context = get()
        Room.databaseBuilder<AppDatabase>(
            context = applicationContext,
            name = applicationContext.getDatabasePath("main.db").absolutePath,
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}