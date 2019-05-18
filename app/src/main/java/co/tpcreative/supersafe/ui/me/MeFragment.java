package co.tpcreative.supersafe.ui.me;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
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
import co.tpcreative.supersafe.common.util.ConvertUtils;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.SyncData;
import co.tpcreative.supersafe.model.ThemeApp;
import co.tpcreative.supersafe.model.User;

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
    @BindView(R.id.tvOther)
    TextView tvOther;
    @BindView(R.id.tvAvailableSpaces)
    TextView tvAvailableSpaces;

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
        presenter = new MePresenter();
        presenter.bindView(this);
        presenter.onShowUserInfo();

        if (presenter.mUser!=null){
            if (presenter.mUser.verified){
                tvStatus.setText(getString(R.string.view_user_info));
            }
            else{
                tvStatus.setText(getString(R.string.verify_change));
            }
           tvEmail.setText(presenter.mUser.email);
        }

    }

    public void onUpdatedView(){
        final boolean isPremium = User.getInstance().isPremium();
        if (isPremium){
            tvPremiumLeft.setText(getString(R.string.you_are_in_premium_features));
            ThemeApp themeApp = ThemeApp.getInstance().getThemeInfo();
            tvPremiumLeft.setTextColor(getResources().getColor(themeApp.getPrimaryColor()));
            if (presenter.mUser.driveConnected) {
                tvEnableCloud.setText(getString(R.string.no_limited_cloud_sync_storage));
            } else {
                tvEnableCloud.setText(getString(R.string.enable_cloud_sync));
            }
        }
        else if (User.getInstance().isPremiumComplimentary()){
            String dayLeft = SingletonPremiumTimer.getInstance().getDaysLeft();
            if (dayLeft!=null){
                String sourceString = Utils.getFontString(R.string.premium_left,dayLeft);
                tvPremiumLeft.setText(Html.fromHtml(sourceString));
            }
            if (presenter.mUser.driveConnected) {
                tvEnableCloud.setText(getString(R.string.no_limited_cloud_sync_storage));
            } else {
                tvEnableCloud.setText(getString(R.string.enable_cloud_sync));
            }
        }
        else{
            if (presenter.mUser.driveConnected) {
                String value;
                final SyncData syncData = presenter.mUser.syncData;
                if (syncData != null) {
                    int result = Navigator.LIMIT_UPLOAD - syncData.left;
                    value = String.format(getString(R.string.monthly_used), result + "", ""+Navigator.LIMIT_UPLOAD);
                } else {
                    value = String.format(getString(R.string.monthly_used), "0", ""+Navigator.LIMIT_UPLOAD);
                }
                tvEnableCloud.setText(value);
            } else {
                tvEnableCloud.setText(getString(R.string.enable_cloud_sync));
            }
            if (presenter.mUser.verified){
                tvPremiumLeft.setText(getString(R.string.premium_expired));
                tvPremiumLeft.setTextColor(getResources().getColor(R.color.red_300));
            }
        }
    }

    @Override
    public void onPremiumTimer(String days, String hours, String minutes, String seconds) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final boolean isPremium = User.getInstance().isPremium();
                    if (isPremium){
                        tvPremiumLeft.setText(getString(R.string.you_are_in_premium_features));
                    }
                    else{
                        String sourceString = Utils.getFontString(R.string.premium_left,days);
                        tvPremiumLeft.setText(Html.fromHtml(sourceString));
                    }
                }
            });
        }
        catch (Exception e){
            SingletonPremiumTimer.getInstance().onStop();
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"onDestroy");
    }

    @Override
    public void onStop() {
        super.onStop();
        Utils.Log(TAG,"onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.Log(TAG,"onPause");
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG,"visit :"+isVisibleToUser);
        if (isVisibleToUser) {
            SingletonManagerTab.getInstance().setVisetFloatingButton(View.INVISIBLE);
            final boolean isPremium = User.getInstance().isPremium();
            if (!isPremium){
                SingletonPremiumTimer.getInstance().setListener(this);
            }
            onUpdatedView();
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
        onUpdatedView();
        try {
            if (presenter.mUser != null) {
                if (presenter.mUser.verified) {
                    tvStatus.setText(getString(R.string.view_user_info));
                } else {
                    tvStatus.setText(getString(R.string.verify_change));
                }
                tvEmail.setText(presenter.mUser.email);
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

                String others = String.format(getString(R.string.others_default), "" + presenter.others);
                tvOther.setText(others);

                String availableSpaces =  ConvertUtils.byte2FitMemorySize(Utils.getAvailableSpaceInBytes());
                tvAvailableSpaces.setText(availableSpaces);
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
