package global.msnthrp.mokshan.data.storage

import global.msnthrp.mokshan.data.repository.dictionary.toDomain
import global.msnthrp.mokshan.data.repository.dictionary.toEntities
import global.msnthrp.mokshan.data.repository.dictionary.toEntity
import global.msnthrp.mokshan.data.storage.base.Converters
import global.msnthrp.mokshan.data.storage.dictionary.DictionaryEntity
import global.msnthrp.mokshan.domain.dictionary.DictionaryEntry
import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertersTest {

    @Test
    fun listIsJoinedByPipe() {
        assertEquals("hello|hi", Converters.fromListOfStrings(listOf("hello", "hi")))
        assertEquals("hello", Converters.fromListOfStrings(listOf("hello")))
    }

    @Test
    fun stringIsSplitByPipe() {
        assertEquals(listOf("hello", "hi"), Converters.toListOfStrings("hello|hi"))
        assertEquals(listOf("hello"), Converters.toListOfStrings("hello"))
    }

    @Test
    fun regularListsRoundTrip() {
        val list = listOf("how are you?", "how do you do?", "привет!")

        assertEquals(list, Converters.toListOfStrings(Converters.fromListOfStrings(list)))
    }

    @Test
    fun emptyListDoesNotRoundTrip_knownBugB16() {
        // KNOWN BUG B16: "" is split into a single empty string
        assertEquals("", Converters.fromListOfStrings(emptyList()))
        assertEquals(listOf(""), Converters.toListOfStrings(""))
    }

    @Test
    fun valueWithPipeDoesNotRoundTrip_knownBugB16() {
        assertEquals(listOf("a", "b"), Converters.toListOfStrings(Converters.fromListOfStrings(listOf("a|b"))))
    }

    @Test
    fun entityToDomain_keepsAllFieldsAndDropsId() {
        val entity = DictionaryEntity(id = 7, mokshan = "лац", native = listOf("fine"), addedTs = 5)

        assertEquals(DictionaryEntry("лац", listOf("fine"), 5), listOf(entity).toDomain().single())
    }

    @Test
    fun domainToEntity_hasGeneratedIdPlaceholder() {
        val entry = DictionaryEntry("лац", listOf("fine"), 5)

        assertEquals(DictionaryEntity(0, "лац", listOf("fine"), 5), entry.toEntity())
        assertEquals(listOf(entry.toEntity()), listOf(entry).toEntities())
    }

    @Test
    fun emptyListsConvertToEmptyLists() {
        assertEquals(emptyList(), emptyList<DictionaryEntity>().toDomain())
        assertEquals(emptyList(), emptyList<DictionaryEntry>().toEntities())
    }
}
