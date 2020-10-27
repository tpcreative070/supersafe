package co.tpcreative.supersafe.ui.multiselects.adapter
import android.content.Context
import android.view.LayoutInflater
import android.widget.BaseAdapter

abstract class CustomGenericAdapter<T>(context: Context?, protected var arrayList: ArrayList<T>?) : BaseAdapter() {
    protected var context: Context?
    protected var layoutInflater: LayoutInflater?
    protected var size = 0
    override fun getCount(): Int {
        return arrayList!!.size
    }

    override fun getItem(position: Int): T? {
        return arrayList?.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setLayoutParams(size: Int) {
        this.size = size
    }

    fun releaseResources() {
        arrayList = null
        context = null
    }

    init {
        this.context = context
        layoutInflater = LayoutInflater.from(this.context)
    }
}