package co.tpcreative.supersafe.ui.albumcover;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.DateUtils;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.EnumEvent;
import co.tpcreative.supersafe.model.Event;
import co.tpcreative.supersafe.model.EventItem;
import co.tpcreative.supersafe.model.HeaderItem;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.ListItem;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class AlbumCoverPresenter extends Presenter<BaseView> {

    protected MainCategories mMainCategories;
    protected List<Items>mList;
    protected List<MainCategories> mListMainCategories;
    protected List<ListItem> mListItem;
    private static final String TAG = AlbumCoverPresenter.class.getSimpleName();

    protected Date dateEventItems = buildRandomDateInCurrentMonth();
    protected Date dateEventCategories = buildRandomDateInCurrentMonth();

    public AlbumCoverPresenter(){
        mMainCategories = new MainCategories();
        mList = new ArrayList<>();
        mListItem = new ArrayList<>();
    }

    private Date buildRandomDateInCurrentMonth() {
        Random random = new Random();
        return DateUtils.buildDate(random.nextInt(31) + 1);
    }

    public void getData(Activity activity){
        BaseView view = view();
        Bundle bundle = activity.getIntent().getExtras();
        try{
            final MainCategories mainCategories = (MainCategories) bundle.get(MainCategories.class.getSimpleName());
            if (mainCategories!=null){
                this.mMainCategories = mainCategories;
                view.onSuccessful("Successful",EnumStatus.RELOAD);
            }
            Utils.Log(TAG,new Gson().toJson(this.mMainCategories));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<Items> getData(){
        BaseView view = view();
        mList.clear();
        final List<Items> data = InstanceGenerator.getInstance(view.getContext()).getListItems(mMainCategories.categories_local_id,false,mMainCategories.isFakePin);
        if (data!=null){
            mList = data;
            final Items oldItem = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getItemId(mMainCategories.items_id);
            if (oldItem!=null){
                for (int i = 0 ; i<mList.size() ; i++){
                    mList.get(i).date = dateEventItems;
                    if (oldItem.items_id.equals(mList.get(i).items_id)){
                        mList.get(i).isChecked = true;
                    }
                    else{
                        mList.get(i).isChecked = false;
                    }
                }
            }
            else {
                for (int i = 0 ; i<mList.size() ; i++){
                    mList.get(i).date = dateEventItems;
                    mList.get(i).isChecked = false;
                }
            }
        }
        Utils.Log(TAG,"List :"+ mList.size());
        view.onSuccessful("Successful",EnumStatus.GET_LIST_FILE);
        return mList;
    }

    public List<MainCategories> getListCategories(){
        BaseView view = view();
        final List<MainCategories> data = MainCategories.getInstance().getCategoriesDefault();
        if (data!=null){
            mListMainCategories = data;
            final MainCategories oldMainCategories =  InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesLocalId(mMainCategories.mainCategories_Local_Id);
            if (oldMainCategories!=null){
                for (int i = 0 ; i<mListMainCategories.size() ; i++){
                    mListMainCategories.get(i).date = dateEventCategories;
                    if (oldMainCategories.categories_local_id.equals(mList.get(i).categories_local_id)){
                        mListMainCategories.get(i).isChecked = true;
                    }
                    else{
                        mListMainCategories.get(i).isChecked = false;
                    }
                }
            }
            else{
                for (int i = 0 ; i<mListMainCategories.size() ; i++){
                    mListMainCategories.get(i).date = dateEventCategories;
                    mListMainCategories.get(i).isChecked = false;
                }
            }
        }
        return mListMainCategories;
    }


    public void onGetListItems(){
        BaseView view = view();
        mListItem.clear();
        Map<Date, List<Event>> events = toMap(getEventItem());
        for (Date date : events.keySet()) {
            HeaderItem header = new HeaderItem(date);
            mListItem.add(header);
            for (Event event : events.get(date)) {
                EventItem item = new EventItem(event,event.getEvent());
                mListItem.add(item);
            }
        }
        view.onSuccessful("Successful",EnumStatus.GET_LIST_FILE);
    }

    @NonNull
    private Map<Date, List<Event>> toMap(@NonNull List<Event> events) {
        Map<Date, List<Event>> map = new TreeMap<>();
        for (Event event : events) {
            List<Event> value = map.get(event.getDate());
            if (value == null) {
                value = new ArrayList<>();
                map.put(event.getDate(), value);
            }
            value.add(event);
        }
        return map;
    }

    public List<Event> getEventItem(){
        List<Event> event = new ArrayList<>();
        for (Items index : getData()){
            event.add(new Event(index,null, EnumEvent.ITEMS,dateEventItems));
        }
        for (MainCategories index: getListCategories()){
            event.add(new Event(null,index, EnumEvent.MAIN_CATEGORIES,dateEventCategories));
        }
        return event;
    }

    private String getString(int res){
        BaseView view = view();
        String value = view.getContext().getString(res);
        return value;
    }


}
