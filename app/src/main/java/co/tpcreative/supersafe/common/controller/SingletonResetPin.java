package co.tpcreative.supersafe.common.controller;
import android.os.CountDownTimer;
import com.google.gson.Gson;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;

public class SingletonResetPin {
    private static SingletonResetPin instance ;
    private static final String TAG = SingletonResetPin.class.getSimpleName();
    public long waitingLeft = 0;
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
                waitingLeft = secondsRemaining;
                Utils.Log(TAG,""+secondsRemaining);
                Utils.onPushEventBus(EnumStatus.WAITING_LEFT);
            }
            @Override
            public void onFinish() {
                Utils.Log(TAG,"Finish :");
                final User mUser = User.getInstance().getUserInfo();
                Utils.Log(TAG,new Gson().toJson(mUser));
                mUser.isWaitingSendMail = true;
                Utils.setUserPreShare(mUser);
                mCountDownTimer= null;
                ServiceManager.getInstance().onSendEmail();
                ServiceManager.getInstance().setIsWaitingSendMail(false);
                Utils.onPushEventBus(EnumStatus.WAITING_DONE);
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
            Utils.setUserPreShare(mUser);
        }
        else{
            try {
                ServiceManager.getInstance().setIsWaitingSendMail(false);
                final User mUser = User.getInstance().getUserInfo();
                mUser.isWaitingSendMail = false;
                Utils.setUserPreShare(mUser);
            }
            catch (Exception e){
            }
        }
    }


}
