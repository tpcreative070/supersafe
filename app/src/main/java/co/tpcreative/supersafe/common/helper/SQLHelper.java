package co.tpcreative.supersafe.common.helper;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.supersafe.common.entities.InstanceGenerator;
import co.tpcreative.supersafe.common.entities.ItemEntity;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.model.ItemEntityModel;
import co.tpcreative.supersafe.model.ItemModel;
import co.tpcreative.supersafe.model.MainCategoryEntityModel;
import co.tpcreative.supersafe.model.MainCategoryModel;

public class SQLHelper {

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
        final List<ItemEntityModel> mResult = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getItemList(true, false);
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
        final List<ItemEntityModel> list = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getAllListItems(false,false);
        List<ItemModel> mList = new ArrayList<>();
        for (ItemEntityModel index : list){
            mList.add(new ItemModel(index));
        }
        return mList;
    }

    /*Request upload item*/
    public static List<ItemModel> getItemListUpload(){
        List<ItemModel> mList = new ArrayList<>();
        final List<ItemEntityModel> mResult = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getSyncUploadDataItemsListByNull( false);
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

}
