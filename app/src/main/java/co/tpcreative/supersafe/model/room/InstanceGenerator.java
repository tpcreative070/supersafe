package co.tpcreative.supersafe.model.room;
import android.content.Context;
import androidx.room.Database;
import androidx.room.Ignore;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.List;
import java.util.UUID;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.BreakInAlerts;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;

@Database(entities = {Items.class, MainCategories.class, BreakInAlerts.class}, version = 5, exportSchema = false)
public abstract class InstanceGenerator extends RoomDatabase {


    @Ignore
    private static InstanceGenerator instance;

    @Ignore
    public abstract ItemsDao itemsDao();

    @Ignore
    public abstract MainCategoriesDao mainCategoriesDao();

    @Ignore
    public abstract BreakInAlertsDao breakInAlertsDao();

//    @Ignore
//    public abstract SecretDao secretDao();

    @Ignore
    public static final String TAG = InstanceGenerator.class.getSimpleName();

    static final Migration MIGRATION_4_5 = new Migration(4,5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE 'items' ADD COLUMN  'isUpdate' INTEGER NOT NULL DEFAULT 0");
            //database.execSQL("ALTER TABLE 'items' ADD COLUMN  'createDatetime' TEXT NOT NULL DEFAULT '"+Utils.getCurrentDate()+"'");
            //database.execSQL("ALTER TABLE 'items' ADD COLUMN  'updatedDateTime' TEXT NOT NULL DEFAULT '"+Utils.getCurrentDateTimeSort()+"'");
            //database.execSQL("CREATE TABLE 'secret' ('id' INTEGER, "+" 'content' TEXT NOT NULL,"+" 'action_name' INTEGER NOT NULL , PRIMARY KEY('id'))");
            //database.execSQL("CREATE TABLE IF NOT EXISTS 'secret' ('id' INTEGER, "+" 'content' TEXT,"+" 'action_name' INTEGER , PRIMARY KEY('id'))");
            //database.execSQL("CREATE TABLE IF NOT EXISTS 'secret' ('id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'action_name' INTEGER NOT NULL, 'content' TEXT NOT NULL )");
        }
    };

