package co.tpcreative.supersafe.common.controller;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.PurchaseData;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.gson.Gson;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.CheckoutItems;
import co.tpcreative.supersafe.model.EnumPurchase;
import co.tpcreative.supersafe.model.User;

public class PremiumManager implements BillingProcessor.IBillingHandler{

    private static PremiumManager instance;
    private String TAG = PremiumManager.class.getSimpleName();

    /*New version*/
    BillingProcessor bp;

    public static PremiumManager getInstance() {
        if (instance == null) {
            instance = new PremiumManager();
        }
        return instance;
    }


    public void onStartInAppPurchase() {
        bp = new BillingProcessor(SuperSafeApplication.getInstance(),Utils.GOOGLE_CONSOLE_KEY, this);
        bp.initialize();
    }

    public void onStop() {
        if (bp != null) {
            bp.release();
        }
    }

    /*New version*/
    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {

    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {

    }

    @Override
    public void onBillingInitialized() {
        /*Checking life time in-app*/
        if (bp.isPurchased(SuperSafeApplication.getInstance().getString(R.string.life_time))){
            final TransactionDetails details = bp.getPurchaseTransactionDetails(SuperSafeApplication.getInstance().getString(R.string.life_time));
            if (details!=null){
                final PurchaseData mPurchaseData = details.purchaseInfo.purchaseData;
                if (mPurchaseData!=null){
                    onCheckout(mPurchaseData,EnumPurchase.LIFETIME);
                }
            }
        }

        /*Checking subscription for period time 6 months*/
        else if (bp.isSubscribed(SuperSafeApplication.getInstance().getString(R.string.six_months))){
            final TransactionDetails details = bp.getSubscriptionTransactionDetails(SuperSafeApplication.getInstance().getString(R.string.six_months));
            if (details!=null){
                final PurchaseData mPurchaseData = details.purchaseInfo.purchaseData;
                if (mPurchaseData!=null){
                    onCheckout(mPurchaseData,EnumPurchase.SIX_MONTHS);
                }
            }
        }

        /*Checking subscription for period time 1 year*/
        else if (bp.isSubscribed(SuperSafeApplication.getInstance().getString(R.string.one_years))){
            final TransactionDetails details = bp.getSubscriptionTransactionDetails(SuperSafeApplication.getInstance().getString(R.string.one_years));
            if (details!=null){
                final PurchaseData mPurchaseData = details.purchaseInfo.purchaseData;
                if (mPurchaseData!=null){
                    onCheckout(mPurchaseData,EnumPurchase.ONE_YEAR);
                }
            }
        }else {
            onCheckout(null,EnumPurchase.NONE);
        }
    }

    private void onCheckout(PurchaseData data, EnumPurchase purchase){
        CheckoutItems mCheckout = Utils.getCheckoutItems();
        Utils.Log(TAG,"Call checkout....");
        Utils.Log(TAG,new Gson().toJson(data));
        switch (purchase){
            case LIFETIME:
                if (mCheckout != null) {
                    mCheckout.isPurchasedLifeTime = Utils.isRealCheckedOut(data.orderId);
                }
                else{
                    mCheckout = new CheckoutItems();
                    mCheckout.isPurchasedLifeTime = Utils.isRealCheckedOut(data.orderId);
                }
                Utils.setCheckoutItems(mCheckout);
                break;
            case SIX_MONTHS:
                if (mCheckout != null) {
                    if (Utils.isRealCheckedOut(data.orderId)){
                        if (data.autoRenewing){
                            mCheckout.isPurchasedSixMonths = true;
                        }else{
                            mCheckout.isPurchasedSixMonths = false;
                        }
                    }else{
                        mCheckout.isPurchasedSixMonths = false;
                    }
                }
                else{
                    mCheckout = new CheckoutItems();
                    if (Utils.isRealCheckedOut(data.orderId)){
                        if (data.autoRenewing){
                            mCheckout.isPurchasedSixMonths = true;
                        }else{
                            mCheckout.isPurchasedSixMonths = false;
                        }
                    }else{
                        mCheckout.isPurchasedSixMonths = false;
                    }
                }
                Utils.setCheckoutItems(mCheckout);
            case ONE_YEAR:
                if (mCheckout != null) {
                    if (Utils.isRealCheckedOut(data.orderId)){
                        if (data.autoRenewing){
                            mCheckout.isPurchasedOneYears = true;
                        }else{
                            mCheckout.isPurchasedOneYears = false;
                        }
                    }else{
                        mCheckout.isPurchasedOneYears = false;
                    }
                }
                else{
                    mCheckout = new CheckoutItems();
                    if (Utils.isRealCheckedOut(data.orderId)){
                        if (data.autoRenewing){
                            mCheckout.isPurchasedOneYears = true;
                        }else{
                            mCheckout.isPurchasedOneYears = false;
                        }
                    }else{
                        mCheckout.isPurchasedOneYears = false;
                    }
                }
                Utils.setCheckoutItems(mCheckout);
                break;
            default:
                Utils.setCheckoutItems(new CheckoutItems());
                break;
        }
    }
}
