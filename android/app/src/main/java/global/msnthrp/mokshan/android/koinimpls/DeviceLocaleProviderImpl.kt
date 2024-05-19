package global.msnthrp.mokshan.android.koinimpls

import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage
import global.msnthrp.mokshan.usecase.DeviceLocaleProvider
import java.util.Locale

class DeviceLocaleProviderImpl : DeviceLocaleProvider {
    override fun getDeviceLocale(): ForeignLanguage {
        val locale = Locale.getDefault().language
        return when (locale) {
            Locale("ru").language -> ForeignLanguage.RUSSIAN
            else -> ForeignLanguage.ENGLISH
        }
    }
}