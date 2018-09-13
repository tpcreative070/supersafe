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
    public String global_id;
    public String subFolderName;
    public String mimeType;
    public String name;
    public String globalName;
    public int degrees;
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


}
