package co.tpcreative.supersafe.common.api.response;

import com.google.gson.Gson;

import java.io.Serializable;

public class BaseResponse implements Serializable {
    public String message ;
    public boolean error ;
    public long nextPage;
    public String toFormResponse() {
        return new Gson().toJson(this);
    }
}
