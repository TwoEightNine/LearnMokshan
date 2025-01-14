package global.msnthrp.mokshan.android

import android.app.Application
import global.msnthrp.learmokshan.android.features.dictionary.dictionaryModule
import global.msnthrp.learnmokshan.BuildConfig
import global.msnthrp.mokshan.android.core.utils.LeMokBuildConfig
import global.msnthrp.mokshan.android.features.articles.articlesModule
import global.msnthrp.mokshan.android.features.lessons.lessonsModules
import global.msnthrp.mokshan.android.koinimpls.AndroidFileProducer
import global.msnthrp.mokshan.android.koinimpls.AndroidNowProvider
import global.msnthrp.mokshan.android.koinimpls.DeviceLocaleProviderImpl
import global.msnthrp.mokshan.android.koinimpls.LeMokBuildConfigImpl
import global.msnthrp.mokshan.android.koinimpls.ProdServerConfig
import global.msnthrp.mokshan.android.koinimpls.SandboxServerConfig
import global.msnthrp.mokshan.androidModules
import global.msnthrp.mokshan.data.network.ServerConfig
import global.msnthrp.mokshan.data.repository.repositoryModule
import global.msnthrp.mokshan.data.storage.base.FileProducer
import global.msnthrp.mokshan.usecase.DeviceLocaleProvider
import global.msnthrp.mokshan.usecase.useCaseModule
import global.msnthrp.mokshan.utils.NowProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.dsl.module

class LeMokApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LeMokApplication)
            loadKoinModules(
                listOf(
                    repositoryModule,
                    useCaseModule,
                    androidModules,

                    articlesModule,
                    lessonsModules,
                    dictionaryModule,

                    module {
                        single<DeviceLocaleProvider> { DeviceLocaleProviderImpl() }
                        single<LeMokBuildConfig> { LeMokBuildConfigImpl() }
                        single<FileProducer> { AndroidFileProducer(applicationContext = get()) }
                        single<NowProvider> { AndroidNowProvider() }

                        if (BuildConfig.DEBUG) {
                            single<ServerConfig> { SandboxServerConfig(ProdServerConfig()) }
                        } else {
                            single<ServerConfig> { ProdServerConfig() }
                        }
                    }
                )
            )
        }
    }
}