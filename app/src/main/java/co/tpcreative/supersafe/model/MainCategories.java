package co.tpcreative.supersafe.model;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.graphics.drawable.Drawable;
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
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@Entity(tableName = "maincategories")
public class MainCategories implements Serializable{

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

    @Ignore
    private static MainCategories instance ;

    @Ignore
    private transient String []ListIcon =new  String[]{"baseline_photo_white_48",
            "baseline_how_to_vote_white_48",
            "baseline_local_movies_white_48",
            "baseline_favorite_border_white_48",
            "baseline_delete_white_48"};

    @Ignore
    private transient String []ListColor =new  String[]{"#34bdb7",
            "#03A9F4",
            "#9E9D24",
            "#AA00FF",
            "#371989",
            "#FF9800"};


    /*Send data to camera action*/

    private static final String TAG = MainCategories.class.getSimpleName();


    public MainCategories(String categories_id,String categories_local_id,String categories_hex_name, String categories_name, String image, String icon, long categories_max,boolean isDelete,boolean isChange,boolean isSyncOwnServer,boolean isFakePin) {
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
    }


    public MainCategories(){
        this.image = null;
        this.categories_name = null;
        this.categories_local_id = null;
        this.categories_id = null;
        this.categories_hex_name = null;
        this.categories_max = 0;
        this.isDelete = false;
        this.isChange = false;
        this.isFakePin = false;
    }

    @Ignore
    public List<MainCategories> getList(){
        List<MainCategories> mList = new ArrayList<>();
        final List<MainCategories> list = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListCategories(false);
        final List<MainCategories> listM = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListCategories(false);

        if (list!=null && list.size()>0){
            mList.addAll(list);
            Utils.Log(TAG,"Found data :"+ list.size());
        }
        else{
            final Map<String,MainCategories> map = MainCategories.getInstance().getMainCategoriesDefault();
            Utils.Log(TAG,"No Data " + map.size());
            Utils.Log(TAG,"New special value :"+new Gson().toJson(listM));
            for (Map.Entry<String,MainCategories> index : map.entrySet()){
                final MainCategories main = index.getValue();
                mList.add(main);
                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(main);
            }
            //getObservable(mList);
        }

        final List<Items>listDelete = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getDeleteLocalListItems(true,EnumDelete.NONE.ordinal(),false);
        if (listDelete!=null){
            if (listDelete.size()>0){
                final MainCategories items = getTrashItem();
                items.categories_max = System.currentTimeMillis();
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


    @Ignore
    public List<MainCategories> getListFakePin(){
        final List<MainCategories> list = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListCategories(true);
        list.add(getMainItemFakePin());
        Collections.sort(list, new Comparator<MainCategories>() {
            @Override
            public int compare(MainCategories lhs, MainCategories rhs) {
                int count_1 = (int) lhs.categories_max;
                int count_2 = (int) rhs.categories_max;
                return count_1 - count_2;
            }
        });
        return list;
    }

    @Ignore
    public Map<String,MainCategories>getMainCategoriesDefault(){
        Map<String,MainCategories> map = new HashMap<>();
        map.put(Utils.getHexCode("1234"),new MainCategories("null",Utils.getHexCode("1234"),Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_main_album)), SuperSafeApplication.getInstance().getString(R.string.key_main_album),ListColor[0] ,ListIcon[0],1000,false,false,false,false));
        map.put(Utils.getHexCode("1235"),new MainCategories("null",Utils.getHexCode("1235"),Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_card_ids)), SuperSafeApplication.getInstance().getString(R.string.key_card_ids), ListColor[1] ,ListIcon[1],System.currentTimeMillis()+1000,false,false,false,false));
        map.put(Utils.getHexCode("1236"),new MainCategories("null",Utils.getHexCode("1236"),Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_videos)), SuperSafeApplication.getInstance().getString(R.string.key_videos), ListColor[2] ,ListIcon[2],System.currentTimeMillis()+5000,false,false,false,false));
        map.put(Utils.getHexCode("1237"),new MainCategories("null",Utils.getHexCode("1237"),Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_significant_other)), SuperSafeApplication.getInstance().getString(R.string.key_significant_other),ListColor[3],ListIcon[3],System.currentTimeMillis() +10000,false,false,false,false));
        return map;
    }

    @Ignore
    public MainCategories getTrashItem(){
        return new MainCategories("null",Utils.getUUId(),Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_trash)), SuperSafeApplication.getInstance().getString(R.string.key_trash), ListColor[4],ListIcon[4],System.currentTimeMillis(),false,false,false,false);
    }

    @Ignore
    public MainCategories getMainItemFakePin(){
        return new MainCategories("null",Utils.getHexCode("1234"),Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_main_album)), SuperSafeApplication.getInstance().getString(R.string.key_main_album),ListColor[0] ,ListIcon[0],1000,false,false,false,true);
    }

    @Ignore
    public boolean onAddCategories(String categories_hex_name,String name,boolean isFakePin){
        try {
            final MainCategories main = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesItemId(categories_hex_name,isFakePin);
            if (main==null){
                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(new MainCategories("null",Utils.getUUId(),Utils.getHexCode(name),name,ListColor[0],ListIcon[0],System.currentTimeMillis(),false,true,false,isFakePin));
                return true;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Ignore
    public boolean onAddFakePinCategories(String categories_hex_name,String name,boolean isFakePin){
        try {
            final MainCategories main = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesItemId(categories_hex_name,isFakePin);
            if (main==null){
                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(new MainCategories("null",Utils.getUUId(),Utils.getHexCode(name),name,ListColor[0],ListIcon[0],System.currentTimeMillis(),false,false,false,isFakePin));
                return true;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Ignore
    public boolean onChangeCategories(MainCategories mainCategories){
        try {
            String hex_name = Utils.getHexCode(mainCategories.categories_name);
            boolean mIsFakePin = mainCategories.isFakePin;
            MainCategories response = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesItemId(hex_name,mIsFakePin);
            if (response==null){
                mainCategories.categories_hex_name = hex_name;
                mainCategories.isChange = true;
                mainCategories.isSyncOwnServer = false;
                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mainCategories);
                return true;
            }
            Utils.Log(TAG,"value changed :"+ new Gson().toJson(response));
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


    @Ignore
    public Drawable getDrawable(Context mContext, String name) {
        try {
            int resourceId = mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName());
            return mContext.getResources().getDrawable(resourceId);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Ignore
    public static MainCategories getInstance(){
        if (instance==null){
            instance = new MainCategories();
        }
        return instance;
    }

    @Ignore
    public Map<String,Object> objectToHashMap(final MainCategories items){
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> myMap = new Gson().fromJson(new Gson().toJson(items), type);
        return myMap;
    }


}
