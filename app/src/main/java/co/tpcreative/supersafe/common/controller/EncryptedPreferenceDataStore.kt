package co.tpcreative.supersafe.common.controller
import android.content.Context
import androidx.preference.PreferenceDataStore


class EncryptedPreferenceDataStore private constructor(context: Context) : PreferenceDataStore() {
    companion object {
        @Volatile private var INSTANCE: EncryptedPreferenceDataStore? = null
        fun getInstance(context: Context): EncryptedPreferenceDataStore =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: EncryptedPreferenceDataStore(context).also { INSTANCE = it }
                }
    }

    override fun putString(key: String, value: String?) {
        AppPrefs.encryptedPrefs.write(key, value ?: "")
    }

    override fun putStringSet(key: String, values: Set<String>?) {
        AppPrefs.encryptedPrefs.write(key, values ?: setOf())
    }

    override fun putInt(key: String, value: Int) {
        AppPrefs.encryptedPrefs.write(key, value)
    }

    override fun putLong(key: String, value: Long) {
        AppPrefs.encryptedPrefs.write(key, value)
    }

    override fun putFloat(key: String, value: Float) {
        AppPrefs.encryptedPrefs.write(key, value)
    }

    override fun putBoolean(key: String, value: Boolean) {
        AppPrefs.encryptedPrefs.write(key, value)
    }

    override fun getString(key: String, defValue: String?): String? {
        return AppPrefs.encryptedPrefs.read(key, defValue ?: "")
    }

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        return AppPrefs.encryptedPrefs.read(key, defValues ?: setOf())
    }

    override fun getInt(key: String, defValue: Int): Int {
        return AppPrefs.encryptedPrefs.read(key, defValue)
    }

    override fun getLong(key: String, defValue: Long): Long {
        return AppPrefs.encryptedPrefs.read(key, defValue)
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return AppPrefs.encryptedPrefs.read(key, defValue)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return AppPrefs.encryptedPrefs.read(key, defValue)
    }
}