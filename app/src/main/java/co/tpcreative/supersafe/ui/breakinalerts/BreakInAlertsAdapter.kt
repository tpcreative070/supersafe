package co.tpcreative.supersafe.ui.breakinalertsimport
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import butterknife.BindView
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import co.tpcreative.supersafe.model.BreakInAlertsModel
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import java.io.File
import java.util.*

class BreakInAlertsAdapter(inflater: LayoutInflater, activity: Activity?, itemSelectedListener: ItemSelectedListener?) : BaseAdapter<BreakInAlertsModel, BaseHolder<BreakInAlertsModel>>(inflater) {
    private val myActivity: Activity?
    private val itemSelectedListener: ItemSelectedListener?
    private val TAG = BreakInAlertsAdapter::class.java.simpleName
    var options: RequestOptions? = RequestOptions()
            .centerCrop()
            .override(70, 70)
            .priority(Priority.HIGH)

    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<BreakInAlertsModel> {
        return ItemHolder(inflater!!.inflate(R.layout.break_in_alerts_item, parent, false))
    }

    inner class ItemHolder(itemView: View) : BaseHolder<BreakInAlertsModel>(itemView) {
        @BindView(R.id.imgPicture)
        var imgPicture: AppCompatImageView? = null

        @BindView(R.id.tvTime)
        var tvTime: AppCompatTextView? = null

        @BindView(R.id.tvPin)
        var tvPin: AppCompatTextView? = null
        private var mPosition = 0
        override fun bind(data: BreakInAlertsModel, position: Int) {
            super.bind(data, position)
            this.mPosition = position
            val locale = Locale("en")
            Locale.setDefault(locale)
            val messages: TimeAgoMessages = TimeAgoMessages.Builder().withLocale(locale).build()
            tvTime?.setText(TimeAgo.using(data.time, messages))
            tvPin?.setText(data.pin)
            Glide.with(myActivity!!)
                    .load(File(data.fileName))
                    .apply(options!!).into(imgPicture!!)
        }

        @OnClick(R.id.llHome)
        fun onClickedItem(view: View?) {
            itemSelectedListener?.onClickItem(position)
        }
    }

    interface ItemSelectedListener {
        open fun onClickItem(position: Int)
    }

    init {
        myActivity = activity
        this.itemSelectedListener = itemSelectedListener
    }
}