package co.tpcreative.supersafe.ui.accountmanager
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseGoogleApi
import co.tpcreative.supersafe.common.extension.toSpanned
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.AppLists
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.User
import kotlinx.android.synthetic.main.activity_account_manager.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class AccountManagerAct : BaseGoogleApi(), BaseView<EmptyModel>, AccountManagerAdapter.ItemSelectedListener {
    var presenter: AccountManagerPresenter? = null
    var adapter: AccountManagerAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_manager)
        intUI()
    }

    fun onUpdatedView() {
        val mUser: User? = Utils.getUserInfo()
        if (mUser != null) {
            tvEmail?.text = mUser.email
            if (mUser.verified) {
                tvStatusAccount?.setTextColor(ContextCompat.getColor(this,R.color.ColorBlueV1))
                tvStatusAccount?.text = getString(R.string.verified)
            } else {
                tvStatusAccount?.setTextColor(ContextCompat.getColor(this,R.color.red))
                tvStatusAccount?.text = getString(R.string.unverified)
            }
        }
        val isPremium: Boolean = Utils.isPremium()
        if (isPremium) {
            tvLicenseStatus?.setTextColor(ContextCompat.getColor(this,R.color.ColorBlueV1))
            tvLicenseStatus?.text = getString(R.string.premium)
            rlPremium?.visibility = View.VISIBLE
            rlPremiumComplimentary?.visibility = View.GONE
        } else {
            rlPremium?.visibility = View.GONE
            rlPremiumComplimentary?.visibility = View.VISIBLE
            tvLicenseStatus?.text = getString(R.string.free)
            val sourceString: String? = Utils.getFontString(R.string.upgrade_premium_to_use_full_features, getString(R.string.premium_uppercase))
            tvPremiumLeft?.text = sourceString?.toSpanned()
        }
    }

    override fun onClickItem(position: Int) {
        val app: AppLists = presenter?.mList!!.get(position)
        val isInstalled: Boolean = app.isInstalled
        if (!isInstalled) {
            val uri = Uri.parse("market://details?id=" + app.packageName)
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
                        Uri.parse("http://play.google.com/store/apps/details?id=" + app.packageName)))
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
        }
    }

    override fun onResume() {
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
        presenter?.unbindView()
    }

    override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    override fun onDriveClientReady() {}
    override fun onDriveSuccessful() {
        Utils.Log(TAG, "onDriveSuccessful")
        btnSignOut?.visibility = View.VISIBLE
    }

    override fun onDriveError() {
        Utils.Log(TAG, "onDriveError")
    }

    override fun onDriveSignOut() {
        Utils.Log(TAG, "onDriveSignOut")
    }

    override fun onDriveRevokeAccess() {
        Utils.Log(TAG, "onDriveRevokeAccess")
    }

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun startServiceNow() {}
    override fun getContext(): Context? {
        return applicationContext
    }

    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.RELOAD -> {
                adapter?.setDataSource(presenter?.mList)
            }
        }
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
    override fun isSignIn(): Boolean {
        return false
    }

    companion object {
        private val TAG = AccountManagerAct::class.java.simpleName
    }
}