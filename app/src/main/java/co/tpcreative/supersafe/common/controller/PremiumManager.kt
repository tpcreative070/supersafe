package co.tpcreative.supersafe.common.controller
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.extension.getCheckoutItems
import co.tpcreative.supersafe.common.extension.putCheckoutItems
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
    override fun onPurchaseHistoryRestored() {
        Utils.Log(TAG,"Restored..........")
    }
    override fun onBillingError(errorCode: Int, error: Throwable?) {}
    override fun onBillingInitialized() {
        /*Checking life time in-app*/
        bp?.let {bpResult ->
            val detailTimeLife: TransactionDetails? = bpResult.getPurchaseTransactionDetails(SuperSafeApplication.getInstance().getString(R.string.life_time))
            val detailSixMonths: TransactionDetails? = bpResult.getSubscriptionTransactionDetails(SuperSafeApplication.getInstance().getString(R.string.six_months))
            val detailsOneYear: TransactionDetails? = bpResult.getSubscriptionTransactionDetails(SuperSafeApplication.getInstance().getString(R.string.one_years))
            if (detailTimeLife != null) {
                val mPurchaseData: PurchaseData = detailTimeLife.purchaseInfo.purchaseData
                onCheckout(mPurchaseData, EnumPurchase.LIFETIME)
            }else if (detailSixMonths!=null) {
                val mPurchaseData: PurchaseData = detailSixMonths.purchaseInfo.purchaseData
                onCheckout(mPurchaseData, EnumPurchase.SIX_MONTHS)
            } else if (detailsOneYear!=null) {
                val mPurchaseData: PurchaseData = detailsOneYear.purchaseInfo.purchaseData
                onCheckout(mPurchaseData, EnumPurchase.ONE_YEAR)
            } else {
                onCheckout(PurchaseData(), EnumPurchase.NONE)
            }
        }
        bp?.loadOwnedPurchasesFromGoogle()
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
                Utils.putCheckoutItems(mCheckout)
            }
            EnumPurchase.SIX_MONTHS -> {
                if (mCheckout != null) {
                    if (Utils.isRealCheckedOut(data.orderId)) {
                        mCheckout.isPurchasedSixMonths = data.autoRenewing
                        Utils.Log(TAG,"Checking...6")
                        /*When subscription request to recharge*/
                        if (!mCheckout.isPurchasedSixMonths && Utils.checkingServicesToStopPremiumFeatures()) {
                            Utils.onPushEventBus(EnumStatus.EXPIRED_SUBSCRIPTIONS)
                        }
                    } else {
                        mCheckout.isPurchasedSixMonths = false
                    }
                } else {
                    mCheckout = CheckoutItems()
                    if (Utils.isRealCheckedOut(data.orderId)) {
                        mCheckout.isPurchasedSixMonths = data.autoRenewing
                        /*When subscription request to recharge*/
                        if (!mCheckout.isPurchasedSixMonths && Utils.checkingServicesToStopPremiumFeatures()) {
                            Utils.onPushEventBus(EnumStatus.EXPIRED_SUBSCRIPTIONS)
                        }
                    } else {
                        mCheckout.isPurchasedSixMonths = false
                    }
                }
                Utils.putCheckoutItems(mCheckout)
                Utils.Log(TAG,"Preparing save ${Gson().toJson(mCheckout)}")
            }
            EnumPurchase.ONE_YEAR -> {
                Utils.Log(TAG,"one year...")
                if (mCheckout != null) {
                    if (Utils.isRealCheckedOut(data.orderId)) {
                        mCheckout.isPurchasedOneYears = data.autoRenewing
                        Utils.Log(TAG,"Checking...1y")
                        /*When subscription request to recharge*/
                        if (!mCheckout.isPurchasedOneYears && Utils.checkingServicesToStopPremiumFeatures()) {
                            Utils.onPushEventBus(EnumStatus.EXPIRED_SUBSCRIPTIONS)
                        }
                    } else {
                        mCheckout.isPurchasedOneYears = false
                    }
                } else {
                    mCheckout = CheckoutItems()
                    if (Utils.isRealCheckedOut(data.orderId)) {
                        mCheckout.isPurchasedOneYears = data.autoRenewing
                        /*When subscription request to recharge*/
                        if (!mCheckout.isPurchasedOneYears && Utils.checkingServicesToStopPremiumFeatures()) {
                            Utils.onPushEventBus(EnumStatus.EXPIRED_SUBSCRIPTIONS)
                        }
                    } else {
                        mCheckout.isPurchasedOneYears = false
                    }
                }
                Utils.Log(TAG,"Preparing save ${Gson().toJson(mCheckout)}")
                Utils.putCheckoutItems(mCheckout)
            }
            else -> {
                /*When the user cancel and refund*/
                if (Utils.checkingServicesToStopPremiumFeatures()){
                    Utils.onPushEventBus(EnumStatus.EXPIRED_SUBSCRIPTIONS)
                }
                Utils.checkingExistingSaver()
                Utils.putCheckoutItems(CheckoutItems())
            }
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