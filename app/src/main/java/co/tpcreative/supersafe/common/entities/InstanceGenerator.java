package co.tpcreative.supersafe.common.entities;
import android.content.Context;
import androidx.room.Database;
import androidx.room.Ignore;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.BreakInAlerts;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.ItemEntityModel;
import co.tpcreative.supersafe.model.ItemModel;
import co.tpcreative.supersafe.model.MainCategoryEntityModel;
import co.tpcreative.supersafe.model.MainCategoryModel;

@Database(entities = {ItemEntity.class, MainCategoryEntity.class, BreakInAlerts.class}, version = 5, exportSchema = false)
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

    public void onInsert(ItemEntity cTalkManager){
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

    public void onUpdate(ItemEntity cTalkManager){
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

    public final  List<ItemEntityModel> getListItems(final String categories_local_id, boolean isDeleteLocal, boolean isExport, boolean isFakePin){
        try{
            if (categories_local_id==null){
                return null;
            }
            try{
                final List<ItemEntity> mList =  instance.itemsDao().loadAll(categories_local_id,isDeleteLocal,isExport,isFakePin);
                final List<ItemEntityModel> mData = new ArrayList<>();
                if (mList!=null){
                    for (ItemEntity index : mList){
                        mData.add(new ItemEntityModel(index));
                    }
                    return mData;
                }
            }
            catch (Exception e){

            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final List<ItemEntityModel> getListItems(final String categories_local_id, boolean isDeleteLocal, boolean isFakePin){
        if (categories_local_id==null){
            return null;
        }
        try{
            final List<ItemEntity> mList =  instance.itemsDao().loadAll(categories_local_id,isDeleteLocal,isFakePin);
            final List<ItemEntityModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntity index : mList){
                    mData.add(new ItemEntityModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){

        }
        return null;
    }

    public final  List<ItemEntityModel> getListItems(final String categories_local_id, int formatType, boolean isDeleteLocal, boolean isFakePin){
        if (categories_local_id==null){
            return null;
        }
        try{
            final List<ItemEntity> mList =  instance.itemsDao().loadAll(categories_local_id,formatType,isDeleteLocal,isFakePin);
            final List<ItemEntityModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntity index : mList){
                    mData.add(new ItemEntityModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){

        }
        return null;
    }

    public final List<ItemEntityModel> getListItems(final String categories_local_id, boolean isFakePin){
        try{
            final List<ItemEntity> mList =  instance.itemsDao().loadAll(categories_local_id,isFakePin);
            final List<ItemEntityModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntity index : mList){
                    mData.add(new ItemEntityModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public final  List<ItemEntityModel> getListAllItems(boolean isFakePin){
        try{
            final List<ItemEntity> mList = instance.itemsDao().loadAll(isFakePin);
            final List<ItemEntityModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntity index : mList){
                    mData.add(new ItemEntityModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final  List<ItemEntityModel> getListAllItemsSaved(boolean isSaved, boolean isSyncCloud){
        try{
            final List<ItemEntity> mList = instance.itemsDao().loadAllSaved(isSaved,isSyncCloud);
            final List<ItemEntityModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntity index : mList){
                    mData.add(new ItemEntityModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<ItemEntity> getListAllItemsSaved(int  formatType){
        try{
            return instance.itemsDao().loadAllSaved(formatType);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public final  List<ItemEntityModel> getListAllItems(boolean isDeleteLocal, boolean isFakePin){
        try{
            final List<ItemEntity> mList =  instance.itemsDao().loadAll(isDeleteLocal,isFakePin);
            final List<ItemEntityModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntity index : mList){
                    mData.add(new ItemEntityModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public static final List<ItemEntityModel> getDeleteLocalListItems(boolean isDeleteLocal, int deleteAction, boolean isFakePin){
        try{
            final List<ItemEntity> mList =  instance.itemsDao().loadDeleteLocalDataItems(isDeleteLocal,deleteAction,isFakePin);
            final List<ItemEntityModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntity index : mList){
                    mData.add(new ItemEntityModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<ItemEntity> getDeleteLocalAndGlobalListItems(boolean isDeleteLocal, boolean isDeleteGlobal, boolean isFakePin){
        try{
            return instance.itemsDao().loadDeleteLocalAndGlobalDataItems(isDeleteLocal,isDeleteGlobal,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<ItemEntity> getListSyncUploadDataItems(boolean isFakePin){
        try{
            return instance.itemsDao().loadSyncDataItems(false,false,EnumStatus.UPLOAD.ordinal(),isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<ItemEntity> getListSyncUploadDataItemsByNull(boolean isFakePin){
        try{
            return instance.itemsDao().loadSyncDataItemsByCategoriesIdNull(false,false,EnumStatus.UPLOAD.ordinal(),isFakePin,"null");
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final List<ItemEntityModel> getSyncUploadDataItemsListByNull(boolean isFakePin){
        try{
            final List<ItemEntity> mList =  instance.itemsDao().loadSyncDataItemsByCategoriesIdNull(false,false,EnumStatus.UPLOAD.ordinal(),isFakePin,"null");
            final List<ItemEntityModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntity index : mList){
                    mData.add(new ItemEntityModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<ItemEntity> getListSyncDownloadDataItems(boolean isFakePin){
        try{
            return instance.itemsDao().loadSyncDataItems(false,false,EnumStatus.DOWNLOAD.ordinal(),isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final List<ItemEntityModel> getListSyncData(boolean isSyncCloud, boolean isFakePin){
        try{
            final List<ItemEntity> mList =  instance.itemsDao().loadSyncData(isSyncCloud,isFakePin);
            final List<ItemEntityModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntity index : mList){
                    mData.add(new ItemEntityModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public final synchronized List<ItemEntity> getListSyncData(boolean isSyncCloud, boolean isSaver, boolean isWaitingForExporting, boolean isFakePin){
        try{
            return instance.itemsDao().loadSyncData(isSyncCloud,isSaver,isWaitingForExporting,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final List<ItemEntityModel> getListSyncData(boolean isSyncCloud, boolean isSaver, boolean isFakePin){
        try{
            final List<ItemEntity> mList =  instance.itemsDao().loadSyncData(isSyncCloud,isSaver,isFakePin);
            final List<ItemEntityModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntity index : mList){
                    mData.add(new ItemEntityModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public final synchronized ItemEntity getItemId(int id, boolean isFakePin){
        try{
            return instance.itemsDao().loadItemId(id,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final ItemEntityModel getItemId(String item_id){
        try{
            final ItemEntity mResult =  instance.itemsDao().loadItemId(item_id);
            if (mResult != null){
                return  new ItemEntityModel(mResult);
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public final  ItemEntityModel getLatestId(String categories_local_id, boolean isDeleteLocal, boolean isFakePin){
        try{
            final ItemEntity mResult =  instance.itemsDao().getLatestId(categories_local_id,isDeleteLocal,isFakePin);
            if (mResult!=null){
                return  new ItemEntityModel(mResult);
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final ItemEntityModel getItemId(String item_id, boolean isFakePin){
        try{
            final ItemEntity mResult =  instance.itemsDao().loadItemId(item_id,isFakePin);
            if (mResult!=null){
                return new ItemEntityModel(mResult);
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<ItemEntity> getListItemId(String item_id, boolean isFakePin){
        try{
            return instance.itemsDao().loadListItemId(item_id,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized ItemEntity getItemId(String item_id, boolean isSyncCloud, boolean isFakePin){
        try{
            return instance.itemsDao().loadItemId(item_id,isSyncCloud,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public final List<ItemEntityModel> getListItemId(boolean isSyncCloud, boolean isFakePin){
        try{
            final List<ItemEntity> mList =  instance.itemsDao().loadListItemId(isSyncCloud,isFakePin);
            final List<ItemEntityModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntity index : mList){
                    mData.add(new ItemEntityModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final  List<ItemEntityModel> getItemList(boolean isSyncCloud, boolean isFakePin){
        try{
            final List<ItemEntity> mList =  instance.itemsDao().loadListItemId(isSyncCloud,isFakePin);
            final List<ItemEntityModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntity index : mList){
                    mData.add(new ItemEntityModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final List<ItemEntityModel> getLoadListItemUpdate(boolean isUpdate, boolean isSyncCloud, boolean isSyncOwnServer, boolean isFakePin){
        try{
            final List<ItemEntity> mList =  instance.itemsDao().loadListItemUpdate(isUpdate,isSyncCloud,isSyncOwnServer,isFakePin);
            final List<ItemEntityModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntity index : mList){
                    mData.add(new ItemEntityModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }



    public final synchronized List<ItemEntity> getListItemId(boolean isSyncCloud, boolean isSyncOwnServer, boolean isFakePin){
        try{
            return instance.itemsDao().loadListItemId(isSyncCloud,isSyncOwnServer,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }



    public final  boolean onDelete(ItemEntity entity){
        try{
            instance.itemsDao().delete(entity);
            return true;
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return false;
    }


    public final boolean onDelete(MainCategoryEntity entity){
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
    public void onInsert(MainCategoryEntityModel item){
        try {
            if (item==null){
                return;
            }
            instance.mainCategoriesDao().insert(new MainCategoryEntity(item));
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
    }

    public  void onUpdate(MainCategoryEntityModel cTalkManager){
        try {
            if (cTalkManager==null){
                Utils.Log(TAG,"Null???? ");
            }
            Utils.Log(TAG,"Updated :"+cTalkManager.categories_id);
            instance.mainCategoriesDao().update(new MainCategoryEntity(cTalkManager));
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
    }

    public final  List<MainCategoryEntityModel> getListCategories(boolean isFakePin){
        try{
            final List<MainCategoryEntity> mResult =  instance.mainCategoriesDao().loadAll(isFakePin);
            final List<MainCategoryEntityModel> mData = new ArrayList<>();
            if (mResult!=null){
                for (MainCategoryEntity index : mResult){
                    mData.add(new MainCategoryEntityModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final List<MainCategoryEntityModel> getListCategories(String categories_local_id , boolean isDelete , boolean isFakePin){
        try{
            final List<MainCategoryEntity> mList =  instance.mainCategoriesDao().loadAll(categories_local_id,isDelete,isFakePin);
            final List<MainCategoryEntityModel> mData = new ArrayList<>();
            if (mList!=null){
                for (MainCategoryEntity index : mList){
                    mData.add(new MainCategoryEntityModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final List<MainCategoryEntityModel> getListCategories(boolean isDelete, boolean isFakePin){
        try{
            final List<MainCategoryEntity> mList =  instance.mainCategoriesDao().loadAll(isDelete,isFakePin);
            final List<MainCategoryEntityModel> mData = new ArrayList<>();
            if (mList!=null){
                for (MainCategoryEntity index : mList){
                    mData.add(new MainCategoryEntityModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<MainCategoryEntityModel> getChangedCategoryList(){
        try{
            final List<MainCategoryEntity> mList =  instance.mainCategoriesDao().loadAllChangedItem(true,false);
            final List<MainCategoryEntityModel> mData = new ArrayList<>();
            if (mList!=null){
                for (MainCategoryEntity index : mList){
                    mData.add(new MainCategoryEntityModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final  List<MainCategoryEntityModel> geCategoryList(boolean isDelete, boolean isFakePin){
        try{
            final List<MainCategoryEntity> mList = instance.mainCategoriesDao().loadAll(isDelete,isFakePin);
            final List<MainCategoryEntityModel> mData = new ArrayList<>();
            if (mList!=null){
                for (MainCategoryEntity index : mList){
                    mData.add(new MainCategoryEntityModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public final  MainCategoryEntityModel getCategoriesItemId(String categories_hex_name, boolean isFakePin){
        try{
            final MainCategoryEntity mResult =  instance.mainCategoriesDao().loadItemId(categories_hex_name,isFakePin);
            if (mResult!=null){
                return new MainCategoryEntityModel(mResult);
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public final synchronized int getLatestItem(){
        try{
            MainCategoryEntity categories =   instance.mainCategoriesDao().loadLatestItem(1);
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


    public final synchronized List<MainCategoryEntity> loadListAllItemId(String categories_hex_name, boolean isFakePin){
        try{
            return instance.mainCategoriesDao().loadListAllItemId(categories_hex_name,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public final  MainCategoryEntityModel getCategoriesLocalId(String categories_local_id, boolean isFakePin){
        try{
            final MainCategoryEntity mResult =  instance.mainCategoriesDao().loadItemLocalId(categories_local_id,isFakePin);
            if (mResult!=null){
                new MainCategoryEntityModel(mResult);
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized MainCategoryEntity getCategoriesLocalId(String categories_local_id){
        try{
            return instance.mainCategoriesDao().loadLocalId(categories_local_id);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final  MainCategoryEntityModel getCategoriesId(String categories_id, boolean isFakePin){
        try{
            final MainCategoryEntity mResult = instance.mainCategoriesDao().loadItemCategoriesId(categories_id,isFakePin);
            if (mResult!=null){
                return  new MainCategoryEntityModel(mResult);
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized MainCategoryEntity loadItemCategoriesSync(boolean isSyncOwnServer, boolean isFakePin){
        try{
            return instance.mainCategoriesDao().loadItemCategoriesSync(isSyncOwnServer,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<MainCategoryEntity> loadListItemCategoriesSync(boolean isSyncOwnServer, boolean isFakePin){
        try{
            return instance.mainCategoriesDao().loadListItemCategoriesSync(isSyncOwnServer,isFakePin);
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<MainCategoryEntity> loadListItemCategoriesSync(boolean isSyncOwnServer, int limit, boolean isFakePin){
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

    public void onCleanDatabase(){
        instance.breakInAlertsDao().deleteBreakInAlerts();
        instance.itemsDao().deleteAllItems();
        instance.mainCategoriesDao().deleteAllCategories();
    }


    /*Improved sqlite*/
    public final List<ItemEntityModel> getAllListItems(){
        try{
            final List<ItemEntity> mValue =  instance.itemsDao().loadAll(false);
            List<ItemEntityModel> mList = new ArrayList<>();
            for ( ItemEntity index : mValue){
                mList.add(new ItemEntityModel(index));
            }
            return mList;
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final List<ItemEntityModel> getAllListItems(boolean isSyncCloud,boolean isFake){
        try{
            final List<ItemEntity> mValue =  instance.itemsDao().loadAll(false);
            List<ItemEntityModel> mList = new ArrayList<>();
            for ( ItemEntity index : mValue){
                mList.add(new ItemEntityModel(index));
            }
            return mList;
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


}



