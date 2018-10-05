package co.tpcreative.supersafe.ui.settings;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;

public class SettingsActivity extends BaseActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private static final String FRAGMENT_TAG = SettingsActivity.class.getSimpleName() + "::fragmentTag";
    private static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        activity = this;
        onDrawOverLay(this);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);

        if (fragment == null) {
            fragment = Fragment.instantiate(this, SettingsFragment.class.getName());
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case Navigator.THEME_SETTINGS :{
                if (resultCode==RESULT_OK){
                    Utils.Log(TAG,"recreate........................");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recreate();
                        }
                    });
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
        }
        return false;
    }


    public static class SettingsFragment extends PreferenceFragmentCompat {

        private Preference mAccount;

        private Preference mLockScreen;

        private Preference mTheme;

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
                    if (preference instanceof Preference){
                        if (preference.getKey().equals(getString(R.string.key_account))){
                            Log.d(TAG,"value : ");
                            Navigator.onManagerAccount(getContext());
                        }
                        else if (preference.getKey().equals(getString(R.string.key_lock_screen))){
                            Navigator.onMoveToChangePin(getContext(),false);
                            Utils.Log(TAG,"Action here");
                        }
                        else if (preference.getKey().equals(getString(R.string.key_theme))){
                            Navigator.onMoveThemeSettings(activity);
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
            mAccount = findPreference(getString(R.string.key_account));
            mAccount.setOnPreferenceChangeListener(createChangeListener());
            mAccount.setOnPreferenceClickListener(createActionPreferenceClickListener());

            /*Lock Screen*/
            mLockScreen =  findPreference(getString(R.string.key_lock_screen));
            mLockScreen.setOnPreferenceChangeListener(createChangeListener());
            mLockScreen.setOnPreferenceClickListener(createActionPreferenceClickListener());

            /*Update Theme*/
            mTheme = findPreference(getString(R.string.key_theme));
            mTheme.setOnPreferenceClickListener(createActionPreferenceClickListener());
            mTheme.setOnPreferenceChangeListener(createChangeListener());

        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_general);
        }

    }



}
