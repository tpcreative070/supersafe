package co.tpcreative.supersafe.ui.albumdetail
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
import butterknife.OnLongClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import co.tpcreative.supersafe.common.adapter.FooterViewHolder
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.ConvertUtils
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumFormatType
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.model.ThemeApp
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.snatik.storage.Storage

class AlbumDetailVerticalAdapter(inflater: LayoutInflater, private val context: Context?, itemSelectedListener: ItemSelectedListener?) : BaseAdapter<ItemModel, BaseHolder<ItemModel>>(inflater) {
    var options: RequestOptions? = RequestOptions()
            .centerCrop()
            .override(200, 200)
            .placeholder(R.color.material_gray_100)
            .error(R.color.red_200)
            .priority(Priority.HIGH)
    private val itemSelectedListener: ItemSelectedListener?
    private val storage: Storage?
    private val TAG = AlbumDetailVerticalAdapter::class.java.simpleName
    val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    var note1: Drawable? = ContextCompat.getDrawable(SuperSafeApplication.getInstance(),themeApp!!.getAccentColor())
    override fun getItemCount(): Int {
        if (mDataSource == null) {
            return 0
        }
        return if (mDataSource.size == 0) {
            // Return 1 here to show nothing
            1
        } else mDataSource.size + 1
        // Add another extra view to show the footer view
        // So there are two extra views need to be populated
    }

    // Now define getItemViewType of your own.
    override fun getItemViewType(position: Int): Int {
        return if (position == mDataSource.size) {
            // This is where we'll add footer.
            FOOTER_VIEW
        } else super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<ItemModel> {
        return if (viewType == FOOTER_VIEW) {
            FooterViewHolder(inflater!!.inflate(R.layout.album_detail_item_footer, parent, false))
        } else ItemHolder(inflater!!.inflate(R.layout.custom_item_verical, parent, false))
    }

    interface ItemSelectedListener {
        fun onClickItem(position: Int)
        fun onLongClickItem(position: Int)
    }

    inner class ItemHolder(itemView: View) : BaseHolder<ItemModel>(itemView) {
        @BindView(R.id.imgAlbum)
        var imgAlbum: AppCompatImageView? = null

        @BindView(R.id.tvTitle)
        var tvTitle: AppCompatTextView? = null

        @BindView(R.id.tvSizeCreatedDate)
        var tvSizeCreatedDate: AppCompatTextView? = null

        @BindView(R.id.view_alpha)
        var alpha: View? = null
        var mPosition = 0
        override fun bind(data: ItemModel, position: Int) {
            super.bind(data, position)
            mPosition = position
            Utils.Log(TAG, "Position $position")
            if (data.isChecked) {
                alpha?.setAlpha(0.5f)
            } else {
                alpha?.setAlpha(0.0f)
            }
            try {
                val path: String? = data.thumbnailPath
                val formatTypeFile = EnumFormatType.values()[data.formatType]
                tvTitle?.setText(data.title)
                val value: String? = data.size?.toLong()?.let { ConvertUtils.byte2FitMemorySize(it) }
                tvSizeCreatedDate?.setText(value + " created " + Utils.getCurrentDate(data.originalName))
                when (formatTypeFile) {
                    EnumFormatType.AUDIO -> {
                        Glide.with(context!!)
                                .load(note1)
                                .apply(options!!).into(imgAlbum!!)
                    }
                    EnumFormatType.VIDEO -> {
                        if (storage?.isFileExist(path)!!) {
                            imgAlbum?.setRotation(data.degrees.toFloat())
                            Glide.with(context!!)
                                    .load(storage.readFile(path))
                                    .apply(options!!).into(imgAlbum!!)
                        }
                    }
                    EnumFormatType.IMAGE -> {
                        if (storage?.isFileExist(path)!!) {
                            imgAlbum?.setRotation(data.degrees.toFloat())
                            Glide.with(context!!)
                                    .load(storage.readFile(path))
                                    .apply(options!!).into(imgAlbum!!)
                        }
                    }
                    EnumFormatType.FILES -> {
                        Glide.with(context!!)
                                .load(note1)
                                .apply(options!!).into(imgAlbum!!)
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

    companion object {
        private const val FOOTER_VIEW = 1
    }

    init {
        storage = Storage(context)
        this.itemSelectedListener = itemSelectedListener
        storage.setEncryptConfiguration(SuperSafeApplication.Companion.getInstance().getConfigurationFile())
    }
}