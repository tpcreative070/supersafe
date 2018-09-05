package co.tpcreative.suppersafe.model;
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
    public String globalCategories_Id;
    public int fileType;
    public String thumbnailPath;
    public String originalPath;
    public String mimeType;


    @Ignore
    private boolean isChecked;
    @Ignore
    private boolean isDeleted;

    public Items(boolean isSync,int fileType,String name,String thumbnailPath,String originalPath,String local_id,String global_id,String localCategories_Id ,String globalCategories_Id,String mimeType){
        this.name = name;
        this.fileType = fileType;
        this.isSync = isSync;
        this.local_id = local_id;
        this.global_id = global_id;
        this.localCategories_Id = localCategories_Id;
        this.globalCategories_Id = globalCategories_Id;
        this.thumbnailPath = thumbnailPath;
        this.originalPath = originalPath;
        this.mimeType = mimeType;
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
