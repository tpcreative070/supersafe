package co.tpcreative.suppersafe.common;
import com.google.gson.Gson;

import java.io.Serializable;

import co.tpcreative.suppersafe.model.User;

public class BaseResponse implements Serializable {

    public String message;
    public boolean error;


    public BaseResponse() {
    }

    public String toFormResponse() {
        return (new Gson()).toJson(this);
    }

}
