package co.tpcreative.supersafe.common.adapter
import android.view.View
import androidx.recyclerview.widget.RecyclerView
open class BaseHolder<V>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    open fun bind(data: V, position: Int) {}
    fun event() {}
}