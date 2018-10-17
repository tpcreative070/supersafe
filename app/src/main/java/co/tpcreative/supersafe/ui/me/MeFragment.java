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

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.BaseFragment;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.controller.SingletonManagerTab;
import co.tpcreative.supersafe.common.controller.SingletonPremiumTimer;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.SyncData;
import co.tpcreative.supersafe.model.Theme;

public class MeFragment extends BaseFragment implements BaseView,SingletonPremiumTimer.SingletonPremiumTimerListener{

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
    @BindView(R.id.tvPremiumLeft)
    TextView tvPremiumLeft;
    @BindView(R.id.tvAudios)
    TextView tvAudios;
    @BindView(R.id.tvPhotos)
    TextView tvPhotos;
    @BindView(R.id.tvVideos)
    TextView tvVideos;

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

        final Theme mTheme = Theme.getInstance().getThemeInfo();
        if (mTheme!=null){
            imgSettings.setColorFilter(getContext().getResources().getColor(mTheme.getPrimaryColor()), PorterDuff.Mode.SRC_ATOP);
            //imgPro.setColorFilter(getContext().getResources().getColor(R.color.holo_blue_dark), PorterDuff.Mode.SRC_ATOP);
        }

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
        String value = String.format(getString(R.string.premium_left),"30");
        tvPremiumLeft.setText(value);
        Log.d(TAG,"work");
    }

    @Override
    public void onPremiumTimer(String days, String hours, String minutes, String seconds) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String value = String.format(getString(R.string.premium_left),days);
                    tvPremiumLeft.setText(value);
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
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
            SingletonPremiumTimer.getInstance().setListener(this);
        }
        else{
            SingletonPremiumTimer.getInstance().setListener(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        presenter.onCalculate();
        presenter.onShowUserInfo();
        try {
            if (presenter.mUser != null) {
                if (presenter.mUser.verified) {
                    tvStatus.setText(getString(R.string.view_change));
                } else {
                    tvStatus.setText(getString(R.string.verify_change));
                }
                tvEmail.setText(presenter.mUser.email);
            }
            if (presenter.mUser.driveConnected) {
                String value;
                final SyncData syncData = presenter.mUser.syncData;
                if (syncData != null) {
                    int result = 100 - syncData.left;
                    value = String.format(getString(R.string.monthly_used), result + "", "100");
                } else {
                    value = String.format(getString(R.string.monthly_used), "0", "100");
                }
                tvEnableCloud.setText(value);
            } else {
                tvEnableCloud.setText(getString(R.string.enable_cloud_sync));
            }
        }
        catch (Exception e){
            e.printStackTrace();
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

    @OnClick(R.id.llPremium)
    public void onClickedPremium(View view){
        Navigator.onMoveToPremium(getContext());
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
        switch (status) {
            case RELOAD: {
                String photos = String.format(getString(R.string.photos_default), "" + presenter.photos);
                tvPhotos.setText(photos);

                String videos = String.format(getString(R.string.videos_default), "" + presenter.videos);
                tvVideos.setText(videos);

                String audios = String.format(getString(R.string.audios_default), "" + presenter.audios);
                tvAudios.setText(audios);
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
