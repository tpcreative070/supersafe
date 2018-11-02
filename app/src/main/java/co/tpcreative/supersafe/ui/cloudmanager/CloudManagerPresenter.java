package co.tpcreative.supersafe.ui.cloudmanager;
import android.app.Activity;
import android.content.Context;
import com.google.gson.Gson;
import java.util.List;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class CloudManagerPresenter extends Presenter<BaseView<Long>> {

    private static final String TAG = CloudManagerPresenter.class.getSimpleName();

    public CloudManagerPresenter() {

    }

    public void onGetDriveAbout() {
        Utils.Log(TAG, "onGetDriveAbout" + "--" + EnumStatus.GET_DRIVE_ABOUT.name());
        BaseView<Long> view = view();
        if (NetworkUtil.pingIpAddress(view.getContext())) {
            view.onError("No internet connection", EnumStatus.GET_DRIVE_ABOUT);
            return;
        }

        if (ServiceManager.getInstance().getMyService() == null) {
            view.onError("Service is null", EnumStatus.GET_DRIVE_ABOUT);
            return;
        }

        ServiceManager.getInstance().getMyService().getDriveAbout(new BaseView() {
            @Override
            public void onStartLoading(EnumStatus status) {

            }

            @Override
            public void onStopLoading(EnumStatus status) {

            }

            @Override
            public void onError(String message) {

                Utils.Log(TAG,message);
            }

            @Override
            public void onError(String message, EnumStatus status) {
                Utils.Log(TAG, message + "--" + status.name());
                view.onError(message, status);
            }

            @Override
            public void onSuccessful(String message) {

            }

            @Override
            public void onSuccessful(String message, EnumStatus status) {
                onGetList();
                view.onSuccessful(message, status);
                Utils.Log(TAG, message + "--" + status.name());
            }

            @Override
            public void onSuccessful(String message, EnumStatus status, Object object) {
                Utils.Log(TAG, message + "--" + status.name());
            }

            @Override
            public void onSuccessful(String message, EnumStatus status, List list) {

            }

            @Override
            public Context getContext() {
                return null;
            }

            @Override
            public Activity getActivity() {
                return null;
            }
        });

    }

    private void onGetList() {
        Utils.Log(TAG, "onGetList" + "--" + EnumStatus.GET_LIST_FILES_IN_APP.name());
        BaseView view = view();
        if (NetworkUtil.pingIpAddress(view.getContext())) {
            view.onError("No internet connection", EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }

        if (ServiceManager.getInstance().getMyService() == null) {
            view.onError("Service is null", EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }

        ServiceManager.getInstance().getMyService().onGetListFileInApp(new BaseView<Integer>() {
            @Override
            public void onError(String message, EnumStatus status) {
                view.onError(message, status);
                Utils.Log(TAG, "error");
            }

            @Override
            public void onSuccessful(String message) {
                Utils.Log(TAG, "onSuccessful ??");
            }

            @Override
            public void onSuccessful(String message, EnumStatus status) {
                Utils.Log(TAG, "onSuccessful !! " + status.name());
            }

            @Override
            public void onSuccessful(String message, EnumStatus status, Integer object) {
                if (object == 0) {
                    final User mUser = User.getInstance().getUserInfo();
                    if (mUser != null) {
                        Utils.Log(TAG, "onSuccessful 2");
                        if (mUser.driveAbout != null) {
                            Utils.Log(TAG, "onSuccessful 3");
                            mUser.driveAbout.inAppUsed = 0;
                            PrefsController.putString(SuperSafeApplication.getInstance().getString(R.string.key_user), new Gson().toJson(mUser));
                            view.onSuccessful("Successful", status);
                        }
                    }
                    Utils.Log(TAG, "onSuccessful 4");

                } else {
                    final List<Items> mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListItemId(true, false);
                    long countSize = 0;
                    try {
                        for (Items index : mList) {
                            countSize += Long.parseLong(index.size);
                        }
                        final User mUser = User.getInstance().getUserInfo();
                        if (mUser != null) {
                            if (mUser.driveAbout != null) {
                                mUser.driveAbout.inAppUsed = countSize;
                                PrefsController.putString(SuperSafeApplication.getInstance().getString(R.string.key_user), new Gson().toJson(mUser));
                                view.onSuccessful("Successful", status);
                            }
                        }
                        Utils.Log(TAG, "onSuccessful 5");
                    } catch (Exception e) {
                        Utils.Log(TAG, "onSuccessful 6");
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onSuccessful(String message, EnumStatus status, List<Integer> list) {

            }

            @Override
            public void onStartLoading(EnumStatus status) {

            }

            @Override
            public void onStopLoading(EnumStatus status) {

            }

            @Override
            public void onError(String message) {

            }

            @Override
            public Context getContext() {
                return null;
            }

            @Override
            public Activity getActivity() {
                return null;
            }
        });
    }

}
