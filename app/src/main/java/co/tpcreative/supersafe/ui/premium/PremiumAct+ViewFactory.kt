package co.tpcreative.supersafe.ui.premium
import android.graphics.Color
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.extension.toSpanned
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.AppBarStateChangeListener
import co.tpcreative.supersafe.model.CheckoutItems
import co.tpcreative.supersafe.model.EnumPurchase
import co.tpcreative.supersafe.viewmodel.PremiumViewModel
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.PurchaseData
import com.google.android.material.appbar.AppBarLayout
import de.mrapp.android.dialog.MaterialDialog
import kotlinx.android.synthetic.main.activity_premium.*
import kotlinx.android.synthetic.main.activity_premium.appbar
import kotlinx.android.synthetic.main.activity_premium.collapsing_toolbar
import kotlinx.android.synthetic.main.activity_premium.toolbar

fun PremiumAct.initUI(){
    TAG = this::class.java.simpleName
    setupViewModel()
    window.statusBarColor = Color.TRANSPARENT
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    var fragment: Fragment? = supportFragmentManager.findFragmentByTag(PremiumAct.FRAGMENT_TAG)
    if (fragment == null) {
        val mFactory: FragmentFactory = supportFragmentManager.fragmentFactory
        fragment = mFactory.instantiate(ClassLoader.getSystemClassLoader(), PremiumAct.SettingsFragment::class.java.name)
    }
    val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
    transaction.replace(R.id.content_frame, fragment)
    transaction.commit()
    /*Init In app purchase*/onStartInAppPurchase()
    onUpdatedView()
    scrollView?.smoothScrollTo(0, 0)

    llMonths.setOnClickListener {
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

    llYears.setOnClickListener {
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

    llLifeTime.setOnClickListener {
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

    appbar.addOnOffsetChangedListener(object: AppBarStateChangeListener() {
        override fun onStateChanged(appBarLayout: AppBarLayout?, state: State?) {
            Utils.Log(TAG, state?.name)
            when(state) {
                State.COLLAPSED -> { collapsing_toolbar.title = getString(R.string.premium)/* Do something */ }
                State.EXPANDED -> { collapsing_toolbar.title = ""/* Do something */ }
                State.IDLE -> { collapsing_toolbar.title = "" /* Do something */ }
            }
        }
    })
}

fun PremiumAct.onUpdatedView() {
    val isPremium: Boolean = Utils.isPremium()
    if (isPremium) {
        tvTitle?.text= (getText(R.string.you_are_in_premium_features))
        tvPremiumLeft?.visibility = (View.GONE)
        llTwo?.visibility = (View.VISIBLE)
    } else {
        val sourceString: String? = Utils.getFontString(R.string.upgrade_premium_to_use_full_features, getString(R.string.premium_uppercase))
        tvPremiumLeft?.text = sourceString?.toSpanned()
        tvTitle?.text = getString(R.string.choose_your_plans)
    }
}

fun PremiumAct.onCheckout(data: PurchaseData?, purchase: EnumPurchase?) {
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
                    mCheckout.isPurchasedSixMonths = data.autoRenewing
                } else {
                    mCheckout.isPurchasedSixMonths = false
                }
            } else {
                mCheckout = CheckoutItems()
                if (Utils.isRealCheckedOut(data?.orderId!!)) {
                    mCheckout.isPurchasedSixMonths = data.autoRenewing
                } else {
                    mCheckout.isPurchasedSixMonths = false
                }
            }
            Utils.setCheckoutItems(mCheckout)
            if (Utils.isRealCheckedOut(data.orderId)) {
                mCheckout.isPurchasedOneYears = data.autoRenewing
            } else {
                mCheckout.isPurchasedOneYears = false
            }
            Utils.setCheckoutItems(mCheckout)
        }
        EnumPurchase.ONE_YEAR -> {
            if (mCheckout != null) {
                if (Utils.isRealCheckedOut(data?.orderId!!)) {
                    mCheckout.isPurchasedOneYears = data.autoRenewing
                } else {
                    mCheckout.isPurchasedOneYears = false
                }
            } else {
                mCheckout = CheckoutItems()
                if (Utils.isRealCheckedOut(data?.orderId!!)) {
                    mCheckout.isPurchasedOneYears = data.autoRenewing
                } else {
                    mCheckout.isPurchasedOneYears = false
                }
            }
            Utils.setCheckoutItems(mCheckout)
        }
        else -> Utils.setCheckoutItems(CheckoutItems())
    }
}

fun PremiumAct.askWarningFakeCheckout() {
    val dialogBuilder = MaterialDialog.Builder(this, Utils.getCurrentTheme())
    dialogBuilder.setTitle(R.string.key_alert)
    dialogBuilder.setPadding(40, 40, 40, 0)
    dialogBuilder.setMargin(60, 0, 60, 0)
    dialogBuilder.setMessage(getString(R.string.warning_fake_checkout))
    dialogBuilder.setPositiveButton(R.string.got_it) { p0, p1 ->
        finish()
        Utils.Log(TAG,"call finish here")
    }
    val dialog = dialogBuilder.create()
    dialog.show()
}

fun PremiumAct.onStartInAppPurchase() {
    bp = BillingProcessor(this, Utils.GOOGLE_CONSOLE_KEY, this)
    bp?.initialize()
}

fun PremiumAct.checkout(data : PurchaseData){
    viewModel.checkout(data).observe(this, Observer {
        when(it.status){
            Status.SUCCESS -> onUpdatedView()
            else -> Utils.onBasicAlertNotify(this,"Alert",it.message ?:"")
        }
    })
}

private fun PremiumAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(PremiumViewModel::class.java)
}

