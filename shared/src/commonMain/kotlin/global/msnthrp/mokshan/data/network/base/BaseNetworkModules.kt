package global.msnthrp.mokshan.data.network.base

import org.koin.dsl.module

/** Koin property that turns on HTTP header logging; the application sets it in debug builds. */
const val HTTP_LOGGING_PROPERTY = "mokshan.network.logHeaders"

val baseNetworkModule = module {
    single { createHttpClient(logHeaders = getProperty(HTTP_LOGGING_PROPERTY, false)) }
    single {
        NetworkClient(
            client = get(),
            cacheStorage = get(),
        )
    }
}
