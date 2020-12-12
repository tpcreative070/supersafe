package co.tpcreative.supersafe.common.views
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent

class GestureTap(private val listener: GestureTapListener?) : GestureDetector.SimpleOnGestureListener() {
    override fun onDoubleTap(e: MotionEvent?): Boolean {
        Log.i("onDoubleTap :", "" + e?.getAction())
        listener?.onDoubleTap()
        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        Log.i("onSingleTap :", "" + e?.getAction())
        listener?.onSingleTap()
        return true
    }

    interface GestureTapListener {
        open fun onDoubleTap()
        open fun onSingleTap()
    }

}