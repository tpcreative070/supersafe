package co.tpcreative.supersafe.model
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.ThemeUtil
import co.tpcreative.supersafe.common.util.Utils
import java.io.Serializable
import java.util.*
class ThemeApp : Serializable {
    private var id = 0
    private var primaryColor = 0
    private var primaryDarkColor = 0
    private var accentColor = 0
    var isCheck = false

    constructor() {}
    constructor(primaryColor: Int, primaryDarkColor: Int, accentColor: Int) {
        this.primaryColor = primaryColor
        this.primaryDarkColor = primaryDarkColor
        this.accentColor = accentColor
        isCheck = false
    }

    constructor(id: Int, primaryColor: Int, primaryDarkColor: Int, accentColor: Int) {
        this.id = id
        this.primaryColor = primaryColor
        this.primaryDarkColor = primaryDarkColor
        this.accentColor = accentColor
        isCheck = false
    }

    fun getId(): Int {
        return id
    }

    fun setId(id: Int) {
        this.id = id
    }

    fun getPrimaryColor(): Int {
        return primaryColor
    }

    fun setPrimaryColor(primaryColor: Int) {
        this.primaryColor = primaryColor
    }


    fun getPrimaryDarkColor(): Int {
        return primaryDarkColor
    }

    fun setPrimaryDarkColor(primaryDarkColor: Int) {
        this.primaryDarkColor = primaryDarkColor
    }

    fun getAccentColor(): Int {
        return accentColor
    }

    fun setAccentColor(accentColor: Int) {
        this.accentColor = accentColor
    }

    fun getThemeInfo(): ThemeApp {
        try {
            val value: Int = Utils.getThemeColor()
            val mThem: List<ThemeApp> = ThemeUtil.getThemeList()
            if (mThem.size > value) {
                return mThem[value]
            }else {
                Utils.putThemeColor(0)
            }
        } catch (e: Exception) {
            Utils.putThemeColor(0)
            e.printStackTrace()
        }
        return ThemeApp(0, R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorButton)
    }

    fun getList(): MutableList<ThemeApp>? {
        try {
            return ThemeUtil.getThemeList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ArrayList()
    }

    companion object {
        private var instance: ThemeApp? = null
        fun getInstance(): ThemeApp? {
            if (instance == null) {
                instance = ThemeApp()
            }
            return instance
        }
    }
}