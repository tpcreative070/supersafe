package co.tpcreative.suppersafe.model;
import java.io.Serializable;

import co.tpcreative.suppersafe.common.api.response.BaseResponse;

public class User extends BaseResponse implements Serializable{
    public String email;
    public String code ;
    public Authorization author;
    public User(){
        this.email = "";
    }
}
