package global.msnthrp.mokshan.android.koinimpls

import global.msnthrp.mokshan.utils.NowProvider

class AndroidNowProvider : NowProvider {
    override fun getNow(): Long {
        return System.currentTimeMillis()
    }
}