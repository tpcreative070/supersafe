package co.tpcreative.suppersafe.ui.privates;

import java.util.ArrayList;
import java.util.List;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.presenter.Presenter;
import co.tpcreative.suppersafe.model.MainCategories;

public class PrivatePresenter extends Presenter<PrivateView> {
    protected List<MainCategories> mList;
    public PrivatePresenter(){
        mList = new ArrayList<>();
    }

    public void  getData(){
        PrivateView view = view();
        mList.clear();
        mList.add(new MainCategories("0",null,view.getContext().getString(R.string.key_main_album), R.drawable.face_1));
        mList.add(new MainCategories("1",null,view.getContext().getString(R.string.key_card_ids), R.drawable.face_2));
        mList.add(new MainCategories("2",null,view.getContext().getString(R.string.key_videos), R.drawable.face_3));
        mList.add(new MainCategories("3",null,view.getContext().getString(R.string.key_significant_other), R.drawable.face_4));
    }

}
