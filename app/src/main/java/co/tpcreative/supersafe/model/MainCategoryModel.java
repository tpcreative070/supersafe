package co.tpcreative.supersafe.model;

import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

import co.tpcreative.supersafe.common.entities.MainCategoryEntity;

public class MainCategoryModel implements Serializable {
    public int id;
    public String image;
    public String icon;
    public long categories_max;
    public String categories_local_id;
    public String categories_id;
    public String categories_hex_name;
    public String categories_name;
    public boolean isDelete ;
    public boolean isChange ;
    public boolean isSyncOwnServer;
    public boolean isFakePin;
    public String pin ;
    public boolean isCustom_Cover;
    public String items_id;
    public String mainCategories_Local_Id;
    public String unique_id;


    /*Custom field to show to view*/
    public Date date;
    public boolean isChecked;

    /*Fetch item from local db*/
    public MainCategoryModel(MainCategoryEntityModel entity){
        this.id = entity.id;
        this.image = entity.image;
        this.icon = entity.icon;
        this.categories_max = entity.categories_max;
        this.categories_local_id = entity.categories_local_id;
        this.categories_id = entity.categories_id;
        this.categories_hex_name = entity.categories_hex_name;
        this.categories_name = entity.categories_name;
        this.isDelete = entity.isDelete;
        this.isChange  = entity.isChange;
        this.isSyncOwnServer = entity.isSyncOwnServer;
        this.isFakePin = entity.isFakePin;
        this.pin  = entity.pin;
        this.isCustom_Cover = entity.isCustom_Cover;
        this.items_id = entity.items_id;
        this.mainCategories_Local_Id = entity.mainCategories_Local_Id;
        this.unique_id = entity.unique_id;
    }
}
