package co.tpcreative.suppersafe.ui.privates;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
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
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.BaseFragment;
import co.tpcreative.suppersafe.common.Navigator;
import co.tpcreative.suppersafe.common.controller.SingletonManagerTab;

public class PrivateFragment extends BaseFragment implements PrivateView,PrivateAdapter.ItemSelectedListener{

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
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClickItem(int position) {
        Log.d(TAG,"Position :"+ position);
        Navigator.onMoveAlbumDetail(getContext());
    }

    @Override
    public void onAddToFavoriteSelected(int position) {

    }

    @Override
    public void onPlayNextSelected(int position) {
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
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
        }
    }

}
