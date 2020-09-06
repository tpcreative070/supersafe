package co.tpcreative.supersafe.common.entities;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;
import co.tpcreative.supersafe.model.BreakInAlertsEntityModel;

@Entity(tableName = "breakinalerts")
public class BreakInAlertsEntity implements Serializable{
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String fileName;
    public long time;
    public String pin;

    @Ignore
    private static BreakInAlertsEntity instance;

    @Ignore
    public static BreakInAlertsEntity getInstance(){
        if (instance==null){
            instance = new BreakInAlertsEntity();
        }
        return instance;
    }

    @Ignore
    public BreakInAlertsEntity(){
        this.fileName = null;
        this.time = 0;
        this.pin = null;
    }

    @Ignore
    public BreakInAlertsEntity(BreakInAlertsEntityModel value){
        this.id = value.id;
        this.fileName = value.fileName;
        this.time = value.time;
        this.pin = value.pin;
    }

    public BreakInAlertsEntity(int id, String fileName,long time,String pin){
        this.id = id;
        this.fileName = fileName;
        this.time = time;
        this.pin = pin;
    }
}
