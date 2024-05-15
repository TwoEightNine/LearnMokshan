package global.msnthrp.mokshan

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform