package global.msnthrp.mokshan.android.koinimpls

import global.msnthrp.mokshan.android.BuildConfig
import global.msnthrp.mokshan.android.core.utils.LeMokBuildConfig

class LeMokBuildConfigImpl : LeMokBuildConfig {
    override val versionName: String
        get() = BuildConfig.VERSION_NAME
}