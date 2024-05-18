package global.msnthrp.mokshan.data.network

import global.msnthrp.mokshan.data.network.base.baseNetworkModule
import global.msnthrp.mokshan.data.network.phrasebook.phrasebookNetworkModule
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

val networkModules = module {
    loadKoinModules(
        listOf(
            baseNetworkModule,
            phrasebookNetworkModule,
        )
    )
}