package co.tpcreative.supersafe.model;
import com.google.gson.Gson;
import java.io.Serializable;
import co.tpcreative.supersafe.common.util.Utils;

public class DriveDescription implements Serializable{

    private static String TAG = DriveDescription.class.getSimpleName();
    public String categories_local_id;
    public String categories_id;
    public String fileExtension;
    public String thumbnailPath;
    public String originalPath;
    public String title;
    public String global_original_id;
    public String global_thumbnail_id;
    public String mimeType;
    public String thumbnailName;
    public String originalName;
    public String items_id;
    public int degrees;
    public int formatType;
    public boolean thumbnailSync;
    public boolean originalSync;
    public int fileType;
    public String size;
    public boolean isDeleteLocal;
    public boolean isDeleteGlobal;



    public int statusProgress;
    public int deleteAction;
    public boolean isFakePin;
    public boolean isSaver;
    public boolean isExport;
    public boolean isWaitingForExporting;
    public int custom_items;

    private static DriveDescription instance;

    public static DriveDescription getInstance(){
        if (instance==null){
            instance = new DriveDescription();
        }
        return instance;
    }

    public String convertToHex(final String value){
        try {
            return Utils.stringToHex(value);
        }
        catch (Exception e){
            Utils.Log(TAG,"Error :" + e.getMessage());
            e.printStackTrace();
            Utils.onWriteLog(TAG +"-" + e.getMessage(),EnumStatus.WRITE_FILE);
        }
        return null;
    }

    public DriveDescription hexToObject(String value){
        try {
            String result = Utils.hexToString(value);
            return new Gson().fromJson(result,DriveDescription.class);
        }
        catch (Exception e){
            Utils.Log(TAG,"Error :" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

}
