package co.tpcreative.supersafe.common.preference
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceViewHolder
import co.tpcreative.supersafe.R

class MySwitchPreference @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int = 0
) : CheckBoxPreference(context, attrs, defStyleAttr) {
    init {
        widgetLayoutResource = R.layout.custom_preferences_item
    }
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        with(holder.itemView) {
            val checkbox = this.findViewById(android.R.id.checkbox) as SwitchCompat
            checkbox.visibility = View.VISIBLE
        }
    }
}