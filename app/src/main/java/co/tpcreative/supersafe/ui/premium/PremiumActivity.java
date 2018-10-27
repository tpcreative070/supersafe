package co.tpcreative.supersafe.ui.premium;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.SingletonPremiumTimer;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.ui.settings.SettingsActivity;

public class PremiumActivity extends BaseActivity implements SingletonPremiumTimer.SingletonPremiumTimerListener{

    private static final String TAG = PremiumActivity.class.getSimpleName();
    private static final String FRAGMENT_TAG = SettingsActivity.class.getSimpleName() + "::fragmentTag";
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.tvPremiumLeft)
    TextView tvPremiumLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null) {
            fragment = Fragment.instantiate(this, PremiumActivity.SettingsFragment.class.getName());
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();

        String value = Utils.getFontString(R.string.your_complimentary_premium_remaining,"30");
        tvPremiumLeft.setText(Html.fromHtml(value));
        onDrawOverLay(this);
    }

    @Override
    public void onPremiumTimer(String days, String hours, String minutes, String seconds) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String value = Utils.getFontString(R.string.your_complimentary_premium_remaining,days);
                    tvPremiumLeft.setText(Html.fromHtml(value));
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @OnClick(R.id.llMonths)
    public void onClickedMonths(View view){
        Utils.Log(TAG,"Months");
    }

    @OnClick(R.id.llYears)
    public void onClickedYears(View view){
        Utils.Log(TAG,"Years");
    }

    @OnClick(R.id.llLifeTime)
    public void onClickedLifeTime(View view){
        Utils.Log(TAG,"LifeTime");
    }

    @Override
    public void onStillScreenLock(EnumStatus status) {
        switch (status){
            case FINISH:{
                finish();
                break;
            }
        }
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onRegisterHomeWatcher();
        SuperSafeApplication.getInstance().writeKeyHomePressed(PremiumActivity.class.getSimpleName());
        SingletonPremiumTimer.getInstance().setListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SingletonPremiumTimer.getInstance().setListener(null);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private Preference mAccount;



        private Preference.OnPreferenceChangeListener createChangeListener() {
            return new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(final Preference preference, final Object newValue) {
                    return true;
                }
            };
        }

        private Preference.OnPreferenceClickListener createActionPreferenceClickListener() {
            return new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (preference instanceof Preference){

                    }
                    return true;
                }
            };
        }

        @Override
        public final void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_general_premium);
        }
    }
}
