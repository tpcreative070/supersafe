package co.tpcreative.supersafe.ui.enterpin
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
class ItemSpaceDecoration(private val mHorizontalSpaceWidth: Int, private val mVerticalSpaceHeight: Int, private val mSpanCount: Int, private val mIncludeEdge: Boolean) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position: Int = parent.getChildAdapterPosition(view)
        val column = position % mSpanCount
        if (mIncludeEdge) {
            outRect.right = mHorizontalSpaceWidth - column * mHorizontalSpaceWidth / mSpanCount
            outRect.left = (column + 1) * mHorizontalSpaceWidth / mSpanCount
            if (position < mSpanCount) {
                outRect.top = mVerticalSpaceHeight
            }
            outRect.bottom = mVerticalSpaceHeight
        } else {
            outRect.right = column * mHorizontalSpaceWidth / mSpanCount
            outRect.left = mHorizontalSpaceWidth - (column + 1) * mHorizontalSpaceWidth / mSpanCount
            if (position >= mSpanCount) {
                outRect.top = mVerticalSpaceHeight
            }
        }
    }
}