package co.tpcreative.supersafe.common.adapter
import android.R
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.common.adapter.DividerItemDecoration

class DividerItemDecoration(context: Context?, orientation: Int) : RecyclerView.ItemDecoration() {
    private var mDivider: Drawable? = null
    private var mOrientation = 0
    private var mMarginTop = 0
    private var mMarginBottom = 0
    private var mMarginLeft = 0
    private var mMarginRight = 0
    fun setDrawable(mResource: Drawable?) {
        mDivider = mResource
    }

    fun getDivider(): Drawable? {
        return mDivider
    }

    fun setOrientation(orientation: Int) {
        require(!(orientation != DividerItemDecoration.Companion.HORIZONTAL_LIST && orientation != DividerItemDecoration.Companion.VERTICAL_LIST)) { "invalid orientation" }
        mOrientation = orientation
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (mOrientation == DividerItemDecoration.Companion.VERTICAL_LIST) {
            drawVertical(c, parent)
        } else {
            drawHorizontal(c, parent)
        }
    }

    fun drawVertical(c: Canvas?, parent: RecyclerView) {
        val left: Int = parent.getPaddingLeft()
        val right: Int = parent.getWidth() - parent.getPaddingRight()
        val childCount: Int = parent.getChildCount()
        for (i in 0 until childCount) {
            val child: View = parent.getChildAt(i)
            val params: RecyclerView.LayoutParams = child
                    .layoutParams as RecyclerView.LayoutParams
            val top: Int = child.bottom + params.bottomMargin
            val bottom = top + mDivider?.getIntrinsicHeight()!!
            mDivider?.setBounds(left, top, right, bottom)
            if (c != null) {
                mDivider?.draw(c)
            }
        }
    }

    fun drawHorizontal(c: Canvas?, parent: RecyclerView) {
        val top: Int = parent.getPaddingTop()
        val bottom: Int = parent.getHeight() - parent.getPaddingBottom()
        val childCount: Int = parent.getChildCount()
        for (i in 0 until childCount) {
            val child: View = parent.getChildAt(i)
            val params: RecyclerView.LayoutParams = child
                    .layoutParams as RecyclerView.LayoutParams
            val left: Int = child.right + params.rightMargin
            val right = left + mDivider!!.getIntrinsicHeight()
            mDivider?.setBounds(left, top, right, bottom)
            if (c != null) {
                mDivider?.draw(c)
            }
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (mOrientation == DividerItemDecoration.Companion.VERTICAL_LIST) {
            outRect.set(0, 0, 0, mDivider!!.getIntrinsicHeight())
        } else {
            outRect.set(0, 0, mDivider!!.getIntrinsicWidth(), 0)
        }
    }

    fun setMarginTop(marginTop: Int) {
        mMarginTop = marginTop
    }

    fun getMarginTop(): Int {
        return mMarginTop
    }

    fun setMarginBottom(marginBottom: Int) {
        mMarginBottom = marginBottom
    }

    fun getMarginBottom(): Int {
        return mMarginBottom
    }

    fun setMarginLeft(mMarginLeft: Int) {
        this.mMarginLeft = mMarginLeft
    }

    fun getMarginLeft(): Int {
        return mMarginLeft
    }

    fun setMarginRight(marginRight: Int) {
        mMarginRight = marginRight
    }

    fun getMarginRight(): Int {
        return mMarginRight
    }

    companion object {
        private val ATTRS: IntArray? = intArrayOf(
                R.attr.listDivider
        )
        val TAG = DividerItemDecoration::class.java.simpleName
        val HORIZONTAL_LIST: Int = androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
        val VERTICAL_LIST: Int = androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
    }

    init {
        val a: TypedArray? = DividerItemDecoration.ATTRS?.let { context!!.obtainStyledAttributes(it) }
        if (mDivider == null) {
            mDivider = a?.getDrawable(0)
        }
        a?.recycle()
        setOrientation(orientation)
    }
}