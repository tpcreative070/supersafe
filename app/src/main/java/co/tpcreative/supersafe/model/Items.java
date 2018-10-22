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
import co.tpcreative.supersafe.common.util.Utils;


@Entity(tableName = "items")
public class Items implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String originalName;
    public String thumbnailName;
    public boolean isSyncCloud;
    public boolean isSyncOwnServer;
    public String global_original_id;
    public String global_thumbnail_id;
    public String local_id;
    public String categories_local_id;
    public String categories_id;
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
    public int deleteAction;
    public boolean isFakePin;

    @Ignore
    public boolean isChecked;
    @Ignore
    public boolean isDeleted;
    @Ignore
    public boolean isOriginalGlobalId;

    private static Items instance;

    @Ignore
    private static final String TAG = Items.class.getSimpleName();


    @Ignore
    public static Items getInstance(){
        if (instance==null){
            instance = new Items();
        }
        return instance;
    }


    public Items(boolean isSyncCloud,boolean isSyncOwnServer, boolean originalSync, boolean thumbnailSync, int degrees, int fileType, int formatType, String title, String originalName, String thumbnailName, String items_id, String originalPath, String thumbnailPath, String local_id, String global_original_id, String global_thumbnail_id, String categories_id, String categories_local_id, String mimeType, String fileExtension, String driveDescription, EnumStatus enumStatus, String size, int statusProgress, boolean isDeleteLocal, boolean isDeleteGlobal, int deleteAction,boolean isFakePin){
        this.originalName = originalName;
        this.thumbnailName = thumbnailName;
        this.items_id = items_id;
        this.fileType = fileType;
        this.formatType = formatType;
        this.title = title;
        this.isSyncCloud = isSyncCloud;
        this.isSyncOwnServer = isSyncOwnServer;
        this.thumbnailSync = thumbnailSync;
        this.originalSync = originalSync;
        this.degrees = degrees;
        this.local_id = local_id;
        this.global_original_id = global_original_id;
        this.global_thumbnail_id = global_thumbnail_id;
        this.categories_id = categories_id;
        this.categories_local_id = categories_local_id;
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
        this.deleteAction = deleteAction;
        this.isFakePin = isFakePin;
    }

    public Items(){
        this.thumbnailName = "";
        this.isSyncCloud = false;
        this.isSyncOwnServer = false;
        this.isOriginalGlobalId = false;
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

    @Ignore
    public Items getObject(String value){
        try {
            if (value==null){
                return null;
            }
            final Items items = new Gson().fromJson(value,Items.class);
            Utils.Log(TAG,new Gson().toJson(items));
            return items;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
