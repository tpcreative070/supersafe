package co.tpcreative.suppersafe.common.api.response;

import com.google.gson.Gson;

import java.io.Serializable;

public class BaseResponse implements Serializable {
    public String message ;
    public boolean error ;
    public String toFormResponse() {
        return new Gson().toJson(this);
    }
}
