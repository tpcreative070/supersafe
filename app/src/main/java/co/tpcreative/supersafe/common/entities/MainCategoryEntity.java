package co.tpcreative.supersafe.common.entities;
import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.helper.SQLHelper;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumDelete;
import co.tpcreative.supersafe.model.ItemModel;
import co.tpcreative.supersafe.model.MainCategoryEntityModel;
import co.tpcreative.supersafe.model.MainCategoryModel;

@Entity(tableName = "maincategories")
public class MainCategoryEntity implements Serializable{

    @PrimaryKey(autoGenerate = true)
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
    @Ignore
    public Date date;
    @Ignore
    public boolean isChecked;

    @Ignore
    private static MainCategoryEntity instance ;


    @Ignore
    public static MainCategoryEntity getInstance(){
        if (instance==null){
            instance = new MainCategoryEntity();
        }
        return instance;
    }


    /*Send data to camera action*/

    private static final String TAG = MainCategoryEntity.class.getSimpleName();

    public MainCategoryEntity(String categories_id, String categories_local_id, String categories_hex_name, String categories_name, String image, String icon, long categories_max, boolean isDelete, boolean isChange, boolean isSyncOwnServer, boolean isFakePin, String pin, String items_id, String mainCategories_Local_Id, boolean isCustom_Cover) {
        this.categories_name = categories_name;
        this.image = image;
        this.icon = icon;
        this.categories_local_id = categories_local_id;
        this.categories_id = categories_id;
        this.categories_hex_name = categories_hex_name;
        this.categories_max = categories_max;
        this.isDelete = isDelete;
        this.isChange = isChange;
        this.isFakePin = isFakePin;
        this.pin = pin;
        this.items_id = items_id;
        this.mainCategories_Local_Id = mainCategories_Local_Id;
        this.isCustom_Cover = isCustom_Cover;
        this.isSyncOwnServer = isSyncOwnServer;
    }

    @Ignore
    public MainCategoryEntity(){
        this.image = null;
        this.categories_name = null;
        this.categories_local_id = null;
        this.categories_id = null;
        this.categories_hex_name = null;
        this.categories_max = 0;
        this.isDelete = false;
        this.isChange = false;
        this.isFakePin = false;
        this.pin = "";
        this.items_id = null;
        this.mainCategories_Local_Id = null;
        this.isCustom_Cover = false;
    }


    /*Delete category*/
    public MainCategoryEntity(MainCategoryEntityModel entityModel){
        this.id = entityModel.id;
        this.image = entityModel.image;
        this.icon = entityModel.icon;
        this.categories_max = entityModel.categories_max;
        this.categories_local_id = entityModel.categories_local_id;
        this.categories_id = entityModel.categories_id;
        this.categories_hex_name = entityModel.categories_hex_name;
        this.categories_name = entityModel.categories_name;
        this.isDelete  = entityModel.isDelete;
        this.isChange  = entityModel.isChange;
        this.isSyncOwnServer = entityModel.isSyncOwnServer;
        this.isFakePin = entityModel.isFakePin;
        this.pin  = entityModel.pin;
        this.isCustom_Cover = entityModel.isCustom_Cover;
        this.items_id = entityModel.items_id;
        this.mainCategories_Local_Id = entityModel.mainCategories_Local_Id;
    }
}
