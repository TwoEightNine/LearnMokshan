package global.msnthrp.mokshan.data.storage

import global.msnthrp.mokshan.data.repository.dictionary.toDomain
import global.msnthrp.mokshan.data.repository.dictionary.toEntities
import global.msnthrp.mokshan.data.repository.dictionary.toEntity
import global.msnthrp.mokshan.data.storage.base.Converters
import global.msnthrp.mokshan.data.storage.dictionary.DictionaryEntity
import global.msnthrp.mokshan.domain.dictionary.DictionaryEntry
import kotlin.random.Random
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
    fun emptyList_roundTrips() {
        assertEquals("", Converters.fromListOfStrings(emptyList()))
        assertEquals(emptyList(), Converters.toListOfStrings(""))
    }

    @Test
    fun listWithOnlyAnEmptyItem_isStoredLikeAnEmptyList() {
        // the single ambiguous case: "" cannot be told apart from "no items"
        assertEquals("", Converters.fromListOfStrings(listOf("")))
    }

    @Test
    fun emptyItemsBetweenOthers_roundTrip() {
        val list = listOf("a", "", "b", "")

        assertEquals("a||b|", Converters.fromListOfStrings(list))
        assertEquals(list, Converters.toListOfStrings("a||b|"))
    }

    @Test
    fun valueWithPipe_isEscapedAndRoundTrips() {
        val list = listOf("a|b", "c")

        assertEquals("a\\|b|c", Converters.fromListOfStrings(list))
        assertEquals(list, Converters.toListOfStrings(Converters.fromListOfStrings(list)))
    }

    @Test
    fun valueWithBackslash_isEscapedAndRoundTrips() {
        listOf(
            listOf("a\\b"), listOf("trailing\\"), listOf("\\"), listOf("\\|"), listOf("\\\\|\\"),
            listOf("x\\", "y"), listOf("|", "|"),
        ).forEach { list ->
            assertEquals(list, Converters.toListOfStrings(Converters.fromListOfStrings(list)), list.toString())
        }
    }

    @Test
    fun rowsWrittenBeforeEscapingExisted_areReadUnchanged() {
        assertEquals(listOf("hello", "hi"), Converters.toListOfStrings("hello|hi"))
        assertEquals(listOf("how are you?", "привет!"), Converters.toListOfStrings("how are you?|привет!"))
        // a lone backslash that does not escape anything stays a normal character
        assertEquals(listOf("a\\b"), Converters.toListOfStrings("a\\b"))
    }

    @Test
    fun anyListOfRegularAndSpecialCharacters_roundTrips() {
        val alphabet = listOf('a', 'б', ' ', '|', '\\', '"')
        val random = Random(7)
        repeat(3000) {
            val list = List(random.nextInt(0, 5)) {
                String(CharArray(random.nextInt(0, 6)) { alphabet[random.nextInt(alphabet.size)] })
            }
            if (list == listOf("")) return@repeat

            assertEquals(list, Converters.toListOfStrings(Converters.fromListOfStrings(list)), list.toString())
        }
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
