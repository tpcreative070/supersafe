package co.tpcreative.supersafe.ui.trash
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import co.tpcreative.supersafe.common.extension.isFileExist
import co.tpcreative.supersafe.common.extension.readFile
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.views.SquaredImageView
import co.tpcreative.supersafe.common.views.SquaredView
import co.tpcreative.supersafe.model.*
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.album_detail_item.view.*
import tpcreative.co.qrscanner.common.extension.setColorFilter

class TrashAdapter(inflater: LayoutInflater, private val context: Context?,private val itemSelectedListener: ItemSelectedListener?) : BaseAdapter<ItemModel, BaseHolder<ItemModel>>(inflater) {
    var options: RequestOptions = RequestOptions()
            .centerCrop()
            .override(200, 200)
            .priority(Priority.HIGH)
    private val TAG = TrashAdapter::class.java.simpleName
    var themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    var note1: Drawable? = ContextCompat.getDrawable(SuperSafeApplication.getInstance(),themeApp?.getAccentColor()!!)
    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<ItemModel> {
        return ItemHolder(inflater!!.inflate(R.layout.album_detail_item, parent, false))
    }

    interface ItemSelectedListener {
        fun onClickItem(position: Int)
    }

    inner class ItemHolder(itemView: View) : BaseHolder<ItemModel>(itemView) {
        val imgAlbum: SquaredImageView = itemView.imgAlbum
        private val imgVideoCam: AppCompatImageView = itemView.imgVideoCam
        val tvTitle: AppCompatTextView = itemView.tvTitle
        private val progressingBar: ProgressBar = itemView.progressingBar
        private val imgCheck: AppCompatImageView = itemView.imgCheck
        private val viewAlpha: SquaredView = itemView.view_alpha
        var imgSelect: SquaredImageView = itemView.imgSelect
        var mPosition = 0
        override fun bind(data: ItemModel, position: Int) {
            super.bind(data, position)
            mPosition = position
            if (data.isChecked) {
                viewAlpha.alpha = 0.5f
                imgSelect.visibility = View.VISIBLE
            } else {
                viewAlpha.alpha = 0.0f
                imgSelect.visibility = View.INVISIBLE
            }
            try {
                val path: String = data.getThumbnail()
                when (EnumFormatType.values()[data.formatType]) {
                    EnumFormatType.AUDIO -> {
                        imgVideoCam.visibility = View.VISIBLE
                        imgVideoCam.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_music_note_white_48))
                        tvTitle.visibility = View.VISIBLE
                        tvTitle.text = data.title
                        Glide.with(context)
                                .load(note1)
                                .apply(options).into(imgAlbum)
                    }
                    EnumFormatType.FILES -> {
                        imgVideoCam.visibility = View.VISIBLE
                        imgVideoCam.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_insert_drive_file_white_48))
                        tvTitle.visibility = View.VISIBLE
                        tvTitle.text = data.title
                        Glide.with(context)
                                .load(note1)
                                .apply(options).into(imgAlbum)
                    }
                    EnumFormatType.VIDEO -> {
                        imgVideoCam.visibility = View.VISIBLE
                        imgVideoCam.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_videocam_white_36))
                        tvTitle.visibility = View.INVISIBLE
                        if (path.isFileExist()) {
                            imgAlbum.rotation = data.degrees.toFloat()
                            Glide.with(context)
                                    .load(path.readFile())
                                    .apply(options).into(imgAlbum)
                        }
                    }
                    EnumFormatType.IMAGE -> {
                        tvTitle.visibility = View.INVISIBLE
                        imgVideoCam.visibility = View.INVISIBLE
                        if (path.isFileExist()) {
                            imgAlbum.rotation = data.degrees.toFloat()
                            Glide.with(context!!)
                                    .load(path.readFile())
                                    .apply(options).into(imgAlbum)
                        }
                    }
                }
                progressingBar.indeterminateDrawable?.setColorFilter(ContextCompat.getColor(context!!,themeApp?.getAccentColor()!!),EnumMode.DST_ATOP)
                when (EnumStatusProgress.values()[data.statusProgress]) {
                    EnumStatusProgress.PROGRESSING -> {
                        imgCheck.visibility = View.INVISIBLE
                        progressingBar.visibility = View.VISIBLE
                    }
                    EnumStatusProgress.DONE -> {
                        imgCheck.visibility = View.VISIBLE
                        progressingBar.visibility = View.INVISIBLE
                    }
                    else -> {
                        imgCheck.visibility = View.INVISIBLE
                        progressingBar.visibility = View.INVISIBLE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            itemView.rlHome.setOnClickListener {
                itemSelectedListener?.onClickItem(mPosition)
            }
        }
    }
}