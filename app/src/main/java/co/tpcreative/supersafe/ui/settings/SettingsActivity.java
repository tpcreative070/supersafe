package co.tpcreative.supersafe.ui.settings;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import co.tpcreative.supersafe.BuildConfig;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;
import spencerstudios.com.bungeelib.Bungee;

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

    protected void setStatusBarColored(AppCompatActivity context, int colorPrimary, int colorPrimaryDark) {
        context.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources()
                .getColor(colorPrimary)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = context.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(context,colorPrimaryDark));
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
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EnumStatus event) {
        switch (event){
            case RECREATE:{
                co.tpcreative.supersafe.model.Theme theme = co.tpcreative.supersafe.model.Theme.getInstance().getThemeInfo();
                setStatusBarColored(this,theme.getPrimaryColor(),theme.getPrimaryDarkColor());
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
                if (fragment == null) {
                    fragment = Fragment.instantiate(this, SettingsFragment.class.getName());
                }
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, fragment);
                transaction.commit();
                EventBus.getDefault().post(EnumStatus.COMPLETED_RECREATE);
                break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case Navigator.THEME_SETTINGS :{
                if (resultCode==RESULT_OK){
                    Utils.Log(TAG,"recreate........................");
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
                            if (User.getInstance().isPremiumExpired()){
                                onShowDialog(getString(R.string.your_premium_has_expired));
                                return true;
                            }
                            Navigator.onMoveThemeSettings(activity);
                        }
                        else if (preference.getKey().equals(getString(R.string.key_break_in_alert))){

                            if (User.getInstance().isPremiumExpired()){
                                onShowDialog(getString(R.string.your_premium_has_expired));
                                return true;
                            }

                            Navigator.onMoveBreakInAlerts(getContext());
                        }
                        else if (preference.getKey().equals(getString(R.string.key_fake_pin))){

                            if (User.getInstance().isPremiumExpired()){
                                onShowDialog(getString(R.string.your_premium_has_expired));
                                return true;
                            }

                            Navigator.onMoveFakePin(getContext());
                        }
                        else if (preference.getKey().equals(getString(R.string.key_secret_door))){

                            if (User.getInstance().isPremiumExpired()){
                                onShowDialog(getString(R.string.your_premium_has_expired));
                                return true;
                            }

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
                            if (User.getInstance().isPremiumExpired()){
                                onShowDialog(getString(R.string.your_premium_has_expired));
                                return true;
                            }
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

        public void onShowDialog(String message){
            MaterialDialog.Builder builder =  new MaterialDialog.Builder(getContext())
                    .title(getString(R.string.confirm))
                    .theme(Theme.LIGHT)
                    .content(message)
                    .titleColor(getResources().getColor(R.color.black))
                    .negativeText(getString(R.string.cancel))
                    .positiveText(getString(R.string.ok))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Navigator.onMoveToPremium(getContext());
                        }
                    });


            builder.show();
        }

    }

}
