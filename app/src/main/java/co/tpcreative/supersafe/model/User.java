package co.tpcreative.supersafe.model;
import android.view.View;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Comparator;

import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.api.response.BaseResponse;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;

public class User extends BaseResponse implements Serializable{

    public String email;
    public String _id;
    public String other_email;
    public long current_milliseconds;
    public String name;
    public int role;
    public boolean change;
    public boolean active;
    public String code ;
    public String cloud_id;
    public Authorization author;
    public boolean verified ;
    public String access_token;
    public boolean driveConnected;
    public DriveAbout driveAbout;
    public boolean isRefresh;
    public boolean isDownLoad;
    public boolean isUpload;
    public boolean isRequestSync;
    public boolean isUpdateView;
    public CheckoutItems checkout;
    private static User instance ;

    public static User getInstance(){
        if (instance==null){
            instance = new User();
        }
        return instance;
    }

    public User getUserInfo(){
        try{
            String value = PrefsController.getString(SuperSafeApplication.getInstance().getString(R.string.key_user),null);
            if (value!=null){
                final User mUser = new Gson().fromJson(value,User.class);
                if (mUser!=null){
                    return mUser;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean isPremium(){
        final User mUser = getUserInfo();
        if (mUser!=null){
            final CheckoutItems mCheckout = mUser.checkout;
            if (mCheckout!=null){
                if (mCheckout.isPurchasedLifeTime || mCheckout.isPurchasedOneYears || mCheckout.isPurchasedSixMonths){
                   return true;
                }
            }
        }
        return  false;
    }

    public boolean isPremiumComplimentary(){
        final User mUser = getUserInfo();
        if (mUser!=null){
           if (mUser.premium!=null){
               if (mUser.premium.status){
                   return true;
               }
           }
        }
        return  false;
    }

    public boolean isPremiumExpired(){
        if (!isPremium()){
            if (!isPremiumComplimentary()){
                return true;
            }
        }
        return false;
    }


    public boolean isCheckAllowUpload(){
        final boolean expired = isPremiumExpired();
        if (expired){
            final SyncData syncData = User.getInstance().getUserInfo().syncData;
            if (syncData!=null){
                if (syncData.left==0){
                    return false;
                }
            }
        }
        return true;
    }

}
