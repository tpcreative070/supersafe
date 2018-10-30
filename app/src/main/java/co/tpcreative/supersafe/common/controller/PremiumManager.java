package co.tpcreative.supersafe.common.controller;

import com.google.gson.Gson;

import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.Sku;

import java.util.ArrayList;
import java.util.List;

import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;

public class PremiumManager {

    private static PremiumManager instance;
    private Checkout mCheckout;
    private Checkout mCheckoutLifeTime;
    private InventoryCallback mInventoryCallback;
    private InventoryCallbackLifeTime mInventoryCallbackLifeTime;
    private Inventory.Product mProduct;
    private Inventory.Product mProductLifeTime;
    private Sku mLifeTime;
    private String TAG = PremiumManager.class.getSimpleName();

    public static PremiumManager getInstance() {
        if (instance == null) {
            instance = new PremiumManager();
        }
        return instance;
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
            if (mProduct != null) {
                if (mProduct.getSkus().size() > 0) {
                    for (int i = 0; i < mProduct.getSkus().size(); i++) {
                        Sku index = mProduct.getSkus().get(i);
                        if (index.id.code.equals(SuperSafeApplication.getInstance().getString(R.string.six_months))) {
                            final Purchase purchaseExpire = mProduct.getPurchaseInState(index, Purchase.State.EXPIRED);
                            final User user = User.getInstance().getUserInfo();
                            if (purchaseExpire != null) {
                                if (user != null) {
                                    if (user.checkout != null) {
                                        user.checkout.isPurchasedSixMonths = false;
                                    }
                                    PrefsController.putString(getString(R.string.key_user), new Gson().toJson(user));
                                }
                                Utils.onWriteLog(" response expired months :" + new Gson().toJson(purchaseExpire), EnumStatus.CHECKOUT);
                            } else {
                                final Purchase purchase = mProduct.getPurchaseInState(index, Purchase.State.PURCHASED);
                                if (purchase == null) {
                                    if (user != null) {
                                        if (user.checkout != null) {
                                            user.checkout.isPurchasedSixMonths = false;
                                        }
                                        PrefsController.putString(getString(R.string.key_user), new Gson().toJson(user));
                                    }
                                } else {
                                    if (user != null) {
                                        if (user.checkout != null) {
                                            user.checkout.isPurchasedSixMonths = true;
                                        }
                                        PrefsController.putString(getString(R.string.key_user), new Gson().toJson(user));
                                    }
                                }
                                Utils.onWriteLog(" response purchased momths :" + new Gson().toJson(purchase), EnumStatus.CHECKOUT);
                            }
                        } else if (index.id.code.equals(SuperSafeApplication.getInstance().getString(R.string.one_years))) {
                            final Purchase purchaseExpire = mProduct.getPurchaseInState(index, Purchase.State.EXPIRED);
                            final User user = User.getInstance().getUserInfo();
                            if (purchaseExpire != null) {
                                if (user != null) {
                                    if (user.checkout != null) {
                                        user.checkout.isPurchasedOneYears = false;
                                    }
                                    PrefsController.putString(getString(R.string.key_user), new Gson().toJson(user));
                                }
                                Utils.onWriteLog(" response expired years :" + new Gson().toJson(purchaseExpire), EnumStatus.CHECKOUT);
                            } else {
                                final Purchase purchase = mProduct.getPurchaseInState(index, Purchase.State.PURCHASED);
                                if (purchase == null) {
                                    if (user != null) {
                                        if (user.checkout != null) {
                                            user.checkout.isPurchasedOneYears = false;
                                        }
                                        PrefsController.putString(getString(R.string.key_user), new Gson().toJson(user));
                                    }
                                } else {
                                    if (user != null) {
                                        if (user.checkout != null) {
                                            user.checkout.isPurchasedOneYears = true;
                                        }
                                        PrefsController.putString(getString(R.string.key_user), new Gson().toJson(user));
                                    }
                                }
                                Utils.onWriteLog(" response purchased year :" + new Gson().toJson(purchase), EnumStatus.CHECKOUT);
                            }
                        }
                    }
                }
            }
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
            if (mProductLifeTime != null) {
                if (mProductLifeTime.getSkus().size() > 0) {
                    for (int i = 0; i < mProductLifeTime.getSkus().size(); i++) {
                        Sku index = mProductLifeTime.getSkus().get(i);
                        if (index.id.code.equals(SuperSafeApplication.getInstance().getString(R.string.life_time))) {
                            mLifeTime = index;
                            if (mProductLifeTime.isPurchased(mLifeTime)) {
                                final User user = User.getInstance().getUserInfo();
                                if (user != null) {
                                    if (user.checkout != null) {
                                        user.checkout.isPurchasedLifeTime = true;
                                        PrefsController.putString(getString(R.string.key_user), new Gson().toJson(user));
                                    }
                                }
                            }
                            Utils.onWriteLog("response Life time :" + new Gson().toJson(index), EnumStatus.CHECKOUT);
                        }
                    }
                }
            }
        }
    }


    public void onStartInAppPurchase() {
        final Billing billing = SuperSafeApplication.getInstance().getBilling();
        mCheckout = Checkout.forApplication(billing);
        mCheckoutLifeTime = Checkout.forApplication(billing);

        mInventoryCallback = new InventoryCallback();
        mInventoryCallbackLifeTime = new InventoryCallbackLifeTime();


        mCheckout.start();
        mCheckoutLifeTime.start();

        reloadInventory();
        reloadInventoryLifeTime();
    }

    private void reloadInventory() {
        List<String> mList = new ArrayList<>();
        mList.add(getString(R.string.six_months));
        mList.add(getString(R.string.one_years));
        final Inventory.Request request = Inventory.Request.create();
        request.loadAllPurchases();
        request.loadSkus(ProductTypes.SUBSCRIPTION, mList);
        mCheckout.loadInventory(request, mInventoryCallback);
    }

    private void reloadInventoryLifeTime() {
        List<String> mList = new ArrayList<>();
        mList.add(getString(R.string.life_time));
        final Inventory.Request request = Inventory.Request.create();
        request.loadAllPurchases();
        request.loadSkus(ProductTypes.IN_APP, mList);
        mCheckoutLifeTime.loadInventory(request, mInventoryCallbackLifeTime);
    }

    public String getString(int value) {
        return SuperSafeApplication.getInstance().getString(value);
    }

    public void onStop() {
        if (mCheckout != null) {
            mCheckout.stop();
        }
        if (mCheckoutLifeTime != null) {
            mCheckoutLifeTime.stop();
        }
    }


}
