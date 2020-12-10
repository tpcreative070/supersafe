package co.tpcreative.supersafe.common.controller
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.text.TextUtils
import co.tpcreative.supersafe.common.services.SuperSafeApplication

object PrefsController {
    private val DEFAULT_SUFFIX: String = "_preferences"
    private var mPrefs: SharedPreferences? = null
    private var mContext: Context? = null
    private val LENGTH: String = "#LENGTH"
    private val TAG = PrefsController::class.java.simpleName

    private fun initPrefs(context: Context?, prefsName: String?, mode: Int) {
        mPrefs = context?.getSharedPreferences(prefsName, mode)
        mContext = context
        if (SuperSafeApplication.getInstance().isLiveMigration()){
            context?.let {
                AppPrefs.initEncryptedPrefs(it)
                AppPrefs.initPrefs(it)
            }
        }
    }

    /*This is old*/
    private fun getOldInt(key: String?, defValue: Int): Int {
        return getPreferences()?.getInt(key, defValue) ?: 0
    }

    private fun getOldBoolean(key: String?, defValue: Boolean): Boolean {
        return getPreferences()?.getBoolean(key, defValue) ?: false
    }

    private fun getOldLong(key: String?, defValue: Long): Long {
        return getPreferences()?.getLong(key, defValue) ?: 0
    }

    private fun getOldDouble(key: String?, defValue: Double): Double {
        return java.lang.Double.longBitsToDouble(getPreferences()?.getLong(key, java.lang.Double.doubleToLongBits(defValue)) ?: 0)
    }

    private fun getOldFloat(key: String?, defValue: Float): Float {
        return getPreferences()?.getFloat(key, defValue) ?: 0f
    }

    private fun getOldString(key: String?, defValue: String?): String? {
        return getPreferences()?.getString(key, defValue)
    }

    private fun putOldLong(key: String?, value: Long) {
        val editor: SharedPreferences.Editor? = getPreferences()?.edit()
        editor?.putLong(key, value)
        editor?.apply()
    }

    private fun putOldInt(key: String?, value: Int) {
        val editor: SharedPreferences.Editor? = getPreferences()?.edit()
        editor?.putInt(key, value)
        editor?.apply()
    }

    private fun putOldDouble(key: String?, value: Double) {
        val editor: SharedPreferences.Editor? = getPreferences()?.edit()
        editor?.putLong(key, java.lang.Double.doubleToRawLongBits(value))
        editor?.apply()
    }

    private fun putOldFloat(key: String?, value: Float) {
        val editor: SharedPreferences.Editor? = getPreferences()?.edit()
        editor?.putFloat(key, value)
        editor?.apply()
    }

    private fun putOldBoolean(key: String?, value: Boolean) {
        val editor: SharedPreferences.Editor? = getPreferences()?.edit()
        editor?.putBoolean(key, value)
        editor?.apply()
    }

    private fun putOldString(key: String?, value: String?) {
        val editor: SharedPreferences.Editor? = getPreferences()?.edit()
        editor?.putString(key, value)
        editor?.apply()
    }

    private fun getPreferences(): SharedPreferences? {
        if (mPrefs != null) {
            return mPrefs
        }
        throw RuntimeException(
                "Prefs class not correctly instantiated please call Builder.setContext().build(); in the Application class onCreate.")
    }

    fun getInt(key: String?, defValue: Int): Int {
        return if (SuperSafeApplication.getInstance().isLiveMigration()){
            if (contains(key)){
                val mResult = getOldInt(key,defValue)
                putInt(key,mResult)
                removeOldPreKey(key)
                mResult
            }else{
                AppPrefs.encryptedPrefs.read(key,defValue)
            }
        }else{
            getOldInt(key,defValue)
        }
    }

    fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return if (SuperSafeApplication.getInstance().isLiveMigration()){
            if (contains(key)){
                val mResult = getOldBoolean(key,defValue)
                putBoolean(key,mResult)
                removeOldPreKey(key)
                mResult
            }else{
                AppPrefs.encryptedPrefs.read(key,defValue)
            }
        }else{
            getOldBoolean(key,defValue)
        }
    }

    fun getLong(key: String?, defValue: Long): Long {
        return if (SuperSafeApplication.getInstance().isLiveMigration()){
            if (contains(key)){
                val mResult = getOldLong(key,defValue)
                putLong(key,mResult)
                removeOldPreKey(key)
                mResult
            }else{
                AppPrefs.encryptedPrefs.read(key,defValue)
            }
        }else{
            getOldLong(key,defValue)
        }
    }

    fun getDouble(key: String?, defValue: Double): Double {
        return if (SuperSafeApplication.getInstance().isLiveMigration()){
            if (contains(key)){
                val mResult = getOldDouble(key,defValue)
                putDouble(key,mResult)
                removeOldPreKey(key)
                mResult
            }else{
                AppPrefs.encryptedPrefs.read(key,defValue)
            }
        }else{
            getOldDouble(key,defValue)
        }
    }

    fun getFloat(key: String?, defValue: Float): Float {
        return if (SuperSafeApplication.getInstance().isLiveMigration()){
            if (contains(key)){
                val mResult = getOldFloat(key,defValue)
                putFloat(key,mResult)
                removeOldPreKey(key)
                mResult
            }else{
                AppPrefs.encryptedPrefs.read(key,defValue)
            }
        }else{
            getOldFloat(key,defValue)
        }
    }

    fun getString(key: String?, defValue: String?): String? {
        return if (SuperSafeApplication.getInstance().isLiveMigration()){
            if (contains(key)){
                val mResult = getOldString(key,defValue)
                putString(key,mResult)
                removeOldPreKey(key)
                mResult
            }else{
                AppPrefs.encryptedPrefs.read(key,defValue)
            }
        }else{
            getOldString(key,defValue)
        }
    }

    fun putLong(key: String?, value: Long) {
        if (SuperSafeApplication.getInstance().isLiveMigration()){
            if (contains(key)){
                removeOldPreKey(key)
            }
            AppPrefs.encryptedPrefs.write(key,value)
        }else{
            putOldLong(key,value)
        }
    }

    fun putInt(key: String?, value: Int) {
        if (SuperSafeApplication.getInstance().isLiveMigration()){
            if (contains(key)){
                removeOldPreKey(key)
            }
            AppPrefs.encryptedPrefs.write(key,value)
        }else{
            putOldInt(key,value)
        }
    }

    fun putDouble(key: String?, value: Double) {
        if (SuperSafeApplication.getInstance().isLiveMigration()){
            if (contains(key)){
                removeOldPreKey(key)
            }
            AppPrefs.encryptedPrefs.write(key,value)
        }else{
            putOldDouble(key,value)
        }
    }

    fun putFloat(key: String?, value: Float) {
        if (SuperSafeApplication.getInstance().isLiveMigration()){
            if (contains(key)){
                removeOldPreKey(key)
            }
            AppPrefs.encryptedPrefs.write(key,value)
        }else{
            putOldFloat(key,value)
        }
    }

    fun putBoolean(key: String?, value: Boolean) {
        if (SuperSafeApplication.getInstance().isLiveMigration()){
            if (contains(key)){
                removeOldPreKey(key)
            }
            AppPrefs.encryptedPrefs.write(key,value)
        }else{
            putOldBoolean(key,value)
        }
    }

    fun putString(key: String?, value: String?) {
        if (SuperSafeApplication.getInstance().isLiveMigration()){
            if (contains(key)){
                removeOldPreKey(key)
            }
            AppPrefs.encryptedPrefs.write(key,value)
        }else{
            putOldString(key,value)
        }
    }

    private fun removeOldPreKey(key: String?) {
        val prefs: SharedPreferences? = getPreferences()
        val editor: SharedPreferences.Editor? = prefs?.edit()
        if (prefs?.contains(key + LENGTH) == true) {
            val stringSetLength: Int = prefs.getInt(key + LENGTH, -1)
            if (stringSetLength >= 0) {
                editor?.remove(key + LENGTH)
                for (i in 0 until stringSetLength) {
                    editor?.remove("$key[$i]")
                }
            }
        }
        editor?.remove(key)
        editor?.apply()
    }

    operator fun contains(key: String?): Boolean {
        return getPreferences()?.contains(key) ?: false
    }

    fun clear(){
        if (SuperSafeApplication.getInstance().isLiveMigration()){
            AppPrefs.encryptedPrefs.clear()
        }else{
            val editor: SharedPreferences.Editor? = getPreferences()?.edit()?.clear()
            editor?.apply()
        }
    }

    class Builder {
        private var mKey: String? = null
        private var mContext: Context? = null
        private var mMode = -1
        private var mUseDefault = false

        fun setPrefsName(prefsName: String?): Builder {
            mKey = prefsName
            return this
        }

        fun setContext(context: Context?): Builder {
            mContext = context
            return this
        }

        fun setMode(mode: Int): Builder {
            mMode = if (mode == ContextWrapper.MODE_PRIVATE) {
                mode
            } else {
                throw RuntimeException("The mode in the sharedpreference can only be set ContextWrapper.MODE_PRIVATE")
            }
            return this
        }

        fun setUseDefaultSharedPreference(defaultSharedPreference: Boolean): Builder {
            mUseDefault = defaultSharedPreference
            return this
        }

        fun build() {
            if (mContext == null) {
                throw RuntimeException("Context not set, please set context before building the Prefs instance.")
            }
            if (TextUtils.isEmpty(mKey)) {
                mKey = mContext?.packageName
            }
            if (mUseDefault) {
                mKey += DEFAULT_SUFFIX
            }
            if (mMode == -1) {
                mMode = ContextWrapper.MODE_PRIVATE
            }
            initPrefs(mContext, mKey, mMode)
        }
    }
}