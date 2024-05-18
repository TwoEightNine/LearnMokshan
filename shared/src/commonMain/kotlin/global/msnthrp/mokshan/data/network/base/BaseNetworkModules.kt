package global.msnthrp.mokshan.data.network.base

import org.koin.dsl.module

val baseNetworkModule = module {
    single { client }
}