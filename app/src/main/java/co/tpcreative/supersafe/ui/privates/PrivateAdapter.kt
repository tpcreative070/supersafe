package co.tpcreative.supersafe.ui.privates
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.PopupMenu
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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.snatik.storage.Storage

class PrivateAdapter(inflater: LayoutInflater, private val context: Context?, itemSelectedListener: ItemSelectedListener) : BaseAdapter<MainCategoryModel, BaseHolder<MainCategoryModel>>(inflater) {
    private val storage: Storage?
    private val itemSelectedListener: ItemSelectedListener?
    private val TAG = PrivateAdapter::class.java.simpleName
    var themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    var note1: Drawable? = ContextCompat.getDrawable(SuperSafeApplication.getInstance(),themeApp?.getAccentColor()!!)
    var options: RequestOptions? = RequestOptions()
            .centerCrop()
            .override(400, 400)
            .placeholder(themeApp?.getPrimaryColor()!!)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .error(themeApp?.getAccentColor()!!)
            .priority(Priority.HIGH)

    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<MainCategoryModel> {
        return ItemHolder(inflater!!.inflate(R.layout.private_item, parent, false))
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
            if (data.pin == "") {
                val mList: MutableList<ItemModel>? = SQLHelper.getListItems(data.categories_local_id, data.isFakePin)
                val items: ItemModel? = SQLHelper.getItemId(data.items_id)
                if (items != null && mList != null && mList.size > 0) {
                    when (EnumFormatType.values()[items.formatType]) {
                        EnumFormatType.AUDIO -> {
                            if (context != null) {
                                Glide.with(context)
                                        .load(note1)
                                        .apply(options!!)
                                        .into(imgAlbum!!)
                            }
                            imgIcon?.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_music_note_white_48))
                        }
                        EnumFormatType.FILES -> {
                            Glide.with(context!!)
                                    .load(note1)
                                    .apply(options!!)
                                    .into(imgAlbum!!)
                            imgIcon?.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_insert_drive_file_white_48))
                        }
                        else -> {
                            try {
                                if (storage?.isFileExist("" + items.thumbnailPath)!!) {
                                    imgAlbum?.setRotation(items.degrees.toFloat())
                                    Glide.with(context!!)
                                            .load(storage.readFile(items.thumbnailPath))
                                            .apply(options!!)
                                            .into(imgAlbum!!)
                                    imgIcon?.setVisibility(View.INVISIBLE)
                                } else {
                                    imgAlbum?.setImageResource(0)
                                    val myColor = Color.parseColor(data.image)
                                    imgAlbum?.setBackgroundColor(myColor)
                                    imgIcon?.setImageDrawable(SQLHelper.getDrawable(context, data.icon))
                                    imgIcon?.setVisibility(View.VISIBLE)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } else {
                    imgAlbum?.setImageResource(0)
                    val mainCategories: MainCategoryModel? = SQLHelper.getCategoriesPosition(data.mainCategories_Local_Id)
                    if (mainCategories != null) {
                        imgIcon?.setImageDrawable(SQLHelper.getDrawable(SuperSafeApplication.getInstance(), mainCategories.icon))
                        imgIcon?.setVisibility(View.VISIBLE)
                        try {
                            val myColor = Color.parseColor(mainCategories.image)
                            imgAlbum?.setBackgroundColor(myColor)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        imgAlbum?.setImageResource(0)
                        imgIcon?.setImageDrawable(SQLHelper.getDrawable(context, data.icon))
                        imgIcon?.setVisibility(View.VISIBLE)
                        try {
                            val myColor = Color.parseColor(data.image)
                            imgAlbum?.setBackgroundColor(myColor)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } else {
                imgAlbum?.setImageResource(0)
                imgIcon?.setImageResource(R.drawable.baseline_https_white_48)
                imgIcon?.setVisibility(View.VISIBLE)
                try {
                    val myColor = Color.parseColor(data.image)
                    imgAlbum?.setBackgroundColor(myColor)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            tvTitle?.setText(data.categories_name)
            mPosition = position
        }

        @OnClick(R.id.rlHome)
        fun onClicked(view: View?) {
            Utils.Log(TAG, "Position $mPosition")
            itemSelectedListener?.onClickItem(mPosition)
        }

        @OnClick(R.id.overflow)
        fun onClickedOverFlow(view: View) {
            if (data?.categories_hex_name == Utils.getHexCode(context!!.getString(R.string.key_trash))) {
                showPopupMenu(view, R.menu.menu_trash_album, mPosition)
            } else if (data?.categories_hex_name == Utils.getHexCode(context.getString(R.string.key_main_album))) {
                showPopupMenu(view, R.menu.menu_main_album, mPosition)
            } else {
                if (data?.pin == "") {
                    showPopupMenu(view, R.menu.menu_album, mPosition)
                } else {
                    showPopupMenu(view, R.menu.menu_main_album, mPosition)
                }
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
        open fun onClickItem(position: Int)
        open fun onSetting(position: Int)
        open fun onDeleteAlbum(position: Int)
        open fun onEmptyTrash(position: Int)
    }

    init {
        storage = Storage(context)
        storage.setEncryptConfiguration(SuperSafeApplication.Companion.getInstance().getConfigurationFile())
        this.itemSelectedListener = itemSelectedListener
    }
}