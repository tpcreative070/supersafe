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
import co.tpcreative.supersafe.model.CheckoutItems
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumPurchase
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.viewmodel.PremiumViewModel
import com.anjlab.android.iab.v3.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_premium.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PremiumAct : BaseActivity(), BillingProcessor.IBillingHandler {

    /*In app purchase*/
    lateinit var viewModel : PremiumViewModel

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
            else -> Utils.Log(TAG, "Nothing")
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
            Utils.onPushEventBus(EnumStatus.PREMIUM)
        }
    }

    /* Start in app purchase */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!(bp?.handleActivityResult(requestCode, resultCode, data))!!) super.onActivityResult(requestCode, resultCode, data)
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
                    checkout(mData)
                    if (Utils.isRealCheckedOut(mData.orderId)) {
                        onCheckout(mData, EnumPurchase.fromString(mData.productId))
                    }
                    else{
                        Utils.setCheckoutItems(CheckoutItems())
                        askWarningFakeCheckout()
                    }
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
        val FRAGMENT_TAG: String = SettingsFragment::class.java.getSimpleName() + "::fragmentTag"
    }
}