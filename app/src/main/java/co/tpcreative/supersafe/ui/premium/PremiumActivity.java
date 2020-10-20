package co.tpcreative.supersafe.ui.premium;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceFragmentCompat;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.PurchaseData;
import com.anjlab.android.iab.v3.PurchaseInfo;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.gson.Gson;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
//import org.solovyev.android.checkout.ActivityCheckout;
//import org.solovyev.android.checkout.Billing;
//import org.solovyev.android.checkout.BillingRequests;
//import org.solovyev.android.checkout.Checkout;
//import org.solovyev.android.checkout.Inventory;
//import org.solovyev.android.checkout.ProductTypes;
//import org.solovyev.android.checkout.Purchase;
//import org.solovyev.android.checkout.RequestListener;
//import org.solovyev.android.checkout.Sku;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.presenter.BaseView;
//import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.CheckoutItems;
import co.tpcreative.supersafe.model.EnumPurchase;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.ui.settings.SettingsActivity;

public class PremiumActivity extends BaseActivity implements BaseView, BillingProcessor.IBillingHandler {

    private static final String TAG = PremiumActivity.class.getSimpleName();
    private static final String FRAGMENT_TAG = SettingsActivity.class.getSimpleName() + "::fragmentTag";
    @BindView(R.id.scrollView)
    NestedScrollView scrollView;
    @BindView(R.id.tvPremiumLeft)
    AppCompatTextView tvPremiumLeft;
    @BindView(R.id.tvMonthly)
    AppCompatTextView tvMonthly;
    @BindView(R.id.tvYearly)
    AppCompatTextView tvYearly;
    @BindView(R.id.tvLifeTime)
    AppCompatTextView tvLifeTime;
    @BindView(R.id.tvTitle)
    AppCompatTextView tvTitle;
    @BindView(R.id.llTwo)
    LinearLayout llTwo;
    /*In app purchase*/
    private PremiumPresenter presenter;

