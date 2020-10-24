package co.tpcreative.supersafe.ui.fakepin
import android.app.Activity
import android.graphics.Color
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.PopupMenu
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
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.snatik.storage.Storage

class FakePinComponentAdapter(inflater: LayoutInflater, context: Activity?, itemSelectedListener: ItemSelectedListener?) : BaseAdapter<MainCategoryModel, BaseHolder<MainCategoryModel>>(inflater) {
    private val context: Activity?
    private val storage: Storage?
    private val itemSelectedListener: ItemSelectedListener?
    private val TAG = FakePinComponentAdapter::class.java.simpleName
    var options: RequestOptions? = RequestOptions()
            .centerCrop()
            .override(400, 400)
            .placeholder(R.color.colorPrimary)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .error(R.color.colorPrimary)
            .priority(Priority.HIGH)

    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<MainCategoryModel> {
        return ItemHolder(inflater!!.inflate(R.layout.fake_pin_item, parent, false))
    }

    inner class ItemHolder(itemView: View) : BaseHolder<MainCategoryModel>(itemView) {
        private var data: MainCategoryModel? = null

        @BindView(R.id.imgAlbum)
        var imgAlbum: AppCompatImageView? = null

        @BindView(R.id.tvTitle)
        var tvTitle: AppCompatTextView? = null

        @BindView(R.id.imgIcon)
        var imgIcon: AppCompatImageView? = null
        var mPosition = 0
        override fun bind(data: MainCategoryModel, position: Int) {
            super.bind(data, position)
            this.data = data
            val items: ItemModel? = SQLHelper.getLatestId(data.categories_local_id, false, true)
            if (items != null) {
                val formatTypeFile = EnumFormatType.values()[items.formatType]
                when (formatTypeFile) {
                    EnumFormatType.AUDIO -> {
                        Glide.with(context!!)
                                .load(R.drawable.bg_button_rounded)
                                .apply(options!!)
                                .into(imgAlbum!!)
                        imgIcon?.setVisibility(View.INVISIBLE)
                    }
                    EnumFormatType.FILES -> {
                        Glide.with(context!!)
                                .load(R.drawable.bg_button_rounded)
                                .apply(options!!)
                                .into(imgAlbum!!)
                        imgIcon?.setVisibility(View.INVISIBLE)
                    }
                    else -> {
                        try {
                            if (storage?.isFileExist("" + items.thumbnailPath)!!) {
                                Glide.with(context!!)
                                        .load(storage.readFile(items.thumbnailPath))
                                        .apply(options!!)
                                        .into(imgAlbum!!)
                                imgIcon?.setVisibility(View.INVISIBLE)
                            } else {
                                imgAlbum?.setImageResource(0)
                                val myColor = Color.parseColor(data.image)
                                imgAlbum?.setBackgroundColor(myColor)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } else {
                imgAlbum?.setImageResource(0)
                imgIcon?.setImageDrawable(SQLHelper.getDrawable(context, data.icon))
                imgIcon?.setVisibility(View.VISIBLE)
                try {
                    val myColor = Color.parseColor(data.image)
                    imgAlbum?.setBackgroundColor(myColor)
                } catch (e: Exception) {
                }
            }
            tvTitle?.setText(data.categories_name)
            mPosition = position
        }

        @OnClick(R.id.rlHome)
        fun onClicked(view: View?) {
            itemSelectedListener?.onClickItem(mPosition)
        }

        @OnClick(R.id.overflow)
        fun onClickedOverFlow(view: View) {
            if (data?.categories_hex_name == context?.getString(R.string.key_trash)?.let { Utils.getHexCode(it) }) {
                showPopupMenu(view, R.menu.menu_trash_album, mPosition)
            } else if (data?.categories_hex_name == context?.getString(R.string.key_main_album)?.let { Utils.getHexCode(it) }) {
                showPopupMenu(view, R.menu.menu_main_album, mPosition)
            } else {
                showPopupMenu(view, R.menu.menu_album, mPosition)
            }
        }
    }

    private fun showPopupMenu(view: View, menu: Int, position: Int) {
        val popup = PopupMenu(context!!, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(menu, popup.menu)
        popup.setOnMenuItemClickListener(MyMenuItemClickListener(position))
        popup.show()
    }

    internal inner class MyMenuItemClickListener(var position: Int) : PopupMenu.OnMenuItemClickListener {
        override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
            when (menuItem?.getItemId()) {
                R.id.action_settings -> {
                    itemSelectedListener?.onSetting(position)
                    return true
                }
                R.id.action_delete -> {
                    itemSelectedListener?.onDeleteAlbum(position)
                    return true
                }
                R.id.action_empty_trash -> {
                    itemSelectedListener?.onEmptyTrash(position)
                    return true
                }
                else -> {
                }
            }
            return false
        }
    }

    interface ItemSelectedListener {
        fun onClickItem(position: Int)
        fun onSetting(position: Int)
        fun onDeleteAlbum(position: Int)
        fun onEmptyTrash(position: Int)
    }

    init {
        this.context = context
        storage = Storage(context)
        storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile())
        this.itemSelectedListener = itemSelectedListener
    }
}