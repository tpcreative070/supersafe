package co.tpcreative.supersafe.model;
import com.google.gson.Gson;
import java.io.Serializable;
import co.tpcreative.supersafe.common.util.Utils;

public class DriveEvent implements Serializable {

    private static DriveEvent instance;
    public String items_id;
    public int fileType;
    private static final String TAG = DriveEvent.class.getSimpleName();


    public static DriveEvent getInstance(){
        if (instance==null){
            instance = new DriveEvent();
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

    public DriveEvent hexToObject(String value){
        try {
            String result = Utils.hexToString(value);
            return new Gson().fromJson(result,DriveEvent.class);
        }
        catch (Exception e){
            Utils.Log(TAG,"Error :" + e.getMessage());
            Utils.onWriteLog(TAG +"-" + e.getMessage(),EnumStatus.WRITE_FILE);
            e.printStackTrace();
        }
        return null;
    }




}
