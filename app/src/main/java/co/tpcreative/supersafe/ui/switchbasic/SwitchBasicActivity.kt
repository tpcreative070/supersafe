package co.tpcreative.supersafe.ui.switchbasic
import android.os.Bundle
import androidx.appcompat.widget.AppCompatTextView
import butterknife.BindView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivity
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStatus
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

//import org.solovyev.android.checkout.ActivityCheckout;
//import org.solovyev.android.checkout.Billing;
//import org.solovyev.android.checkout.Checkout;
//import org.solovyev.android.checkout.Inventory;
//import org.solovyev.android.checkout.ProductTypes;
//import org.solovyev.android.checkout.RequestListener;
//import org.solovyev.android.checkout.Sku;
class SwitchBasicActivity : BaseActivity() {
    //    /*In app purchase*/
    //    private ActivityCheckout mCheckout;
    //    private ActivityCheckout mCheckoutLifeTime;
    //    private SwitchBasicActivity.InventoryCallback mInventoryCallback;
    //    private SwitchBasicActivity.InventoryCallbackLifeTime mInventoryCallbackLifeTime;
    //    private Inventory.Product mProduct;
    //    private Inventory.Product mProductLifeTime;
    @BindView(R.id.tvMonthly)
    var tvMonthly: AppCompatTextView? = null

    @BindView(R.id.tvYearly)
    var tvYearly: AppCompatTextView? = null

    @BindView(R.id.tvLifeTime)
    var tvLifeTime: AppCompatTextView? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_switch_basic)
        // onStartInAppPurchase();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
        }
    }

    protected override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        onRegisterHomeWatcher()
        //SuperSafeApplication.getInstance().writeKeyHomePressed(SwitchBasicActivity.class.getSimpleName());
    }

    protected override fun onDestroy() {
        super.onDestroy()
        Utils.Log(BaseActivity.Companion.TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
    }

    protected override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    } //    private class InventoryCallback implements Inventory.Callback {
    //        @Override
    //        public void onLoaded(Inventory.Products products) {
    //            final Inventory.Product product = products.get(ProductTypes.SUBSCRIPTION);
    //            if (!product.supported) {
    //                // billing is not supported, user can't purchase anything
    //                return;
    //            }
    //            mProduct = product;
    //            if (mProduct!=null){
    //                if (mProduct.getSkus().size()>0){
    //                    for (int i=0;i<mProduct.getSkus().size();i++){
    //                        Sku index = mProduct.getSkus().get(i);
    //                        if (index.id.code.equals(getString(R.string.six_months))){
    //                            tvMonthly.setText(index.price);
    //                        }
    //                        else if (index.id.code.equals(getString(R.string.one_years))){
    //                            tvYearly.setText(index.price);
    //                        }
    //                    }
    //                }
    //            }
    //            Utils.Log(TAG,"value : "+ new Gson().toJson(product));
    //        }
    //    }
    //    private class InventoryCallbackLifeTime implements Inventory.Callback {
    //        @Override
    //        public void onLoaded(Inventory.Products products) {
    //            final Inventory.Product product = products.get(ProductTypes.IN_APP);
    //            if (!product.supported) {
    //                // billing is not supported, user can't purchase anything
    //                return;
    //            }
    //            mProductLifeTime = product;
    //            if (mProductLifeTime!=null){
    //                if (mProductLifeTime.getSkus().size()>0){
    //                    for (int i=0;i<mProductLifeTime.getSkus().size();i++){
    //                        Sku index = mProductLifeTime.getSkus().get(i);
    //                        if (index.id.code.equals(getString(R.string.life_time))){
    //                            tvLifeTime.setText(index.price);
    //                        }
    //                    }
    //                }
    //            }
    //            Utils.Log(TAG,"value : "+ new Gson().toJson(product));
    //        }
    //    }
    /**
     * @return [RequestListener] that reloads inventory when the action is finished
     */
    //    private <T> RequestListener<T> makeRequestListener() {
    //        return new RequestListener<T>() {
    //            @Override
    //            public void onSuccess(@Nonnull T result) {
    //                try {
    //                }
    //                catch (Exception e){
    //                    Toast.makeText(getApplicationContext(),"Error "+e.getMessage(),Toast.LENGTH_SHORT).show();
    //                }
    //                reloadInventory();
    //                reloadInventoryLifeTime();
    //            }
    //            @Override
    //            public void onError(int response, @Nonnull Exception e) {
    //                reloadInventory();
    //                reloadInventoryLifeTime();
    //            }
    //        };
    //    }
    //    public void onStartInAppPurchase(){
    //        final Billing billing = SuperSafeApplication.getInstance().getBilling();
    //        mCheckout = Checkout.forActivity(this, billing);
    //        mCheckoutLifeTime = Checkout.forActivity(this,billing);
    //
    //        mInventoryCallback = new SwitchBasicActivity.InventoryCallback();
    //        mInventoryCallbackLifeTime  = new SwitchBasicActivity.InventoryCallbackLifeTime();
    //
    //
    //        mCheckout.start();
    //        mCheckoutLifeTime.start();
    //
    //        reloadInventory();
    //        reloadInventoryLifeTime();
    //    }
    //    private void reloadInventory() {
    //        List<String> mList = new ArrayList<>();
    //        mList.add(getString(R.string.six_months));
    //        mList.add(getString(R.string.one_years));
    //        //mList.add(getString(R.string.life_time));
    //
    //        final Inventory.Request request = Inventory.Request.create();
    //        // load purchase info
    //        request.loadAllPurchases();
    //        // load SKU details
    //        request.loadSkus(ProductTypes.SUBSCRIPTION,mList);
    //        mCheckout.loadInventory(request, mInventoryCallback);
    //    }
    //
    //    private void reloadInventoryLifeTime() {
    //        List<String> mList = new ArrayList<>();
    //        mList.add(getString(R.string.life_time));
    //        final Inventory.Request request = Inventory.Request.create();
    //        // load purchase info
    //        request.loadAllPurchases();
    //        // load SKU details
    //        request.loadSkus(ProductTypes.IN_APP,mList);
    //        mCheckoutLifeTime.loadInventory(request, mInventoryCallbackLifeTime);
    //    }
}