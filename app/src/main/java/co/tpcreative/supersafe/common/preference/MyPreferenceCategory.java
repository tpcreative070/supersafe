package co.tpcreative.supersafe.common.preference;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.model.Theme;


public class MyPreferenceCategory extends PreferenceCategory {

    public MyPreferenceCategory(Context context) {
        super(context);
    }

    public MyPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyPreferenceCategory(Context context, AttributeSet attrs,int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        final Theme theme = Theme.getInstance().getThemeInfo();
        titleView.setTextColor(getContext().getResources().getColor(theme.getAccentColor()));
        titleView.setAllCaps(false);
        titleView.setTextSize(17);
    }

}
