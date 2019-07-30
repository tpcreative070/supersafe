package co.tpcreative.supersafe.common.preference;
import android.content.Context;
import android.util.AttributeSet;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class MyPreference extends Preference {

    private static final String TAG = MyPreference.class.getSimpleName();
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

    }

}
