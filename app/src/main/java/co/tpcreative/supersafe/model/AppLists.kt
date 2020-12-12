package co.tpcreative.supersafe.model
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

class AppLists {
    var ic_name: String? = null
    var title: String? = null
    var description: String? = null
    var link: String? = null
    var packageName: String? = null
    var isInstalled = false

    constructor() {}
    constructor(ic_name: String?, title: String?, description: String?, isInstalled: Boolean, packageName: String?, link: String?) {
        this.ic_name = ic_name
        this.title = title
        this.description = description
        this.isInstalled = isInstalled
        this.link = link
        this.packageName = packageName
    }

    fun getDrawable(mContext: Context?, name: String?): Drawable? {
        try {
            val resourceId = mContext?.getResources()?.getIdentifier(name, "drawable", mContext.getPackageName())
            return ContextCompat.getDrawable(mContext!!,resourceId!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    companion object {
        private var instance: AppLists? = null
        fun getInstance(): AppLists? {
            if (instance == null) {
                instance = AppLists()
            }
            return instance
        }
    }
}