package co.tpcreative.supersafe.ui.albumdetail
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
import butterknife.OnLongClick
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
        open fun onClickItem(position: Int)
        open fun onLongClickItem(position: Int)
    }

    inner class ItemHolder(itemView: View) : BaseHolder<ItemModel>(itemView) {
        @BindView(R.id.imgAlbum)
        var imgAlbum: AppCompatImageView? = null

        @BindView(R.id.imgVideoCam)
        var imgVideoCam: AppCompatImageView? = null

        @BindView(R.id.tvTitle)
        var tvTitle: AppCompatTextView? = null

        @BindView(R.id.progressingBar)
        var progressingBar: ProgressBar? = null

        @BindView(R.id.imgCheck)
        var imgCheck: AppCompatImageView? = null

        @BindView(R.id.view_alpha)
        var view_alpha: View? = null

        @BindView(R.id.imgSelect)
        var imgSelect: AppCompatImageView? = null
        var mPosition = 0

        @BindView(R.id.rlHome)
        var rlHome: RelativeLayout? = null
        override fun bind(data: ItemModel, position: Int) {
            super.bind(data, position)
            mPosition = position
            Utils.Log(TAG, "Position $position")
            if (data.isChecked) {
                view_alpha?.setAlpha(0.5f)
                imgSelect?.setVisibility(View.VISIBLE)
            } else {
                view_alpha?.setAlpha(0.0f)
                imgSelect?.setVisibility(View.INVISIBLE)
            }
            try {
                val path: String? = data.thumbnailPath
                val formatTypeFile = EnumFormatType.values()[data.formatType]
                when (formatTypeFile) {
                    EnumFormatType.AUDIO -> {
                        imgVideoCam?.setVisibility(View.VISIBLE)
                        imgVideoCam?.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_music_note_white_48))
                        tvTitle?.setVisibility(View.VISIBLE)
                        tvTitle?.setText(data.title)
                        Glide.with(context!!)
                                .load(note1)
                                .apply(options!!).into(imgAlbum!!)
                    }
                    EnumFormatType.VIDEO -> {
                        imgVideoCam?.setVisibility(View.VISIBLE)
                        imgVideoCam?.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_videocam_white_36))
                        tvTitle?.setVisibility(View.INVISIBLE)
                        if (storage?.isFileExist(path)!!) {
                            imgAlbum?.setRotation(data.degrees.toFloat())
                            Glide.with(context!!)
                                    .load(storage.readFile(path))
                                    .apply(options!!).into(imgAlbum!!)
                        }
                    }
                    EnumFormatType.IMAGE -> {
                        tvTitle?.setVisibility(View.INVISIBLE)
                        imgVideoCam?.setVisibility(View.INVISIBLE)
                        if (storage?.isFileExist(path)!!) {
                            imgAlbum?.setRotation(data.degrees.toFloat())
                            Glide.with(context!!)
                                    .load(storage.readFile(path))
                                    .apply(options!!).into(imgAlbum!!)
                        }
                    }
                    EnumFormatType.FILES -> {
                        imgVideoCam?.setVisibility(View.VISIBLE)
                        imgVideoCam?.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_insert_drive_file_white_48))
                        tvTitle?.setVisibility(View.VISIBLE)
                        tvTitle?.setText(data.title)
                        Glide.with(context!!)
                                .load(note1)
                                .apply(options!!).into(imgAlbum!!)
                    }
                }
                progressingBar?.getIndeterminateDrawable()?.setColorFilter(ContextCompat.getColor(context!!,themeApp!!.getAccentColor()), EnumMode.SRC_ATOP)
                val progress = EnumStatusProgress.values()[data.statusProgress]
                when (progress) {
                    EnumStatusProgress.PROGRESSING -> {
                        imgCheck?.setVisibility(View.INVISIBLE)
                        progressingBar?.setVisibility(View.VISIBLE)
                    }
                    EnumStatusProgress.DONE -> {
                        if (data.isSaver) {
                            imgCheck?.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_attach_money_white_48))
                            imgCheck?.setVisibility(View.VISIBLE)
                        } else {
                            imgCheck?.setVisibility(View.VISIBLE)
                        }
                        progressingBar?.setVisibility(View.INVISIBLE)
                    }
                    else -> {
                        imgCheck?.setVisibility(View.INVISIBLE)
                        progressingBar?.setVisibility(View.INVISIBLE)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @OnClick(R.id.rlHome)
        fun onClicked(view: View?) {
            itemSelectedListener?.onClickItem(mPosition)
        }

        @OnLongClick(R.id.rlHome)
        fun onLongClickedItem(view: View?): Boolean {
            itemSelectedListener?.onLongClickItem(mPosition)
            return true
        }
    }

    init {
        storage = Storage(context)
        this.itemSelectedListener = itemSelectedListener
        storage.setEncryptConfiguration(SuperSafeApplication.Companion.getInstance().getConfigurationFile())
    }
}