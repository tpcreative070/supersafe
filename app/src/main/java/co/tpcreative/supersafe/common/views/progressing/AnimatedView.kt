package co.tpcreative.supersafe.common.views.progressing
import android.content.Context
import android.view.View

internal class AnimatedView(context: Context?) : View(context) {
    var target = 0
    var xFactor: Float
        get() = x / target
        set(xFactor) {
            x = target * xFactor
        }
}