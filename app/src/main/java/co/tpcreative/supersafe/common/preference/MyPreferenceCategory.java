package co.tpcreative.supersafe.common.preference;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;
import co.tpcreative.supersafe.model.ThemeApp;


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
        final ThemeApp themeApp = ThemeApp.getInstance().getThemeInfo();
        titleView.setTextColor(getContext().getResources().getColor(themeApp.getAccentColor()));
        titleView.setAllCaps(false);
        titleView.setTextSize(17);
    }

}
