package global.msnthrp.mokshan.data.storage.base

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import okio.Path

typealias Prefs = DataStore<Preferences>

fun Prefs(fileName: String, fileProducer: FileProducer): Prefs {
    return PreferenceDataStoreFactory.createWithPath(
        // an unreadable file would make every read fail forever; start over instead
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        produceFile = { fileProducer.produce(fileName) }
    )
}

fun interface FileProducer {
    fun produce(fileName: String): Path
}