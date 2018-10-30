package co.tpcreative.supersafe.common.api.response;

import com.google.gson.Gson;

import org.solovyev.android.checkout.Purchase;

import java.io.Serializable;

import co.tpcreative.supersafe.model.Premium;
import co.tpcreative.supersafe.model.SyncData;

public class BaseResponse implements Serializable {
    public String message ;
    public boolean error ;
    public String nextPage;
    public Premium premium;
    public SyncData syncData;
    public Purchase purchase;
    public String toFormResponse() {
        return new Gson().toJson(this);
    }
}
