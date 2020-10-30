package co.tpcreative.supersafe.ui.albumdetail
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.snatik.storage.Storage
import kotlinx.android.synthetic.main.album_detail_item.view.*
import tpcreative.co.qrscanner.common.extension.setColorFilter

class AlbumDetailAdapter(inflater: LayoutInflater, private val context: Context?, itemSelectedListener: ItemSelectedListener?) : BaseAdapter<ItemModel, BaseHolder<ItemModel>>(inflater) {
    var options: RequestOptions? = RequestOptions()
            .centerCrop()
            .override(200, 200)
            .placeholder(R.color.material_gray_100)
            .error(R.color.red_200)
            .priority(Priority.HIGH)
    private val itemSelectedListener: ItemSelectedListener?
    private val storage: Storage?
    private val TAG = AlbumDetailAdapter::class.java.simpleName
    val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    var note1: Drawable? = ContextCompat.getDrawable(SuperSafeApplication.getInstance(),themeApp!!.getAccentColor())
    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<ItemModel> {
        return ItemHolder(inflater!!.inflate(R.layout.album_detail_item, parent, false))
    }

    interface ItemSelectedListener {
        fun onClickItem(position: Int)
        fun onLongClickItem(position: Int)
    }

    inner class ItemHolder(view: View) : BaseHolder<ItemModel>(view) {

        val imgAlbum = itemView.imgAlbum


        val imgVideoCam = itemView.imgVideoCam

        val tvTitle = itemView.tvTitle

        val progressingBar = itemView.progressingBar

        val imgCheck = itemView.imgCheck

        val view_alpha = itemView.view_alpha

        val imgSelect = itemView.imgSelect
        var mPosition = 0

        val rlHome = itemView.rlHome
        override fun bind(data: ItemModel, position: Int) {
            super.bind(data, position)
            mPosition = position
            if (data.isChecked) {
                view_alpha?.alpha = 0.5f
                imgSelect?.visibility = View.VISIBLE
            } else {
                view_alpha?.alpha = 0.0f
                imgSelect?.visibility = View.INVISIBLE
            }
            try {
                val path: String? = data.getThumbnail()
                when (EnumFormatType.values()[data.formatType]) {
                    EnumFormatType.AUDIO -> {
                        imgVideoCam?.visibility = View.VISIBLE
                        imgVideoCam?.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_music_note_white_48))
                        tvTitle?.visibility = View.VISIBLE
                        tvTitle?.text = data.title
                        Glide.with(context!!)
                                .load(note1)
                                .apply(options!!).into(imgAlbum!!)
                    }
                    EnumFormatType.VIDEO -> {
                        imgVideoCam?.visibility = View.VISIBLE
                        imgVideoCam?.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_videocam_white_36))
                        tvTitle?.visibility = View.INVISIBLE
                        if (storage?.isFileExist(path)!!) {
                            imgAlbum?.rotation = data.degrees.toFloat()
                            Glide.with(context!!)
                                    .load(storage.readFile(path))
                                    .apply(options!!).into(imgAlbum!!)
                        }
                    }
                    EnumFormatType.IMAGE -> {
                        tvTitle?.visibility = View.INVISIBLE
                        imgVideoCam?.visibility = View.INVISIBLE
                        if (storage?.isFileExist(path)!!) {
                            imgAlbum?.rotation = data.degrees.toFloat()
                            Glide.with(context!!)
                                    .load(storage.readFile(path))
                                    .apply(options!!).into(imgAlbum!!)
                        }
                    }
                    EnumFormatType.FILES -> {
                        imgVideoCam?.visibility = View.VISIBLE
                        imgVideoCam?.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_insert_drive_file_white_48))
                        tvTitle?.visibility = View.VISIBLE
                        tvTitle?.text = data.title
                        Glide.with(context!!)
                                .load(note1)
                                .apply(options!!).into(imgAlbum!!)
                    }
                }
                progressingBar?.indeterminateDrawable?.setColorFilter(ContextCompat.getColor(context!!,themeApp!!.getAccentColor()), EnumMode.SRC_ATOP)
                when (EnumStatusProgress.values()[data.statusProgress]) {
                    EnumStatusProgress.PROGRESSING -> {
                        imgCheck?.visibility = View.INVISIBLE
                        progressingBar?.visibility = View.VISIBLE
                    }
                    EnumStatusProgress.DONE -> {
                        if (data.isSaver) {
                            imgCheck?.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_attach_money_white_48))
                            imgCheck?.visibility = View.VISIBLE
                        } else {
                            imgCheck?.visibility = View.VISIBLE
                        }
                        progressingBar?.visibility = View.INVISIBLE
                    }
                    else -> {
                        imgCheck?.visibility = View.INVISIBLE
                        progressingBar?.visibility = View.INVISIBLE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            rlHome.setOnClickListener {
                itemSelectedListener?.onClickItem(mPosition)
            }

            rlHome.setOnLongClickListener {
                itemSelectedListener?.onLongClickItem(mPosition)
                true
            }
        }
    }

    init {
        storage = Storage(context)
        this.itemSelectedListener = itemSelectedListener
        storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile())
    }
}