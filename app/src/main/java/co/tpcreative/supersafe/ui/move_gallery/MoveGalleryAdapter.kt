package co.tpcreative.supersafe.ui.move_gallery
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.model.EnumFormatType
import co.tpcreative.supersafe.model.GalleryAlbum
import co.tpcreative.supersafe.model.ItemModel
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.snatik.storage.Storage
import kotlinx.android.synthetic.main.item_move_gallery.view.*

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

        val rl_item = itemView.rl_item
        val imgAlbum = itemView.image
        val tvTitle = itemView.tvTitle
        val tvPhotos = itemView.tvPhotos
        val tvVideos = itemView.tvVideos
        val tvAudios = itemView.tvAudios
        var tvOthers = itemView.tvOthers
        private var mPosition = 0
        private var data: GalleryAlbum? = null
        override fun bind(mData: GalleryAlbum, position: Int) {
            super.bind(mData, position)
            data = mData
            val items: ItemModel? = SQLHelper.getLatestId(data?.main?.categories_local_id, false, data?.main?.isFakePin!!)
            if (items != null) {
                when (EnumFormatType.values()[items.formatType]) {
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
            tvPhotos?.text = photos
            tvVideos?.text = videos
            tvAudios?.text = audios
            tvOthers?.text = others
            tvTitle?.text = data?.main?.categories_name
            mPosition = position
            rl_item.setOnClickListener {
                ls?.onClickGalleryItem(mPosition)
            }
        }
    }

    init {
        storage = Storage(mContext)
        storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile())
        ls = itemSelectedListener
    }
}