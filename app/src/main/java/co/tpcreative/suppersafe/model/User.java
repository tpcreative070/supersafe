package co.tpcreative.suppersafe.model;
import com.google.gson.Gson;

import java.io.Serializable;

import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.api.response.BaseResponse;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;

public class User extends BaseResponse implements Serializable{

    public String email;
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
    public boolean isInitMainCategoriesProgressing;

    private static User instance ;

    public static User getInstance(){
        if (instance==null){
            instance = new User();
        }
        return instance;
    }

    public User getUserInfo(){
        try{
            String value = PrefsController.getString(SupperSafeApplication.getInstance().getString(R.string.key_user),null);
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



}
