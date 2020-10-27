package co.tpcreative.supersafe.ui.premium
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.preference.PreferenceFragmentCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumPurchase
import co.tpcreative.supersafe.model.EnumStatus
import com.anjlab.android.iab.v3.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_premium.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PremiumAct : BaseActivity(), BaseView<EmptyModel>, BillingProcessor.IBillingHandler {

    /*In app purchase*/
    var presenter: PremiumPresenter? = null

    /*New version*/
    var bp: BillingProcessor? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium)
         initUI()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            EnumStatus.PREMIUM -> {
                scrollView?.smoothScrollTo(0, 0)
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
        if (bp != null) {
            bp?.release()
        }
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

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_general_premium)
            EventBus.getDefault().post(EnumStatus.PREMIUM)
        }
    }

    /* Start in app purchase */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!(bp?.handleActivityResult(requestCode, resultCode, data))!!) super.onActivityResult(requestCode, resultCode, data)
    }

    /*Presenter*/
    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun onError(message: String?) {}
    override fun onError(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.CHECKOUT -> {
                Toast.makeText(applicationContext, "Message $message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        if (status == EnumStatus.CHECKOUT) {
            Utils.Log(TAG, message + "-" + status.name)
            onUpdatedView()
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
    override fun getContext(): Context? {
        return this
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                Utils.Log(TAG, "home action")
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /*New version*/
    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        Utils.Log(TAG, "Checkout detail " + Gson().toJson(details))
        if (details != null) {
            val mInfo: PurchaseInfo? = details.purchaseInfo
            if (mInfo != null) {
                val mData: PurchaseData? = mInfo.purchaseData
                if (mData != null) {
                    onCheckout(mData, EnumPurchase.fromString(mData.productId))
                }
            }
        }
    }

    override fun onPurchaseHistoryRestored() {
        Utils.Log(TAG, "onPurchaseHistoryRestored")
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        Utils.Log(TAG, "onBillingError $error")
    }

    override fun onBillingInitialized() {
        Utils.Log(TAG, "onBillingInitialized")
        /*Life time period time*/
        val mLifeTime: SkuDetails? = bp?.getPurchaseListingDetails(getString(R.string.life_time))
        if (mLifeTime != null) {
            tvLifeTime?.text = mLifeTime.priceText
        }
        /*Six month period time*/
        val mSixMonths: SkuDetails? = bp?.getSubscriptionListingDetails(getString(R.string.six_months))
        if (mSixMonths != null) {
            tvMonthly?.text = mSixMonths.priceText
        }
        /*One year period time*/
        val mOneYear: SkuDetails? = bp?.getSubscriptionListingDetails(getString(R.string.one_years))
        if (mOneYear != null) {
            tvYearly?.text = mOneYear.priceText
        }
        val mTransaction: TransactionDetails? = bp?.getSubscriptionTransactionDetails(getString(R.string.six_months))
        if (mTransaction != null) {
            Utils.Log(TAG, "Result of 6 months " + Gson().toJson(mTransaction))
        }
    }

    companion object {
        val FRAGMENT_TAG: String? = SettingsFragment::class.java.getSimpleName() + "::fragmentTag"
    }
}