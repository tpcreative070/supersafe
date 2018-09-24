package co.tpcreative.supersafe.common.preference;
import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import co.tpcreative.supersafe.R;
import de.mrapp.android.preference.Preference;


public class MyPreference extends Preference {

    private Context context;

    public MyPreference(Context context) {
        super(context);
        this.context = context ;
    }

    public MyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public MyPreference(Context context, AttributeSet attrs,int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        ImageView imageView = (ImageView) view.findViewById(android.R.id.icon);
        imageView.setColorFilter(getContext().getResources().getColor(R.color.material_gray_700), PorterDuff.Mode.SRC_ATOP);
    }

}
