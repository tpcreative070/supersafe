package co.tpcreative.supersafe.common.request;

public class UserCloudRequest {
    public String user_id;
    public String cloud_id;
    public String device_id;

    /*Check cloud id*/
    public UserCloudRequest(String user_id,String cloud_id){
        this.user_id = user_id;
        this.cloud_id =  cloud_id;
    }

    /*Add user cloud*/
    public UserCloudRequest(String user_id,String cloud_id,String device_id){
        this.user_id = user_id;
        this.cloud_id = cloud_id;
        this.device_id = device_id;
    }
}
