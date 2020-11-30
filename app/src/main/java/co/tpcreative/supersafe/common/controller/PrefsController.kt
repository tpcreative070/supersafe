package co.tpcreative.supersafe.common.controller

import android.annotation.TargetApi
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.os.Build
import android.text.TextUtils
import java.util.*

object PrefsController {
    private val DEFAULT_SUFFIX: String? = "_preferences"
    private val LENGTH: String? = "#LENGTH"
    private var mPrefs: SharedPreferences? = null
    private var mContext: Context? = null
    private val TAG = PrefsController::class.java.simpleName

    /**
     * Initialize the Prefs helper class to keep a reference to the SharedPreference for this
     * application the SharedPreference will use the package thumbnailName of the application as the Key.
     *
     *
     * This method is deprecated please us the new builder.
     *
     * @param context the Application context.
     */
    @Deprecated("")
    fun initPrefs(context: Context?) {
        Builder().setContext(context)?.build()
        mContext = context
    }

    /**
     * @hide
     */
    private fun initPrefs(context: Context?, prefsName: String?, mode: Int) {
        mPrefs = context?.getSharedPreferences(prefsName, mode)
        mContext = context
    }

    fun getContext(): Context? {
        return mContext
    }

    /**
     * Returns an instance of the shared preference for this app.
     *
     * @return an Instance of the SharedPreference
     * @throws RuntimeException if sharedpreference instance has not been instatiated yet.
     */
    fun getPreferences(): SharedPreferences? {
        if (mPrefs != null) {
            return mPrefs
        }
        throw RuntimeException(
                "Prefs class not correctly instantiated please call Builder.setContext().build(); in the Application class onCreate.")
    }

    /**
     * @return Returns a map containing a list of pairs key/value representing
     * the preferences.
     * @see SharedPreferences.getAll
     */
    fun getAll(): MutableMap<String?, *>? {
        return getPreferences()?.getAll()
    }

    /**
     * @param key      The thumbnailName of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this thumbnailName that is not
     * an int.
     * @see SharedPreferences.getInt
     */
    fun getInt(key: String?, defValue: Int): Int {
        return getPreferences()?.getInt(key, defValue) ?: 0
    }

