package co.tpcreative.supersafe.ui.help
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.model.HelpAndSupportModel
import com.jaychang.srv.SimpleCell
import com.jaychang.srv.SimpleViewHolder
import kotlinx.android.synthetic.main.help_support_items.view.*

class HelpAndSupportCell(item: HelpAndSupportModel) : SimpleCell<HelpAndSupportModel, HelpAndSupportCell.ViewHolder>(item) {
    private var listener: ItemSelectedListener? = null
    fun setListener(listener: ItemSelectedListener?) {
        this.listener = listener
    }

    override fun getLayoutRes(): Int {
        return R.layout.help_support_items
    }

    /*
    - Return a ViewHolder instance
     */
    override fun onCreateViewHolder(parent: ViewGroup, cellView: View): HelpAndSupportCell.ViewHolder {
        return ViewHolder(cellView)
    }

    /*
    - Bind data to widgets in our viewholder.
     */
    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int, context: Context, o: Any?) {
        val data: HelpAndSupportModel = item
        viewHolder.tvTitle.text = data.title
        if (data.nummberName != null) {
            viewHolder.tvPosition.setText(data.nummberName)
            viewHolder.imgIcon.visibility = View.GONE
        }
        viewHolder.llHome.setOnClickListener {
            if (listener != null) {
                listener?.onClickItem(i)
            }
        }
    }

    /**
     * - Our ViewHolder class.
     * - Inner static class.
     * Define your view holder, which must extend SimpleViewHolder.
     */
    class ViewHolder(itemView: View) : SimpleViewHolder(itemView) {
        val tvTitle: AppCompatTextView = itemView.tvTitle
        val imgIcon: AppCompatImageView = itemView.imgIcon
        val llHome: LinearLayout = itemView.llHome
        var tvPosition: AppCompatTextView = itemView.tvPosition
    }

    interface ItemSelectedListener {
        fun onClickItem(position: Int)
    }

    companion object {
        private val TAG = HelpAndSupportCell::class.java.simpleName
    }
}