package content

import okio.Path
import okio.Path.Companion.toPath
import java.nio.file.Paths


actual fun getContentDir(): Path {
    return Paths.get("").toAbsolutePath().toString()
        .split("/")
        .dropLast(1)
        .plus("content")
        .joinToString(separator = "/")
        .toPath()
}