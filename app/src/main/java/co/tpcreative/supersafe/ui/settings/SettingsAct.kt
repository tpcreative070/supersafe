package co.tpcreative.supersafe.ui.settings
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import co.tpcreative.supersafe.BuildConfig
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.controller.EncryptedPreferenceDataStore
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.extension.instantiate
import co.tpcreative.supersafe.common.extension.setRequestSyncData
import co.tpcreative.supersafe.common.helper.ThemeHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import de.mrapp.android.dialog.MaterialDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SettingsAct : BaseActivity() {
    private var isChangedTheme = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        activity = this
        initUI()
        val fragment = supportFragmentManager.instantiate(SettingsFragment::class.java.name)
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.content_frame, fragment)
        transaction.commit()
    }

    override fun setStatusBarColored(context: AppCompatActivity, colorPrimary: Int, colorPrimaryDark: Int) {
        context.supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat
                .getColor(context, colorPrimary)))
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(context, colorPrimaryDark)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.RECREATE -> {
                val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
                themeApp?.getPrimaryColor()?.let { setStatusBarColored(this, it, themeApp.getPrimaryDarkColor()) }
                val fragment: Fragment = supportFragmentManager.instantiate(SettingsFragment::class.java.name)
                val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.content_frame, fragment)
                transaction.commit()
                isChangedTheme = true
            }
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            EnumStatus.CLOSED -> {
                if (isChangedTheme) {
                    setResult(Activity.RESULT_OK, intent)
                }
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
        /*Checking change style mode*/
        ServiceManager.getInstance()?.onStartService()
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Navigator.THEME_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Log(TAG, "recreate........................")
                }
            }
            Navigator.ENABLE_CLOUD -> {
                Utils.Log(TAG, "onResultResponse :$resultCode")
//                if (resultCode == Activity.RESULT_OK) {
//                    if (Utils.isVerifiedAccount()) {
//                        if (Utils.isConnectedToGoogleDrive()) {
//                            Navigator.onManagerCloud(this)
//                        } else {
//                            Navigator.onCheckSystem(this, null)
//                        }
//                    } else {
//                        Navigator.onVerifyAccount(this)
//                    }
//                }
            }
        }
    }

    override fun onBackPressed() {
        if (isChangedTheme) {
            setResult(Activity.RESULT_OK, intent)
        }
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        var mPosition = Utils.getPositionThemeMode()
        private var mAccount: Preference? = null
        private var mLockScreen: Preference? = null
        private var mTheme: Preference? = null
        private var mBreakInAlerts: Preference? = null
        private var mFakePin: Preference? = null
        private var mSecretDoor: Preference? = null
        private var mHelpSupport: Preference? = null
        private var mPrivacyPolicy: Preference? = null
        private var mPrivateCloud: Preference? = null
        private var mAlbumLock: Preference? = null
        private var mUpgrade: Preference? = null
        private var mVersion: Preference? = null
        private var mRateApp: Preference? = null
        private var mLightMode : Preference? = null

        /**
         * Creates and returns a listener, which allows to adapt the app's theme, when the value of the
         * corresponding preference has been changed.
         *
         * @return The listener, which has been created, as an instance of the type [ ]
         */
        private fun createChangeListener(): Preference.OnPreferenceChangeListener? {
            return Preference.OnPreferenceChangeListener { preference, newValue -> true }
        }

        private fun createActionPreferenceClickListener(): Preference.OnPreferenceClickListener? {
            return Preference.OnPreferenceClickListener { preference ->
                if (preference is Preference) {
                    if (preference.key == getString(R.string.key_account)) {
                        Utils.Log(TAG, "value : ")
                        Navigator.onManagerAccount(context!!)
                    } else if (preference.key == getString(R.string.key_lock_screen)) {
                        Navigator.onMoveToChangePin(context!!, EnumPinAction.NONE)
                        Utils.Log(TAG, "Action here")
                    } else if (preference.key == getString(R.string.key_theme)) {
                        if (!Utils.isPremium()) {
                            onShowPremium()
                            return@OnPreferenceClickListener true
                        }
                        Navigator.onMoveThemeSettings(activity!!)
                    } else if (preference.key == getString(R.string.key_break_in_alert)) {
                        if (!Utils.isPremium()) {
                            onShowPremium()
                            return@OnPreferenceClickListener true
                        }
                        Navigator.onMoveBreakInAlerts(context!!)
                    } else if (preference.key == getString(R.string.key_fake_pin)) {
                        if (!Utils.isPremium()) {
                            onShowPremium()
                            return@OnPreferenceClickListener true
                        }
                        Navigator.onMoveFakePin(context!!)
                    } else if (preference.key == getString(R.string.key_secret_door)) {
                        if (!Utils.isPremium()) {
                            onShowPremium()
                            return@OnPreferenceClickListener true
                        }
                        Navigator.onMoveSecretDoor(context!!)
                    } else if (preference.key == getString(R.string.key_help_support)) {
                        Navigator.onMoveHelpSupport(context!!)
                    } else if (preference.key == getString(R.string.key_about_SuperSafe)) {
                        Navigator.onMoveAboutSuperSafe(context!!)
                    } else if (preference.key == getString(R.string.key_private_cloud)) {
                        if (Utils.isVerifiedAccount()) {
                            if (Utils.isConnectedToGoogleDrive()) {
                                Navigator.onManagerCloud(context!!)
                            } else {
                                Navigator.onCheckSystem(activity!!, null)
                            }
                        } else {
                            Navigator.onVerifyAccount(context!!)
                        }
                    } else if (preference.key == getString(R.string.key_album_lock)) {
                        if (!Utils.isPremium()) {
                            onShowPremium()
                            return@OnPreferenceClickListener true
                        }
                        Navigator.onMoveUnlockAllAlbums(context!!)
                    } else if (preference.key == getString(R.string.key_upgrade)) {
                        Navigator.onMoveToPremium(context!!)
                    } else if (preference.key == getString(R.string.key_privacy_policy)) {
                        Utils.Log(TAG, "Log here")
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.tvPrimacyPolicy)))
                        startActivity(intent)
                    } else if (preference.key == getString(R.string.key_rate)) {
                        onRateApp()
                    }
                    else if (preference.key == getString(R.string.key_dark_mode)){
                        if (!Utils.isPremium()) {
                            onShowPremium()
                            return@OnPreferenceClickListener true
                        }
                        askDeviceMode {
                            Utils.setRequestSyncData(true)
                            ThemeHelper.applyTheme(EnumThemeModel.byPosition(Utils.getPositionThemeMode()))
                        }
                    }
                }
                true
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            /*Account*/mAccount = findPreference(getString(R.string.key_account))
            mAccount?.onPreferenceChangeListener = createChangeListener()
            mAccount?.onPreferenceClickListener = createActionPreferenceClickListener()

            /*Lock Screen*/mLockScreen = findPreference(getString(R.string.key_lock_screen))
            mLockScreen?.onPreferenceChangeListener = createChangeListener()
            mLockScreen?.onPreferenceClickListener = createActionPreferenceClickListener()

            /*Update ThemeApp*/mTheme = findPreference(getString(R.string.key_theme))
            mTheme?.onPreferenceClickListener = createActionPreferenceClickListener()
            mTheme?.onPreferenceChangeListener = createChangeListener()

            /*Break-In-Alerts*/mBreakInAlerts = findPreference(getString(R.string.key_break_in_alert))
            mBreakInAlerts?.onPreferenceClickListener = createActionPreferenceClickListener()
            mBreakInAlerts?.onPreferenceChangeListener = createChangeListener()

            /*Fake-Pin*/mFakePin = findPreference(getString(R.string.key_fake_pin))
            mFakePin?.onPreferenceClickListener = createActionPreferenceClickListener()
            mFakePin?.onPreferenceChangeListener = createChangeListener()

            /*Secret door*/mSecretDoor = findPreference(getString(R.string.key_secret_door))
            mSecretDoor?.onPreferenceClickListener = createActionPreferenceClickListener()
            mSecretDoor?.onPreferenceChangeListener = createChangeListener()

            /*Light mode*/
            mLightMode = findPreference(getString(R.string.key_dark_mode))
            mLightMode?.onPreferenceClickListener = createActionPreferenceClickListener()
            mLightMode?.onPreferenceChangeListener = createChangeListener()

            /*Private Cloud*/mPrivateCloud = findPreference(getString(R.string.key_private_cloud))
            mPrivateCloud?.onPreferenceClickListener = createActionPreferenceClickListener()
            mPrivateCloud?.onPreferenceChangeListener = createChangeListener()

            /*Help And support*/mHelpSupport = findPreference(getString(R.string.key_help_support))
            mHelpSupport?.onPreferenceClickListener = createActionPreferenceClickListener()
            mHelpSupport?.onPreferenceChangeListener = createChangeListener()

            /*About SuperSafe*/mPrivacyPolicy = findPreference(getString(R.string.key_privacy_policy))
            mPrivacyPolicy?.onPreferenceClickListener = createActionPreferenceClickListener()
            mPrivacyPolicy?.onPreferenceChangeListener = createChangeListener()

            /*Album Lock*/mAlbumLock = findPreference(getString(R.string.key_album_lock))
            mAlbumLock?.onPreferenceClickListener = createActionPreferenceClickListener()
            mAlbumLock?.onPreferenceChangeListener = createChangeListener()

            /*Upgrade*/mUpgrade = findPreference(getString(R.string.key_upgrade))
            mUpgrade?.onPreferenceClickListener = createActionPreferenceClickListener()
            mUpgrade?.onPreferenceChangeListener = createChangeListener()

            /*Version*/mVersion = findPreference(getString(R.string.key_version_app))
            mVersion?.onPreferenceChangeListener = createChangeListener()
            mVersion?.onPreferenceClickListener = createActionPreferenceClickListener()
            mVersion?.summary = String.format(getString(R.string.key_super_safe_version), BuildConfig.VERSION_NAME)


            /*Rate app*/mRateApp = findPreference(getString(R.string.key_rate))
            mRateApp?.onPreferenceChangeListener = createChangeListener()
            mRateApp?.onPreferenceClickListener = createActionPreferenceClickListener()
            mRateApp?.isVisible = false
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            if (SuperSafeApplication.getInstance().isLiveMigration()){
                preferenceManager.preferenceDataStore = EncryptedPreferenceDataStore.getInstance(requireContext())
            }
            addPreferencesFromResource(R.xml.pref_general)
        }

        private fun onRateApp() {
            val uri = Uri.parse("market://details?id=" + getString(R.string.supersafe_live))
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getString(R.string.supersafe_live))))
            }
        }

        private fun onShowPremium() {
            try {
                val builder = MaterialDialog.Builder(context!!,Utils.getCurrentThemeMode())
                val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
                builder.setHeaderBackground(themeApp?.getAccentColor()!!)
                builder.setTitle(getString(R.string.this_is_premium_feature))
                builder.setMessage(getString(R.string.upgrade_now))
                builder.setCustomHeader(R.layout.custom_header)
                builder.setPadding(40, 40, 40, 0)
                builder.setMargin(60, 0, 60, 0)
                builder.showHeader(true)
                builder.setPositiveButton(getString(R.string.get_premium), object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                        Navigator.onMoveToPremium(context!!)
                    }
                })
                builder.setNegativeButton(getText(R.string.later)) { _, _ -> }
                builder.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun askDeviceMode(onCompleted: (done: Boolean) -> Unit) {
            val dialogBuilder = MaterialDialog.Builder(context!!, Utils.getCurrentThemeMode())
            dialogBuilder.setTitle(R.string.change_mode)
            dialogBuilder.setPadding(40, 40, 40, 0)
            dialogBuilder.setMargin(60, 0, 60, 0)
            dialogBuilder.setSingleChoiceItems(R.array.themeEntryArray, Utils.getPositionThemeMode(), DialogInterface.OnClickListener { _, m ->
                this.mPosition = m
            })
            dialogBuilder.setPositiveButton(R.string.yes) { _, i ->
                Utils.setPositionThemeMode(EnumThemeModel.byPosition(mPosition).ordinal)
                onCompleted(true)
            }
            val dialog = dialogBuilder.create()
            dialog.show()
        }
    }

    companion object {
        private val TAG = SettingsAct::class.java.simpleName
        private var activity: Activity? = null
    }
}