package co.tpcreative.supersafe.ui.settings;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import co.tpcreative.supersafe.BuildConfig;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.ui.switchbasic.SwitchBasicActivity;
import spencerstudios.com.bungeelib.Bungee;


public class SettingsActivity extends BaseActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private static final String FRAGMENT_TAG = SettingsActivity.class.getSimpleName() + "::fragmentTag";
    private static Activity activity;
    private boolean isChangedTheme;

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
                isChangedTheme = true;
                break;
            }
            case FINISH:{
                Navigator.onMoveToFaceDown(this);
                break;
            }
            case CLOSED:{
                if (isChangedTheme){
                    Intent intent = getIntent();
                    setResult(RESULT_OK,intent);
                }
                break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        onRegisterHomeWatcher();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"OnDestroy");
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
    }


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
        if (isChangedTheme){
            Intent intent = getIntent();
            setResult(RESULT_OK,intent);
        }
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

        private Preference mRateApp;

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
                            Utils.Log(TAG,"value : ");
                            Navigator.onManagerAccount(getContext());
                        }
                        else if (preference.getKey().equals(getString(R.string.key_lock_screen))){
                            Navigator.onMoveToChangePin(getContext(),EnumPinAction.NONE);
                            Utils.Log(TAG,"Action here");
                        }
                        else if (preference.getKey().equals(getString(R.string.key_theme))){
                            if (User.getInstance().isPremiumExpired()){
                                //onShowDialog(getString(R.string.your_premium_has_expired));
                                onShowPremium();
                                return true;
                            }
                            Navigator.onMoveThemeSettings(activity);
                        }
                        else if (preference.getKey().equals(getString(R.string.key_break_in_alert))){
                            if (User.getInstance().isPremiumExpired()){
                                //onShowDialog(getString(R.string.your_premium_has_expired));
                                onShowPremium();
                                return true;
                            }
                            Navigator.onMoveBreakInAlerts(getContext());
                        }
                        else if (preference.getKey().equals(getString(R.string.key_fake_pin))){
                            if (User.getInstance().isPremiumExpired()){
                                //onShowDialog(getString(R.string.your_premium_has_expired));
                                onShowPremium();
                                return true;
                            }
                            Navigator.onMoveFakePin(getContext());
                        }
                        else if (preference.getKey().equals(getString(R.string.key_secret_door))){
                            if (User.getInstance().isPremiumExpired()){
                                //onShowDialog(getString(R.string.your_premium_has_expired));
                                onShowPremium();
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
                                //onShowDialog(getString(R.string.your_premium_has_expired));
                                onShowPremium();
                                return true;
                            }
                            Navigator.onMoveUnlockAllAlbums(getContext());
                        }
                        else if (preference.getKey().equals(getString(R.string.key_upgrade))){
                            Navigator.onMoveToPremium(getContext());
                        }
                        else if (preference.getKey().equals(getString(R.string.key_privacy_policy))){
                            Utils.Log(TAG,"Log here");
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.tvPrimacyPolicy)));
                            startActivity(intent);
                        }
                        else if (preference.getKey().equals(getString(R.string.key_rate))){
                            onRateApp();
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


            /*Rate app*/
            mRateApp = findPreference(getString(R.string.key_rate));
            mRateApp.setOnPreferenceChangeListener(createChangeListener());
            mRateApp.setOnPreferenceClickListener(createActionPreferenceClickListener());
            mRateApp.setVisible(false);

        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_general);
        }

        public void onRateApp() {
            Uri uri = Uri.parse("market://details?id=" + getString(R.string.supersafe_live));
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getString(R.string.supersafe_live))));
            }
        }


        public void onShowPremium(){
            try {
                de.mrapp.android.dialog.MaterialDialog.Builder builder = new de.mrapp.android.dialog.MaterialDialog.Builder(getContext());
                co.tpcreative.supersafe.model.Theme theme = co.tpcreative.supersafe.model.Theme.getInstance().getThemeInfo();
                builder.setHeaderBackground(theme.getAccentColor());
                builder.setTitle(getString(R.string.this_is_premium_feature));
                builder.setMessage(getString(R.string.upgrade_now));
                builder.setCustomHeader(R.layout.custom_header);
                builder.setPadding(40,40,40,0);
                builder.setMargin(60,0,60,0);
                builder.showHeader(true);
                builder.setPositiveButton(getString(R.string.get_premium), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Navigator.onMoveToPremium(getContext());
                    }
                });

                builder.setNegativeButton(getText(R.string.later), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                de.mrapp.android.dialog.MaterialDialog dialog = builder.show();
                builder.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button positive = dialog.findViewById(android.R.id.button1);
                        Button negative = dialog.findViewById(android.R.id.button2);
                        TextView textView = (TextView) dialog.findViewById(android.R.id.message);

                        if (positive!=null && negative!=null && textView!=null){
                            positive.setTextColor(getContext().getResources().getColor(theme.getAccentColor()));
                            negative.setTextColor(getContext().getResources().getColor(theme.getAccentColor()));
                            textView.setTextSize(16);
                        }
                    }
                });
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

    }

}
