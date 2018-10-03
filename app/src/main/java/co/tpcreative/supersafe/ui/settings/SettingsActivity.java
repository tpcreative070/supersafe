package co.tpcreative.supersafe.ui.settings;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import com.ftinc.kit.util.SizeUtils;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.preference.MyPreference;
import co.tpcreative.supersafe.common.util.Utils;
import de.mrapp.android.preference.ListPreference;


public class SettingsActivity extends BaseActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    private SlidrConfig mConfig;
    private static final String FRAGMENT_TAG = SettingsActivity.class.getSimpleName() + "::fragmentTag";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //android O fix bug orientation
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int primary = getResources().getColor(R.color.colorPrimary);
        int secondary = getResources().getColor(R.color.colorPrimaryDark);

        mConfig = new SlidrConfig.Builder()
                .primaryColor(primary)
                .secondaryColor(secondary)
                .position(SlidrPosition.LEFT)
                .velocityThreshold(2400)
                .touchSize(SizeUtils.dpToPx(this, 32))
                .build();
        Slidr.attach(this, mConfig);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);

        if (fragment == null) {
            fragment = Fragment.instantiate(this, SettingsFragment.class.getName());
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();


    }


    public static class SettingsFragment extends PreferenceFragmentCompat {

        /**
         * The {@link ListPreference}.
         */

        private MyPreference mAccount;

        private MyPreference mLockScreen;

        /**
         * Creates and returns a listener, which allows to adapt the app's theme, when the value of the
         * corresponding preference has been changed.
         *
         * @return The listener, which has been created, as an instance of the type {@link
         * Preference.OnPreferenceChangeListener}
         */

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
                    if (preference instanceof MyPreference){
                        if (preference.getKey().equals(getString(R.string.key_account))){
                            Log.d(TAG,"value : ");
                            Navigator.onManagerAccount(getContext());
                        }
                        else if (preference.getKey().equals(getString(R.string.key_lock_screen))){
                            Navigator.onMoveToChangePin(getContext(),false);
                            Utils.Log(TAG,"Action here");
                        }
                    }
                    return true;
                }
            };
        }

        @Override
        public final void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            /*Account*/
            mAccount = (MyPreference)findPreference(getString(R.string.key_account));
            mAccount.setOnPreferenceChangeListener(createChangeListener());
            mAccount.setOnPreferenceClickListener(createActionPreferenceClickListener());

            mLockScreen = (MyPreference) findPreference(getString(R.string.key_lock_screen));
            mLockScreen.setOnPreferenceChangeListener(createChangeListener());
            mLockScreen.setOnPreferenceClickListener(createActionPreferenceClickListener());
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_general);
        }

    }



}
