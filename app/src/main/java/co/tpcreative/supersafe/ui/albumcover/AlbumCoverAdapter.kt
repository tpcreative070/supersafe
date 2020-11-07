package co.tpcreative.supersafe.ui.albumcover
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumFormatType
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.model.MainCategoryModel
import co.tpcreative.supersafe.model.ThemeApp
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.snatik.storage.Storage
import kotlinx.android.synthetic.main.album_cover_item.view.*

class AlbumCoverAdapter(inflater: LayoutInflater, private val context: Context?, mainCategories: MainCategoryModel?, itemSelectedListener: ItemSelectedListener?) : BaseAdapter<ItemModel, BaseHolder<ItemModel>>(inflater) {
    var options: RequestOptions? = RequestOptions()
            .centerCrop()
            .override(200, 200)
            .placeholder(R.color.material_gray_100)
            .error(R.color.red_200)
            .priority(Priority.HIGH)
    private val itemSelectedListener: ItemSelectedListener?
    private val storage: Storage?
    private val categories: MainCategoryModel?
    var themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    private val TAG = AlbumCoverAdapter::class.java.simpleName
    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<ItemModel> {
        return ItemHolder(inflater!!.inflate(R.layout.album_cover_item, parent, false))
    }

    interface ItemSelectedListener {
        fun onClickItem(position: Int)
    }

    inner class ItemHolder(itemView: View) : BaseHolder<ItemModel>(itemView) {
        val imgAlbum = itemView.imgAlbum
        val imgIcon = itemView.imgIcon
        val imgSelect = itemView.imgSelect
        val view_alpha = itemView.view_alpha
        var mPosition = 0
        var rlHome = itemView.rlHome
        var items: ItemModel? = null
        override fun bind(data: ItemModel, position: Int) {
            super.bind(data, position)
            Utils.Log(TAG,"Loading....")
            mPosition = position
            items = data
            if (data.isChecked) {
                view_alpha?.alpha = 0.5f
                imgSelect?.visibility = View.VISIBLE
            } else {
                view_alpha?.alpha = 0.0f
                imgSelect?.visibility = View.INVISIBLE
            }
            try {
                when (EnumFormatType.values()[items!!.formatType]) {
                    EnumFormatType.AUDIO -> {
                        val note1 = ContextCompat.getDrawable(context!!,themeApp!!.getAccentColor())
                        Glide.with(context)
                                .load(note1)
                                .apply(options!!)
                                .into(imgAlbum!!)
                        imgIcon?.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.baseline_insert_drive_file_white_48))
                    }
                    EnumFormatType.FILES -> {
                        val note1 = ContextCompat.getDrawable(context!!,themeApp!!.getAccentColor())
                        Glide.with(context)
                                .load(note1)
                                .apply(options!!)
                                .into(imgAlbum!!)
                        imgIcon?.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_files))
                    }
                    else -> {
                        try {
                            if (storage?.isFileExist("" + items?.getThumbnail())!!) {
                                items?.degrees?.toFloat()?.let { imgAlbum?.setRotation(it) }
                                Glide.with(context!!)
                                        .load(storage.readFile(items?.getThumbnail()))
                                        .apply(options!!)
                                        .into(imgAlbum!!)
                                imgIcon?.visibility = View.INVISIBLE
                            } else {
                                imgAlbum?.setImageResource(0)
                                val myColor = Color.parseColor(categories?.image)
                                imgAlbum?.setBackgroundColor(myColor)
                                imgIcon?.setImageDrawable(SQLHelper.getDrawable(context, categories?.icon))
                                imgIcon?.visibility = View.VISIBLE
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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

    init {
        storage = Storage(context)
        categories = mainCategories
        this.itemSelectedListener = itemSelectedListener
        storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile())
    }
}