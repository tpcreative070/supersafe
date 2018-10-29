package co.tpcreative.supersafe.ui.settings;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import co.tpcreative.supersafe.BuildConfig;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;

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
    public void onNotifier(EnumStatus status) {
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            case Navigator.ENABLE_CLOUD:{
                Utils.Log(TAG,"onResultResponse :" + resultCode);
                if (resultCode==RESULT_OK){
                    final User mUser = User.getInstance().getUserInfo();
                    if (mUser!=null){
                        if (mUser.verified){
                            if (!mUser.driveConnected){
                                Navigator.onCheckSystem(this,null);
                            }
                            else{
                                Navigator.onManagerCloud(this);
                            }
                        }
                        else{
                            Navigator.onVerifyAccount(this);
                        }
                    }
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

        private Preference mBreakInAlerts;

        private Preference mFakePin;

        private Preference mSecretDoor;

        private Preference mHelpSupport;

        private Preference mPrivacyPolicy;

        private Preference mPrivateCloud;

        private Preference mAlbumLock ;

        private Preference mUpgrade;

        private Preference mVersion;

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
                            Navigator.onMoveToChangePin(getContext(), EnumPinAction.NONE);
                            Utils.Log(TAG,"Action here");
                        }
                        else if (preference.getKey().equals(getString(R.string.key_theme))){
                            Navigator.onMoveThemeSettings(activity);
                        }
                        else if (preference.getKey().equals(getString(R.string.key_break_in_alert))){
                            Navigator.onMoveBreakInAlerts(getContext());
                        }
                        else if (preference.getKey().equals(getString(R.string.key_fake_pin))){
                            Navigator.onMoveFakePin(getContext());
                        }
                        else if (preference.getKey().equals(getString(R.string.key_secret_door))){
                            Navigator.onMoveSecretDoor(getContext());
                        }
                        else if (preference.getKey().equals(getString(R.string.key_help_support))){
                            Navigator.onMoveHelpSupport(getContext());
                        }
                        else if (preference.getKey().equals(getString(R.string.key_about_SuperSafe))){
                            Navigator.onMoveAboutSuperSafe(getContext());
                        }
                        else if (preference.getKey().equals(getString(R.string.key_private_cloud))){
                            final User mUser = User.getInstance().getUserInfo();
                            if (mUser!=null){
                                if (mUser.verified){
                                    if (!mUser.driveConnected){
                                        Navigator.onCheckSystem(getActivity(),null);
                                    }
                                    else{
                                        Navigator.onManagerCloud(getContext());
                                    }
                                }
                                else{
                                    Navigator.onVerifyAccount(getContext());
                                }
                            }
                        }
                        else if (preference.getKey().equals(getString(R.string.key_album_lock))){
                            Navigator.onMoveUnlockAllAlbums(getContext());
                        }
                        else if (preference.getKey().equals(getString(R.string.key_upgrade))){
                            Navigator.onMoveToPremium(getContext());
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

            /*Break-In-Alerts*/
            mBreakInAlerts = findPreference(getString(R.string.key_break_in_alert));
            mBreakInAlerts.setOnPreferenceClickListener(createActionPreferenceClickListener());
            mBreakInAlerts.setOnPreferenceChangeListener(createChangeListener());

            /*Fake-Pin*/
            mFakePin = findPreference(getString(R.string.key_fake_pin));
            mFakePin.setOnPreferenceClickListener(createActionPreferenceClickListener());
            mFakePin.setOnPreferenceChangeListener(createChangeListener());

            /*Secret door*/
            mSecretDoor = findPreference(getString(R.string.key_secret_door));
            mSecretDoor.setOnPreferenceClickListener(createActionPreferenceClickListener());
            mSecretDoor.setOnPreferenceChangeListener(createChangeListener());

            /*Private Cloud*/
            mPrivateCloud = findPreference(getString(R.string.key_private_cloud));
            mPrivateCloud.setOnPreferenceClickListener(createActionPreferenceClickListener());
            mPrivateCloud.setOnPreferenceChangeListener(createChangeListener());

            /*Help And support*/
            mHelpSupport = findPreference(getString(R.string.key_help_support));
            mHelpSupport.setOnPreferenceClickListener(createActionPreferenceClickListener());
            mHelpSupport.setOnPreferenceChangeListener(createChangeListener());

            /*About SuperSafe*/
            mPrivacyPolicy = findPreference(getString(R.string.key_privacy_policy));
            mPrivacyPolicy.setOnPreferenceClickListener(createActionPreferenceClickListener());
            mPrivacyPolicy.setOnPreferenceChangeListener(createChangeListener());

            /*Album Lock*/
            mAlbumLock = findPreference(getString(R.string.key_album_lock));
            mAlbumLock.setOnPreferenceClickListener(createActionPreferenceClickListener());
            mAlbumLock.setOnPreferenceChangeListener(createChangeListener());

            /*Upgrade*/
            mUpgrade = findPreference(getString(R.string.key_upgrade));
            mUpgrade.setOnPreferenceClickListener(createActionPreferenceClickListener());
            mUpgrade.setOnPreferenceChangeListener(createChangeListener());

            /*Version*/
            mVersion = findPreference(getString(R.string.key_version_app));
            mVersion.setOnPreferenceChangeListener(createChangeListener());
            mVersion.setOnPreferenceClickListener(createActionPreferenceClickListener());
            mVersion.setSummary(String.format(getString(R.string.key_super_safe_version),BuildConfig.VERSION_NAME));

        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_general);
        }

    }
}
