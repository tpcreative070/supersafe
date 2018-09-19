package co.tpcreative.supersafe.model;
import com.google.gson.Gson;
import java.io.Serializable;
import co.tpcreative.supersafe.common.util.Utils;

public class DriveTitle implements Serializable {

    private static DriveTitle instance;
    public String globalName;
    public int fileType;
    private static final String TAG = DriveTitle.class.getSimpleName();


    public static DriveTitle getInstance(){
        if (instance==null){
            instance = new DriveTitle();
        }
        return instance;
    }

    public String convertToHex(final String value){
        try {
            return Utils.stringToHex(value);
        }
        catch (Exception e){
            Utils.Log(TAG,"Error :" + e.getMessage());
            Utils.onWriteLog(TAG +"-" + e.getMessage(),EnumStatus.WRITE_FILE);
            e.printStackTrace();
        }
        return null;
    }

    public DriveTitle hexToObject(String value){
        try {
            String result = Utils.hexToString(value);
            return new Gson().fromJson(result,DriveTitle.class);
        }
        catch (Exception e){
            Utils.Log(TAG,"Error :" + e.getMessage());
            Utils.onWriteLog(TAG +"-" + e.getMessage(),EnumStatus.WRITE_FILE);
            e.printStackTrace();
        }
        return null;
    }




}
