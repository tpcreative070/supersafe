package co.tpcreative.supersafe.common.request;
import java.io.Serializable;

import co.tpcreative.supersafe.BuildConfig;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;

public class TrackingRequest implements Serializable {
    public String user_id;
    public String device_id;
    public String device_type;
    public String manufacturer;
    public String name_model;
    public String version;
    public String versionRelease;
    public String appVersionRelease;
    public String app_id;
    public String channel_code;

    public TrackingRequest(String user_id,String device_id){
        this.user_id = user_id;
        this.device_id = device_id;
        this.device_type = SuperSafeApplication.getInstance().getString(R.string.device_type);
        this.manufacturer =  SuperSafeApplication.getInstance().getManufacturer();
        this.name_model = SuperSafeApplication.getInstance().getModel();
        this.version = ""+SuperSafeApplication.getInstance().getVersion();
        this.versionRelease = SuperSafeApplication.getInstance().getVersionRelease();
        this.appVersionRelease =  BuildConfig.VERSION_NAME;
        this.app_id = SuperSafeApplication.getInstance().getPackageId();
        this.channel_code = "C002";
    }
}
