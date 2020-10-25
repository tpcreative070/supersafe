package co.tpcreative.supersafe.ui.settings
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import co.tpcreative.supersafe.BuildConfig
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.extension.instantiate
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ThemeApp
import co.tpcreative.supersafe.model.User
import de.mrapp.android.dialog.MaterialDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SettingsActivity : BaseActivity() {
    private var isChangedTheme = false
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        activity = this
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        var fragment = supportFragmentManager.instantiate(SettingsFragment::class.java.name)
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.content_frame, fragment)
        transaction.commit()
    }

    protected override fun setStatusBarColored(context: AppCompatActivity, colorPrimary: Int, colorPrimaryDark: Int) {
        context.getSupportActionBar()?.setBackgroundDrawable(ColorDrawable(ContextCompat
                .getColor(context!!,colorPrimary)))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = context.getWindow()
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(context, colorPrimaryDark)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.RECREATE -> {
                val themeApp: ThemeApp? = ThemeApp.Companion.getInstance()?.getThemeInfo()
                themeApp?.getPrimaryColor()?.let { setStatusBarColored(this, it, themeApp.getPrimaryDarkColor()) }
                var fragment: Fragment = supportFragmentManager.instantiate(SettingsFragment::class.java.name)
                val transaction: FragmentTransaction = getSupportFragmentManager().beginTransaction()
                transaction.replace(R.id.content_frame, fragment)
                transaction.commit()
                isChangedTheme = true
            }
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            EnumStatus.CLOSED -> {
                if (isChangedTheme) {
                    val intent: Intent = getIntent()
                    setResult(Activity.RESULT_OK, intent)
                }
            }
        }
    }

    protected override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        onRegisterHomeWatcher()
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
    }

    protected override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Navigator.THEME_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Log(TAG, "recreate........................")
                }
            }
            Navigator.ENABLE_CLOUD -> {
                Utils.Log(TAG, "onResultResponse :$resultCode")
                if (resultCode == Activity.RESULT_OK) {
                    val mUser: User? = Utils.getUserInfo()
                    if (mUser != null) {
                        if (mUser.verified) {
                            if (!mUser.driveConnected) {
                                Navigator.onCheckSystem(this, null)
                            } else {
                                Navigator.onManagerCloud(this)
                            }
                        } else {
                            Navigator.onVerifyAccount(this)
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (isChangedTheme) {
            val intent: Intent = getIntent()
            setResult(Activity.RESULT_OK, intent)
        }
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }

    class SettingsFragment : PreferenceFragmentCompat() {
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
                        Navigator.onManagerAccount(getContext()!!)
                    } else if (preference.key == getString(R.string.key_lock_screen)) {
                        Navigator.onMoveToChangePin(getContext()!!, EnumPinAction.NONE)
                        Utils.Log(TAG, "Action here")
                    } else if (preference.key == getString(R.string.key_theme)) {
                        if (BuildConfig.DEBUG) {
                            Navigator.onMoveThemeSettings(activity!!)
                            return@OnPreferenceClickListener true
                        }
                        if (!Utils.isPremium()) {
                            onShowPremium()
                            return@OnPreferenceClickListener true
                        }
                        Navigator.onMoveThemeSettings(activity!!)
                    } else if (preference.key == getString(R.string.key_break_in_alert)) {
                        if (BuildConfig.DEBUG) {
                            Navigator.onMoveBreakInAlerts(getContext()!!)
                            return@OnPreferenceClickListener true
                        }
                        if (!Utils.isPremium()) {
                            onShowPremium()
                            return@OnPreferenceClickListener true
                        }
                        Navigator.onMoveBreakInAlerts(getContext()!!)
                    } else if (preference.key == getString(R.string.key_fake_pin)) {
                        if (BuildConfig.DEBUG) {
                            Navigator.onMoveFakePin(getContext()!!)
                            return@OnPreferenceClickListener true
                        }
                        if (!Utils.isPremium()) {
                            onShowPremium()
                            return@OnPreferenceClickListener true
                        }
                        Navigator.onMoveFakePin(getContext()!!)
                    } else if (preference.key == getString(R.string.key_secret_door)) {
                        if (BuildConfig.DEBUG) {
                            Navigator.onMoveSecretDoor(getContext()!!)
                            return@OnPreferenceClickListener true
                        }
                        if (!Utils.isPremium()) {
                            onShowPremium()
                            return@OnPreferenceClickListener true
                        }
                        Navigator.onMoveSecretDoor(getContext()!!)
                    } else if (preference.key == getString(R.string.key_help_support)) {
                        Navigator.onMoveHelpSupport(getContext()!!)
                    } else if (preference.key == getString(R.string.key_about_SuperSafe)) {
                        Navigator.onMoveAboutSuperSafe(getContext()!!)
                    } else if (preference.key == getString(R.string.key_private_cloud)) {
                        val mUser: User? = Utils.getUserInfo()
                        if (mUser != null) {
                            if (mUser.verified) {
                                if (!mUser.driveConnected) {
                                    Navigator.onCheckSystem(getActivity()!!, null)
                                } else {
                                    Navigator.onManagerCloud(getContext()!!)
                                }
                            } else {
                                Navigator.onVerifyAccount(getContext()!!)
                            }
                        }
                    } else if (preference.key == getString(R.string.key_album_lock)) {
                        if (BuildConfig.DEBUG) {
                            Navigator.onMoveUnlockAllAlbums(getContext()!!)
                            return@OnPreferenceClickListener true
                        }
                        if (!Utils.isPremium()) {
                            onShowPremium()
                            return@OnPreferenceClickListener true
                        }
                        Navigator.onMoveUnlockAllAlbums(getContext()!!)
                    } else if (preference.key == getString(R.string.key_upgrade)) {
                        Navigator.onMoveToPremium(getContext()!!)
                    } else if (preference.key == getString(R.string.key_privacy_policy)) {
                        Utils.Log(TAG, "Log here")
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.tvPrimacyPolicy)))
                        startActivity(intent)
                    } else if (preference.key == getString(R.string.key_rate)) {
                        onRateApp()
                    }
                }
                true
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            /*Account*/mAccount = findPreference(getString(R.string.key_account))
            mAccount?.setOnPreferenceChangeListener(createChangeListener())
            mAccount?.setOnPreferenceClickListener(createActionPreferenceClickListener())

            /*Lock Screen*/mLockScreen = findPreference(getString(R.string.key_lock_screen))
            mLockScreen?.setOnPreferenceChangeListener(createChangeListener())
            mLockScreen?.setOnPreferenceClickListener(createActionPreferenceClickListener())

            /*Update ThemeApp*/mTheme = findPreference(getString(R.string.key_theme))
            mTheme?.setOnPreferenceClickListener(createActionPreferenceClickListener())
            mTheme?.setOnPreferenceChangeListener(createChangeListener())

            /*Break-In-Alerts*/mBreakInAlerts = findPreference(getString(R.string.key_break_in_alert))
            mBreakInAlerts?.setOnPreferenceClickListener(createActionPreferenceClickListener())
            mBreakInAlerts?.setOnPreferenceChangeListener(createChangeListener())

            /*Fake-Pin*/mFakePin = findPreference(getString(R.string.key_fake_pin))
            mFakePin?.setOnPreferenceClickListener(createActionPreferenceClickListener())
            mFakePin?.setOnPreferenceChangeListener(createChangeListener())

            /*Secret door*/mSecretDoor = findPreference(getString(R.string.key_secret_door))
            mSecretDoor?.setOnPreferenceClickListener(createActionPreferenceClickListener())
            mSecretDoor?.setOnPreferenceChangeListener(createChangeListener())

            /*Private Cloud*/mPrivateCloud = findPreference(getString(R.string.key_private_cloud))
            mPrivateCloud?.setOnPreferenceClickListener(createActionPreferenceClickListener())
            mPrivateCloud?.setOnPreferenceChangeListener(createChangeListener())

            /*Help And support*/mHelpSupport = findPreference(getString(R.string.key_help_support))
            mHelpSupport?.setOnPreferenceClickListener(createActionPreferenceClickListener())
            mHelpSupport?.setOnPreferenceChangeListener(createChangeListener())

            /*About SuperSafe*/mPrivacyPolicy = findPreference(getString(R.string.key_privacy_policy))
            mPrivacyPolicy?.setOnPreferenceClickListener(createActionPreferenceClickListener())
            mPrivacyPolicy?.setOnPreferenceChangeListener(createChangeListener())

            /*Album Lock*/mAlbumLock = findPreference(getString(R.string.key_album_lock))
            mAlbumLock?.setOnPreferenceClickListener(createActionPreferenceClickListener())
            mAlbumLock?.setOnPreferenceChangeListener(createChangeListener())

            /*Upgrade*/mUpgrade = findPreference(getString(R.string.key_upgrade))
            mUpgrade?.setOnPreferenceClickListener(createActionPreferenceClickListener())
            mUpgrade?.setOnPreferenceChangeListener(createChangeListener())

            /*Version*/mVersion = findPreference(getString(R.string.key_version_app))
            mVersion?.setOnPreferenceChangeListener(createChangeListener())
            mVersion?.setOnPreferenceClickListener(createActionPreferenceClickListener())
            mVersion?.setSummary(java.lang.String.format(getString(R.string.key_super_safe_version), BuildConfig.VERSION_NAME))


            /*Rate app*/mRateApp = findPreference(getString(R.string.key_rate))
            mRateApp?.setOnPreferenceChangeListener(createChangeListener())
            mRateApp?.setOnPreferenceClickListener(createActionPreferenceClickListener())
            mRateApp?.setVisible(false)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_general)
        }

        fun onRateApp() {
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

        fun onShowPremium() {
            try {
                val builder = MaterialDialog.Builder(getContext()!!)
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
                        Navigator.onMoveToPremium(getContext()!!)
                    }
                })
                builder.setNegativeButton(getText(R.string.later), object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface?, i: Int) {}
                })
                val dialog = builder.show()
                builder.setOnShowListener(object : DialogInterface.OnShowListener {
                    override fun onShow(dialogInterface: DialogInterface?) {
                        val positive = dialog.findViewById<Button?>(android.R.id.button1)
                        val negative = dialog.findViewById<Button?>(android.R.id.button2)
                        val textView: AppCompatTextView? = dialog.findViewById<View?>(android.R.id.message) as AppCompatTextView?
                        if (positive != null && negative != null && textView != null) {
                            positive.setTextColor(ContextCompat.getColor(context!!,themeApp?.getAccentColor()!!))
                            negative.setTextColor(ContextCompat.getColor(context!!,themeApp.getAccentColor()!!))
                            textView.setTextSize(16f)
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private val TAG = SettingsActivity::class.java.simpleName
        private val FRAGMENT_TAG: String? = SettingsActivity::class.java.simpleName + "::fragmentTag"
        private var activity: Activity? = null
    }
}