package co.tpcreative.supersafe.ui.player
import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.model.ThemeApp
import com.snatik.storage.Storage

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
        @BindView(R.id.tvTitle)
        var tvTitle: AppCompatTextView? = null
        @BindView(R.id.imgPlaying)
        var imgPlaying: AppCompatImageView? = null
        private var mPosition = 0
        private var data: ItemModel? = null
        override fun bind(mData: ItemModel, position: Int) {
            super.bind(mData, position)
            data = mData
            if (data?.isChecked!!) {
                imgPlaying?.setVisibility(View.VISIBLE)
            } else {
                imgPlaying?.setVisibility(View.INVISIBLE)
            }
            themeApp?.getAccentColor()?.let { ContextCompat.getColor(SuperSafeApplication.getInstance(), it) }?.let { imgPlaying?.setColorFilter(it, PorterDuff.Mode.SRC_IN) }
            Utils.Log(TAG, "position :" + data?.isChecked)
            tvTitle?.setText(data?.title)
            mPosition = position
        }

        @OnClick(R.id.rl_item)
        fun onClickedItem(view: View?) {
            ls?.onClickGalleryItem(mPosition)
        }
    }

    init {
        storage = Storage(mContext)
        storage.setEncryptConfiguration(SuperSafeApplication.Companion.getInstance().getConfigurationFile())
        ls = itemSelectedListener
    }
}