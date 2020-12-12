package co.tpcreative.supersafe.common.views
import android.content.Context
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.clearDecorations() {
    if (itemDecorationCount > 0) {
        for (i in itemDecorationCount - 1 downTo 0) {
            removeItemDecorationAt(i)
        }
    }
}

fun RecyclerView.addListOfDecoration(context: Context) {
    this.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
}

fun RecyclerView.addGridOfDecoration(spanCount : Int,spacing: Int) {
    this.addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, true))
}