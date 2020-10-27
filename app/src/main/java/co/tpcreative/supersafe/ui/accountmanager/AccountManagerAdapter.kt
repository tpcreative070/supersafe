package co.tpcreative.supersafe.ui.accountmanager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Encrypter
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.model.AppLists
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.snatik.storage.Storage
import kotlinx.android.synthetic.main.app_items.view.*
import java.security.NoSuchAlgorithmException

class AccountManagerAdapter(inflater: LayoutInflater, private val context: Context?, itemSelectedListener: ItemSelectedListener?) : BaseAdapter<AppLists, BaseHolder<AppLists>>(inflater) {
    var options: RequestOptions? = RequestOptions()
            .centerCrop()
            .override(400, 400)
            .placeholder(R.drawable.baseline_music_note_white_48)
            .error(R.drawable.baseline_music_note_white_48)
            .priority(Priority.HIGH)
    private val itemSelectedListener: ItemSelectedListener?
    private var encrypter: Encrypter? = null
    private val storage: Storage?
    private val TAG = AccountManagerAdapter::class.java.simpleName
    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<AppLists> {
        return ItemHolder(inflater!!.inflate(R.layout.app_items, parent, false))
    }

    interface ItemSelectedListener {
        open fun onClickItem(position: Int)
    }

    inner class ItemHolder(itemView: View) : BaseHolder<AppLists>(itemView) {
        val imgIconApp = itemView.imgIconApp
        var tvTitle = itemView.tvTitle
        var tvDescription = itemView.tvDescription
        var tvStatus = itemView.tvStatus
        var mPosition = 0
        var items: AppLists? = null
        override fun bind(data: AppLists, position: Int) {
            super.bind(data, position)
            mPosition = position
            items = data
            try {
                imgIconApp?.setImageDrawable(SQLHelper.getDrawable(context, data.ic_name))
                tvTitle?.text = data.title
                tvDescription?.text = data.description
                if (data.isInstalled) {
                    tvStatus?.text = SuperSafeApplication.getInstance().getString(R.string.installed)
                    tvStatus?.setTextColor(ContextCompat.getColor(SuperSafeApplication.getInstance(),R.color.material_green_300))
                } else {
                    tvStatus?.text = SuperSafeApplication.getInstance().getString(R.string.learn_more)
                    tvStatus?.setTextColor(ContextCompat.getColor(SuperSafeApplication.getInstance(),R.color.colorButton))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            itemView.llHome.setOnClickListener {
                itemSelectedListener?.onClickItem(mPosition)
            }
        }
    }

    init {
        storage = Storage(context)
        this.itemSelectedListener = itemSelectedListener
        try {
            encrypter = Encrypter()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }
}