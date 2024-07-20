package global.msnthrp.mokshan.android.koinimpls

import android.content.Context
import global.msnthrp.mokshan.data.storage.base.FileProducer
import okio.Path
import okio.Path.Companion.toOkioPath
import java.io.File

class AndroidFileProducer(private val applicationContext: Context) : FileProducer {

    override fun produce(fileName: String): Path {
        return File(applicationContext.filesDir, fileName).toOkioPath()
    }
}