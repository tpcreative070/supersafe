package co.tpcreative.suppersafe.ui.me;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.BaseFragment;
import co.tpcreative.suppersafe.common.Navigator;
import co.tpcreative.suppersafe.common.controller.SingletonManagerTab;
import co.tpcreative.suppersafe.ui.privates.PrivateFragment;

public class MeFragment extends BaseFragment {

    private static final String TAG = MeFragment.class.getSimpleName();

    @BindView(R.id.nsv)
    NestedScrollView nestedScrollView;

    @BindView(R.id.imgSettings)
    ImageView imgSettings;
    @BindView(R.id.imgPro)
    ImageView imgPro;


    public static MeFragment newInstance(int index) {
        MeFragment fragment = new MeFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    public MeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    protected View getLayoutId(LayoutInflater inflater, ViewGroup viewGroup) {
        ConstraintLayout view = (ConstraintLayout) inflater.inflate(
                R.layout.fragment_me, viewGroup, false);
        return view;
    }

    @Override
    protected void work() {
        super.work();

        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    Log.d(TAG,"hide");
                } else {
                    Log.d(TAG,"show");
                }
            }
        });

        imgSettings.setColorFilter(getContext().getResources().getColor(R.color.colorBackground), PorterDuff.Mode.SRC_ATOP);
        imgPro.setColorFilter(getContext().getResources().getColor(R.color.colorBackground), PorterDuff.Mode.SRC_ATOP);

        Log.d(TAG,"work");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG,"visit :"+isVisibleToUser);
        if (isVisibleToUser) {
            SingletonManagerTab.getInstance().setVisetFloatingButton(View.INVISIBLE);
        }
    }

    @OnClick(R.id.llSettings)
    public void onSettings(View view){
        Navigator.onSettings(getActivity());
        Log.d(TAG,"Settings");
    }


    @OnClick(R.id.tvVerifyAccount)
    public void onVerifyAccount(View view){
        Navigator.onVerifyAccount(getActivity());
    }




}