    /**
     * @param key      The thumbnailName of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this thumbnailName that is not
     * a boolean.
     * @see SharedPreferences.getBoolean
     */
    fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return getPreferences()?.getBoolean(key, defValue) ?: false
    }

    /**
     * @param key      The thumbnailName of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this thumbnailName that is not
     * a long.
     * @see SharedPreferences.getLong
     */
    fun getLong(key: String?, defValue: Long): Long {
        return getPreferences()?.getLong(key, defValue) ?: 0
    }

    /**
     * Returns the double that has been saved as a long raw bits value in the long preferences.
     *
     * @param key      The thumbnailName of the preference to retrieve.
     * @param defValue the double Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this thumbnailName that is not
     * a long.
     * @see SharedPreferences.getLong
     */
    fun getDouble(key: String?, defValue: Double): Double {
        return java.lang.Double.longBitsToDouble(getPreferences()?.getLong(key, java.lang.Double.doubleToLongBits(defValue)) ?: 0)
    }

    /**
     * @param key      The thumbnailName of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this thumbnailName that is not
     * a float.
     * @see SharedPreferences.getFloat
     */
    fun getFloat(key: String?, defValue: Float): Float {
        return getPreferences()?.getFloat(key, defValue) ?: 0f
    }

    /**
     * @param key      The thumbnailName of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this thumbnailName that is not
     * a String.
     * @see SharedPreferences.getString
     */
    fun getString(key: String?, defValue: String?): String? {
        return getPreferences()?.getString(key, defValue)
    }

    /**
     * @param key      The thumbnailName of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return Returns the preference values if they exist, or defValues.
     * Throws ClassCastException if there is a preference with this thumbnailName
     * that is not a Set.
     * @see SharedPreferences.getStringSet
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    fun getStringSet(key: String?, defValue: MutableSet<String?>?): MutableSet<String?>? {
        val prefs: SharedPreferences? = getPreferences()
        return prefs?.getStringSet(key, defValue)
        return defValue
    }

    /**
     * @param key   The thumbnailName of the preference to modify.
     * @param value The new value for the preference.
     * @see Editor.putLong
     */
    fun putLong(key: String?, value: Long) {
        val editor: SharedPreferences.Editor? = getPreferences()?.edit()
        editor?.putLong(key, value)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor?.commit()
        } else {
            editor?.apply()
        }
    }

    /**
     * @param key   The thumbnailName of the preference to modify.
     * @param value The new value for the preference.
     * @see Editor.putInt
     */
    fun putInt(key: String?, value: Int) {
        val editor: SharedPreferences.Editor? = getPreferences()?.edit()
        editor?.putInt(key, value)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor?.commit()
        } else {
            editor?.apply()
        }
    }

    /**
     * Saves the double as a long raw bits inside the preferences.
     *
     * @param key   The thumbnailName of the preference to modify.
     * @param value The double value to be save in the preferences.
     * @see Editor.putLong
     */
    fun putDouble(key: String?, value: Double) {
        val editor: SharedPreferences.Editor? = getPreferences()?.edit()
        editor?.putLong(key, java.lang.Double.doubleToRawLongBits(value))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor?.commit()
        } else {
            editor?.apply()
        }
    }

    /**
     * @param key   The thumbnailName of the preference to modify.
     * @param value The new value for the preference.
     * @see Editor.putFloat
     */
    fun putFloat(key: String?, value: Float) {
        val editor: SharedPreferences.Editor? = getPreferences()?.edit()
        editor?.putFloat(key, value)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor?.commit()
        } else {
            editor?.apply()
        }
    }

    /**
     * @param key   The thumbnailName of the preference to modify.
     * @param value The new value for the preference.
     * @see Editor.putBoolean
     */
    fun putBoolean(key: String?, value: Boolean) {
        val editor: SharedPreferences.Editor? = getPreferences()?.edit()
        editor?.putBoolean(key, value)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor?.commit()
        } else {
            editor?.apply()
        }
    }

    /**
     * @param key   The thumbnailName of the preference to modify.
     * @param value The new value for the preference.
     * @see Editor.putString
     */
    fun putString(key: String?, value: String?) {
        val editor: SharedPreferences.Editor? = getPreferences()?.edit()
        editor?.putString(key, value)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor?.commit()
        } else {
            editor?.apply()
        }
    }

    /**
     * @param key   The thumbnailName of the preference to modify.
     * @param value The new value for the preference.
     * @see Editor.putStringSet
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    fun putStringSet(key: String?, value: MutableSet<String?>?) {
        val editor: SharedPreferences.Editor? = getPreferences()?.edit()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            editor?.putStringSet(key, value)
        } else {
            // Workaround for pre-HC's lack of StringSets
            var stringSetLength = 0
            if (mPrefs?.contains(key + LENGTH) == true) {
                // First read what the value was
                stringSetLength = mPrefs?.getInt(key + LENGTH, -1) ?: 0
            }
            editor?.putInt(key + LENGTH, value?.size ?: 0)
            var i = 0
            if (value != null) {
                for (aValue in value) {
                    editor?.putString("$key[$i]", aValue)
                    i++
                }
            }
            while (i < stringSetLength) {

                // Remove any remaining values
                editor?.remove("$key[$i]")
                i++
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor?.commit()
        } else {
            editor?.apply()
        }
    }

    /**
     * @param key The thumbnailName of the preference to remove.
     * @see Editor.remove
     */
    fun remove(key: String?) {
        val prefs: SharedPreferences? = getPreferences()
        val editor: SharedPreferences.Editor? = prefs?.edit()
        if (prefs?.contains(key + LENGTH) == true) {
            // Workaround for pre-HC's lack of StringSets
            val stringSetLength: Int = prefs.getInt(key + LENGTH, -1)
            if (stringSetLength >= 0) {
                editor?.remove(key + LENGTH)
                for (i in 0 until stringSetLength) {
                    editor?.remove("$key[$i]")
                }
            }
        }
        editor?.remove(key)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor?.commit()
        } else {
            editor?.apply()
        }
    }

    /**
     * @param key The thumbnailName of the preference to check.
     * @return true if preference contains this key value.
     * @see SharedPreferences.contains
     */
    operator fun contains(key: String?): Boolean {
        return getPreferences()?.contains(key) ?: false
    }

    /**
     * @return the [Editor] for chaining. The changes have already been committed/applied through the execution of this method.
     * @see Editor.clear
     */
    fun clear(): SharedPreferences.Editor? {
        val editor: SharedPreferences.Editor? = getPreferences()?.edit()?.clear()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor?.commit()
        } else {
            editor?.apply()
        }
        return editor
    }

    fun edit(): SharedPreferences.Editor? {
        return getPreferences()?.edit()
    }

    /**
     * Builder class for the EasyPrefs instance. You only have to call this once in the Application onCreate. And in the rest of the code base you can call Prefs.method thumbnailName.
     */
    class Builder {
        private var mKey: String? = null
        private var mContext: Context? = null
        private var mMode = -1
        private var mUseDefault = false

        /**
         * Set the filename of the sharedprefence instance  usually this is the applications packagename.xml but for migration purposes or customization.
         *
         * @param prefsName the filename used for the sharedpreference
         * @return the [com.pixplicity.easyprefs.library.Prefs.Builder] object.
         */
        fun setPrefsName(prefsName: String?): Builder? {
            mKey = prefsName
            return this
        }

        /**
         * Set the context used to instantiate the sharedpreferences
         *
         * @param context the application context
         * @return the [com.pixplicity.easyprefs.library.Prefs.Builder] object.
         */
        fun setContext(context: Context?): Builder? {
            mContext = context
            return this
        }

        /**
         * Set the mode of the sharedpreference instance.
         *
         * @param mode Operating mode.  Use 0 or [Context.MODE_PRIVATE] for the
         * default operation, [Context.MODE_WORLD_READABLE]
         * @return the [com.pixplicity.easyprefs.library.Prefs.Builder] object.
         * @see Context.getSharedPreferences
         */
        fun setMode(mode: Int): Builder? {
            mMode = if (mode == ContextWrapper.MODE_PRIVATE || mode == ContextWrapper.MODE_WORLD_READABLE || mode == ContextWrapper.MODE_WORLD_WRITEABLE || mode == ContextWrapper.MODE_MULTI_PROCESS) {
                mode
            } else {
                throw RuntimeException("The mode in the sharedpreference can only be set too ContextWrapper.MODE_PRIVATE, ContextWrapper.MODE_WORLD_READABLE, ContextWrapper.MODE_WORLD_WRITEABLE or ContextWrapper.MODE_MULTI_PROCESS")
            }
            return this
        }

        /**
         * Set the default sharedpreference file thumbnailName. Often the package thumbnailName of the application is used, but if the [android.preference.PreferenceActivity] or [android.preference.PreferenceFragment] is used android append that with _preference.
         *
         * @param defaultSharedPreference true if default sharedpreference thumbnailName should used.
         * @return the [com.pixplicity.easyprefs.library.Prefs.Builder] object.
         */
        fun setUseDefaultSharedPreference(defaultSharedPreference: Boolean): Builder? {
            mUseDefault = defaultSharedPreference
            return this
        }

        /**
         * Initialize the sharedpreference instance to used in the application.
         *
         * @throws RuntimeException if context has not been set.
         */
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