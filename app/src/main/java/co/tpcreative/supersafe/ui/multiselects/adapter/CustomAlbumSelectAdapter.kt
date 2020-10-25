package co.tpcreative.supersafe.ui.multiselects.adapter
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.AlbumMultiItems
import co.tpcreative.supersafe.model.EnumFormatType
import co.tpcreative.supersafe.model.MimeTypeFile
import co.tpcreative.supersafe.model.ThemeApp
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import java.util.*

class CustomAlbumSelectAdapter(context: Context?, albums: ArrayList<AlbumMultiItems>?) : CustomGenericAdapter<AlbumMultiItems>(context, albums) {
    var options: RequestOptions? = RequestOptions()
            .centerCrop()
            .override(200, 200)
            .placeholder(R.drawable.image_placeholder)
            .error(R.drawable.ic_music)
            .priority(Priority.HIGH)
    val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    var note1: Drawable? = ContextCompat.getDrawable(SuperSafeApplication.getInstance(),themeApp?.getAccentColor()!!)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var convertView = convertView
        val viewHolder: ViewHolder
        if (convertView == null) {
            convertView = layoutInflater?.inflate(R.layout.grid_view_item_album_select, null)
            viewHolder = ViewHolder()
            viewHolder.imageView = convertView?.findViewById<View?>(R.id.image_view_album_image) as AppCompatImageView?
            viewHolder.textView = convertView?.findViewById<View?>(R.id.text_view_album_name) as AppCompatTextView?
            viewHolder.imgAudioVideo = convertView?.findViewById<View?>(R.id.imgAudioVideo) as AppCompatImageView?
            convertView?.setTag(viewHolder)
        } else {
            viewHolder = convertView.tag as ViewHolder
        }
        viewHolder.imageView?.layoutParams?.width = size
        viewHolder.imageView?.layoutParams?.height = size
        viewHolder.textView?.setText(arrayList?.get(position)?.name)
        val data: AlbumMultiItems? = arrayList?.get(position)
        try {
            val extensionFile: String? = Utils.getFileExtension(data?.cover)
            Log.d("value customer", extensionFile +"")
            val mimeTypeFile: MimeTypeFile? = Utils.mediaTypeSupport().get(extensionFile)
            if (mimeTypeFile != null) {
                val formatTypeFile = EnumFormatType.values()[mimeTypeFile.formatType?.ordinal!!]
                when (formatTypeFile) {
                    EnumFormatType.AUDIO -> {
                        viewHolder.imgAudioVideo?.visibility = View.VISIBLE
                        viewHolder.imgAudioVideo?.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_music_note_white_48))
                        Glide.with(context!!)
                                .load(note1)
                                .apply(options!!).into(viewHolder.imageView!!)
                    }
                    EnumFormatType.FILES -> {
                        viewHolder.imgAudioVideo?.visibility = View.VISIBLE
                        viewHolder.imgAudioVideo?.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_insert_drive_file_white_48))
                        Glide.with(context!!)
                                .load(note1)
                                .apply(options!!).into(viewHolder.imageView!!)
                    }
                    EnumFormatType.VIDEO -> {
                        viewHolder.imgAudioVideo?.visibility = View.VISIBLE
                        viewHolder.imgAudioVideo?.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_videocam_white_36))
                        Glide.with(context!!)
                                .load(data?.cover)
                                .apply(options!!).into(viewHolder.imageView!!)
                    }
                    else -> {
                        viewHolder.imgAudioVideo?.visibility = View.INVISIBLE
                        Glide.with(context!!)
                                .load(data?.cover)
                                .apply(options!!).into(viewHolder.imageView!!)
                    }
                }
            } else {
                Glide.with(context!!)
                        .load(R.drawable.ic_music)
                        .apply(options!!).into(viewHolder.imageView!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return convertView
    }

    private class ViewHolder {
        var imageView: AppCompatImageView? = null
        var imgAudioVideo: AppCompatImageView? = null
        var textView: AppCompatTextView? = null
    }
}