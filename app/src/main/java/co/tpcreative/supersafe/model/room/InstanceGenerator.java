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
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.BreakInAlerts;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;

@Database(entities = {Items.class, MainCategories.class, BreakInAlerts.class}, version = 3, exportSchema = false)
public abstract class InstanceGenerator extends RoomDatabase {

    @Ignore
    private static InstanceGenerator instance;

    @Ignore
    public abstract ItemsDao itemsDao();

    @Ignore
    public abstract MainCategoriesDao mainCategoriesDao();

    @Ignore
    public abstract BreakInAlertsDao breakInAlertsDao();

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
                Utils.Log(TAG,"Null???? ");
                return;
            }
            instance.itemsDao().update(cTalkManager);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }


    public final synchronized List<Items> getListItems(final String categories_local_id,boolean isDeleteLocal){
        try{
            if (categories_local_id==null){
                return null;
            }
            return instance.itemsDao().loadAll(categories_local_id,isDeleteLocal);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }


    public final synchronized List<Items> getListItems(final String categories_local_id){
        try{
            if (categories_local_id==null){
                return null;
            }
            return instance.itemsDao().loadAll(categories_local_id);
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

    public final synchronized List<Items> getDeleteLocalListItems(boolean isDeleteLocal,int deleteAction){
        try{
            return instance.itemsDao().loadDeleteLocalDataItems(isDeleteLocal,deleteAction);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<Items> getDeleteLocalAndGlobalListItems(boolean isDeleteLocal,boolean isDeleteGlobal){
        try{
            return instance.itemsDao().loadDeleteLocalAndGlobalDataItems(isDeleteLocal,isDeleteGlobal);
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

    public final synchronized Items getLatestId(String categories_local_id,boolean isDeleteLocal){
        try{
            return instance.itemsDao().getLatestId(categories_local_id,isDeleteLocal);
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

    public final synchronized Items getItemId(String localId,boolean isSyncCloud){
        try{
            return instance.itemsDao().loadItemId(localId,isSyncCloud);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }


    public final synchronized List<Items> getListItemId(boolean isSyncCloud){
        try{
            return instance.itemsDao().loadListItemId(isSyncCloud);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<Items> getListItemId(boolean isSyncCloud,boolean isSyncOwnServer){
        try{
            return instance.itemsDao().loadListItemId(isSyncCloud,isSyncOwnServer);
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

    public final synchronized boolean onDelete(MainCategories entity){
        try{
            instance.mainCategoriesDao().delete(entity);
            return true;
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return false;
    }

    public final synchronized boolean onDeleteAll(String categories_local_id){
        try{
            instance.itemsDao().deleteAll(categories_local_id);
            return true;
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return false;
    }




    /*Main categories*/



    public synchronized void onInsert(MainCategories item){
        try {
            if (item==null){
                return;
            }
            instance.mainCategoriesDao().insert(item);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public synchronized void onUpdate(MainCategories cTalkManager){
        try {
            if (cTalkManager==null){
                Utils.Log(TAG,"Null???? ");
            }
            Utils.Log(TAG,"Updated :"+cTalkManager.categories_id);
            instance.mainCategoriesDao().update(cTalkManager);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public final synchronized List<MainCategories> getListCategories(){
        try{
            return instance.mainCategoriesDao().loadAll();
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<MainCategories> getListCategories(boolean isDelete){
        try{
            return instance.mainCategoriesDao().loadAll(isDelete);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }


    public final synchronized MainCategories getCategoriesItemId(String categories_hex_name){
        try{
            return instance.mainCategoriesDao().loadItemId(categories_hex_name);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<MainCategories> loadListAllItemId(String categories_hex_name){
        try{
            return instance.mainCategoriesDao().loadListAllItemId(categories_hex_name);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized MainCategories getCategoriesLocalId(String categories_local_id){
        try{
            return instance.mainCategoriesDao().loadItemLocalId(categories_local_id);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized MainCategories getCategoriesId(String categories_id){
        try{
            return instance.mainCategoriesDao().loadItemCategoriesId(categories_id);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized MainCategories loadItemCategoriesSync(boolean isSyncOwnServer){
        try{
            return instance.mainCategoriesDao().loadItemCategoriesSync(isSyncOwnServer);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<MainCategories> loadListItemCategoriesSync(boolean isSyncOwnServer){
        try{
            return instance.mainCategoriesDao().loadListItemCategoriesSync(isSyncOwnServer);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<MainCategories> loadListItemCategoriesSync(boolean isSyncOwnServer,int limit){
        try{
            return instance.mainCategoriesDao().loadListItemCategoriesSync(isSyncOwnServer,limit);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }



    /*Break In Alerts*/

    /*Items action*/

    public synchronized void onInsert(BreakInAlerts cTalkManager){
        try {
            if (cTalkManager==null){
                return;
            }
            instance.breakInAlertsDao().insert(cTalkManager);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public synchronized void onUpdate(BreakInAlerts cTalkManager){
        try {
            if (cTalkManager==null){
                Utils.Log(TAG,"Null???? ");
                return;
            }
            instance.breakInAlertsDao().update(cTalkManager);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public synchronized void onDelete(BreakInAlerts cTalkManager){
        try {
            if (cTalkManager==null){
                Utils.Log(TAG,"Null???? ");
                return;
            }
            instance.breakInAlertsDao().delete(cTalkManager);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public final synchronized List<BreakInAlerts> getList(){
        try{
            return instance.breakInAlertsDao().loadAll();
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

}



