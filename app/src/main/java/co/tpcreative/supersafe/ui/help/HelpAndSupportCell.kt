package co.tpcreative.supersafe.ui.helpimport
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import butterknife.BindView
import butterknife.ButterKnife
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.model.HelpAndSupport
import com.jaychang.srv.SimpleCell
import com.jaychang.srv.SimpleViewHolder

class HelpAndSupportCell(item: HelpAndSupport) : SimpleCell<HelpAndSupport, HelpAndSupportCell.ViewHolder>(item) {
    private var listener: ItemSelectedListener? = null
    fun setListener(listener: HelpAndSupportCell.ItemSelectedListener?) {
        this.listener = listener
    }

    protected override fun getLayoutRes(): Int {
        return R.layout.help_support_items
    }

    /*
    - Return a ViewHolder instance
     */
    protected override fun onCreateViewHolder(parent: ViewGroup, cellView: View): HelpAndSupportCell.ViewHolder {
        return ViewHolder(cellView)
    }

    /*
    - Bind data to widgets in our viewholder.
     */
    protected override fun onBindViewHolder(viewHolder: ViewHolder, i: Int, context: Context, o: Any?) {
        val data: HelpAndSupport = getItem()
        viewHolder.tvTitle?.setText(data.title)
        if (data.nummberName != null) {
            viewHolder.tvPosition?.setText(data.nummberName)
            viewHolder.imgIcon?.visibility = View.GONE
        }
        viewHolder.llHome?.setOnClickListener {
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
        @BindView(R.id.tvTitle)
        var tvTitle: AppCompatTextView? = null

        @BindView(R.id.imgIcon)
        var imgIcon: AppCompatImageView? = null

        @BindView(R.id.llHome)
        var llHome: LinearLayout? = null

        @BindView(R.id.tvPosition)
        var tvPosition: AppCompatTextView? = null

        init {
            ButterKnife.bind(this, itemView)
        }
    }

    interface ItemSelectedListener {
        open fun onClickItem(position: Int)
    }

    companion object {
        private val TAG = HelpAndSupportCell::class.java.simpleName
    }
}