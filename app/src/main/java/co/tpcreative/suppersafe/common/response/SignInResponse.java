package co.tpcreative.suppersafe.common.response;
import java.io.Serializable;

import co.tpcreative.suppersafe.common.BaseResponse;
import co.tpcreative.suppersafe.model.User;


public class SignInResponse extends BaseResponse{
    public User user;
}
