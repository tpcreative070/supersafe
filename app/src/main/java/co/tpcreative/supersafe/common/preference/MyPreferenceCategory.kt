package co.tpcreative.supersafe.common.preference
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.model.ThemeApp

class MyPreferenceCategory @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int = 0
) : PreferenceCategory(context, attrs, defStyleAttr) {
    init {
        widgetLayoutResource = R.layout.preference_category_layout
    }
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        with(holder.itemView) {
            val title = this.findViewById(android.R.id.title) as TextView
            val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
            title.setTextColor(ContextCompat.getColor(SuperSafeApplication.getInstance(), themeApp!!.getAccentColor()))
        }
    }
}