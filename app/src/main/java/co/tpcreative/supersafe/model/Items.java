package co.tpcreative.supersafe.model;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

@Entity(tableName = "items")
public class Items implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String originalName;
    public String thumbnailName;
    public boolean isSync;
    public String global_original_id;
    public String global_thumbnail_id;
    public String local_id;
    public String localCategories_Id;
    public String localCategories_Name;
    public String localCategories_Count;
    public int formatType;
    public String thumbnailPath;
    public String originalPath;
    public String mimeType;
    public String fileExtension;
    public String description;
    public int statusAction;
    public String items_id;
    public int degrees;
    public boolean thumbnailSync;
    public boolean originalSync;
    public int fileType;
    public String title;
    public String size;
    public int statusProgress;
    public boolean isDeleteLocal;
    public boolean isDeleteGlobal;


    @Ignore
    private boolean isChecked;
    @Ignore
    private boolean isDeleted;
    @Ignore
    private static Items instance;

    @Ignore
    public static Items getInstance(){
        if (instance==null){
            instance = new Items();
        }
        return instance;
    }

    public Items(boolean isSync,boolean originalSync,boolean thumbnailSync,int degrees,int fileType,int formatType,String title,String originalName,String thumbnailName,String items_id,String originalPath,String thumbnailPath,String local_id,String global_original_id,String global_thumbnail_id,String localCategories_Id ,String localCategories_Name,String localCategories_Count,String mimeType,String fileExtension,String driveDescription,EnumStatus enumStatus,String size,int statusProgress,boolean isDeleteLocal,boolean isDeleteGlobal){
        this.originalName = originalName;
        this.thumbnailName = thumbnailName;
        this.items_id = items_id;
        this.fileType = fileType;
        this.formatType = formatType;
        this.title = title;
        this.isSync = isSync;
        this.thumbnailSync = thumbnailSync;
        this.originalSync = originalSync;
        this.degrees = degrees;
        this.local_id = local_id;
        this.global_original_id = global_original_id;
        this.global_thumbnail_id = global_thumbnail_id;
        this.localCategories_Id = localCategories_Id;
        this.localCategories_Name = localCategories_Name;
        this.localCategories_Count = localCategories_Count;
        this.originalPath = originalPath;
        this.thumbnailPath = thumbnailPath;
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
        this.description = driveDescription;
        this.statusAction = enumStatus.ordinal();
        this.size = size;
        this.statusProgress = statusProgress;
        this.isDeleteLocal = isDeleteLocal ;
        this.isDeleteGlobal = isDeleteGlobal;
    }

    public Items(){
        this.thumbnailName = "";
        this.isSync = false;
    }

    @Ignore
    public boolean isChecked() {
        return isChecked;
    }

    @Ignore
    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Ignore
    public boolean isDeleted() {
        return isDeleted;
    }

    @Ignore
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    @Ignore
    public int getId() {
        return id;
    }

    @Ignore
    public void setId(int id) {
        this.id = id;
    }

    @Ignore
    public String getUUId(){
        try {
            return UUID.randomUUID().toString();
        }
        catch (Exception e){
            return ""+System.currentTimeMillis();
        }
    }

    @Ignore
    public Map<String,Object> objectToHashMap(final Items items){
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> myMap = new Gson().fromJson(new Gson().toJson(items), type);
        return myMap;
    }

}
