package co.tpcreative.supersafe.ui.move_gallery
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import butterknife.BindView
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.views.SquaredImageView
import co.tpcreative.supersafe.model.EnumFormatType
import co.tpcreative.supersafe.model.GalleryAlbum
import co.tpcreative.supersafe.model.ItemModel
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.snatik.storage.Storage

class MoveGalleryAdapter(inflater: LayoutInflater, private val mContext: Context?, itemSelectedListener: ItemSelectedListener?) : BaseAdapter<GalleryAlbum, BaseHolder<GalleryAlbum>>(inflater) {
    private val storage: Storage?
    private val ls: ItemSelectedListener?
    private val TAG = MoveGalleryAdapter::class.java.simpleName
    var options: RequestOptions? = RequestOptions()
            .centerCrop()
            .placeholder(R.mipmap.ic_launcher_round)
            .priority(Priority.HIGH)

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<GalleryAlbum> {
        return ItemHolder(inflater!!.inflate(R.layout.item_move_gallery, parent, false))
    }

    interface ItemSelectedListener {
        open fun onClickGalleryItem(position: Int)
    }

    inner class ItemHolder(itemView: View) : BaseHolder<GalleryAlbum>(itemView) {
        @BindView(R.id.rl_item)
        var rl_item: RelativeLayout? = null

        @BindView(R.id.image)
        var imgAlbum: SquaredImageView? = null

        @BindView(R.id.tvTitle)
        var tvTitle: AppCompatTextView? = null

        @BindView(R.id.tvPhotos)
        var tvPhotos: AppCompatTextView? = null

        @BindView(R.id.tvVideos)
        var tvVideos: AppCompatTextView? = null

        @BindView(R.id.tvAudios)
        var tvAudios: AppCompatTextView? = null

        @BindView(R.id.tvOthers)
        var tvOthers: AppCompatTextView? = null
        private var mPosition = 0
        private var data: GalleryAlbum? = null
        override fun bind(mData: GalleryAlbum, position: Int) {
            super.bind(mData, position)
            data = mData
            val items: ItemModel? = SQLHelper.getLatestId(data?.main?.categories_local_id, false, data?.main?.isFakePin!!)
            if (items != null) {
                val formatTypeFile = EnumFormatType.values()[items.formatType]
                when (formatTypeFile) {
                    EnumFormatType.AUDIO -> {
                        Glide.with(mContext!!)
                                .load(R.drawable.bg_button_rounded)
                                .apply(options!!)
                                .into(imgAlbum!!)
                    }
                    EnumFormatType.FILES -> {
                        Glide.with(mContext!!)
                                .load(R.drawable.bg_button_rounded)
                                .apply(options!!)
                                .into(imgAlbum!!)
                    }
                    else -> {
                        try {
                            if (storage?.isFileExist("" + items.thumbnailPath)!!) {
                                Glide.with(mContext!!)
                                        .load(storage.readFile(items.thumbnailPath))
                                        .apply(options!!)
                                        .into(imgAlbum!!)
                            } else {
                                imgAlbum?.setImageResource(0)
                                val myColor = Color.parseColor(data?.main?.image)
                                imgAlbum?.setBackgroundColor(myColor)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } else {
                imgAlbum?.setImageResource(0)
                try {
                    val myColor = Color.parseColor(data?.main?.image)
                    imgAlbum?.setBackgroundColor(myColor)
                } catch (e: Exception) {
                }
            }
            val photos = String.format(mContext!!.getString(R.string.photos_default), "" + data?.photos)
            val videos = String.format(mContext.getString(R.string.videos_default), "" + data?.videos)
            val audios = String.format(mContext.getString(R.string.audios_default), "" + data?.audios)
            val others = String.format(mContext.getString(R.string.others_default), "" + data?.audios)
            tvPhotos?.setText(photos)
            tvVideos?.setText(videos)
            tvAudios?.setText(audios)
            tvOthers?.setText(others)
            tvTitle?.setText(data?.main?.categories_name)
            mPosition = position
        }

        @OnClick(R.id.rl_item)
        fun onClickedItem(view: View?) {
            ls?.onClickGalleryItem(mPosition)
        }
    }

    init {
        storage = Storage(mContext)
        storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile())
        ls = itemSelectedListener
    }
}