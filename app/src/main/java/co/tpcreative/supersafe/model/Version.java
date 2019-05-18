package co.tpcreative.supersafe.model;
import java.io.Serializable;
import java.util.HashMap;

public class Version implements Serializable{
    public String title;
    public boolean release;
    public String version_name;
    public int version_code;
    public HashMap<Object,String>content;
}
