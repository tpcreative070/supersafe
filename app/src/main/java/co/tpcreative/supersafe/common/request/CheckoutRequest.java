package co.tpcreative.supersafe.common.request;
import java.io.Serializable;
import co.tpcreative.supersafe.BuildConfig;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;

public class CheckoutRequest implements Serializable {

    public String user_id;
    public String device_id;
    public boolean autoRenewing;
    public String orderId;
    public String packageName;
    public String sku;
    public String state;
    public String token;
    public String device_type;
    public String manufacturer;
    public String name_model;
    public String version;
    public String versionRelease;
    public String appVersionRelease;

    public CheckoutRequest(String user_id,boolean autoRenewing,String orderId,String sku,String state,String token){
        this.user_id = user_id;
        this.autoRenewing = autoRenewing;
        this.sku = sku;
        this.orderId = orderId;
        this.device_id = SuperSafeApplication.getInstance().getDeviceId();
        this.device_type = SuperSafeApplication.getInstance().getString(R.string.device_type);
        this.manufacturer =  SuperSafeApplication.getInstance().getManufacturer();
        this.name_model = SuperSafeApplication.getInstance().getModel();
        this.version = ""+SuperSafeApplication.getInstance().getVersion();
        this.versionRelease = SuperSafeApplication.getInstance().getVersionRelease();
        this.appVersionRelease =  BuildConfig.VERSION_NAME;
        this.packageName = SuperSafeApplication.getInstance().getPackageId();
        this.state = state;
        this.token = token;
    }
}
