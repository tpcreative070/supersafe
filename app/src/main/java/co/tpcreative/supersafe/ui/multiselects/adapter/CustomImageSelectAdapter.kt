package co.tpcreative.supersafe.ui.multiselects.adapter
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumFormatType
import co.tpcreative.supersafe.model.Image
import co.tpcreative.supersafe.model.MimeTypeFile
import co.tpcreative.supersafe.model.ThemeApp
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.grid_view_item_image_select.view.*
import java.util.*

class CustomImageSelectAdapter(context: Context?, images: ArrayList<Image>?) : CustomGenericAdapter<Image>(context, images) {
    private val mContext: Context? = null
    var options: RequestOptions? = RequestOptions()
            .centerCrop()
            .override(200, 200)
            .placeholder(R.drawable.image_placeholder)
            .priority(Priority.HIGH)
    val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    var note1: Drawable? = ContextCompat.getDrawable(SuperSafeApplication.getInstance(),themeApp?.getAccentColor()!!)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val viewHolder: ViewHolder
        if (convertView == null) {
            convertView = layoutInflater?.inflate(R.layout.grid_view_item_image_select, null)
            viewHolder = ViewHolder()
            viewHolder.imageView = convertView?.image_view_image_select
            viewHolder.imgAudioVideo = convertView?.imgAudioVideo
            viewHolder.tvTitle = convertView?.tvTitle
            viewHolder.view = convertView?.view_alpha
            convertView?.setTag(viewHolder)
        } else {
            viewHolder = convertView.tag as ViewHolder
        }
        viewHolder.imageView?.layoutParams?.width = size
        viewHolder.imageView?.layoutParams?.height = size
        viewHolder.view?.layoutParams?.width = size
        viewHolder.view?.layoutParams?.height = size
        if (arrayList?.get(position)?.isSelected!!) {
            viewHolder.view?.alpha = 0.5f
            (convertView as FrameLayout?)?.setForeground(ContextCompat.getDrawable(context!!,R.drawable.ic_done_white))
        } else {
            viewHolder.view?.alpha = 0.0f
            (convertView as FrameLayout?)?.foreground = null
        }
        val data = arrayList!![position]
        try {
            val extensionFile: String? = Utils.getFileExtension(data.path)
            val mimeTypeFile: MimeTypeFile? = Utils.mediaTypeSupport().get(extensionFile)
            if (mimeTypeFile != null) {
                when (EnumFormatType.values()[mimeTypeFile.formatType?.ordinal!!]) {
                    EnumFormatType.AUDIO -> {
                        viewHolder.imgAudioVideo?.visibility = View.VISIBLE
                        viewHolder.imgAudioVideo?.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_music_note_white_48))
                        viewHolder.tvTitle?.visibility = View.VISIBLE
                        viewHolder.tvTitle?.text = data.name
                        Glide.with(context!!)
                                .load(note1)
                                .apply(options!!).into(viewHolder.imageView!!)
                    }
                    EnumFormatType.FILES -> {
                        viewHolder.imgAudioVideo?.visibility = View.VISIBLE
                        viewHolder.imgAudioVideo?.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_insert_drive_file_white_48))
                        viewHolder.tvTitle?.visibility = View.VISIBLE
                        viewHolder.tvTitle?.text = data.name
                        Glide.with(context!!)
                                .load(note1)
                                .apply(options!!).into(viewHolder.imageView!!)
                    }
                    EnumFormatType.VIDEO -> {
                        viewHolder.imgAudioVideo?.visibility = View.VISIBLE
                        viewHolder.imgAudioVideo?.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_videocam_white_36))
                        viewHolder.tvTitle?.visibility = View.INVISIBLE
                        Glide.with(context!!)
                                .load(data.path)
                                .apply(options!!).into(viewHolder.imageView!!)
                    }
                    else -> {
                        viewHolder.tvTitle?.visibility = View.INVISIBLE
                        viewHolder.imgAudioVideo?.visibility = View.INVISIBLE
                        Glide.with(context!!)
                                .load(data.path)
                                .apply(options!!).into(viewHolder.imageView!!)
                    }
                }
            } else {
                viewHolder.tvTitle?.visibility = View.INVISIBLE
                Glide.with(context!!)
                        .load(R.drawable.ic_music)
                        .apply(options!!).into(viewHolder.imageView!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return convertView!!
    }

    private class ViewHolder {
        var imageView: AppCompatImageView? = null
        var imgAudioVideo: AppCompatImageView? = null
        var view: View? = null
        var tvTitle: AppCompatTextView? = null
    }

    companion object {
        private val TAG = CustomImageSelectAdapter::class.java.simpleName
    }

    init {
        this.context = context
    }
}