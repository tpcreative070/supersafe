package co.tpcreative.supersafe.model;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Date;
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
    public String categories_local_id;
    public String categories_id;
    public int formatType;
    public String thumbnailPath;
    public String originalPath;
    public String mimeType;
    public String fileExtension;
    public int statusAction;
    public String items_id;
    public int degrees;
    public boolean thumbnailSync;
    public boolean originalSync;
    public int fileType;
    public String title;
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
    public boolean isUpdate;

    //public String createDatetime;
   // public String updatedDateTime;

    @Ignore
    public boolean isChecked;
    @Ignore
    public boolean isDeleted;
    @Ignore
    public boolean isOriginalGlobalId;
    @Ignore
    public Date date;
    @Ignore
    public String name ;
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

    public Items(Items items){
        this.originalName = items.originalName;
        this.thumbnailName = items.thumbnailName;
        this.items_id = items.items_id;
        this.fileType = items.fileType;
        this.formatType = items.formatType;
        this.title = items.title;
        this.isSyncCloud = items.isSyncCloud;
        this.isSyncOwnServer = items.isSyncOwnServer;
        this.thumbnailSync = items.thumbnailSync;
        this.originalSync = items.originalSync;
        this.degrees = items.degrees;
        this.global_original_id = items.global_original_id;
        this.global_thumbnail_id = items.global_thumbnail_id;
        this.categories_id = items.categories_id;
        this.categories_local_id = items.categories_local_id;
        this.originalPath = items.originalPath;
        this.thumbnailPath = items.thumbnailPath;
        this.mimeType = items.mimeType;
        this.fileExtension = items.fileExtension;
        this.statusAction = items.statusAction;
        this.size = items.size;
        this.statusProgress = items.statusProgress;
        this.isDeleteLocal = items.isDeleteLocal ;
        this.isDeleteGlobal = items.isDeleteGlobal;
        this.deleteAction = items.deleteAction;
        this.isFakePin = items.isFakePin;
        this.isSaver = items.isSaver;
        this.isExport = items.isExport;
        this.isWaitingForExporting = items.isWaitingForExporting;
        this.custom_items = items.custom_items;
        this.isUpdate = items.isUpdate;
    }

    public Items(boolean isSyncCloud,boolean isSyncOwnServer, boolean originalSync, boolean thumbnailSync, int degrees, int fileType, int formatType, String title, String originalName, String thumbnailName, String items_id, String originalPath, String thumbnailPath, String global_original_id, String global_thumbnail_id, String categories_id, String categories_local_id, String mimeType, String fileExtension, EnumStatus enumStatus, String size, int statusProgress, boolean isDeleteLocal, boolean isDeleteGlobal, int deleteAction,boolean isFakePin,boolean isSaver,boolean isExport,boolean isWaitingForExporting,int custom_items,boolean isUpdate){
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
        this.global_original_id = global_original_id;
        this.global_thumbnail_id = global_thumbnail_id;
        this.categories_id = categories_id;
        this.categories_local_id = categories_local_id;
        this.originalPath = originalPath;
        this.thumbnailPath = thumbnailPath;
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
        this.statusAction = enumStatus.ordinal();
        this.size = size;
        this.statusProgress = statusProgress;
        this.isDeleteLocal = isDeleteLocal ;
        this.isDeleteGlobal = isDeleteGlobal;
        this.deleteAction = deleteAction;
        this.isFakePin = isFakePin;
        this.isSaver = isSaver;
        this.isExport = isExport;
        this.isWaitingForExporting = isWaitingForExporting;
        this.custom_items = custom_items;
        this.isUpdate = isUpdate;
    }

    public Items(){
        this.thumbnailName = "";
        this.isSyncCloud = false;
        this.isSyncOwnServer = false;
        this.isOriginalGlobalId = false;
        this.isUpdate = false;
    }
    public Items(String fileExtension,
                 String originalPath,
                 String thumbnailPath,
                 String categories_id,
                 String categories_local_id,
                 String mimeType,
                 String uuId,
                 EnumFormatType formatType,
                 int degrees,
                 boolean thumbnailSync,
                 boolean originalSync,
                 String global_original_id,
                 String global_thumbnail_id,
                 EnumFileType fileType,
                 String originalName,
                 String name,
                 String thumbnailName,
                 String size,
                 EnumStatusProgress progress,
                 boolean isDeleteLocal,
                 boolean isDeleteGlobal,
                 EnumDelete deleteAction,
                 boolean isFakePin,
                 boolean isSaver,
                 boolean isExport,
                 boolean isWaitingForExporting,
                 int custom_items,
                 boolean isSyncCloud,
                 boolean isSyncOwnServer,
                 boolean isUpdate,
                 EnumStatus statusAction
                 ){
        this.fileExtension = fileExtension;
        this.originalPath = originalPath;
        this.thumbnailPath = thumbnailPath;
        this.categories_id = categories_id;
        this.categories_local_id = categories_local_id;
        this.mimeType = mimeType;
        this.items_id = uuId;
        this.formatType = formatType.ordinal();
        this.degrees = degrees;
        this.thumbnailSync = thumbnailSync;
        this.originalSync = originalSync;
        this.global_original_id = global_original_id;
        this.global_thumbnail_id = global_thumbnail_id;
        this.fileType = fileType.ordinal();
        this.originalName = originalName;
        this.title = name;
        this.thumbnailName = thumbnailName;
        this.size = size;
        this.statusProgress = progress.ordinal();
        this.isDeleteLocal = isDeleteLocal;
        this.isDeleteGlobal = isDeleteGlobal;
        this.deleteAction = deleteAction.ordinal();
        this.isFakePin = isFakePin;
        this.isSaver = isSaver;
        this.isExport = isExport;
        this.isWaitingForExporting = isWaitingForExporting;
        this.custom_items = custom_items;
        this.isSyncCloud = isSyncCloud;
        this.isSyncOwnServer = isSyncOwnServer;
        this.isUpdate = isUpdate;
        this.statusAction = statusAction.ordinal();
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
