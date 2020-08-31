package co.tpcreative.supersafe.common.request;

import java.io.Serializable;

import co.tpcreative.supersafe.common.entities.MainCategoryEntity;
import co.tpcreative.supersafe.model.MainCategoryModel;

public class CategoriesRequest implements Serializable {
    public String user_id;
    public String cloud_id;
    public String device_id;
    public String categories_id;

    public String categories_name;
    public String categories_hex_name;
    public String icon;
    public String image;
    public long categories_max;
    public boolean isDelete;
    public boolean isChange;
    public boolean isSyncOwnServer;

    /*Get list*/
    public CategoriesRequest(String user_id,String cloud_id,String device_id){
        this.user_id = user_id;
        this.cloud_id = cloud_id;
        this.device_id = device_id;
    }

    /*Delete category*/
    public CategoriesRequest(String user_id,String cloud_id,String device_id,String categories_id ){
        this.user_id = user_id;
        this.cloud_id = cloud_id;
        this.device_id = device_id;
        this.categories_id = categories_id;
    }

    /*Sync category*/
    public CategoriesRequest(String user_id, String cloud_id, String device_id , MainCategoryModel mainCategories){
        this.user_id = user_id;
        this.cloud_id = cloud_id;
        this.device_id = device_id;
        this.categories_id = mainCategories.categories_id;
        this.categories_name = mainCategories.categories_name;
        this.categories_hex_name = mainCategories.categories_hex_name;
        this.icon = mainCategories.icon;
        this.image = mainCategories.image;
        this.categories_max = mainCategories.categories_max;
        this.isDelete = mainCategories.isDelete;
        this.isChange = mainCategories.isChange;
        this.isSyncOwnServer = mainCategories.isSyncOwnServer;
    }
}