package co.tpcreative.supersafe.model.room;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;
import java.util.List;
import java.util.UUID;

import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;

@Database(entities = {Items.class}, version = 2, exportSchema = false)
public abstract class InstanceGenerator extends RoomDatabase {

    @Ignore
    private static InstanceGenerator instance;

    @Ignore
    public abstract ItemsDao itemsDao();

    @Ignore
    public static final String TAG = InstanceGenerator.class.getSimpleName();

    public static InstanceGenerator getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    InstanceGenerator.class,
                    SuperSafeApplication.getInstance().getString(R.string.key_database))
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


    /*Items action*/

    public synchronized void onInsert(Items cTalkManager){
        try {
            if (cTalkManager==null){
                return;
            }
            instance.itemsDao().insert(cTalkManager);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public synchronized void onUpdate(Items cTalkManager){
        try {
            if (cTalkManager==null){
                return;
            }
            instance.itemsDao().update(cTalkManager);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public final synchronized List<Items> getListItems(final String localId,boolean isDeleteLocal){
        try{
            if (localId==null){
                return null;
            }
            return instance.itemsDao().loadAll(localId,isDeleteLocal);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<Items> getListItems(){
        try{
            return instance.itemsDao().loadAll();
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<Items> getDeleteLocalListItems(boolean isDeleteLocal){
        try{
            return instance.itemsDao().loadDeleteLocalDataItems(isDeleteLocal);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<Items> getListSyncUploadDataItems(){
        try{
            return instance.itemsDao().loadSyncDataItems(false,false,EnumStatus.UPLOAD.ordinal());
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<Items> getListSyncDownloadDataItems(){
        try{
            return instance.itemsDao().loadSyncDataItems(false,false,EnumStatus.DOWNLOAD.ordinal());
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized Items getItemId(int id){
        try{
            return instance.itemsDao().loadItemId(id);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized Items getLatestId(String id,boolean isDeleteLocal){
        try{
            return instance.itemsDao().getLatestId(id,isDeleteLocal);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized Items getItemId(String localId){
        try{
            return instance.itemsDao().loadItemId(localId);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<Items> getListItemId(String localId){
        try{
            return instance.itemsDao().loadListItemId(localId);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized Items getItemId(String localId,boolean isSync){
        try{
            return instance.itemsDao().loadItemId(localId,isSync);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }


    public final synchronized List<Items> getListItemId(boolean isSync){
        try{
            return instance.itemsDao().loadListItemId(isSync);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }


    public final synchronized Items getLocalId(String localId){
        try{
            return instance.itemsDao().loadLocalId(localId);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }


    public final synchronized boolean onDelete(Items entity){
        try{
            instance.itemsDao().delete(entity);
            return true;
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return false;
    }

    public final synchronized boolean onDeleteAll(String id){
        try{
            instance.itemsDao().deleteAll(id);
            return true;
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return false;
    }



}



