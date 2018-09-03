package co.tpcreative.suppersafe.model;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;

public class MainCategories implements Serializable {

    private final int imageResource;
    private final String id;
    private static MainCategories instance ;

    private static final String TAG = MainCategories.class.getSimpleName();

    public MainCategories(String id, String name, int imageResource ) {
        this.name = name;
        this.imageResource = imageResource;
        this.id = id;
    }


    public MainCategories(){
        this.id = null;
        this.imageResource = 0;
        this.name = null;
    }

    public List<MainCategories> getList(){
        List<MainCategories> list = new ArrayList<>();
        list.add(new MainCategories(null, SupperSafeApplication.getInstance().getString(R.string.key_main_album),0));
        list.add(new MainCategories(null, SupperSafeApplication.getInstance().getString(R.string.key_card_ids),0));
        list.add(new MainCategories(null, SupperSafeApplication.getInstance().getString(R.string.key_videos),0));
        list.add(new MainCategories(null, SupperSafeApplication.getInstance().getString(R.string.key_significant_other),0));
        return list;
    }

    public String isCheckId(String name){
        final List<MainCategories>mList = getMainCategoriesList();
        for (MainCategories index : mList){
            if (name.equals(index.name)){
                return index.id;
            }
        }
        return null;
    }

    public List<MainCategories> getMainCategoriesList(){
        try{
            String value = PrefsController.getString(SupperSafeApplication.getInstance().getString(R.string.key_main_categories),null);
            Log.d(TAG,"value :"+value);
            if (value!=null){
                Type listType = new TypeToken<ArrayList<MainCategories>>(){}.getType();
                final List<MainCategories> mainCategoriesList = new Gson().fromJson(value,listType);
                if (mainCategoriesList!=null){
                    return mainCategoriesList;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
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

    private final String name;

    public int getImageResource() {
        return imageResource;
    }

    public String getId() {
        return id;
    }


}
