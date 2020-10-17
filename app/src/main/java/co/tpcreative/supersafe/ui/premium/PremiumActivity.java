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
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceFragmentCompat;
import com.google.gson.Gson;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.RequestListener;
import org.solovyev.android.checkout.Sku;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.ui.settings.SettingsActivity;

public class PremiumActivity extends BaseActivity implements BaseView{

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
    private ActivityCheckout mCheckout;
    private ActivityCheckout mCheckoutLifeTime;
    private InventoryCallback mInventoryCallback;
    private InventoryCallbackLifeTime mInventoryCallbackLifeTime;
    private Inventory.Product mProduct;
    private Inventory.Product mProductLifeTime;
    private Sku mMonths;
    private Sku mYears;
    private Sku mLifeTime;
    private boolean isPurchased;
    private PremiumPresenter presenter;

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
            fragment = Fragment.instantiate(this, PremiumActivity.SettingsFragment.class.getName());
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
        final boolean isPremium = User.getInstance().isPremium();
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
        if (mProduct==null){
            return;
        }
        if (User.getInstance().isPremium()){
            return;
        }
        Utils.Log(TAG,"Months");
        if (mProduct.getSkus()!=null && mProduct.getSkus().size()>0){
            if (mMonths!=null){
                final Purchase purchase = mProduct.getPurchaseInState(mMonths, Purchase.State.PURCHASED);
                if (purchase != null) {
                    Toast.makeText(getApplicationContext(),"Already charged",Toast.LENGTH_SHORT).show();
                    consume(purchase);
                } else {
                    Utils.Log(TAG,"value...?"+ new Gson().toJson(mMonths));
                    purchase(mMonths);
                }
            }
        }
    }

    @OnClick(R.id.llYears)
    public void onClickedYears(View view){
        Utils.Log(TAG,"Years");
        if (mProduct==null){
            return;
        }
        if (User.getInstance().isPremium()){
            return;
        }
        if (mProduct.getSkus()!=null && mProduct.getSkus().size()>0){
            if (mYears!=null){
                final Purchase purchase = mProduct.getPurchaseInState(mYears, Purchase.State.PURCHASED);
                if (purchase != null) {
                    Toast.makeText(getApplicationContext(),"Already charged",Toast.LENGTH_SHORT).show();
                    consume(purchase);
                } else {
                    Utils.Log(TAG,"value...?"+ new Gson().toJson(mYears));
                    purchase(mYears);
                }
            }
        }
    }

    @OnClick(R.id.llLifeTime)
    public void onClickedLifeTime(View view){
        if (mProductLifeTime==null){
            return;
        }
        if (User.getInstance().isPremium()){
            return;
        }
        if (mProductLifeTime.getSkus()!=null && mProductLifeTime.getSkus().size()>0){
            if (mLifeTime!=null){
                //final Purchase purchase = mProductLifeTime.getPurchaseInState(mLifeTime, Purchase.State.PURCHASED);
                if (isPurchased) {
                    Toast.makeText(getApplicationContext(),"Already charged",Toast.LENGTH_SHORT).show();
                    //consumeLifeTime(purchase);
                } else {
                    Utils.Log(TAG,"value...?"+ new Gson().toJson(mLifeTime));
                    purchaseLifeTime(mLifeTime);
                }
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
        super.onDestroy();
        Utils.Log(TAG,"OnDestroy");
        EventBus.getDefault().unregister(this);
        presenter.unbindView();
        /*Destroy In App Purchase*/
        if (mCheckout!=null){
            mCheckout.stop();
        }
        if (mCheckoutLifeTime!=null){
            mCheckoutLifeTime.stop();
        }
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
        mCheckout.onActivityResult(requestCode, resultCode, data);
        mCheckoutLifeTime.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class InventoryCallback implements Inventory.Callback {
        @Override
        public void onLoaded(Inventory.Products products) {
            final Inventory.Product product = products.get(ProductTypes.SUBSCRIPTION);
            if (!product.supported) {
                // billing is not supported, user can't purchase anything
                return;
            }
            mProduct = product;
            if (mProduct!=null){
                if (mProduct.getSkus().size()>0){
                  for (int i=0;i<mProduct.getSkus().size();i++){
                      Sku index = mProduct.getSkus().get(i);
                      if (index.id.code.equals(getString(R.string.six_months))){
                          tvMonthly.setText(index.price);
                          mMonths = index;
                      }
                      else if (index.id.code.equals(getString(R.string.one_years))){
                          tvYearly.setText(index.price);
                          mYears = index;
                      }
                  }
                }
            }
            Utils.Log(TAG,"value : "+ new Gson().toJson(product));
        }
    }

    private class InventoryCallbackLifeTime implements Inventory.Callback {
        @Override
        public void onLoaded(Inventory.Products products) {
            final Inventory.Product product = products.get(ProductTypes.IN_APP);
            if (!product.supported) {
                // billing is not supported, user can't purchase anything
                return;
            }
            mProductLifeTime = product;
            if (mProductLifeTime!=null){
                if (mProductLifeTime.getSkus().size()>0){
                    for (int i=0;i<mProductLifeTime.getSkus().size();i++){
                        Sku index = mProductLifeTime.getSkus().get(i);
                        if (index.id.code.equals(getString(R.string.life_time))){
                            tvLifeTime.setText(index.price);
                            mLifeTime = index;
                            if (mProductLifeTime.isPurchased(mLifeTime)){
                                isPurchased = true;
                            }
                        }
                    }
                }
            }
            Utils.Log(TAG,"value : "+ new Gson().toJson(product));
        }
    }

    /**
     * @return {@link RequestListener} that reloads inventory when the action is finished
     */

    private <T> RequestListener<T> makeRequestListener() {
        return new RequestListener<T>() {
            @Override
            public void onSuccess(@Nonnull T result) {
                try {
                    Utils.onWriteLog(new Gson().toJson("Checkout "+result),EnumStatus.CHECKOUT);
                    if (presenter!=null){
                        final Purchase purchase = (Purchase) result;
                        presenter.onAddCheckout(purchase);
                    }
                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(),"Error "+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
                reloadInventory();
                reloadInventoryLifeTime();
            }
            @Override
            public void onError(int response, @Nonnull Exception e) {
                reloadInventory();
                reloadInventoryLifeTime();
            }
        };
    }

    private void consume(final Purchase purchase) {
        mCheckout.whenReady(new Checkout.EmptyListener() {
            @Override
            public void onReady(@Nonnull BillingRequests requests) {
                requests.consume(purchase.token, makeRequestListener());
            }
        });
    }

    private void consumeLifeTime(final Purchase purchase) {
        mCheckout.whenReady(new Checkout.EmptyListener() {
            @Override
            public void onReady(@Nonnull BillingRequests requests) {
                requests.consume(purchase.token, makeRequestListener());
            }
        });
    }

    public void onStartInAppPurchase(){
        final Billing billing = SuperSafeApplication.getInstance().getBilling();
        mCheckout = Checkout.forActivity(this, billing);
        mCheckoutLifeTime = Checkout.forActivity(this,billing);
        mInventoryCallback = new InventoryCallback();
        mInventoryCallbackLifeTime  = new InventoryCallbackLifeTime();
        mCheckout.start();
        mCheckoutLifeTime.start();
        reloadInventory();
        reloadInventoryLifeTime();
    }

    private void purchase(Sku sku) {
        final RequestListener<Purchase> listener = makeRequestListener();
        mCheckout.startPurchaseFlow(sku, null, listener);
    }

    private void purchaseLifeTime(Sku sku) {
        final RequestListener<Purchase> listener = makeRequestListener();
        mCheckoutLifeTime.startPurchaseFlow(sku, null, listener);
    }

    private void reloadInventory() {
        List<String> mList = new ArrayList<>();
        mList.add(getString(R.string.six_months));
        mList.add(getString(R.string.one_years));
        //mList.add(getString(R.string.life_time));
        final Inventory.Request request = Inventory.Request.create();
        // load purchase info
        request.loadAllPurchases();
        // load SKU details
        request.loadSkus(ProductTypes.SUBSCRIPTION,mList);
        mCheckout.loadInventory(request, mInventoryCallback);
    }

    private void reloadInventoryLifeTime() {
        List<String> mList = new ArrayList<>();
        mList.add(getString(R.string.life_time));
        final Inventory.Request request = Inventory.Request.create();
        // load purchase info
        request.loadAllPurchases();
        // load SKU details
        request.loadSkus(ProductTypes.IN_APP,mList);
        mCheckoutLifeTime.loadInventory(request, mInventoryCallbackLifeTime);
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
        switch (status){
            case CHECKOUT:{
                onUpdatedView();
                break;
            }
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
}
