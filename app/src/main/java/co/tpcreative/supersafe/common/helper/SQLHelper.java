package co.tpcreative.supersafe.common.helper;
import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.room.Ignore;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.entities.InstanceGenerator;
import co.tpcreative.supersafe.common.entities.ItemEntity;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumDelete;
import co.tpcreative.supersafe.model.ItemEntityModel;
import co.tpcreative.supersafe.model.ItemModel;
import co.tpcreative.supersafe.model.MainCategoryEntityModel;
import co.tpcreative.supersafe.model.MainCategoryModel;

public class SQLHelper {

    private static String TAG = SQLHelper.class.getSimpleName();

    public static List<ItemModel> getAllItemList(){
        final List<ItemEntityModel> list = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getAllListItems();
        List<ItemModel> mList = new ArrayList<>();
        for (ItemEntityModel index : list){
            mList.add(new ItemModel(index));
        }
        return mList;
    }

    /*Check request delete category*/
    public static List<MainCategoryModel> getDeleteCategoryRequest(){
        List<MainCategoryModel> mList = new ArrayList<>();
        final List<MainCategoryEntityModel> deleteAlbum = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).geCategoryList(true, false);
        if (deleteAlbum!=null){
            for (MainCategoryEntityModel index : deleteAlbum){
               mList.add(new MainCategoryModel(index));
            }
            return mList;
        }
        return null;
    }

    /*Check request delete item*/
    public static List<ItemModel> getDeleteItemRequest(){
        List<ItemModel> mList = new ArrayList<>();
        final List<ItemEntityModel> mResult = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getDeleteLocalListItems(true,EnumDelete.DELETE_WAITING.ordinal(),false);
        if (mResult!=null){
            for (ItemEntityModel index : mResult){
                mList.add(new ItemModel(index));
            }
            return mList;
        }
        return null;
    }

    /*Delete category*/
    public static void deleteCategory(MainCategoryModel itemModel){
        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(new MainCategoryEntity(new MainCategoryEntityModel(itemModel)));
    }

    /*Delete item*/
    public static void deleteItem(ItemModel itemModel){
        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(new ItemEntity(new ItemEntityModel(itemModel)));
    }

    /*Request download item*/
    public static List<ItemModel> getItemListDownload(){
        final List<ItemEntityModel> list = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getItemListDownload(true,false);
        List<ItemModel> mList = new ArrayList<>();
        for (ItemEntityModel index : list){
            mList.add(new ItemModel(index));
        }
        return mList;
    }

    /*Request upload item*/
    public static List<ItemModel> getItemListUpload(){
        List<ItemModel> mList = new ArrayList<>();
        final List<ItemEntityModel> mResult = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getRequestUploadData( false);
        if (mResult!=null){
            for (ItemEntityModel index : mResult){
                mList.add(new ItemModel(index));
            }
            return mList;
        }
        return null;
    }

    /*Added item*/
    public static void insertedItem(ItemModel itemModel){
        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(new ItemEntity(new ItemEntityModel(itemModel)));
    }

    /*Updated item*/
    public static void updatedItem(ItemModel itemModel){
        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(new ItemEntity(new ItemEntityModel(itemModel)));
    }

    /*Added get item*/
    public static ItemModel getItemById(String items_id){
        final ItemEntityModel items = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getItemId(items_id,false);
        if (items!=null){
            return new ItemModel(items);
        }
        return null;
    }

    /*Get local item list*/
    public static final List<ItemModel> getDeleteLocalListItems(boolean isDeleteLocal, int deleteAction, boolean isFakePin){
        try{
            final List<ItemEntityModel> mList =  InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getDeleteLocalListItems(isDeleteLocal,deleteAction,isFakePin);
            final List<ItemModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntityModel index : mList){
                    mData.add(new ItemModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
        }
        return null;
    }


    /*Get request update item*/
    public static List<ItemModel> getRequestUpdateItemList(){
        List<ItemModel> mList = new ArrayList<>();
        final List<ItemEntityModel> mResult =  InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getLoadListItemUpdate(true,true, true,false);
        if (mResult!=null){
            for (ItemEntityModel index : mResult){
                mList.add(new ItemModel(index));
            }
            return mList;
        }
        return null;
    }

    /*Get item list*/
    public static final List<ItemModel> getListItems(final String categories_local_id, boolean isFakePin){
        try{
            final List<ItemEntityModel> mList =  InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListItems(categories_local_id,isFakePin);
            final List<ItemModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntityModel index : mList){
                    mData.add(new ItemModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
        }
        return null;
    }

    public static final ItemModel getItemId(String item_id, boolean isFakePin){
        try{
            final ItemEntityModel mResult =  InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getItemId(item_id,isFakePin);
            if (mResult!=null){
                return new ItemModel(mResult);
            }
        }
        catch (Exception e){
        }
        return null;
    }


    /*Get request update category */
    public static List<MainCategoryModel> getRequestUpdateCategoryList(){
        List<MainCategoryModel> mList = new ArrayList<>();
        final List<MainCategoryEntityModel> deleteAlbum = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getChangedCategoryList();
        if (deleteAlbum!=null){
            for (MainCategoryEntityModel index : deleteAlbum){
                mList.add(new MainCategoryModel(index));
            }
            return mList;
        }
        return null;
    }

    /*Added category*/
    public static void insertCategory(MainCategoryModel mainCategoryModel){
        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(new MainCategoryEntityModel(mainCategoryModel));
    }

    /*Update Category*/
    public static void updateCategory(MainCategoryModel model){
        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(new MainCategoryEntityModel(model));
    }

    /*Get category item by id*/
    public static MainCategoryModel getCategoriesId(String categories_id, boolean isFakePin){
         final MainCategoryEntityModel mResult = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesId(categories_id,isFakePin);
         if (mResult!=null){
             return new MainCategoryModel(mResult);
         }
         return null;
    }


    /*Get category item by hex name*/
    public static MainCategoryModel getCategoriesItemId(String categories_hex_name, boolean isFakePin){
        final MainCategoryEntityModel mResult = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesItemId(categories_hex_name,isFakePin);
        if (mResult!=null){
            return new MainCategoryModel(mResult);
        }
        return null;
    }

    /*Get local category*/
    public static  MainCategoryModel getCategoriesLocalId(String categories_local_id, boolean isFakePin){
        final MainCategoryEntityModel mResult = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesLocalId(categories_local_id,isFakePin);
        if (mResult!=null){
            return new MainCategoryModel(mResult);
        }
        return null;
    }

    public static final List<MainCategoryModel> getListCategories(boolean isDelete, boolean isFakePin){
        try{
            final List<MainCategoryEntityModel> mList =  InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListCategories(false,false);
            final List<MainCategoryModel> mData = new ArrayList<>();
            if (mList!=null){
                for (MainCategoryEntityModel index : mList){
                    mData.add(new MainCategoryModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
        }
        return null;
    }

    public static final  List<MainCategoryModel> getListCategories(boolean isFakePin){
        try{
            final List<MainCategoryEntityModel> mResult =  InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListCategories(isFakePin);
            final List<MainCategoryModel> mData = new ArrayList<>();
            if (mResult!=null){
                for (MainCategoryEntityModel index : mResult){
                    mData.add(new MainCategoryModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
        }
        return null;
    }


    public static final  List<MainCategoryModel> requestSyncCategories(boolean isSyncOwnServer, boolean isFakePin){
        try{
            final List<MainCategoryEntityModel> mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).loadListItemCategoriesSync(isSyncOwnServer,isFakePin);
            final List<MainCategoryModel> mData = new ArrayList<>();
            if (mList!=null){
                for (MainCategoryEntityModel index : mList){
                    mData.add(new MainCategoryModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public static List<MainCategoryModel> getList(){
        List<MainCategoryModel> mList = new ArrayList<>();
        final List<MainCategoryModel> list = SQLHelper.getListCategories(false,false);

        if (list!=null && list.size()>0){
            mList.addAll(list);
            Utils.Log(TAG,"Found data :"+ list.size());
        }
        else{
            final Map<String, MainCategoryModel> map = getMainCategoriesDefault();
            Utils.Log(TAG,"No Data " + map.size());
            for (Map.Entry<String, MainCategoryModel> index : map.entrySet()){
                final MainCategoryModel main = index.getValue();
                SQLHelper.insertCategory(main);
            }
        }

        final List<ItemModel> listDelete = SQLHelper.getDeleteLocalListItems(true, EnumDelete.NONE.ordinal(),false);
        if (listDelete!=null){
            if (listDelete.size()>0){
                final MainCategoryModel items = getTrashItem();
                final int count  = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getLatestItem();
                items.categories_max = count;
                mList.add(items);
            }
        }

        Collections.sort(mList, new Comparator<MainCategoryModel>() {
            @Override
            public int compare(MainCategoryModel lhs, MainCategoryModel rhs) {
                int count_1 = (int) lhs.categories_max;
                int count_2 = (int) rhs.categories_max;
                return count_1 - count_2;
            }
        });
        return mList;
    }

    public static List<MainCategoryModel> getListMoveGallery(String categories_local_id, boolean isFakePin){
        final List<MainCategoryModel> mList = getListCategories(categories_local_id,false,isFakePin);
        Collections.sort(mList, new Comparator<MainCategoryModel>() {
            @Override
            public int compare(MainCategoryModel lhs, MainCategoryModel rhs) {
                int count_1 = (int) lhs.categories_max;
                int count_2 = (int) rhs.categories_max;
                return count_1 - count_2;
            }
        });
        return mList;
    }

    public static final List<MainCategoryModel> getListCategories(String categories_local_id , boolean isDelete , boolean isFakePin){
        try{
            final List<MainCategoryEntityModel> mList =  InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListCategories(categories_local_id,isDelete,isFakePin);
            final List<MainCategoryModel> mData = new ArrayList<>();
            if (mList!=null){
                for (MainCategoryEntityModel index : mList){
                    mData.add(new MainCategoryModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public static List<MainCategoryModel> getListFakePin(){
        final List<MainCategoryModel> list = SQLHelper.getListCategories(true);
        list.add(getMainItemFakePin());
        Collections.sort(list, new Comparator<MainCategoryModel>() {
            @Override
            public int compare(MainCategoryModel lhs, MainCategoryModel rhs) {
                int count_1 = (int) lhs.categories_max;
                int count_2 = (int) rhs.categories_max;
                return count_1 - count_2;
            }
        });
        return list;
    }

    public static final transient String []ListIcon =new  String[]{
            "baseline_photo_white_48",
            "baseline_how_to_vote_white_48",
            "baseline_local_movies_white_48",
            "baseline_favorite_border_white_48",
            "baseline_delete_white_48",
            "baseline_cake_white_48",
            "baseline_school_white_48"};

    public static final transient String []ListColor =new  String[]{
            "#34bdb7",
            "#03A9F4",
            "#9E9D24",
            "#AA00FF",
            "#371989",
            "#E040FB",
            "#9E9E9E"};


    public static Map<String, MainCategoryModel>getMainCategoriesDefault(){
        Map<String, MainCategoryModel> map = new HashMap<>();
        map.put(Utils.getHexCode("1234"),new MainCategoryModel("null",Utils.getHexCode("1234"),Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_main_album)), SuperSafeApplication.getInstance().getString(R.string.key_main_album),ListColor[0] ,ListIcon[0],0,false,false,false,false,"",Utils.getUUId(),null,false));
        map.put(Utils.getHexCode("1235"),new MainCategoryModel("null",Utils.getHexCode("1235"),Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_photos)), SuperSafeApplication.getInstance().getString(R.string.key_photos), ListColor[1] ,ListIcon[1],1,false,false,false,false,"",Utils.getUUId(),null,false));
        map.put(Utils.getHexCode("1236"),new MainCategoryModel("null",Utils.getHexCode("1236"),Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_videos)), SuperSafeApplication.getInstance().getString(R.string.key_videos), ListColor[2] ,ListIcon[2],2,false,false,false,false,"",Utils.getUUId(),null,false));
        map.put(Utils.getHexCode("1237"),new MainCategoryModel("null",Utils.getHexCode("1237"),Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_significant_other)), SuperSafeApplication.getInstance().getString(R.string.key_significant_other),ListColor[3],ListIcon[3], 3,false,false,false,false,"",Utils.getUUId(),null,false));
        return map;
    }


    public static List<MainCategoryModel> getCategoriesDefault(){
        List<MainCategoryModel> list = new ArrayList<>();
        list.add(new MainCategoryModel("null",null,null, null,ListColor[0] ,ListIcon[0],0,false,false,false,false,"",null,Utils.getHexCode("1234"),false));
        list.add(new MainCategoryModel("null",null,null,null, ListColor[1] ,ListIcon[1],1,false,false,false,false,"",null,Utils.getHexCode("1235"),false));
        list.add(new MainCategoryModel("null",null,null,null, ListColor[2] ,ListIcon[2],2,false,false,false,false,"",null,Utils.getHexCode("1236"),false));
        list.add(new MainCategoryModel("null",null,null,null,ListColor[3],ListIcon[3], 3,false,false,false,false,"",null,Utils.getHexCode("1237"),false));
        list.add(new MainCategoryModel("null",null,null,null,ListColor[5],ListIcon[5], 5,false,false,false,false,"",null,Utils.getHexCode("1238"),false));
        list.add(new MainCategoryModel("null",null,null,null,ListColor[6],ListIcon[6], 6,false,false,false,false,"",null,Utils.getHexCode("1239"),false));
        return list;
    }


    public static MainCategoryModel getTrashItem(){
        return new MainCategoryModel("null",Utils.getUUId(),Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_trash)), SuperSafeApplication.getInstance().getString(R.string.key_trash), ListColor[4],ListIcon[4],System.currentTimeMillis(),false,false,false,false,"",null,null,false);
    }


    public static MainCategoryModel getMainItemFakePin(){
        return new MainCategoryModel("null",Utils.getHexCode("1234"),Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_main_album)), SuperSafeApplication.getInstance().getString(R.string.key_main_album),ListColor[0] ,ListIcon[0],0,false,false,false,true,"",null,null,false);
    }

    public static boolean onAddCategories(String categories_hex_name,String name,boolean isFakePin){
        try {
            final MainCategoryModel main = SQLHelper.getCategoriesItemId(categories_hex_name,isFakePin);
            if (main==null){
                final int count  = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getLatestItem();
                SQLHelper.insertCategory(new MainCategoryModel("null",Utils.getUUId(),Utils.getHexCode(name),name,ListColor[0],ListIcon[0],count,false,true,false,isFakePin,"",null,null,false));
                return true;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


    public static boolean onAddFakePinCategories(String categories_hex_name,String name,boolean isFakePin){
        try {
            final MainCategoryModel main = SQLHelper.getCategoriesItemId(categories_hex_name,isFakePin);
            if (main==null){
                final int count  = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getLatestItem();
                SQLHelper.insertCategory(new MainCategoryModel("null",Utils.getUUId(),Utils.getHexCode(name),name,ListColor[0],ListIcon[0],count,false,false,false,isFakePin,"",null,null,false));
                return true;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


    public static boolean onChangeCategories(MainCategoryModel mainCategories){
        try {
            String hex_name = Utils.getHexCode(mainCategories.categories_name);
            boolean mIsFakePin = mainCategories.isFakePin;
            MainCategoryModel response = SQLHelper.getCategoriesItemId(hex_name,mIsFakePin);
            if (response==null){
                mainCategories.categories_hex_name = hex_name;
                mainCategories.isChange = true;
                mainCategories.isSyncOwnServer = false;
                SQLHelper.updateCategory(mainCategories);
                return true;
            }
            Utils.Log(TAG,"value changed :"+ new Gson().toJson(response));
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


    public static Drawable getDrawable(Context mContext, String name) {
        try {
            int resourceId = mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName());
            return mContext.getResources().getDrawable(resourceId);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }



    public static Map<String,Object> objectToHashMap(final MainCategoryEntity items){
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> myMap = new Gson().fromJson(new Gson().toJson(items), type);
        return myMap;
    }

    public static MainCategoryEntity getObject(String value){
        try {
            if (value==null){
                return null;
            }
            final MainCategoryEntity items = new Gson().fromJson(value, MainCategoryEntity.class);
            Utils.Log(TAG,new Gson().toJson(items));
            return items;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Ignore
    public static MainCategoryModel getCategoriesPosition(final String mainCategories_Local_Id){
        final List<MainCategoryModel> data = getCategoriesDefault();
        if (mainCategories_Local_Id==null){
            return null;
        }
        for (MainCategoryModel index : data){
            if (index.mainCategories_Local_Id.equals(mainCategories_Local_Id)){
                return index;
            }
        }
        return null;
    }


    public static HashMap<String, MainCategoryModel> getMainCurrentCategories(){
        final List<MainCategoryModel> list = SQLHelper.getListCategories(false);
        final HashMap<String, MainCategoryModel>hashMap = new HashMap<>();
        if (list!=null){
            for (int i = 0;i< list.size();i++){
                final MainCategoryModel main  = list.get(i);
                final String categories_id = main.categories_id;
                if (categories_id!=null){
                    hashMap.put(categories_id,main);
                }
            }
        }
        return hashMap;
    }

    public static final  List<ItemModel> getListItems(final String categories_local_id, int formatType, boolean isDeleteLocal, boolean isFakePin){
        if (categories_local_id==null){
            return null;
        }
        try{
            final List<ItemEntityModel> mList =  InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListItems(categories_local_id,  formatType,  isDeleteLocal,  isFakePin);
            final List<ItemModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntityModel index : mList){
                    mData.add(new ItemModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){

        }
        return null;
    }

    public static final ItemModel getItemId(String item_id){
        try{
            final ItemEntityModel mResult = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getItemId(item_id);
            if (mResult != null){
                return  new ItemModel(mResult);
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public static final  List<ItemModel> getListItems(final String categories_local_id, boolean isDeleteLocal, boolean isExport, boolean isFakePin){
        try{
            if (categories_local_id==null){
                return null;
            }
            try{
                final List<ItemEntityModel> mList =  InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListItems(categories_local_id,isDeleteLocal,isExport,isFakePin);
                final List<ItemModel> mData = new ArrayList<>();
                if (mList!=null){
                    for (ItemEntityModel index : mList){
                        mData.add(new ItemModel(index));
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

    public static final List<ItemModel> getListItems(final String categories_local_id, boolean isDeleteLocal, boolean isFakePin){
        if (categories_local_id==null){
            return null;
        }
        try{
            final List<ItemEntityModel> mList =  InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListItems(categories_local_id,isDeleteLocal,isFakePin);
            final List<ItemModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntityModel index : mList){
                    mData.add(new ItemModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){

        }
        return null;
    }

    public static final List<ItemModel> getListSyncData(boolean isSyncCloud, boolean isSaver, boolean isFakePin){
        try{
            final List<ItemEntityModel> mList =  InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListSyncData(isSyncCloud,isSaver,isFakePin);
            final List<ItemModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntityModel index : mList){
                    mData.add(new ItemModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public static final List<ItemModel> getListItemId(boolean isSyncCloud, boolean isFakePin){
        try{
            final List<ItemEntityModel> mList =  InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListItemId(isSyncCloud,isFakePin);
            final List<ItemModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntityModel index : mList){
                    mData.add(new ItemModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public static final  ItemModel getLatestId(String categories_local_id, boolean isDeleteLocal, boolean isFakePin){
        try{
            final ItemEntityModel mResult =  InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getLatestId(categories_local_id,isDeleteLocal,isFakePin);
            if (mResult!=null){
                return  new ItemModel(mResult);
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public static final List<ItemModel> getListSyncData(boolean isSyncCloud, boolean isFakePin){
        try{
            final List<ItemEntityModel> mList =  InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListSyncData(isSyncCloud,isFakePin);
            final List<ItemModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntityModel index : mList){
                    mData.add(new ItemModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public static final  List<ItemModel> getListAllItems(boolean isDeleteLocal, boolean isFakePin){
        try{
            final List<ItemEntityModel> mList =  InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListAllItems(isDeleteLocal,isFakePin);
            final List<ItemModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntityModel index : mList){
                    mData.add(new ItemModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public static final  List<ItemModel> getListAllItemsSaved(boolean isSaved, boolean isSyncCloud){
        try{
            final List<ItemEntityModel> mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListAllItemsSaved(isSaved,isSyncCloud);
            final List<ItemModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntityModel index : mList){
                    mData.add(new ItemModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public static final  List<ItemModel> getListAllItems(boolean isFakePin){
        try{
            final List<ItemEntityModel> mList =InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListAllItems(isFakePin);
            final List<ItemModel> mData = new ArrayList<>();
            if (mList!=null){
                for (ItemEntityModel index : mList){
                    mData.add(new ItemModel(index));
                }
                return mData;
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }


    public static final  MainCategoryModel getCategoriesLocalId(String categories_local_id){
        try{
            final MainCategoryEntityModel mResut =  InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesLocalId(categories_local_id);
            if (mResut!=null){
                return new MainCategoryModel(mResut);
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }
}
