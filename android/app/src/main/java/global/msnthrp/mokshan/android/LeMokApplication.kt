package global.msnthrp.mokshan.android

import android.app.Application
import global.msnthrp.mokshan.android.koinimpls.DeviceLocaleProviderImpl
import global.msnthrp.mokshan.data.repository.repositoryModule
import global.msnthrp.mokshan.usecase.DeviceLocaleProvider
import global.msnthrp.mokshan.usecase.useCaseModule
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.dsl.module
import kotlin.math.sin

class LeMokApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            loadKoinModules(
                listOf(
                    repositoryModule,
                    useCaseModule,
                    module {
                        single<DeviceLocaleProvider> { DeviceLocaleProviderImpl() }
                    }
                )
            )
        }
    }
}