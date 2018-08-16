package co.tpcreative.suppersafe.model;
import java.io.Serializable;

public class Authorization implements Serializable {
    public String device_id;
    public String device_type;
    public String manufacturer;
    public String name_model ;
    public String version;
    public String versionRelease;
    public String session_token;
}
