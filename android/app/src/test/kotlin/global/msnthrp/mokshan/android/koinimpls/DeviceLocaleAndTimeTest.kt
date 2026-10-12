package global.msnthrp.mokshan.android.koinimpls

import global.msnthrp.mokshan.domain.phrasebook.ForeignLanguage
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

class DeviceLocaleAndTimeTest {

    private lateinit var originalLocale: Locale

    @Before
    fun saveLocale() {
        originalLocale = Locale.getDefault()
    }

    @After
    fun restoreLocale() {
        Locale.setDefault(originalLocale)
    }

    private fun languageFor(locale: Locale): ForeignLanguage {
        Locale.setDefault(locale)
        return DeviceLocaleProviderImpl().getDeviceLocale()
    }

    @Test
    fun russianDevice_getsRussian() {
        assertEquals(ForeignLanguage.RUSSIAN, languageFor(Locale.forLanguageTag("ru")))
    }

    @Test
    fun russianVariants_getRussian() {
        assertEquals(ForeignLanguage.RUSSIAN, languageFor(Locale.forLanguageTag("ru-RU")))
        assertEquals(ForeignLanguage.RUSSIAN, languageFor(Locale.forLanguageTag("ru-UA")))
    }

    @Test
    fun englishDevice_getsEnglish() {
        assertEquals(ForeignLanguage.ENGLISH, languageFor(Locale.US))
        assertEquals(ForeignLanguage.ENGLISH, languageFor(Locale.UK))
    }

    @Test
    fun anyOtherLanguage_fallsBackToEnglish() {
        listOf("de", "fr", "uk", "be", "tt", "zh-CN", "ar").forEach { tag ->
            assertEquals(tag, ForeignLanguage.ENGLISH, languageFor(Locale.forLanguageTag(tag)))
        }
    }

    @Test
    fun languageIsReadOnEveryCall_notCached() {
        val provider = DeviceLocaleProviderImpl()

        Locale.setDefault(Locale.forLanguageTag("ru"))
        assertEquals(ForeignLanguage.RUSSIAN, provider.getDeviceLocale())
        Locale.setDefault(Locale.US)
        assertEquals(ForeignLanguage.ENGLISH, provider.getDeviceLocale())
    }

    @Test
    fun nowProvider_returnsCurrentSystemTime() {
        val before = System.currentTimeMillis()

        val now = AndroidNowProvider().getNow()

        assertTrue(now in before..System.currentTimeMillis())
    }
}
