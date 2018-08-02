package co.tpcreative.suppersafe.model.room;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;
import java.util.List;
import java.util.UUID;
import co.tpcreative.suppersafe.model.History;
import co.tpcreative.suppersafe.model.Save;


@Database(entities = {History.class, Save.class}, version = 1, exportSchema = false)
public abstract class InstanceGenerator extends RoomDatabase {

    @Ignore
    private static InstanceGenerator instance;

    @Ignore
    public abstract HistoryDao historyDao();
    @Ignore
    public abstract SaveDao saveDao();


    @Ignore
    public static final String TAG = InstanceGenerator.class.getSimpleName();

    public static InstanceGenerator getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    InstanceGenerator.class,
                    "db-keep-safety.db")
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    public String getUUId(){
        try {
            return UUID.randomUUID().toString();
        }
        catch (Exception e){
            return ""+System.currentTimeMillis();
        }
    }

    public synchronized void onInsert(History cTalkManager){
        try {
            if (cTalkManager==null){
                return;
            }
            instance.historyDao().insert(cTalkManager);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public final synchronized List<History> getList(){
        try{
            return instance.historyDao().loadAll();
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized boolean onDelete(History entity){
        try{
            instance.historyDao().delete(entity);
            return true;
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return false;
    }


    public synchronized void onInsert(Save cTalkManager){
        try {
            if (cTalkManager==null){
                return;
            }
            instance.saveDao().insert(cTalkManager);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public synchronized void onUpdate(Save cTalkManager){
        try {
            if (cTalkManager==null){
                return;
            }
            instance.saveDao().update(cTalkManager);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public final synchronized List<Save> getListSave(){
        try{
            return instance.saveDao().loadAll();
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized boolean onDelete(Save entity){
        try{
            instance.saveDao().delete(entity);
            return true;
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return false;
    }

}



