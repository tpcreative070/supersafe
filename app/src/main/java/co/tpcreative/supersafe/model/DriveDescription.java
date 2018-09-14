package co.tpcreative.supersafe.model;

import com.google.gson.Gson;

import java.io.Serializable;

import co.tpcreative.supersafe.common.util.Utils;

public class DriveDescription implements Serializable{


    public String nameMainCategories;
    public String localCategories_Id;
    public String fileExtension;
    public String thumbnailPath;
    public String originalPath;
    public String thumb;
    public String local_id;
    public String global_original_id;
    public String global_thumbnail_id;
    public String subFolderName;
    public String mimeType;
    public String thumbnailName;
    public String originalName;
    public String globalName;
    public int degrees;
    public int formatType;
    public boolean thumbnailSync;
    public boolean originalSync;
    public int fileType;


    private static DriveDescription instance;

    public static DriveDescription getInstance(){
        if (instance==null){
            instance = new DriveDescription();
        }
        return instance;
    }

    public DriveDescription getDriveDescription(final String value){
        try {
            String mValue = value;
            mValue = mValue.replace("\"\\\"", "''");
            mValue = mValue.replace("\\", "");
            mValue = mValue.replace("\"{", "{");
            mValue = mValue.replace("}\"", "}");
            Utils.Log("response special", mValue);
            DriveDescription description = new Gson().fromJson(mValue,DriveDescription.class);
            return description;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public String convertToHex(final String value){
        try {
            return Utils.stringToHex(value);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public DriveDescription hexToObject(String value){
        try {
            String result = Utils.hexToString(value);
            return new Gson().fromJson(result,DriveDescription.class);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


}
