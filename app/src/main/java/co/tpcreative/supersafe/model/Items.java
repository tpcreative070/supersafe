package co.tpcreative.supersafe.model;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import java.io.Serializable;
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
    public int formatType;
    public String thumbnailPath;
    public String originalPath;
    public String mimeType;
    public String fileExtension;
    public String description;
    public int statusAction;
    public String globalName;
    public int degrees;
    public boolean thumbnailSync;
    public boolean originalSync;
    public int fileType;

    @Ignore
    private boolean isChecked;
    @Ignore
    private boolean isDeleted;


    public Items(boolean isSync,boolean originalSync,boolean thumbnailSync,int degrees,int fileType,int formatType,String originalName,String thumbnailName,String globalName,String originalPath,String thumbnailPath,String local_id,String global_original_id,String global_thumbnail_id,String localCategories_Id ,String mimeType,String fileExtension,String driveDescription,EnumStatus enumStatus){
        this.originalName = originalName;
        this.thumbnailName = thumbnailName;
        this.globalName = globalName;
        this.fileType = fileType;
        this.formatType = formatType;
        this.isSync = isSync;
        this.thumbnailSync = thumbnailSync;
        this.originalSync = originalSync;
        this.degrees = degrees;
        this.local_id = local_id;
        this.global_original_id = global_original_id;
        this.global_thumbnail_id = global_thumbnail_id;
        this.localCategories_Id = localCategories_Id;
        this.originalPath = originalPath;
        this.thumbnailPath = thumbnailPath;
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
        this.description = driveDescription;
        this.statusAction = enumStatus.ordinal();
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

}
