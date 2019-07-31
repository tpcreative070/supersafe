package co.tpcreative.supersafe.common.preference;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceViewHolder;


public class MySwitchPreference extends CheckBoxPreference {

    private Context context;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MySwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        //init();
    }

    public MySwitchPreference(Context context) {
        super(context);
        this.context = context;
    }

    public MySwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        //init();
    }

    public MySwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        //init();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        SwitchCompat checkBox = (SwitchCompat) view.findViewById(android.R.id.checkbox);
        checkBox.setVisibility(View.VISIBLE);
    }

}
