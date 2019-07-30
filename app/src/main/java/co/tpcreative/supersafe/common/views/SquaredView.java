package co.tpcreative.supersafe.common.views;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;


public class SquaredView extends View {
    public SquaredView(Context context) {
        super(context);
    }

    public SquaredView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }

    public void toggle(){
        setVisibility(getVisibility()==VISIBLE?INVISIBLE:VISIBLE);
    }
}
