package co.tpcreative.supersafe.common.api.request;
import com.google.gson.Gson;

public class BaseRequest {
    public String toFormRequest() {
        return new Gson().toJson(this);
    }
}
