package global.msnthrp.mokshan.data.storage.base

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path

typealias Prefs = DataStore<Preferences>

fun Prefs(fileName: String, fileProducer: FileProducer): Prefs {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { fileProducer.produce(fileName) }
    )
}

fun interface FileProducer {
    fun produce(fileName: String): Path
}