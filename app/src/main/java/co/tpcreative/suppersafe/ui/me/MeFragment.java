package co.tpcreative.suppersafe.ui.me;
import android.content.Context;
import android.content.Intent;
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

import com.google.api.services.drive.DriveScopes;
import com.google.gson.Gson;
import com.jaychang.sa.AuthCallback;
import com.jaychang.sa.AuthData;
import com.jaychang.sa.AuthDataHolder;
import com.jaychang.sa.SocialUser;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.SignInActivityWithDrive;
import co.tpcreative.suppersafe.common.BaseFragment;
import co.tpcreative.suppersafe.common.Navigator;
import co.tpcreative.suppersafe.common.controller.ServiceManager;
import co.tpcreative.suppersafe.common.controller.SingletonManagerTab;
import co.tpcreative.suppersafe.demo.oauthor.GoogleAuthActivity;
import co.tpcreative.suppersafe.ui.verifyaccount.VerifyAccountActivity;

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
        ServiceManager.getInstance().onGetLastSignIn();
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

        if (ServiceManager.getInstance().getDriveResourceClient()!=null){
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
       // Navigator.onSettings(getActivity());
        Log.d(TAG,"Settings");
        Intent intent = new Intent(getActivity(), SignInActivityWithDrive.class);
        startActivity(intent);
//        List<String> requiredScopes = new ArrayList<>();
//        requiredScopes.add(DriveScopes.DRIVE);
//        AuthDataHolder.getInstance().googleAuthData = new AuthData(requiredScopes, new AuthCallback() {
//            @Override
//            public void onSuccess(SocialUser socialUser) {
//                Log.d(TAG,"onSuccess : " + socialUser.accessToken);
//                Log.d(TAG,"user :" + new Gson().toJson(socialUser));
//            }
//            @Override
//            public void onError(Throwable throwable) {
//                Log.d(TAG,"onError");
//            }
//
//            @Override
//            public void onCancel() {
//                Log.d(TAG,"onCancel");
//            }
//        });
//        GoogleAuthActivity.start(getActivity());
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
                if (ServiceManager.getInstance().getDriveClient() == null){
                    Navigator.onCheckSystem(getActivity());
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
