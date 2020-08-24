package co.tpcreative.supersafe.common.request;

import java.io.Serializable;

import co.tpcreative.supersafe.common.response.RequestCodeResponse;

public class RequestCodeRequest implements Serializable {

    public String user_id;
    public String session_token;
    public String device_id;
    public RequestCodeRequest(String user_id,String session_token, String device_id){
        this.user_id = user_id;
        this.session_token = session_token;
        this.device_id = device_id;
    }
}
