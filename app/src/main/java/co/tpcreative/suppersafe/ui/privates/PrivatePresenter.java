package co.tpcreative.suppersafe.ui.privates;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.presenter.Presenter;
import co.tpcreative.suppersafe.model.Album;
import co.tpcreative.suppersafe.model.CategoryMain;

public class PrivatePresenter extends Presenter<PrivateView> {
    protected List<CategoryMain> mList;
    public PrivatePresenter(){
        mList = new ArrayList<>();
    }

    public void  getData(){
        PrivateView view = view();
        mList.clear();
        mList.add(new CategoryMain(0,view.getContext().getString(R.string.key_main_album), R.drawable.face_1));
        mList.add(new CategoryMain(1,view.getContext().getString(R.string.key_card_ids), R.drawable.face_2));
        mList.add(new CategoryMain(2,view.getContext().getString(R.string.key_videos), R.drawable.face_3));
        mList.add(new CategoryMain(3,view.getContext().getString(R.string.key_significant_other), R.drawable.face_4));
    }


}
