package co.tpcreative.supersafe.ui.albumcover
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
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

class AlbumCoverAdapter(inflater: LayoutInflater, private val context: Context?, mainCategories: MainCategoryModel?, itemSelectedListener: ItemSelectedListener?) : BaseAdapter<ItemModel, BaseHolder<ItemModel>>(inflater) {
    var options: RequestOptions? = RequestOptions()
            .centerCrop()
            .override(400, 400)
            .placeholder(R.drawable.baseline_music_note_white_48)
            .error(R.drawable.baseline_music_note_white_48)
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
        open fun onClickItem(position: Int)
    }

    inner class ItemHolder(itemView: View) : BaseHolder<ItemModel>(itemView) {
        @BindView(R.id.imgAlbum)
        var imgAlbum: AppCompatImageView? = null

        @BindView(R.id.imgIcon)
        var imgIcon: AppCompatImageView? = null

        @BindView(R.id.imgSelect)
        var imgSelect: AppCompatImageView? = null

        @BindView(R.id.view_alpha)
        var view_alpha: View? = null
        var mPosition = 0

        @BindView(R.id.rlHome)
        var rlHome: RelativeLayout? = null
        var items: ItemModel? = null
        override fun bind(data: ItemModel, position: Int) {
            super.bind(data, position)
            mPosition = position
            Utils.Log(TAG, "load data")
            items = data
            if (data.isChecked) {
                view_alpha?.setAlpha(0.5f)
                imgSelect?.setVisibility(View.VISIBLE)
            } else {
                view_alpha?.setAlpha(0.0f)
                imgSelect?.setVisibility(View.INVISIBLE)
            }
            try {
                val formatTypeFile = EnumFormatType.values()[items!!.formatType]
                when (formatTypeFile) {
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
                            if (storage?.isFileExist("" + items?.thumbnailPath)!!) {
                                items?.degrees?.toFloat()?.let { imgAlbum?.setRotation(it) }
                                Glide.with(context!!)
                                        .load(storage.readFile(items?.thumbnailPath))
                                        .apply(options!!)
                                        .into(imgAlbum!!)
                                imgIcon?.setVisibility(View.INVISIBLE)
                                Utils.Log(TAG, "load data 2")
                            } else {
                                imgAlbum?.setImageResource(0)
                                val myColor = Color.parseColor(categories?.image)
                                imgAlbum?.setBackgroundColor(myColor)
                                imgIcon?.setImageDrawable(SQLHelper.getDrawable(context, categories?.icon))
                                imgIcon?.setVisibility(View.VISIBLE)
                                Utils.Log(TAG, "load data 3")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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
    }

    init {
        storage = Storage(context)
        categories = mainCategories
        this.itemSelectedListener = itemSelectedListener
        storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile())
    }
}