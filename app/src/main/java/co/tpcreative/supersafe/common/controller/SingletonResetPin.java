package co.tpcreative.supersafe.common.controller;
import android.os.CountDownTimer;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.google.gson.Gson;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.User;

public class SingletonResetPin {
    private static SingletonResetPin instance ;
    private static final String TAG = SingletonResetPin.class.getSimpleName();
    public static SingletonResetPin getInstance(){
        if (instance==null){
            instance = new SingletonResetPin();
        }
        return instance;
    }
    private CountDownTimer mCountDownTimer;

    public void onStartTimer(long value){
        Utils.Log(TAG,"onStartTimer");
        if (mCountDownTimer!=null){
            Utils.Log(TAG,"Running............");
            return;
        }
        ServiceManager.getInstance().onStartService();
        ServiceManager.getInstance().setIsWaitingSendMail(true);
        final User mUser = User.getInstance().getUserInfo();
        Utils.Log(TAG,"Start " +new Gson().toJson(mUser));
        mCountDownTimer = new CountDownTimer(value, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                Utils.Log(TAG,""+secondsRemaining);
            }
            @Override
            public void onFinish() {
                Utils.Log(TAG,"Finish :");
                final User mUser = User.getInstance().getUserInfo();
                Utils.Log(TAG,new Gson().toJson(mUser));
                mUser.isWaitingSendMail = true;
                PrefsController.putString(SuperSafeApplication.getInstance().getString(R.string.key_user),new Gson().toJson(mUser));
                mCountDownTimer= null;
                ServiceManager.getInstance().onSendEmail();
                ServiceManager.getInstance().setIsWaitingSendMail(false);
            }
        }.start();
    }

    public void onStop(){
        if (mCountDownTimer!=null){
            mCountDownTimer.cancel();
            mCountDownTimer = null;
            ServiceManager.getInstance().setIsWaitingSendMail(false);
            final User mUser = User.getInstance().getUserInfo();
            mUser.isWaitingSendMail = false;
            PrefsController.putString(SuperSafeApplication.getInstance().getString(R.string.key_user),new Gson().toJson(mUser));
        }
        else{
            try {
                ServiceManager.getInstance().setIsWaitingSendMail(false);
                final User mUser = User.getInstance().getUserInfo();
                mUser.isWaitingSendMail = false;
                PrefsController.putString(SuperSafeApplication.getInstance().getString(R.string.key_user),new Gson().toJson(mUser));
            }
            catch (Exception e){
                Answers.getInstance().logContentView(new ContentViewEvent()
                        .putContentName(SingletonResetPin.class.getSimpleName()+" error")
                        .putContentType("Content error "+e.getMessage())
                        .putContentId(SingletonResetPin.class.getSimpleName()+" "+System.currentTimeMillis()));
            }
        }
    }


}
