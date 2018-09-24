package co.tpcreative.supersafe.ui.me;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.BaseFragment;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.controller.SingletonManagerTab;

public class MeFragment extends BaseFragment implements MeView{

    private static final String TAG = MeFragment.class.getSimpleName();
    @BindView(R.id.nsv)
    NestedScrollView nestedScrollView;
    @BindView(R.id.imgSettings)
    ImageView imgSettings;
    @BindView(R.id.imgPro)
    ImageView imgPro;
    @BindView(R.id.tvEmail)
    TextView tvEmail;
    @BindView(R.id.tvStatus)
    TextView tvStatus;
    @BindView(R.id.tvEnableCloud)
    TextView tvEnableCloud;
    @BindView(R.id.llAboutLocal)
    LinearLayout llAboutLocal;

    private MePresenter presenter;

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

        presenter = new MePresenter();
        presenter.bindView(this);
        presenter.onShowUserInfo();

        if (presenter.mUser!=null){
            if (presenter.mUser.verified){
                tvStatus.setText(getString(R.string.view_change));
            }
            else{
                tvStatus.setText(getString(R.string.verify_change));
            }
           tvEmail.setText(presenter.mUser.email);
        }
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

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        presenter.onShowUserInfo();
        if (presenter.mUser!=null){
            if (presenter.mUser.verified){
                tvStatus.setText(getString(R.string.view_change));
            }
            else{
                tvStatus.setText(getString(R.string.verify_change));
            }
            tvEmail.setText(presenter.mUser.email);
        }

        if (presenter.mUser.driveConnected){
            String value = String.format(getString(R.string.monthly_used),"100","100");
            tvEnableCloud.setText(value);
        }
        else{
            tvEnableCloud.setText(getString(R.string.enable_cloud_sync));
        }
        Log.d(TAG,"OnResume");
    }

    @OnClick(R.id.llSettings)
    public void onSettings(View view){
        Navigator.onSettings(getActivity());
    }


    @OnClick(R.id.llAccount)
    public void onVerifyAccount(View view){
       if (presenter.mUser!=null){
           if (presenter.mUser.verified){
               Navigator.onManagerAccount(getActivity());
           }
           else{
               Navigator.onVerifyAccount(getActivity());
           }
       }
    }

    @OnClick(R.id.llEnableCloud)
    public void onEnableCloud(View view){
        if (presenter.mUser!=null){
            if (presenter.mUser.verified){
                if (!presenter.mUser.driveConnected){
                    Navigator.onCheckSystem(getActivity(),null);
                }
                else{
                    Navigator.onManagerCloud(getActivity());
                }
            }
            else{
                Navigator.onVerifyAccount(getActivity());
            }
        }
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

}
