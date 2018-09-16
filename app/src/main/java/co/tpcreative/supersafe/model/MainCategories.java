package co.tpcreative.supersafe.model;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;

public class MainCategories implements Serializable {

    private final int imageResource;
    private final String localId;
    private final String name;
    private static MainCategories instance ;

    /*Send data to camera action*/

    public transient String intent_localCategories_Name;
    public transient String intent_localCategoriesId;


    private static final String TAG = MainCategories.class.getSimpleName();

    public MainCategories(String localId, String name, int imageResource ) {
        this.name = name;
        this.imageResource = imageResource;
        this.localId = localId;
    }

    public MainCategories(){
        this.imageResource = 0;
        this.name = null;
        this.localId = null;
    }

    public List<MainCategories> getList(){
        List<MainCategories> mList = new ArrayList<>();
        final Map<String,MainCategories> hashMap = MainCategories.getInstance().getMainCategoriesHashList();
        if (hashMap!=null){
            for (Map.Entry<String,MainCategories> index : hashMap.entrySet()){
                mList.add(index.getValue());
            }
        }
        else{
            final Map<String,MainCategories> map = MainCategories.getInstance().getMainCategoriesDefault();
            for (Map.Entry<String,MainCategories> index : map.entrySet()){
                mList.add(index.getValue());
            }
            PrefsController.putString(SuperSafeApplication.getInstance().getString(R.string.key_main_categories_hash_list),new Gson().toJson(map));
        }
        return mList;
    }

    public Map<String,MainCategories>getMainCategoriesDefault(){
        Map<String,MainCategories> map = new HashMap<>();
        map.put(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_main_album)),new MainCategories(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_main_album)), SuperSafeApplication.getInstance().getString(R.string.key_main_album), R.color.material_orange_500));
        map.put(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_card_ids)),new MainCategories(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_card_ids)), SuperSafeApplication.getInstance().getString(R.string.key_card_ids), R.color.blue_light));
        map.put(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_videos)),new MainCategories(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_videos)), SuperSafeApplication.getInstance().getString(R.string.key_videos), R.color.material_lime_800));
        map.put(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_significant_other)),new MainCategories(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_significant_other)), SuperSafeApplication.getInstance().getString(R.string.key_significant_other), R.color.material_purple_a700));
        return map;
    }

    public Map<String,MainCategories>getMainCategoriesHashList(){
        try {
            String value  = PrefsController.getString(SuperSafeApplication.getInstance().getString(R.string.key_main_categories_hash_list),null);
            if (value!=null){
                Type type = new TypeToken<Map<String, MainCategories>>(){}.getType();
                final Map<String,MainCategories> object  = new Gson().fromJson(value,type);
                if (object!=null){
                    return object;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean onAddCategories(String id,String name){
        try {
            final Map<String,MainCategories> map = getMainCategoriesHashList();
            if (map!=null){
                final MainCategories object = map.get(id);
                if (object==null){
                    map.put(id,new MainCategories(id,name,R.color.material_orange_500));
                    PrefsController.putString(SuperSafeApplication.getInstance().getString(R.string.key_main_categories_hash_list),new Gson().toJson(map));
                    return true;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean onDeleteCategories(String id){
        try {
            final Map<String,MainCategories> map = getMainCategoriesHashList();
            if (map!=null){
                final MainCategories object = map.get(id);
                if (object!=null){
                    map.remove(object.getLocalId());
                    PrefsController.putString(SuperSafeApplication.getInstance().getString(R.string.key_main_categories_hash_list),new Gson().toJson(map));
                    Utils.Log(TAG," delete "+ new Gson().toJson(map));
                    return true;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


    public static MainCategories getInstance(){
        if (instance==null){
            instance = new MainCategories();
        }
        return instance;
    }

    public String getName() {
        return name;
    }

    public int getImageResource() {
        return imageResource;
    }

    public String getLocalId() {
        return localId;
    }

}
