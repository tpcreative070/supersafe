package co.tpcreative.supersafe.common.preference
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import co.tpcreative.supersafe.R
import kotlinx.android.synthetic.main.custom_preferences_item_album_settings.view.*

class MyPreferenceAlbumSettings @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int = 0
) : Preference(context, attrs, defStyleAttr) {
    init {
        widgetLayoutResource = R.layout.custom_preferences_item_album_settings
    }
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        with(holder.itemView) {
            // do the view initialization here...
            //textView.text = "Another Text"
            imageViewCover = this.imgCover
            imgViewSuperSafe = this.imgIcon
            imageViewCover?.visibility = View.INVISIBLE
            imgViewSuperSafe?.visibility = View.INVISIBLE
            onUpdatedView?.invoke()
        }
    }
    var imageViewCover : ImageView? = null
    var imgViewSuperSafe : ImageView? = null
    var onUpdatedView : (() -> Unit)? = null
}