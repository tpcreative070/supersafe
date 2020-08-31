package co.tpcreative.supersafe.common.request;

import co.tpcreative.supersafe.common.util.Utils;

public class VerifyCodeRequest {
    public String code ;
    public String user_id;
    public String new_user_id;
    /*Supersafe _id*/
    public String _id;
    public String other_email;
    public String device_id;
    public String session_token;
    public VerifyCodeRequest(){
        this.session_token = Utils.getAccessToken();
    }
}
