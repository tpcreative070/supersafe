package co.tpcreative.supersafe.common.response;

import co.tpcreative.supersafe.common.api.response.BaseResponse;
import co.tpcreative.supersafe.model.User;

public class VerifyCodeResponse extends BaseResponse{
    public String code ;
    public User user;
}
