package co.tpcreative.supersafe.common.controller
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.CheckoutItems
import co.tpcreative.supersafe.model.EnumPurchase
import co.tpcreative.supersafe.model.EnumStatus
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.PurchaseData
import com.anjlab.android.iab.v3.TransactionDetails
import com.google.gson.Gson

class PremiumManager : BillingProcessor.IBillingHandler {
    private val TAG = PremiumManager::class.java.simpleName

    /*New version*/
    private var bp: BillingProcessor? = null
    fun onStartInAppPurchase() {
        bp = BillingProcessor(SuperSafeApplication.getInstance(), Utils.GOOGLE_CONSOLE_KEY, this)
        bp?.initialize()
    }

    fun onStop() {
        bp?.release()
    }

    /*New version*/
    override fun onProductPurchased(productId: String, details: TransactionDetails?) {}
    override fun onPurchaseHistoryRestored() {}
    override fun onBillingError(errorCode: Int, error: Throwable?) {}
    override fun onBillingInitialized() {
        /*Checking life time in-app*/
        bp?.let {bpResult ->
            if (bpResult.isPurchased(SuperSafeApplication.getInstance().getString(R.string.life_time))) {
                val details: TransactionDetails? = bpResult.getPurchaseTransactionDetails(SuperSafeApplication.getInstance().getString(R.string.life_time))
                if (details != null) {
                    val mPurchaseData: PurchaseData = details.purchaseInfo.purchaseData
                    onCheckout(mPurchaseData, EnumPurchase.LIFETIME)
                }
            } else if (bpResult.isSubscribed(SuperSafeApplication.getInstance().getString(R.string.six_months))) {
                val details: TransactionDetails? = bpResult.getSubscriptionTransactionDetails(SuperSafeApplication.getInstance().getString(R.string.six_months))
                if (details != null) {
                    val mPurchaseData: PurchaseData = details.purchaseInfo.purchaseData
                    onCheckout(mPurchaseData, EnumPurchase.SIX_MONTHS)
                }
            } else if (bpResult.isSubscribed(SuperSafeApplication.getInstance().getString(R.string.one_years))) {
                val details: TransactionDetails? = bpResult.getSubscriptionTransactionDetails(SuperSafeApplication.getInstance().getString(R.string.one_years))
                if (details != null) {
                    val mPurchaseData: PurchaseData = details.purchaseInfo.purchaseData
                    onCheckout(mPurchaseData, EnumPurchase.ONE_YEAR)
                }
            } else {
                onCheckout(PurchaseData(), EnumPurchase.NONE)
            }
        }
    }

    private fun onCheckout(data: PurchaseData, purchase: EnumPurchase) {
        var mCheckout: CheckoutItems? = Utils.getCheckoutItems()
        Utils.Log(TAG, "Call checkout....")
        Utils.Log(TAG, Gson().toJson(data))
        when (purchase) {
            EnumPurchase.LIFETIME -> {
                if (mCheckout != null) {
                    mCheckout.isPurchasedLifeTime = Utils.isRealCheckedOut(data.orderId)
                } else {
                    mCheckout = CheckoutItems()
                    mCheckout.isPurchasedLifeTime = Utils.isRealCheckedOut(data.orderId)
                }
                Utils.setCheckoutItems(mCheckout)
            }
            EnumPurchase.SIX_MONTHS -> {
                if (mCheckout != null) {
                    if (Utils.isRealCheckedOut(data.orderId)) {
                        mCheckout.isPurchasedSixMonths = data.autoRenewing
                        if (!mCheckout.isPurchasedSixMonths && !Utils.isAlreadyAskedExpiration()){
                            Utils.onPushEventBus(EnumStatus.EXPIRED_SUBSCRIPTIONS)
                        }
                    } else {
                        mCheckout.isPurchasedSixMonths = false
                    }
                } else {
                    mCheckout = CheckoutItems()
                    if (Utils.isRealCheckedOut(data.orderId)) {
                        mCheckout.isPurchasedSixMonths = data.autoRenewing
                        if (!mCheckout.isPurchasedSixMonths && !Utils.isAlreadyAskedExpiration()){
                            Utils.onPushEventBus(EnumStatus.EXPIRED_SUBSCRIPTIONS)
                        }
                    } else {
                        mCheckout.isPurchasedSixMonths = false
                    }
                }
                Utils.setCheckoutItems(mCheckout)
            }
            EnumPurchase.ONE_YEAR -> {
                if (mCheckout != null) {
                    if (Utils.isRealCheckedOut(data.orderId)) {
                        mCheckout.isPurchasedOneYears = data.autoRenewing
                        if (!mCheckout.isPurchasedOneYears && !Utils.isAlreadyAskedExpiration()){
                            Utils.onPushEventBus(EnumStatus.EXPIRED_SUBSCRIPTIONS)
                        }
                    } else {
                        mCheckout.isPurchasedOneYears = false
                    }
                } else {
                    mCheckout = CheckoutItems()
                    if (Utils.isRealCheckedOut(data.orderId)) {
                        mCheckout.isPurchasedOneYears = data.autoRenewing
                        if (!mCheckout.isPurchasedOneYears && !Utils.isAlreadyAskedExpiration()){
                            Utils.onPushEventBus(EnumStatus.EXPIRED_SUBSCRIPTIONS)
                        }
                    } else {
                        mCheckout.isPurchasedOneYears = false
                    }
                }
                Utils.setCheckoutItems(mCheckout)
            }
            else -> Utils.setCheckoutItems(CheckoutItems())
        }
    }

    companion object {
        private var instance: PremiumManager? = null
        fun getInstance(): PremiumManager {
            if (instance == null) {
                instance = PremiumManager()
            }
            return instance!!
        }
    }
}