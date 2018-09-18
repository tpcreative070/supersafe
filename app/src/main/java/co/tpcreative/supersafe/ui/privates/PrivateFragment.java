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

import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.BaseFragment;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.controller.SingletonManagerTab;
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class PrivateFragment extends BaseFragment implements PrivateView,PrivateAdapter.ItemSelectedListener,SingletonPrivateFragment.SingletonPrivateFragmentListener{

    private static final String TAG = PrivateFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private PrivatePresenter presenter;
    private PrivateAdapter adapter;

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
    public void startLoading() {

    }

    @Override
    public void stopLoading() {

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
            final MainCategories mainCategories = presenter.mList.get(position);
            Navigator.onMoveAlbumDetail(getActivity(),mainCategories);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSetting(int position) {

    }

    @Override
    public void onDeleteAlbum(int position) {
        final MainCategories object = presenter.mList.get(position);
        if (object!=null){
            final boolean response = MainCategories.getInstance().onDeleteCategories(object.localId);
            if (response){
                InstanceGenerator.getInstance(getActivity()).onDeleteAll(object.localId);
                presenter.getData();
            }
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
    public void onReload() {
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
    }
}
