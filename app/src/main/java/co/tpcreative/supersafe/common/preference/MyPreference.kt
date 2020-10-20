package co.tpcreative.supersafe.common.preference
import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder

class MyPreference @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int = 0
) : Preference(context, attrs, defStyleAttr) {
    init {
        widgetLayoutResource = 0
    }
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
    }
}