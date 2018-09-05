package co.tpcreative.suppersafe.model;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;
import co.tpcreative.suppersafe.common.util.Utils;

public class MainCategories implements Serializable {

    private final int imageResource;
    private final String localId;
    @SerializedName("id")
    private final String globalId;
    private static MainCategories instance ;

    private static final String TAG = MainCategories.class.getSimpleName();

    public MainCategories(String localId,String globalId, String name, int imageResource ) {
        this.name = name;
        this.imageResource = imageResource;
        this.localId = localId;
        this.globalId = globalId;
    }

    public MainCategories(){
        this.imageResource = 0;
        this.name = null;
        this.localId = null;
        this.globalId = null;
    }

    public List<MainCategories> getList(){
        List<MainCategories> mList = new ArrayList<>();
        mList.add(new MainCategories(Utils.getHexCode(SupperSafeApplication.getInstance().getString(R.string.key_main_album)),null,SupperSafeApplication.getInstance().getString(R.string.key_main_album), R.drawable.face_1));
        mList.add(new MainCategories(Utils.getHexCode(SupperSafeApplication.getInstance().getString(R.string.key_card_ids)),null,SupperSafeApplication.getInstance().getString(R.string.key_card_ids), R.drawable.face_2));
        mList.add(new MainCategories(Utils.getHexCode(SupperSafeApplication.getInstance().getString(R.string.key_videos)),null,SupperSafeApplication.getInstance().getString(R.string.key_videos), R.drawable.face_3));
        mList.add(new MainCategories(Utils.getHexCode(SupperSafeApplication.getInstance().getString(R.string.key_significant_other)),null,SupperSafeApplication.getInstance().getString(R.string.key_significant_other), R.drawable.face_4));
        return mList;
    }

    public String isCheckId(String name){
        final List<MainCategories>mList = getMainCategoriesList();
        for (MainCategories index : mList){
            if (name.equals(index.name)){
                return index.globalId;
            }
        }
        return null;
    }

    public List<MainCategories> getMainCategoriesList(){
        try{
            String value = PrefsController.getString(SupperSafeApplication.getInstance().getString(R.string.key_main_categories),null);
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

    public String getGlobalId() {
        return globalId;
    }

    public String getLocalId() {
        return localId;
    }
}
