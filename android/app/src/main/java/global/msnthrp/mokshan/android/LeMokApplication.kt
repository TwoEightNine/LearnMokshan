package global.msnthrp.mokshan.android

import android.app.Application
import global.msnthrp.mokshan.data.repository.repositoryModule
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin

class LeMokApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            loadKoinModules(
                listOf(
                    repositoryModule,
                )
            )
        }
    }
}