//    static final Migration MIGRATION_5_6 = new Migration(5,6) {
//        @Override
//        public void migrate(SupportSQLiteDatabase database) {
//            database.execSQL("ALTER TABLE 'items' ADD COLUMN  'isUpdate' INTEGER NOT NULL DEFAULT 0");
//            database.execSQL("CREATE TABLE IF NOT EXISTS 'secret' ('id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'action_name' INTEGER NOT NULL, 'content' TEXT NOT NULL)");
//            //database.execSQL("CREATE TABLE 'secret' ('id' INTEGER, "+" 'content' TEXT,"+" 'action_name' INTEGER, PRIMARY KEY('id'))");
//            // database.execSQL("CREATE TABLE IF NOT EXISTS 'secret' ('id' INTEGER, "+" 'content' TEXT,"+" 'action_name' INTEGER , PRIMARY KEY('id'))");
//        }
//    };


    public static InstanceGenerator getInstance(Context context) {
        if (instance == null) {
//            instance = Room.databaseBuilder(context,
//                     InstanceGenerator.class,
//                     context.getString(R.string.database_name))
//                     .allowMainThreadQueries()
//                     .build();

            instance = Room.databaseBuilder(context,
                    InstanceGenerator.class,context.getString(R.string.key_database))
                    .addMigrations(MIGRATION_4_5)
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

//    public static InstanceGenerator getInstance(Context context) {
//        if (instance == null) {
//            instance = Room.databaseBuilder(context.getApplicationContext(),
//                    InstanceGenerator.class,
//                    SuperSafeApplication.getInstance().getString(R.string.key_database))
//                    .allowMainThreadQueries()
//                    .build();
//        }
//        return instance;
//    }

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
            Utils.Log(TAG,e.getMessage());
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
            Utils.Log(TAG,e.getMessage());
        }
    }

    public final synchronized List<Items> getListItems(final String categories_local_id,boolean isDeleteLocal,boolean isExport,boolean isFakePin){
        try{
            if (categories_local_id==null){
                return null;
            }
            return instance.itemsDao().loadAll(categories_local_id,isDeleteLocal,isExport,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<Items> getListItems(final String categories_local_id,boolean isDeleteLocal,boolean isFakePin){
        try{
            if (categories_local_id==null){
                return null;
            }
            return instance.itemsDao().loadAll(categories_local_id,isDeleteLocal,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }



    public final synchronized List<Items> getListItems(final String categories_local_id,int formatType,boolean isDeleteLocal,boolean isFakePin){
        try{
            if (categories_local_id==null){
                return null;
            }
            return instance.itemsDao().loadAll(categories_local_id,formatType,isDeleteLocal,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public final synchronized List<Items> getListItems(final String categories_local_id,boolean isFakePin){
        try{
            if (categories_local_id==null){
                return null;
            }
            return instance.itemsDao().loadAll(categories_local_id,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public final synchronized List<Items> getListAllItems(boolean isFakePin){
        try{
            return instance.itemsDao().loadAll(isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<Items> getListAllItemsSaved(boolean isSaved,boolean isSyncCloud){
        try{
            return instance.itemsDao().loadAllSaved(isSaved,isSyncCloud);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<Items> getListAllItemsSaved(int  formatType){
        try{
            return instance.itemsDao().loadAllSaved(formatType);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public final synchronized List<Items> getListAllItems(boolean isDeleteLocal,boolean isFakePin){
        try{
            return instance.itemsDao().loadAll(isDeleteLocal,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<Items> getDeleteLocalListItems(boolean isDeleteLocal,int deleteAction,boolean isFakePin){
        try{
            return instance.itemsDao().loadDeleteLocalDataItems(isDeleteLocal,deleteAction,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<Items> getDeleteLocalAndGlobalListItems(boolean isDeleteLocal,boolean isDeleteGlobal,boolean isFakePin){
        try{
            return instance.itemsDao().loadDeleteLocalAndGlobalDataItems(isDeleteLocal,isDeleteGlobal,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<Items> getListSyncUploadDataItems(boolean isFakePin){
        try{
            return instance.itemsDao().loadSyncDataItems(false,false,EnumStatus.UPLOAD.ordinal(),isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<Items> getListSyncUploadDataItemsByNull(boolean isFakePin){
        try{
            return instance.itemsDao().loadSyncDataItemsByCategoriesIdNull(false,false,EnumStatus.UPLOAD.ordinal(),isFakePin,"null");
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<Items> getListSyncDownloadDataItems(boolean isFakePin){
        try{
            return instance.itemsDao().loadSyncDataItems(false,false,EnumStatus.DOWNLOAD.ordinal(),isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<Items> getListSyncData(boolean isSyncCloud,boolean isFakePin){
        try{
            return instance.itemsDao().loadSyncData(isSyncCloud,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public final synchronized List<Items> getListSyncData(boolean isSyncCloud,boolean isSaver,boolean isWaitingForExporting,boolean isFakePin){
        try{
            return instance.itemsDao().loadSyncData(isSyncCloud,isSaver,isWaitingForExporting,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<Items> getListSyncData(boolean isSyncCloud,boolean isSaver,boolean isFakePin){
        try{
            return instance.itemsDao().loadSyncData(isSyncCloud,isSaver,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public final synchronized Items getItemId(int id,boolean isFakePin){
        try{
            return instance.itemsDao().loadItemId(id,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized Items getItemId(String item_id){
        try{
            return instance.itemsDao().loadItemId(item_id);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public final synchronized Items getLatestId(String categories_local_id,boolean isDeleteLocal,boolean isFakePin){
        try{
            return instance.itemsDao().getLatestId(categories_local_id,isDeleteLocal,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized Items getItemId(String item_id,boolean isFakePin){
        try{
            return instance.itemsDao().loadItemId(item_id,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<Items> getListItemId(String item_id,boolean isFakePin){
        try{
            return instance.itemsDao().loadListItemId(item_id,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized Items getItemId(String item_id,boolean isSyncCloud,boolean isFakePin){
        try{
            return instance.itemsDao().loadItemId(item_id,isSyncCloud,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public final synchronized List<Items> getListItemId(boolean isSyncCloud,boolean isFakePin){
        try{
            return instance.itemsDao().loadListItemId(isSyncCloud,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<Items> getLoadListItemUpdate(boolean isUpdate,boolean isSyncCloud,boolean isSyncOwnServer,boolean isFakePin){
        try{
            return instance.itemsDao().loadListItemUpdate(isUpdate,isSyncCloud,isSyncOwnServer,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }



    public final synchronized List<Items> getListItemId(boolean isSyncCloud,boolean isSyncOwnServer,boolean isFakePin){
        try{
            return instance.itemsDao().loadListItemId(isSyncCloud,isSyncOwnServer,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }



    public final synchronized boolean onDelete(Items entity){
        try{
            instance.itemsDao().delete(entity);
            return true;
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return false;
    }

    public final synchronized boolean onDelete(MainCategories entity){
        try{
            instance.mainCategoriesDao().delete(entity);
            return true;
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return false;
    }

    public final synchronized boolean onDeleteAll(String categories_local_id,boolean isFakePin){
        try{
            instance.itemsDao().deleteAll(categories_local_id,isFakePin);
            return true;
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
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
            Utils.Log(TAG,e.getMessage());
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
            Utils.Log(TAG,e.getMessage());
        }
    }

    public final synchronized List<MainCategories> getListCategories(boolean isFakePin){
        try{
            return instance.mainCategoriesDao().loadAll(isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<MainCategories> getListCategories(String categories_local_id ,boolean isDelete ,boolean isFakePin){
        try{
            return instance.mainCategoriesDao().loadAll(categories_local_id,isDelete,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<MainCategories> getListCategories(boolean isDelete,boolean isFakePin){
        try{
            return instance.mainCategoriesDao().loadAll(isDelete,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public final synchronized MainCategories getCategoriesItemId(String categories_hex_name,boolean isFakePin){
        try{
            return instance.mainCategoriesDao().loadItemId(categories_hex_name,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public final synchronized int getLatestItem(){
        try{
            MainCategories categories =   instance.mainCategoriesDao().loadLatestItem(1);
            int count = 0;
            if (categories!=null){
                count +=categories.id +1;
                return count;
            }
            return 0;
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return 0;
    }


    public final synchronized List<MainCategories> loadListAllItemId(String categories_hex_name,boolean isFakePin){
        try{
            return instance.mainCategoriesDao().loadListAllItemId(categories_hex_name,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized MainCategories getCategoriesLocalId(String categories_local_id,boolean isFakePin){
        try{
            return instance.mainCategoriesDao().loadItemLocalId(categories_local_id,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized MainCategories getCategoriesLocalId(String categories_local_id){
        try{
            return instance.mainCategoriesDao().loadLocalId(categories_local_id);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized MainCategories getCategoriesId(String categories_id,boolean isFakePin){
        try{
            return instance.mainCategoriesDao().loadItemCategoriesId(categories_id,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized MainCategories loadItemCategoriesSync(boolean isSyncOwnServer,boolean isFakePin){
        try{
            return instance.mainCategoriesDao().loadItemCategoriesSync(isSyncOwnServer,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<MainCategories> loadListItemCategoriesSync(boolean isSyncOwnServer,boolean isFakePin){
        try{
            return instance.mainCategoriesDao().loadListItemCategoriesSync(isSyncOwnServer,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<MainCategories> loadListItemCategoriesSync(boolean isSyncOwnServer,int limit,boolean isFakePin){
        try{
            return instance.mainCategoriesDao().loadListItemCategoriesSync(isSyncOwnServer,limit,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
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
            Utils.Log(TAG,e.getMessage());
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
            Utils.Log(TAG,e.getMessage());
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
            Utils.Log(TAG,e.getMessage());
        }
    }

    public final synchronized List<BreakInAlerts> getList(){
        try{
            return instance.breakInAlertsDao().loadAll();
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    /*Secret*/
//    public synchronized void onInsert(Secret secret){
//        try {
//            if (secret==null){
//                return;
//            }
//            instance.secretDao().insert(secret);
//        }
//        catch (Exception e){
//            Log.d(TAG,e.getMessage());
//        }
//    }
//
//    public synchronized void onUpdate(Secret secret){
//        try {
//            if (secret==null){
//                Utils.Log(TAG,"Null???? ");
//                return;
//            }
//            instance.secretDao().update(secret);
//        }
//        catch (Exception e){
//            Log.d(TAG,e.getMessage());
//        }
//    }
//
//    public synchronized void onDelete(Secret secret){
//        try {
//            if (secret==null){
//                Utils.Log(TAG,"Null???? ");
//                return;
//            }
//            instance.secretDao().delete(secret);
//        }
//        catch (Exception e){
//            Log.d(TAG,e.getMessage());
//        }
//    }
//
//    public final synchronized Secret loadSecret(int action_name){
//        try{
//            final Secret secret =  instance.secretDao().loadSecretName(action_name);
//            if (secret!=null){
//                if (secret.content!=null){
//                    return secret;
//                }
//            }
//        }
//        catch (Exception e){
//            Log.d(TAG,e.getMessage());
//        }
//        return null;
//    }

    public void onCleanDatabase(){
        instance.breakInAlertsDao().deleteBreakInAlerts();
        instance.itemsDao().deleteAllItems();
        instance.mainCategoriesDao().deleteAllCategories();
    }
}



