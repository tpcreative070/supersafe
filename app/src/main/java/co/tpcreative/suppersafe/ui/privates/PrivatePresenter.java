package co.tpcreative.suppersafe.ui.privates;

import java.util.ArrayList;
import java.util.List;

import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.presenter.Presenter;
import co.tpcreative.suppersafe.model.Album;

public class PrivatePresenter extends Presenter<PrivateView> {
    protected List<Album> mList;
    public PrivatePresenter(){
        mList = new ArrayList<>();
    }

    public void  getData(){
        mList.clear();
        mList.add(new Album("", R.drawable.face_1));
        mList.add(new Album("", R.drawable.face_2));
        mList.add(new Album("", R.drawable.face_3));
        mList.add(new Album("", R.drawable.face_4));
        mList.add(new Album("", R.drawable.face_5));
        mList.add(new Album("", R.drawable.face_6));
        mList.add(new Album("", R.drawable.face_7));
        mList.add(new Album("", R.drawable.face_8));
        mList.add(new Album("", R.drawable.face_9));
        mList.add(new Album("", R.drawable.face_10));
        mList.add(new Album("", R.drawable.face_11));
        mList.add(new Album("", R.drawable.face_12));
    }


}
