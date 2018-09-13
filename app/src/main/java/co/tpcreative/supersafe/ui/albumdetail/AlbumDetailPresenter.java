package co.tpcreative.supersafe.ui.albumdetail;

import java.util.ArrayList;
import java.util.List;

import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class AlbumDetailPresenter extends Presenter<AlbumDetailView> {

    protected List<Items> mList;
    public AlbumDetailPresenter(){
        mList = new ArrayList<>();
    }

//    public void  getData(){
//        AlbumDetailView view = view();
//        mList.clear();
//        mList.add(new Album("", R.drawable.face_1));
//        mList.add(new Album("", R.drawable.face_2));
//        mList.add(new Album("", R.drawable.face_3));
//        mList.add(new Album("", R.drawable.face_4));
//        mList.add(new Album("", R.drawable.face_5));
//        mList.add(new Album("", R.drawable.face_6));
//        mList.add(new Album("", R.drawable.face_7));
//        mList.add(new Album("", R.drawable.face_8));
//        mList.add(new Album("", R.drawable.face_9));
//        mList.add(new Album("", R.drawable.face_10));
//        mList.add(new Album("", R.drawable.face_11));
//        mList.add(new Album("", R.drawable.face_12));
//        view.onReloadData();
//    }

    public void  getData(){
        AlbumDetailView view = view();
        mList.clear();
        String localId = MainCategories.getInstance().intent_localCategoriesId;
        final List<Items> data = InstanceGenerator.getInstance(view.getContext()).getListItems(localId);
        if (data!=null){
            mList = data;
        }
        view.onReloadData();
    }

}
