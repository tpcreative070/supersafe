package co.tpcreative.supersafe.ui.premium
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentTransaction
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.extension.toSpanned
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.CheckoutItems
import co.tpcreative.supersafe.model.EnumPurchase
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.PurchaseData
import kotlinx.android.synthetic.main.activity_premium.*

fun PremiumAct.initUI(){
    TAG = this::class.java.simpleName
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    presenter = PremiumPresenter()
    presenter?.bindView(this)
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
}


fun PremiumAct.onUpdatedView() {
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

fun PremiumAct.onStartInAppPurchase() {
    bp = BillingProcessor(this, Utils.GOOGLE_CONSOLE_KEY, this)
    bp?.initialize()
}
