package global.msnthrp.mokshan.android.features.lessons.topic

import global.msnthrp.mokshan.domain.lessons.TopicInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class TopicInfoExtensionsTest {

    private fun info(title: String, emoji: String? = null) = TopicInfo(
        id = 1,
        lessonsCount = 2,
        title = title,
        description = "d",
        emoji = emoji,
        grammar = null,
    )

    @Test
    fun emoji_isShownWhenPresent() {
        assertEquals("✋", info("Greeting", emoji = "✋").badgeText())
        assertEquals("7️⃣", info("Count", emoji = "7️⃣").badgeText())
    }

    @Test
    fun withoutEmoji_firstLetterOfTitleIsShownUppercase() {
        assertEquals("G", info("greeting").badgeText())
        assertEquals("П", info("приветствие").badgeText())
    }

    @Test
    fun blankEmoji_fallsBackToTitleLetter() {
        assertEquals("G", info("Greeting", emoji = "").badgeText())
        assertEquals("G", info("Greeting", emoji = "   ").badgeText())
    }

    @Test
    fun leadingWhitespaceOfTitle_isSkipped() {
        assertEquals("F", info("  food").badgeText())
    }

    @Test
    fun blankTitleWithoutEmoji_givesEmptyBadgeInsteadOfCrashing() {
        assertEquals("", info("").badgeText())
        assertEquals("", info("   ").badgeText())
    }

    @Test
    fun blankTitleWithEmoji_stillShowsEmoji() {
        assertEquals("✋", info("", emoji = "✋").badgeText())
    }

    @Test
    fun titleStartingWithSupplementaryCharacter_isNotCutInHalf() {
        assertEquals("\uD83D\uDE00", info("\uD83D\uDE00 smile").badgeText())
        assertEquals("\uD801\uDC00", info("\uD801\uDC28x").badgeText()) // Deseret letter, upper case form
    }
}
