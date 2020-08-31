package co.tpcreative.supersafe.common.request;

import java.io.Serializable;

public class OutlookMailRequest implements Serializable {
    public String refresh_token;
    public String access_token;

    public OutlookMailRequest(String refresh_token,String access_token){
        this.refresh_token = refresh_token;
        this.access_token = access_token;
    }
}
