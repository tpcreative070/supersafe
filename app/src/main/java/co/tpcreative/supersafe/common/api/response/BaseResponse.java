package co.tpcreative.supersafe.common.api.response;
import com.anjlab.android.iab.v3.PurchaseData;
import com.google.gson.Gson;
import java.io.Serializable;
import co.tpcreative.supersafe.model.Version;

public class BaseResponse implements Serializable {
    public String message;
    public String responseMessage;
    public boolean error;
    public PurchaseData purchase;
    public Version version;
    public String toFormResponse() {
        return new Gson().toJson(this);
    }
}