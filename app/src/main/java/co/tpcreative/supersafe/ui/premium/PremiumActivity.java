package co.tpcreative.supersafe.ui.premium;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
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
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.SingletonPremiumTimer;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.ui.settings.SettingsActivity;

public class PremiumActivity extends BaseActivity implements SingletonPremiumTimer.SingletonPremiumTimerListener,BaseView{

    private static final String TAG = PremiumActivity.class.getSimpleName();
    private static final String FRAGMENT_TAG = SettingsActivity.class.getSimpleName() + "::fragmentTag";
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.tvPremiumLeft)
    TextView tvPremiumLeft;
    @BindView(R.id.tvMonthly)
    TextView tvMonthly;
    @BindView(R.id.tvYearly)
    TextView tvYearly;
    @BindView(R.id.tvLifeTime)
    TextView tvLifeTime;
    @BindView(R.id.tvTitle)
    TextView tvTitle;

    @BindView(R.id.llOne)
    LinearLayout llOne;
    @BindView(R.id.llTwo)
    LinearLayout llTwo;
    @BindView(R.id.llSwitchToBasic)
    LinearLayout llSwitchToBasic;

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

        onDrawOverLay(this);

        /*Init In app purchase*/
        onStartInAppPurchase();
        onUpdatedView();

    }


    public void onUpdatedView(){
        final boolean isPremium = User.getInstance().isPremium();
        if (isPremium){
            tvTitle.setText(getText(R.string.you_are_in_premium_features));
            tvPremiumLeft.setVisibility(View.GONE);
            llOne.setVisibility(View.VISIBLE);
            llTwo.setVisibility(View.VISIBLE);
            llSwitchToBasic.setVisibility(View.GONE);
        }
        else{
            if (User.getInstance().isPremiumComplimentary()){
                if (SingletonPremiumTimer.getInstance().getDaysLeft()!=null){
                    String dayLeft = SingletonPremiumTimer.getInstance().getDaysLeft();
                    String sourceString = Utils.getFontString(R.string.your_complimentary_premium_remaining,dayLeft);
                    tvPremiumLeft.setText(Html.fromHtml(sourceString));
                }
            }
            else{
                tvTitle.setText(getString(R.string.premium_expired));
                tvTitle.setTextColor(getResources().getColor(R.color.red_300));
                tvPremiumLeft.setVisibility(View.GONE);
                llOne.setVisibility(View.GONE);
                llTwo.setVisibility(View.GONE);
                llSwitchToBasic.setVisibility(View.VISIBLE);
            }
        }

        if (User.getInstance().isPremiumExpired()){
            if (PrefsController.getBoolean(getString(R.string.key_switch_to_basic),false)){
                llOne.setVisibility(View.VISIBLE);
                llTwo.setVisibility(View.VISIBLE);
                llSwitchToBasic.setVisibility(View.GONE);
            }
            else{
                llOne.setVisibility(View.GONE);
                llTwo.setVisibility(View.GONE);
                llSwitchToBasic.setVisibility(View.VISIBLE);

            }
        }

    }

    @OnClick(R.id.btnSwitchToBasic)
    public void onClickedSwitchToBasic(View view){
        PrefsController.putBoolean(getString(R.string.key_switch_to_basic),true);
        recreate();
    }


    @Override
    public void onPremiumTimer(String days, String hours, String minutes, String seconds) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String value = Utils.getFontString(R.string.your_complimentary_premium_remaining,days);
                    tvPremiumLeft.setText(Html.fromHtml(value));
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @OnClick(R.id.llMonths)
    public void onClickedMonths(View view){
        if (User.getInstance().isPremium()){
            return;
        }
        if (mProduct==null){
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

    @Override
    public void onNotifier(EnumStatus status) {
        switch (status){
            case FINISH:{
                finish();
                break;
            }
        }
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onRegisterHomeWatcher();
        SuperSafeApplication.getInstance().writeKeyHomePressed(PremiumActivity.class.getSimpleName());

        final boolean isPremium = User.getInstance().isPremium();
        if (!isPremium){
            SingletonPremiumTimer.getInstance().setListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*Destroy In App Purchase*/
        if (mCheckout!=null){
            mCheckout.stop();
        }
        if (mCheckoutLifeTime!=null){
            mCheckoutLifeTime.stop();
        }
        SingletonPremiumTimer.getInstance().setListener(null);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private Preference mAccount;

        private Preference.OnPreferenceChangeListener createChangeListener() {
            return new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(final Preference preference, final Object newValue) {
                    return true;
                }
            };
        }

        private Preference.OnPreferenceClickListener createActionPreferenceClickListener() {
            return new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (preference instanceof Preference){

                    }
                    return true;
                }
            };
        }

        @Override
        public final void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_general_premium);
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
                Toast.makeText(getApplicationContext(),"Message "+ message,Toast.LENGTH_SHORT).show();
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

    public void onSwitchToBasic(){
        if (User.getInstance().isPremiumExpired()){
            if (!PrefsController.getBoolean(getString(R.string.key_switch_to_basic),false)){
               Navigator.onMoveToFaceDown(getApplicationContext());
            }
        }
        else {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                onSwitchToBasic();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onSwitchToBasic();
    }

}
