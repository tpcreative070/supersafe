package co.tpcreative.supersafe.ui.move_album
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import co.tpcreative.supersafe.common.extension.isFileExist
import co.tpcreative.supersafe.common.extension.readFile
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.views.SquaredImageView
import co.tpcreative.supersafe.model.EnumFormatType
import co.tpcreative.supersafe.model.GalleryAlbum
import co.tpcreative.supersafe.model.ItemModel
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_move_gallery.view.*

class MoveAlbumAdapter(inflater: LayoutInflater, private val mContext: Context?,private val itemSelectedListener: ItemSelectedListener) : BaseAdapter<GalleryAlbum, BaseHolder<GalleryAlbum>>(inflater) {
    private val TAG = MoveAlbumAdapter::class.java.simpleName
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
        fun onClickGalleryItem(position: Int)
    }

    inner class ItemHolder(itemView: View) : BaseHolder<GalleryAlbum>(itemView) {

        val rlItem: RelativeLayout = itemView.rl_item
        val imgAlbum: SquaredImageView = itemView.image
        val tvTitle: AppCompatTextView = itemView.tvTitle
        val tvPhotos: AppCompatTextView = itemView.tvPhotos
        val tvVideos: AppCompatTextView = itemView.tvVideos
        val tvAudios: AppCompatTextView = itemView.tvAudios
        var tvOthers: AppCompatTextView = itemView.tvOthers
        private var mPosition = 0
        private var data: GalleryAlbum? = null
        override fun bind(data: GalleryAlbum, position: Int) {
            super.bind(data, position)
            this.data = data
            val items: ItemModel? = SQLHelper.getLatestId(this.data?.main?.categories_local_id, false, this.data?.main?.isFakePin!!)
            if (items != null) {
                when (EnumFormatType.values()[items.formatType]) {
                    EnumFormatType.AUDIO -> {
                        Glide.with(mContext!!)
                                .load(R.drawable.bg_button_rounded)
                                .apply(options!!)
                                .into(imgAlbum)
                    }
                    EnumFormatType.FILES -> {
                        Glide.with(mContext!!)
                                .load(R.drawable.bg_button_rounded)
                                .apply(options!!)
                                .into(imgAlbum)
                    }
                    else -> {
                        try {
                            if (items.getThumbnail().isFileExist()) {
                                Glide.with(mContext!!)
                                        .load(items.getThumbnail().readFile())
                                        .apply(options!!)
                                        .into(imgAlbum)
                            } else {
                                imgAlbum.setImageResource(0)
                                val myColor = Color.parseColor(this.data?.main?.image)
                                imgAlbum.setBackgroundColor(myColor)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } else {
                imgAlbum.setImageResource(0)
                try {
                    val myColor = Color.parseColor(this.data?.main?.image)
                    imgAlbum.setBackgroundColor(myColor)
                } catch (e: Exception) {
                }
            }
            val photos = String.format(mContext!!.getString(R.string.photos_default), "" + this.data?.photos)
            val videos = String.format(mContext.getString(R.string.videos_default), "" + this.data?.videos)
            val audios = String.format(mContext.getString(R.string.audios_default), "" + this.data?.audios)
            val others = String.format(mContext.getString(R.string.others_default), "" + this.data?.others)
            tvPhotos.text = photos
            tvVideos.text = videos
            tvAudios.text = audios
            tvOthers.text = others
            tvTitle.text = this.data?.main?.categories_name
            mPosition = position
            rlItem.setOnClickListener {
                itemSelectedListener.onClickGalleryItem(mPosition)
            }
        }
    }
}