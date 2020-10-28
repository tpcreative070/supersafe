package co.tpcreative.supersafe.ui.breakinalerts
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import co.tpcreative.supersafe.model.BreakInAlertsModel
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import kotlinx.android.synthetic.main.break_in_alerts_item.view.*
import java.io.File
import java.util.*

class BreakInAlertsAdapter(inflater: LayoutInflater, activity: Activity?, itemSelectedListener: ItemSelectedListener?) : BaseAdapter<BreakInAlertsModel, BaseHolder<BreakInAlertsModel>>(inflater) {
    private val myActivity: Activity? = activity
    private val itemSelectedListener: ItemSelectedListener? = itemSelectedListener
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
        val imgPicture = itemView.imgPicture
        val tvTime = itemView.tvTime
        val tvPin = itemView.tvPin
        private var mPosition = 0
        override fun bind(data: BreakInAlertsModel, position: Int) {
            super.bind(data, position)
            this.mPosition = position
            val locale = Locale("en")
            Locale.setDefault(locale)
            val messages: TimeAgoMessages = TimeAgoMessages.Builder().withLocale(locale).build()
            tvTime?.text = TimeAgo.using(data.time, messages)
            tvPin?.text = data.pin
            Glide.with(myActivity!!)
                    .load(File(data.fileName))
                    .apply(options!!).into(imgPicture!!)

            itemView.llHome.setOnClickListener {
                itemSelectedListener?.onClickItem(mPosition)
            }
        }
    }

    interface ItemSelectedListener {
        fun onClickItem(position: Int)
    }

}