package co.tpcreative.supersafe.model;
import java.io.Serializable;

public class Authorization implements Serializable {
    public String device_id;
    public String device_type;
    public String manufacturer;
    public String name_model ;
    public String version;
    public String versionRelease;
    public String session_token;


     public String user_id;
     public boolean active;
     public String created_date;
     public String updated_date;
     public String date_time;
     public String refresh_token;
     public String public_key;
}
