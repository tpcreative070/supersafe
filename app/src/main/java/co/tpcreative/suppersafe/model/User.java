package co.tpcreative.suppersafe.model;
import java.io.Serializable;

import co.tpcreative.suppersafe.common.BaseResponse;

public class User extends BaseResponse implements Serializable{
    public String email;
    public String token;
    public User(){
        this.email = "";
    }
}