    /*New version*/
    private BillingProcessor bp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        presenter = new PremiumPresenter();
        presenter.bindView(this);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null) {
            final FragmentFactory mFactory = getSupportFragmentManager().getFragmentFactory();
            fragment = mFactory.instantiate(ClassLoader.getSystemClassLoader(),SettingsFragment.class.getName());
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
        /*Init In app purchase*/
        onStartInAppPurchase();
        onUpdatedView();
        scrollView.smoothScrollTo(0,0);
    }

    public void onUpdatedView(){
        final boolean isPremium = Utils.isPremium();
        if (isPremium){
            tvTitle.setText(getText(R.string.you_are_in_premium_features));
            tvPremiumLeft.setVisibility(View.GONE);
            llTwo.setVisibility(View.VISIBLE);
        }
        else{
            String sourceString = Utils.getFontString(R.string.upgrade_premium_to_use_full_features,getString(R.string.premium_uppercase));
            tvPremiumLeft.setText(Html.fromHtml(sourceString));
            tvTitle.setText(getString(R.string.choose_your_plans));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @OnClick(R.id.llMonths)
    public void onClickedMonths(View view){
        if (BillingProcessor.isIabServiceAvailable(this)){
            Utils.Log(TAG,"purchase new");
            if (bp.isSubscribed(getString(R.string.six_months))){
                Utils.Log(TAG,"Already charged");
                bp.loadOwnedPurchasesFromGoogle();
            }else{
                bp.subscribe(this,getString(R.string.six_months));
            }
        }
    }

    @OnClick(R.id.llYears)
    public void onClickedYears(View view){
        Utils.Log(TAG,"Years");
        if (BillingProcessor.isIabServiceAvailable(this)){
            Utils.Log(TAG,"purchase new");
            if (bp.isSubscribed(getString(R.string.one_years))){
                Utils.Log(TAG,"Already charged");
                bp.loadOwnedPurchasesFromGoogle();
            }else{
                bp.subscribe(this,getString(R.string.one_years));
            }
        }
    }

    @OnClick(R.id.llLifeTime)
    public void onClickedLifeTime(View view){
        if (BillingProcessor.isIabServiceAvailable(this)){
            Utils.Log(TAG,"purchase new");
            if (bp.isPurchased(getString(R.string.life_time))){
                Utils.Log(TAG,"Already charged");
                bp.consumePurchase(getString(R.string.life_time));
                bp.loadOwnedPurchasesFromGoogle();
            }else{
                bp.purchase(this,getString(R.string.life_time));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EnumStatus event) {
        switch (event){
            case FINISH:{
                Navigator.onMoveToFaceDown(this);
                break;
            }
            case PREMIUM:{
                scrollView.smoothScrollTo(0,0);
                break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        onRegisterHomeWatcher();
    }

    @Override
    protected void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
        Utils.Log(TAG,"OnDestroy");
        EventBus.getDefault().unregister(this);
        presenter.unbindView();
    }

    @Override
    protected void onStopListenerAWhile() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public final void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_general_premium);
            EventBus.getDefault().post(EnumStatus.PREMIUM);
        }
    }

    /* Start in app purchase */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    public void onStartInAppPurchase(){
        bp = new BillingProcessor(this,Utils.GOOGLE_CONSOLE_KEY, this);
        bp.initialize();
    }

    /*Presenter*/
    @Override
    public void onStartLoading(EnumStatus status) {
    }

    @Override
    public void onStopLoading(EnumStatus status) {
    }

    @Override
    public void onError(String message) {
    }

    @Override
    public void onError(String message, EnumStatus status) {
        switch (status){
            case CHECKOUT:{
                Toast.makeText(getApplicationContext(),"Message "+ message,Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    @Override
    public void onSuccessful(String message) {
    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {
        if (status == EnumStatus.CHECKOUT) {
            Utils.Log(TAG, message + "-" + status.name());
            onUpdatedView();
        }
    }

    @Override
    public void onSuccessful(String message, EnumStatus status, Object object) {
    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List list) {
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                Utils.Log(TAG,"home action");
                onBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
       super.onBackPressed();
    }

    /*New version*/
    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        Utils.Log(TAG,"Checkout detail "+ new Gson().toJson(details));
        if (details!=null){
            final PurchaseInfo mInfo = details.purchaseInfo;
            if (mInfo!=null){
                final PurchaseData mData = mInfo.purchaseData;
                if (mData!=null){
                    onCheckout(mData,EnumPurchase.fromString(mData.productId));
                }
            }
        }
    }

    @Override
    public void onPurchaseHistoryRestored() {
        Utils.Log(TAG,"onPurchaseHistoryRestored");
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Utils.Log(TAG,"onBillingError " + error);
    }

    @Override
    public void onBillingInitialized() {
        Utils.Log(TAG,"onBillingInitialized");
        /*Life time period time*/
        final SkuDetails mLifeTime =  bp.getPurchaseListingDetails(getString(R.string.life_time));
        if (mLifeTime!=null){
            tvLifeTime.setText(mLifeTime.priceText);
        }
        /*Six month period time*/
        final SkuDetails mSixMonths =  bp.getSubscriptionListingDetails(getString(R.string.six_months));
        if (mSixMonths!=null){
            tvMonthly.setText(mSixMonths.priceText);
        }
        /*One year period time*/
        final SkuDetails mOneYear =  bp.getSubscriptionListingDetails(getString(R.string.one_years));
        if (mOneYear!=null){
            tvYearly.setText(mOneYear.priceText);
        }

        final TransactionDetails mTransaction = bp.getSubscriptionTransactionDetails(getString(R.string.six_months));
        if (mTransaction!=null){
            Utils.Log(TAG,"Result of 6 months " + new Gson().toJson(mTransaction));
        }
    }

    public void onCheckout(PurchaseData data, EnumPurchase purchase){
        CheckoutItems mCheckout = Utils.getCheckoutItems();
        Utils.Log(TAG,"Call checkout....");
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
        presenter.onAddCheckout(data);
    }
}
