package co.tpcreative.supersafe.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
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

    public transient String intent_name;
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
        mList.add(new MainCategories(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_main_album)), SuperSafeApplication.getInstance().getString(R.string.key_main_album), R.drawable.face_1));
        mList.add(new MainCategories(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_card_ids)), SuperSafeApplication.getInstance().getString(R.string.key_card_ids), R.drawable.face_2));
        mList.add(new MainCategories(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_videos)), SuperSafeApplication.getInstance().getString(R.string.key_videos), R.drawable.face_3));
        mList.add(new MainCategories(Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_significant_other)), SuperSafeApplication.getInstance().getString(R.string.key_significant_other), R.drawable.face_4));
        return mList;
    }


    public List<MainCategories> getMainCategoriesList(){
        try{
            String value = PrefsController.getString(SuperSafeApplication.getInstance().getString(R.string.key_main_categories),null);
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



    public int getImageResource() {
        return imageResource;
    }

    public String getLocalId() {
        return localId;
    }
}
