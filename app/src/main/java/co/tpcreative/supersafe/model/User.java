package co.tpcreative.supersafe.model;
import java.io.Serializable;
import co.tpcreative.supersafe.common.api.response.BaseResponse;

public class User extends BaseResponse implements Serializable{
    public String email;
    public String _id;
    public String other_email;
    public long current_milliseconds;
    public String name;
    public int role;
    public boolean change;
    public boolean active;
    public String code ;
    public String cloud_id;
    public Authorization author;
    public boolean verified ;
    public String access_token;
    public boolean driveConnected;
    public DriveAbout driveAbout;
    public boolean isRefresh;
    public boolean isDownLoad;
    public boolean isUpload;
    public boolean isRequestSync;
    public boolean isUpdateView;
    public boolean isWaitingSendMail;
    public Premium premium;
    public EmailToken email_token;
    public SyncData syncData;
    private static User instance ;
}
