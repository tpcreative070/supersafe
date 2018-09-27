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
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class MainCategories implements Serializable{

    public int image;
    public int icon;
    public long categories_max;
    public String categories_id;
    public String categories_name;
    public static MainCategories instance ;

    /*Send data to camera action*/


    private static final String TAG = MainCategories.class.getSimpleName();

    public MainCategories(String categories_id, String categories_name, int image, int icon, long categories_max) {
        this.categories_name = categories_name;
        this.image = image;
        this.icon = icon;
        this.categories_id = categories_id;
        this.categories_max = categories_max;
    }

    public MainCategories(){
        this.image = 0;
        this.categories_name = null;
        this.categories_id = null;
        this.categories_max = 0;
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


        final List<Items>list = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getDeleteLocalListItems(true,EnumDelete.NONE.ordinal());
        if (list!=null){
            if (list.size()>0){
                final MainCategories items = getTrashItem();
                items.categories_max = mList.size()+1;
                mList.add(items);
            }
        }

        Collections.sort(mList, new Comparator<MainCategories>() {
            @Override
            public int compare(MainCategories lhs, MainCategories rhs) {
                int count_1 = (int) lhs.categories_max;
                int count_2 = (int) rhs.categories_max;
                return count_1 - count_2;
            }
        });

        return mList;
    }

    public Map<String,MainCategories>getMainCategoriesDefault(){
        Map<String,MainCategories> map = new HashMap<>();
        map.put(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_main_album)),new MainCategories(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_main_album)), SuperSafeApplication.getInstance().getString(R.string.key_main_album), R.color.pocket_color_2,R.drawable.baseline_photo_white_48,1));
        map.put(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_card_ids)),new MainCategories(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_card_ids)), SuperSafeApplication.getInstance().getString(R.string.key_card_ids), R.color.blue_light,R.drawable.baseline_how_to_vote_white_48,2));
        map.put(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_videos)),new MainCategories(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_videos)), SuperSafeApplication.getInstance().getString(R.string.key_videos), R.color.material_lime_800,R.drawable.baseline_local_movies_white_48,3));
        map.put(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_significant_other)),new MainCategories(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_significant_other)), SuperSafeApplication.getInstance().getString(R.string.key_significant_other), R.color.material_purple_a700,R.drawable.baseline_favorite_border_white_48,4));
        return map;
    }

    public MainCategories getTrashItem(){
      return new MainCategories(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_trash)), SuperSafeApplication.getInstance().getString(R.string.key_trash), R.color.colorPrimary,R.drawable.baseline_delete_white_48,1);
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
                    map.put(id,new MainCategories(id,name,R.color.material_orange_500,R.drawable.baseline_photo_white_48,value));
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
                    map.remove(object.categories_id);
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
