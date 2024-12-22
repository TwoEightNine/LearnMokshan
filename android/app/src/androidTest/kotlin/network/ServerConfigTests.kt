package network

import androidx.test.ext.junit.runners.AndroidJUnit4
import global.msnthrp.mokshan.data.network.ServerConfig
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.java.KoinJavaComponent.get

@RunWith(AndroidJUnit4::class)
class ServerConfigTests {

    @Test
    fun checkServerConfig_shouldBeSetUpOnProd() {
        val serverConfig = get<ServerConfig>(ServerConfig::class.java)
        val indexUrls = listOf(
            serverConfig.getTopicsIndexUrl("en"),
            serverConfig.getArticlesIndexUrl("en")
        )

        indexUrls.forEach { indexUrl ->
            assert("sandbox" !in indexUrl) { indexUrl }
        }
    }
}