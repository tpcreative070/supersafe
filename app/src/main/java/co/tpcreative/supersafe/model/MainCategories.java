package co.tpcreative.supersafe.model;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;

public class MainCategories implements Serializable{

    public int imageResource;
    public long localCategories_Count;
    public String localId;
    public String name;
    public static MainCategories instance ;

    /*Send data to camera action*/


    private static final String TAG = MainCategories.class.getSimpleName();

    public MainCategories(String localId, String name, int imageResource,long localCategories_Count ) {
        this.name = name;
        this.imageResource = imageResource;
        this.localId = localId;
        this.localCategories_Count = localCategories_Count;
    }

    public MainCategories(){
        this.imageResource = 0;
        this.name = null;
        this.localId = null;
        this.localCategories_Count = 0;
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

        Collections.sort(mList, new Comparator<MainCategories>() {
            @Override
            public int compare(MainCategories lhs, MainCategories rhs) {
                int count_1 = (int) lhs.localCategories_Count;
                int count_2 = (int) rhs.localCategories_Count;
                return count_1 - count_2;
            }
        });
        return mList;
    }

    public Map<String,MainCategories>getMainCategoriesDefault(){
        Map<String,MainCategories> map = new HashMap<>();
        map.put(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_main_album)),new MainCategories(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_main_album)), SuperSafeApplication.getInstance().getString(R.string.key_main_album), R.color.material_orange_500,1));
        map.put(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_card_ids)),new MainCategories(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_card_ids)), SuperSafeApplication.getInstance().getString(R.string.key_card_ids), R.color.blue_light,2));
        map.put(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_videos)),new MainCategories(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_videos)), SuperSafeApplication.getInstance().getString(R.string.key_videos), R.color.material_lime_800,3));
        map.put(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_significant_other)),new MainCategories(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_significant_other)), SuperSafeApplication.getInstance().getString(R.string.key_significant_other), R.color.material_purple_a700,4));
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

    public boolean onAddCategories(String id,String name,String localCategories_Count){
        try {
            final Map<String,MainCategories> map = getMainCategoriesHashList();
            if (map!=null){
                final MainCategories object = map.get(id);
                if (object==null){
                    long value ;
                    if (localCategories_Count==null){
                         value = map.size()+1;
                    }
                    else{
                         value = Long.parseLong(localCategories_Count);
                    }
                    map.put(id,new MainCategories(id,name,R.color.material_orange_500,value));
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
                    map.remove(object.localId);
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


}
