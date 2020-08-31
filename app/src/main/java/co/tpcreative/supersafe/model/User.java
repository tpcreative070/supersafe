package co.tpcreative.supersafe.model;
import android.view.View;

import androidx.room.Ignore;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.Comparator;

import co.tpcreative.supersafe.BuildConfig;
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
    public boolean isWaitingSendMail;
    public CheckoutItems checkout;
    public Premium premium;
    public EmailToken email_token;
    private static User instance ;

//    @Ignore
//    private final String TAG = User.class.getSimpleName();

    public static User getInstance(){
        synchronized (User.class){
            if (instance==null){
                instance = new User();
            }
            return instance;
        }
    }

    public synchronized User getUserInfo(){
        try{
            String value = PrefsController.getString(SuperSafeApplication.getInstance().getString(R.string.key_user),null);
            if (value!=null){
                Utils.Log("User","get value " + value);
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
        if (BuildConfig.DEBUG){
            return true;
        }
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

    public boolean isCheckAllowUpload(){
        final SyncData syncData = User.getInstance().getUserInfo().syncData;
        if (!isPremium()){
            if (syncData!=null){
                if (syncData.left==0){
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isAllowRequestDriveApis(){
        final User mUser = getUserInfo();
        if (mUser!=null){
            if (mUser.driveConnected){
                if (mUser.access_token !=null && !mUser.access_token.equals("")){
                    if (mUser.cloud_id!=null && !mUser.cloud_id.equals("") && !Utils.isPauseSync()){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
