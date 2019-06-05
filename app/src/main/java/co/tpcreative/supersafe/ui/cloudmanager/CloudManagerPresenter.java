package co.tpcreative.supersafe.ui.cloudmanager;
import android.app.Activity;
import android.content.Context;
import com.google.gson.Gson;
import com.snatik.storage.Storage;
import java.util.List;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class CloudManagerPresenter extends Presenter<BaseView<Long>> {

    private static final String TAG = CloudManagerPresenter.class.getSimpleName();
    protected long sizeFile = 0;
    protected long sizeSaverFiles = 0;
    protected Storage storage;
    public CloudManagerPresenter() {
        storage = new Storage(SuperSafeApplication.getInstance());
    }

    public void onGetSaveData(){
        sizeSaverFiles = 0;
        BaseView view = view();
        final List<Items> mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListSyncData(true,false);
        for (Items index : mList) {
            EnumFormatType formatType = EnumFormatType.values()[index.formatType];
            switch (formatType){
                case IMAGE:{
                    sizeSaverFiles += Long.parseLong(index.size);
                    break;
                }
            }
        }
        view.onSuccessful("Successful",EnumStatus.SAVER);
    }

    public void onDisableSaverSpace(EnumStatus enumStatus){
        sizeFile = 0;
        BaseView view = view();
        final List<Items> mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListSyncData(true,true,false);
        for (int i = 0; i <mList.size();i++) {
            EnumFormatType formatType = EnumFormatType.values()[mList.get(i).formatType];
            switch (formatType){
                case IMAGE:{
                    switch (enumStatus){
                        case GET_LIST_FILE:{
                            sizeFile += Long.parseLong(mList.get(i).size);
                            break;
                        }
                        case DOWNLOAD:{
                            sizeFile = 0;
                            mList.get(i).isSaver = false;
                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mList.get(i));
                            break;
                        }
                    }
                    break;
                }
            }
        }
        view.onSuccessful("Successful",enumStatus);
        Utils.Log(TAG,new Gson().toJson(mList));
    }


    public void onEnableSaverSpace(){
        final List<Items> mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListSyncData(true,false,false);
        if (mList!=null && mList.size()>0){
            for (int i = 0 ; i< mList.size();i++){
                EnumFormatType formatType = EnumFormatType.values()[mList.get(i).formatType];
                switch (formatType){
                    case IMAGE:{
                        mList.get(i).isSaver = true;
                        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mList.get(i));
                        //storage.deleteFile(mList.get(i).originalPath);
                        Utils.Log(TAG,"Continue updating...");
                        break;
                    }
                }
            }
        }
        else{
            Utils.Log(TAG,"Already saver files");
        }
        onGetSaveData();
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
