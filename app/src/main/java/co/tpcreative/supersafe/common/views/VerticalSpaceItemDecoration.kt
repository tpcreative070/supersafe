package co.tpcreative.supersafe.common.views
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class VerticalSpaceItemDecoration(private val mVerticalSpaceHeight: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                state: RecyclerView.State) {
        if (parent.getChildAdapterPosition(view) != (parent.getAdapter()?.getItemCount()?.minus(1))) {
            outRect.bottom = mVerticalSpaceHeight
        }
    }

}