package co.tpcreative.supersafe.ui.photosslideshow;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.snatik.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

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
            final Items mItem = InstanceGenerator.getInstance(view.getContext()).getLocalId(items.local_id);
            if (mItem!=null){
                InstanceGenerator.getInstance(view.getContext()).onDelete(mItem);
                storage.deleteDirectory(SuperSafeApplication.getInstance().getSuperSafe()+mItem.local_id);
                mList.remove(position);
                view.onDeleteSuccessful();
            }
            else{
                Utils.Log(TAG,"Not found");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


}
