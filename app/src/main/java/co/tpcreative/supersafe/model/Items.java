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
    public String name;
    public boolean isSync;
    public String global_id;
    public String local_id;
    public String localCategories_Id;
    public int fileType;
    public String thumbnailPath;
    public String originalPath;
    public String mimeType;
    public String fileExtension;
    public String description;
    public int statusAction;
    public String globalName;
    public int degrees;

    @Ignore
    private boolean isChecked;
    @Ignore
    private boolean isDeleted;


    public Items(boolean isSync,int degrees,int fileType,String name,String globalName,String thumbnailPath,String originalPath,String local_id,String global_id,String localCategories_Id ,String mimeType,String fileExtension,String driveDescription,EnumStatus enumStatus){
        this.name = name;
        this.globalName = globalName;
        this.fileType = fileType;
        this.isSync = isSync;
        this.degrees = degrees;
        this.local_id = local_id;
        this.global_id = global_id;
        this.localCategories_Id = localCategories_Id;
        this.originalPath = originalPath;
        this.thumbnailPath = thumbnailPath;
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
        this.description = driveDescription;
        this.statusAction = enumStatus.ordinal();
    }

    public Items(){
        this.name = "";
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
