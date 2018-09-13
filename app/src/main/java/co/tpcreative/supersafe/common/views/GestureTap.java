package co.tpcreative.supersafe.common.views;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class GestureTap extends GestureDetector.SimpleOnGestureListener {

    private GestureTapListener listener;
    public GestureTap(GestureTapListener listener){
        this.listener = listener;
    }


    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.i("onDoubleTap :", "" + e.getAction());
        if (listener!=null){
            listener.onDoubleTap();
        }
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.i("onSingleTap :", "" + e.getAction());
        if (listener!=null){
            listener.onSingleTap();
        }
        return true;
    }

    public interface GestureTapListener{
        void onDoubleTap();
        void onSingleTap();
    }

}