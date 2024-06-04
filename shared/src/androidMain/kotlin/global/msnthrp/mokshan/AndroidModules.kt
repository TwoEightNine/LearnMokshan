package global.msnthrp.mokshan

import global.msnthrp.mokshan.data.network.base.AndroidCacheStorage
import global.msnthrp.mokshan.data.network.base.NetworkClient
import org.koin.dsl.module

val androidModules = module {
    single<NetworkClient.CacheStorage> {
        AndroidCacheStorage(
            applicationContext = get()
        )
    }
}