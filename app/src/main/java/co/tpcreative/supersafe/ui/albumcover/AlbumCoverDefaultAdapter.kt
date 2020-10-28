package co.tpcreative.supersafe.ui.albumcover
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
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
import co.tpcreative.supersafe.model.MainCategoryModel
import co.tpcreative.supersafe.model.ThemeApp
import kotlinx.android.synthetic.main.album_cover_item.view.*

class AlbumCoverDefaultAdapter(inflater: LayoutInflater, private val context: Context?, private val itemSelectedListener: ItemSelectedListener?) : BaseAdapter<MainCategoryModel, BaseHolder<MainCategoryModel>>(inflater) {
    private val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    private val TAG = AlbumCoverDefaultAdapter::class.java.simpleName
    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<MainCategoryModel> {
        return ItemHolder(inflater!!.inflate(R.layout.album_cover_item, parent, false))
    }

    interface ItemSelectedListener {
        open fun onClickedDefaultItem(position: Int)
    }

    inner class ItemHolder(itemView: View) : BaseHolder<MainCategoryModel>(itemView) {
        val imgAlbum = itemView.imgAlbum
        val imgIcon = itemView.imgIcon
        val imgSelect = itemView.imgSelect
        val view_alpha = itemView.view_alpha
        var mPosition = 0
        var categories: MainCategoryModel? = null
        override fun bind(data: MainCategoryModel, position: Int) {
            super.bind(data, position)
            mPosition = position
            Utils.Log(TAG, "load data")
            categories = data
            if (data.isChecked) {
                view_alpha?.alpha = 0.5f
                imgIcon?.setColorFilter(ContextCompat.getColor(SuperSafeApplication.getInstance(),themeApp!!.getAccentColor()), PorterDuff.Mode.SRC_IN)
                imgSelect?.visibility = View.VISIBLE
            } else {
                imgIcon?.setColorFilter(ContextCompat.getColor(SuperSafeApplication.getInstance(),R.color.white), PorterDuff.Mode.SRC_IN)
                view_alpha?.alpha = 0.0f
                imgSelect?.visibility = View.INVISIBLE
            }
            try {
                imgAlbum?.setImageResource(0)
                val myColor = Color.parseColor(categories?.image)
                imgAlbum?.setBackgroundColor(myColor)
                imgIcon?.setImageDrawable(SQLHelper.getDrawable(context, categories?.icon))
                imgIcon?.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
            }
            itemView.rlHome.setOnClickListener {
                itemSelectedListener?.onClickedDefaultItem(mPosition)
            }
        }
    }

}