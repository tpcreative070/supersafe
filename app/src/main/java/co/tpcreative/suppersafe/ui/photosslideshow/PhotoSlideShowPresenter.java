package co.tpcreative.suppersafe.ui.photosslideshow;
import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;

import com.google.gson.Gson;
import com.snatik.storage.Storage;

import java.lang.reflect.GenericDeclaration;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.presenter.Presenter;
import co.tpcreative.suppersafe.common.util.Utils;
import co.tpcreative.suppersafe.model.Items;
import co.tpcreative.suppersafe.model.room.InstanceGenerator;

public class PhotoSlideShowPresenter extends Presenter<PhotoSlideShowView>{

    private final String TAG = PhotoSlideShowPresenter.class.getSimpleName();
    protected Items items;
    protected List<Items> mList;
    protected Storage storage ;

    public PhotoSlideShowPresenter(){
        mList = new ArrayList<>();
    }

    public void getIntent(Activity context){
        storage = new Storage(context);
        mList.clear();
        try {
            Bundle bundle = context.getIntent().getExtras();
            items = (Items) bundle.get(context.getString(R.string.key_items));
            List<Items> list = (ArrayList<Items>)bundle.get(context.getString(R.string.key_list_items));
            for (Items index : list){
                if (!index.local_id.equals(items.local_id)){
                    mList.add(index);
                }
            }
            mList.add(0,items);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Utils.Log(TAG,new Gson().toJson(items));
    }

    public void onDelete(int position){
        try {
            PhotoSlideShowView view = view();
            final Items items = mList.get(position);
            InstanceGenerator.getInstance(view.getContext()).onDelete(items);
            storage.deleteDirectory(items.local_id);
            mList.remove(position);
            view.onDeleteSuccessful();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


}
