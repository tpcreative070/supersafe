package co.tpcreative.supersafe.ui.theme
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.ThemeApp
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.theme_item.view.*

class ThemeSettingsAdapter(inflater: LayoutInflater, private val context: Context?, private val itemSelectedListener: ItemSelectedListener?) : BaseAdapter<ThemeApp, BaseHolder<ThemeApp>>(inflater) {
    private val TAG = ThemeSettingsAdapter::class.java.simpleName
    var options: RequestOptions = RequestOptions()
            .centerCrop()
            .override(60, 60)
            .priority(Priority.HIGH)

    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<ThemeApp> {
        return ItemHolder(inflater!!.inflate(R.layout.theme_item, parent, false))
    }

    interface ItemSelectedListener {
        open fun onClickItem(position: Int)
    }

    inner class ItemHolder(itemView: View) : BaseHolder<ThemeApp>(itemView) {
        val imgTheme = itemView.imgTheme
        val imgChecked = itemView.imgChecked
        var mPosition = 0
        var themeApp: ThemeApp? = null
        override fun bind(data: ThemeApp, position: Int) {
            super.bind(data, position)
            mPosition = position
            Glide.with(context!!)
                    .load(ContextCompat.getDrawable(context,data.getPrimaryColor()))
                    .apply(options).into(imgTheme!!)
            themeApp = data
            if (data.isCheck) {
                imgChecked?.visibility = View.VISIBLE
            } else {
                imgChecked?.visibility = View.INVISIBLE
            }
            Utils.Log(TAG, "Change position $position")
            itemView.rlHome.setOnClickListener {
                if (itemSelectedListener != null) {
                    for (i in mDataSource.indices) {
                        if (mDataSource[i].isCheck) {
                            mDataSource[i].isCheck = false
                            notifyItemChanged(i)
                        }
                    }
                    mDataSource[mPosition].isCheck = true
                    itemSelectedListener.onClickItem(mPosition)
                }
            }
        }
    }
}