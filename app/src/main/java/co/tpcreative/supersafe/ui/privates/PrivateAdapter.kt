package co.tpcreative.supersafe.ui.privates
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import co.tpcreative.supersafe.common.extension.isFileExist
import co.tpcreative.supersafe.common.extension.readFile
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.SquaredImageView
import co.tpcreative.supersafe.model.EnumFormatType
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.model.MainCategoryModel
import co.tpcreative.supersafe.model.ThemeApp
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.private_item.view.*

class PrivateAdapter(inflater: LayoutInflater, private val context: Context?,private val itemSelectedListener: ItemSelectedListener) : BaseAdapter<MainCategoryModel, BaseHolder<MainCategoryModel>>(inflater) {
    private val TAG = PrivateAdapter::class.java.simpleName
    var themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    var note1: Drawable? = ContextCompat.getDrawable(SuperSafeApplication.getInstance(),themeApp?.getAccentColor()!!)
    var options: RequestOptions = RequestOptions()
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
        val imgAlbum: SquaredImageView = itemView.imgAlbum
        val tvTitle: AppCompatTextView = itemView.tvTitle
        var imgIcon: SquaredImageView = itemView.imgIcon
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
                                        .apply(options)
                                        .into(imgAlbum)
                            }
                            imgIcon.setImageDrawable(ContextCompat.getDrawable(context!!,R.drawable.baseline_music_note_white_48))
                        }
                        EnumFormatType.FILES -> {
                            Glide.with(context!!)
                                    .load(note1)
                                    .apply(options)
                                    .into(imgAlbum)
                            imgIcon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.baseline_insert_drive_file_white_48))
                        }
                        else -> {
                            try {
                                if (items.getThumbnail().isFileExist()) {
                                    imgAlbum.rotation =items.degrees.toFloat()
                                    Glide.with(context!!)
                                            .load(items.getThumbnail().readFile())
                                            .apply(options)
                                            .into(imgAlbum)
                                    imgIcon.visibility = View.INVISIBLE
                                } else {
                                    imgAlbum.setImageResource(0)
                                    val myColor = Color.parseColor(data.image)
                                    imgAlbum.setBackgroundColor(myColor)
                                    imgIcon.setImageDrawable(SQLHelper.getDrawable(context, data.icon))
                                    imgIcon.visibility = View.VISIBLE
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } else {
                    imgAlbum.setImageResource(0)
                    val mainCategories: MainCategoryModel? = SQLHelper.getCategoriesPosition(data.mainCategories_Local_Id)
                    if (mainCategories != null) {
                        imgIcon.setImageDrawable(SQLHelper.getDrawable(SuperSafeApplication.getInstance(), mainCategories.icon))
                        imgIcon.visibility = View.VISIBLE
                        try {
                            val myColor = Color.parseColor(mainCategories.image)
                            imgAlbum.setBackgroundColor(myColor)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        imgAlbum.setImageResource(0)
                        imgIcon.setImageDrawable(SQLHelper.getDrawable(context, data.icon))
                        imgIcon.visibility = View.VISIBLE
                        try {
                            val myColor = Color.parseColor(data.image)
                            imgAlbum.setBackgroundColor(myColor)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } else {
                imgAlbum.setImageResource(0)
                imgIcon.setImageResource(R.drawable.baseline_https_white_48)
                imgIcon.visibility = View.VISIBLE
                try {
                    val myColor = Color.parseColor(data.image)
                    imgAlbum.setBackgroundColor(myColor)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            tvTitle.text = data.categories_name
            mPosition = position
            itemView.rlHome.setOnClickListener {
                Utils.Log(TAG, "Position $mPosition")
                itemSelectedListener.onClickItem(mPosition)
            }

            itemView.overflow.setOnClickListener {
                if (data.categories_hex_name == Utils.getHexCode(context!!.getString(R.string.key_trash))) {
                    showPopupMenu(it, R.menu.menu_trash_album, mPosition)
                } else if (data.categories_hex_name == Utils.getHexCode(context.getString(R.string.key_main_album))) {
                    showPopupMenu(it, R.menu.menu_main_album, mPosition)
                } else {
                    if (data.pin == "") {
                        showPopupMenu(it, R.menu.menu_album, mPosition)
                    } else {
                        showPopupMenu(it, R.menu.menu_main_album, mPosition)
                    }
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
            when (menuItem?.itemId) {
                R.id.action_settings -> {
                    itemSelectedListener.onSetting(position)
                    return true
                }
                R.id.action_delete -> {
                    itemSelectedListener.onDeleteAlbum(position)
                    return true
                }
                R.id.action_empty_trash -> {
                    itemSelectedListener.onEmptyTrash(position)
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
}