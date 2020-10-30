package co.tpcreative.supersafe.common.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.common.util.Utils
import java.util.*

open class BaseAdapter<V, VH : BaseHolder<V>>(inflater: LayoutInflater) : RecyclerView.Adapter<VH>() {
    protected var inflater: LayoutInflater? = inflater
    protected var mDataSource: MutableList<V> = Collections.emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (holder is FooterViewHolder<*>) {
            return
        }
        holder.bind(mDataSource[position]!!, position)
        holder.event()
    }

    override fun getItemId(position: Int): Long {
        return getItemId(position).hashCode().toLong()
    }

    fun getItem(position: Int): V? {
        return mDataSource[position]
    }

    override fun getItemCount(): Int {
        return mDataSource.size
    }

    fun setDataSource(dataSource: MutableList<V>?) {
        try {
            this.mDataSource = ArrayList()
            dataSource?.let {
                this.mDataSource = it
            }
            notifyDataSetChanged()
        } catch (e: IllegalStateException) {
        }
    }

    fun getDataSource(): MutableList<V>? {
        return mDataSource
    }

    fun appendItem(item: V) {
        if (mDataSource.isEmpty()) {
            mDataSource = ArrayList()
        }
        mDataSource.add(item)
        notifyItemInserted(itemCount)
    }

    fun removeAtPosition(position: Int) {
        if (mDataSource.size > position) {
            mDataSource.removeAt(position)
            notifyItemRangeRemoved(position, 1)
        }
    }

    fun removeAt(position: Int) {
        try {
            if (mDataSource.size > position) {
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, mDataSource.size)
            } else {
                Utils.Log(TAG, "Can not delete ")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun appendItems(items: MutableList<V>) {
        if (mDataSource.isEmpty()) {
            setDataSource(items)
        } else {
            val positionStart = itemCount - 1
            mDataSource.addAll(items)
            notifyItemRangeInserted(positionStart, items.size)
        }
    }

    fun addItemAtFirst(item: V) {
        if (mDataSource.isEmpty()) {
            mDataSource = ArrayList()
        }
        mDataSource.add(0, item)
        notifyItemInserted(0)
    }

    fun addAtFirstAndRemoveEnd(item: V) {
        if (mDataSource.isEmpty()) {
            mDataSource = ArrayList()
        }
        mDataSource.add(0, item)
        mDataSource.removeAt(itemCount - 1)
        notifyItemRemoved(itemCount - 1)
        notifyItemInserted(0)
    }

    companion object {
        private val TAG = BaseAdapter::class.java.simpleName
    }

}