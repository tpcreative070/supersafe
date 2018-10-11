package co.tpcreative.supersafe.ui.privates;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.litao.android.lib.Utils.GridSpacingItemDecoration;
import com.snatik.storage.Storage;
import java.util.List;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.BaseFragment;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.controller.SingletonManagerTab;
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.MainCategories;


public class PrivateFragment extends BaseFragment implements BaseView,PrivateAdapter.ItemSelectedListener,SingletonPrivateFragment.SingletonPrivateFragmentListener{

    private static final String TAG = PrivateFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private PrivatePresenter presenter;
    private PrivateAdapter adapter;
    private Storage storage;

    public static PrivateFragment newInstance(int index) {
        PrivateFragment fragment = new PrivateFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    public PrivateFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected View getLayoutId(LayoutInflater inflater, ViewGroup viewGroup) {
        ConstraintLayout view = (ConstraintLayout) inflater.inflate(
                R.layout.fragment_private, viewGroup, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        initRecycleView(inflater);
        SingletonPrivateFragment.getInstance().setListener(this);
        storage = new Storage(SuperSafeApplication.getInstance());
        return view;
    }

    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    protected void work() {
        presenter = new PrivatePresenter();
        presenter.bindView(this);
        presenter.getData();
        adapter.setDataSource(presenter.mList);
        super.work();
    }

    @Override
    public void onStartLoading(EnumStatus status) {

    }

    @Override
    public void onStopLoading(EnumStatus status) {

    }

    @Nullable
    @Override
    public Context getContext() {
        return super.getContext();
    }

    public void initRecycleView(LayoutInflater layoutInflater){
        adapter = new PrivateAdapter(layoutInflater,getContext(),this);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 10, true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClickItem(int position) {
        Log.d(TAG,"Position :"+ position);
        try {
            String value  = Utils.getHexCode(getString(R.string.key_trash));
            if (value.equals(presenter.mList.get(position).categories_hex_name)){
                Navigator.onMoveTrash(getActivity());
            }
            else{
                final MainCategories mainCategories = presenter.mList.get(position);
                Navigator.onMoveAlbumDetail(getActivity(),mainCategories);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSetting(int position) {
        Navigator.onAlbumSettings(getActivity(),presenter.mList.get(position));
    }

    @Override
    public void onDeleteAlbum(int position) {
        Utils.Log(TAG,"Delete album");
        presenter.onDeleteAlbum(position);
    }

    @Override
    public void onEmptyTrash(int position) {
        try {
            presenter.onEmptyTrash();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onUpdateView() {
        if (presenter!=null){
            presenter.getData();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Converting dp to pixel
     */

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG,"visit :"+isVisibleToUser);
        if (isVisibleToUser) {
            SingletonManagerTab.getInstance().setVisetFloatingButton(View.VISIBLE);
            if (presenter!=null){
                presenter.getData();
            }
        }
    }

    @Override
    public void onError(String message, EnumStatus status) {

    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onSuccessful(String message) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {
        switch (status){
            case RELOAD:{
                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (adapter!=null){
                                adapter.setDataSource(presenter.mList);
                            }
                        }
                    });
                }
                catch (Exception e){
                    e.getMessage();
                }
                break;
            }
        }
    }

    @Override
    public void onSuccessful(String message, EnumStatus status, Object object) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List list) {

    }
}
