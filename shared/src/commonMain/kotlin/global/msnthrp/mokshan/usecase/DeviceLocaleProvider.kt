package global.msnthrp.mokshan.usecase

import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage

interface DeviceLocaleProvider {
    fun getDeviceLocale(): ForeignLanguage
}