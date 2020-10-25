package co.tpcreative.supersafe.ui.premium
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceFragmentCompat
import butterknife.BindView
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.extension.toSpanned
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.CheckoutItems
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumPurchase
import co.tpcreative.supersafe.model.EnumStatus
import com.anjlab.android.iab.v3.*
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PremiumActivity : BaseActivity(), BaseView<EmptyModel>, BillingProcessor.IBillingHandler {
    @BindView(R.id.scrollView)
    var scrollView: NestedScrollView? = null

    @BindView(R.id.tvPremiumLeft)
    var tvPremiumLeft: AppCompatTextView? = null

    @BindView(R.id.tvMonthly)
    var tvMonthly: AppCompatTextView? = null

    @BindView(R.id.tvYearly)
    var tvYearly: AppCompatTextView? = null

    @BindView(R.id.tvLifeTime)
    var tvLifeTime: AppCompatTextView? = null

    @BindView(R.id.tvTitle)
    var tvTitle: AppCompatTextView? = null

    @BindView(R.id.llTwo)
    var llTwo: LinearLayout? = null

    /*In app purchase*/
    private var presenter: PremiumPresenter? = null

    /*New version*/
    private var bp: BillingProcessor? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium)
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        presenter = PremiumPresenter()
        presenter?.bindView(this)
        var fragment: Fragment? = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
        if (fragment == null) {
            val mFactory: FragmentFactory = supportFragmentManager.getFragmentFactory()
            fragment = mFactory.instantiate(ClassLoader.getSystemClassLoader(), SettingsFragment::class.java.name)
        }
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.content_frame, fragment)
        transaction.commit()
        /*Init In app purchase*/onStartInAppPurchase()
        onUpdatedView()
        scrollView?.smoothScrollTo(0, 0)
    }

    fun onUpdatedView() {
        val isPremium: Boolean = Utils.isPremium()
        if (isPremium) {
            tvTitle?.text= (getText(R.string.you_are_in_premium_features))
            tvPremiumLeft?.visibility = (View.GONE)
            llTwo?.visibility = (View.VISIBLE)
        } else {
            val sourceString: String? = Utils.getFontString(R.string.upgrade_premium_to_use_full_features, getString(R.string.premium_uppercase))
            tvPremiumLeft?.setText(sourceString?.toSpanned())
            tvTitle?.setText(getString(R.string.choose_your_plans))
        }
    }

    protected override fun onStart() {
        super.onStart()
    }

    @OnClick(R.id.llMonths)
    fun onClickedMonths(view: View?) {
        if (BillingProcessor.isIabServiceAvailable(this)) {
            Utils.Log(TAG, "purchase new")
            if (bp?.isSubscribed(getString(R.string.six_months))!!) {
                Utils.Log(TAG, "Already charged")
                bp?.loadOwnedPurchasesFromGoogle()
            } else {
                bp?.subscribe(this, getString(R.string.six_months))
            }
        }
    }

    @OnClick(R.id.llYears)
    fun onClickedYears(view: View?) {
        Utils.Log(TAG, "Years")
        if (BillingProcessor.isIabServiceAvailable(this)) {
            Utils.Log(TAG, "purchase new")
            if (bp?.isSubscribed(getString(R.string.one_years))!!) {
                Utils.Log(TAG, "Already charged")
                bp?.loadOwnedPurchasesFromGoogle()
            } else {
                bp?.subscribe(this, getString(R.string.one_years))
            }
        }
    }

    @OnClick(R.id.llLifeTime)
    fun onClickedLifeTime(view: View?) {
        if (BillingProcessor.isIabServiceAvailable(this)) {
            Utils.Log(TAG, "purchase new")
            if (bp?.isPurchased(getString(R.string.life_time))!!) {
                Utils.Log(TAG, "Already charged")
                bp?.consumePurchase(getString(R.string.life_time))
                bp?.loadOwnedPurchasesFromGoogle()
            } else {
                bp?.purchase(this, getString(R.string.life_time))
            }
        }
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

    protected override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        onRegisterHomeWatcher()
    }

    protected override fun onDestroy() {
        if (bp != null) {
            bp?.release()
        }
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
        presenter?.unbindView()
    }

    protected override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_general_premium)
            EventBus.getDefault().post(EnumStatus.PREMIUM)
        }
    }

    /* Start in app purchase */
    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!(bp?.handleActivityResult(requestCode, resultCode, data))!!) super.onActivityResult(requestCode, resultCode, data)
    }

    fun onStartInAppPurchase() {
        bp = BillingProcessor(this, Utils.GOOGLE_CONSOLE_KEY, this)
        bp?.initialize()
    }

    /*Presenter*/
    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun onError(message: String?) {}
    override fun onError(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.CHECKOUT -> {
                Toast.makeText(getApplicationContext(), "Message $message", Toast.LENGTH_SHORT).show()
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
        when (item.getItemId()) {
            R.id.home -> {
                Utils.Log(TAG, "home action")
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    /*New version*/
    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        Utils.Log(TAG, "Checkout detail " + Gson().toJson(details))
        if (details != null) {
            val mInfo: PurchaseInfo? = details.purchaseInfo
            if (mInfo != null) {
                val mData: PurchaseData? = mInfo.purchaseData
                if (mData != null) {
                    onCheckout(mData, EnumPurchase.Companion.fromString(mData.productId))
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
            tvLifeTime?.setText(mLifeTime.priceText)
        }
        /*Six month period time*/
        val mSixMonths: SkuDetails? = bp?.getSubscriptionListingDetails(getString(R.string.six_months))
        if (mSixMonths != null) {
            tvMonthly?.setText(mSixMonths.priceText)
        }
        /*One year period time*/
        val mOneYear: SkuDetails? = bp?.getSubscriptionListingDetails(getString(R.string.one_years))
        if (mOneYear != null) {
            tvYearly?.setText(mOneYear.priceText)
        }
        val mTransaction: TransactionDetails? = bp?.getSubscriptionTransactionDetails(getString(R.string.six_months))
        if (mTransaction != null) {
            Utils.Log(TAG, "Result of 6 months " + Gson().toJson(mTransaction))
        }
    }

    fun onCheckout(data: PurchaseData?, purchase: EnumPurchase?) {
        var mCheckout: CheckoutItems? = Utils.getCheckoutItems()
        Utils.Log(TAG, "Call checkout....")
        when (purchase) {
            EnumPurchase.LIFETIME -> {
                if (mCheckout != null) {
                    mCheckout.isPurchasedLifeTime = Utils.isRealCheckedOut(data?.orderId!!)
                } else {
                    mCheckout = CheckoutItems()
                    mCheckout.isPurchasedLifeTime = Utils.isRealCheckedOut(data?.orderId!!)
                }
                Utils.setCheckoutItems(mCheckout)
            }
            EnumPurchase.SIX_MONTHS -> {
                if (mCheckout != null) {
                    if (Utils.isRealCheckedOut(data?.orderId!!)) {
                        if (data.autoRenewing) {
                            mCheckout.isPurchasedSixMonths = true
                        } else {
                            mCheckout.isPurchasedSixMonths = false
                        }
                    } else {
                        mCheckout.isPurchasedSixMonths = false
                    }
                } else {
                    mCheckout = CheckoutItems()
                    if (Utils.isRealCheckedOut(data?.orderId!!)) {
                        if (data.autoRenewing) {
                            mCheckout.isPurchasedSixMonths = true
                        } else {
                            mCheckout.isPurchasedSixMonths = false
                        }
                    } else {
                        mCheckout.isPurchasedSixMonths = false
                    }
                }
                Utils.setCheckoutItems(mCheckout)
                if (Utils.isRealCheckedOut(data.orderId)) {
                    if (data.autoRenewing) {
                        mCheckout.isPurchasedOneYears = true
                    } else {
                        mCheckout.isPurchasedOneYears = false
                    }
                } else {
                    mCheckout.isPurchasedOneYears = false
                }
                Utils.setCheckoutItems(mCheckout)
            }
            EnumPurchase.ONE_YEAR -> {
                if (mCheckout != null) {
                    if (Utils.isRealCheckedOut(data?.orderId!!)) {
                        if (data.autoRenewing) {
                            mCheckout.isPurchasedOneYears = true
                        } else {
                            mCheckout.isPurchasedOneYears = false
                        }
                    } else {
                        mCheckout.isPurchasedOneYears = false
                    }
                } else {
                    mCheckout = CheckoutItems()
                    if (Utils.isRealCheckedOut(data?.orderId!!)) {
                        if (data.autoRenewing) {
                            mCheckout.isPurchasedOneYears = true
                        } else {
                            mCheckout.isPurchasedOneYears = false
                        }
                    } else {
                        mCheckout.isPurchasedOneYears = false
                    }
                }
                Utils.setCheckoutItems(mCheckout)
            }
            else -> Utils.setCheckoutItems(CheckoutItems())
        }
        presenter?.onAddCheckout(data)
    }

    companion object {
        private val TAG = PremiumActivity::class.java.simpleName
        private val FRAGMENT_TAG: String? = SettingsFragment::class.java.getSimpleName() + "::fragmentTag"
    }
}