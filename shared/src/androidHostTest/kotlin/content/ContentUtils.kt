package content

import java.nio.file.Paths
import okio.Path
import okio.Path.Companion.toPath

actual fun getContentDir(): Path {
    return Paths.get("").toAbsolutePath().toString()
        .split("/")
        .dropLast(1)
        .plus("content")
        .joinToString(separator = "/")
        .toPath()
}
