package co.tpcreative.supersafe.ui.player
import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.model.ThemeApp
import kotlinx.android.synthetic.main.activity_player.view.tvTitle
import kotlinx.android.synthetic.main.item_player.view.*

class PlayerAdapter(inflater: LayoutInflater, private val mContext: Context?,private val itemSelectedListener: ItemSelectedListener?) : BaseAdapter<ItemModel, BaseHolder<ItemModel>>(inflater) {
    private val TAG = PlayerAdapter::class.java.simpleName
    private val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<ItemModel> {
        return ItemHolder(inflater!!.inflate(R.layout.item_player, parent, false))
    }

    interface ItemSelectedListener {
        fun onClickGalleryItem(position: Int)
    }

    inner class ItemHolder(itemView: View) : BaseHolder<ItemModel>(itemView) {
        val tvTitle: AppCompatTextView = itemView.tvTitle
        var imgPlaying: AppCompatImageView = itemView.imgPlaying
        private var mPosition = 0
        private var data: ItemModel? = null
        override fun bind(data: ItemModel, position: Int) {
            super.bind(data, position)
            this.data = data
            if (this.data?.isChecked!!) {
                imgPlaying.visibility = View.VISIBLE
            } else {
                imgPlaying.visibility = View.INVISIBLE
            }
            themeApp?.getAccentColor()?.let { ContextCompat.getColor(SuperSafeApplication.getInstance(), it) }?.let { imgPlaying.setColorFilter(it, PorterDuff.Mode.SRC_IN) }
            Utils.Log(TAG, "position :" + this.data?.isChecked)
            tvTitle.text = this.data?.title
            mPosition = position
            itemView.rl_item.setOnClickListener {
                itemSelectedListener?.onClickGalleryItem(mPosition)
            }
        }
    }
}