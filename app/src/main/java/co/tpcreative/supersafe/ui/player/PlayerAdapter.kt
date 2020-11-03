package co.tpcreative.supersafe.ui.player
import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.model.ThemeApp
import com.snatik.storage.Storage
import kotlinx.android.synthetic.main.activity_player.view.tvTitle
import kotlinx.android.synthetic.main.item_player.view.*

class PlayerAdapter(inflater: LayoutInflater, private val mContext: Context?, itemSelectedListener: ItemSelectedListener?) : BaseAdapter<ItemModel, BaseHolder<ItemModel>>(inflater) {
    private val storage: Storage?
    private val ls: ItemSelectedListener?
    private val TAG = PlayerAdapter::class.java.simpleName
    private val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return mDataSource!!.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<ItemModel> {
        return ItemHolder(inflater!!.inflate(R.layout.item_player, parent, false))
    }

    interface ItemSelectedListener {
        open fun onClickGalleryItem(position: Int)
    }

    inner class ItemHolder(itemView: View) : BaseHolder<ItemModel>(itemView) {
        val tvTitle = itemView.tvTitle
        var imgPlaying = itemView.imgPlaying
        private var mPosition = 0
        private var data: ItemModel? = null
        override fun bind(mData: ItemModel, position: Int) {
            super.bind(mData, position)
            data = mData
            if (data?.isChecked!!) {
                imgPlaying?.visibility = View.VISIBLE
            } else {
                imgPlaying?.visibility = View.INVISIBLE
            }
            themeApp?.getAccentColor()?.let { ContextCompat.getColor(SuperSafeApplication.getInstance(), it) }?.let { imgPlaying?.setColorFilter(it, PorterDuff.Mode.SRC_IN) }
            Utils.Log(TAG, "position :" + data?.isChecked)
            tvTitle?.text = data?.title
            mPosition = position
            itemView.rl_item.setOnClickListener {
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