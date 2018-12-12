package co.tpcreative.supersafe.common.controller;
import android.os.CountDownTimer;
import co.tpcreative.supersafe.common.util.Utils;

public class SingletonResetPin {
    private static SingletonResetPin instance ;
    private static final String TAG = SingletonResetPin.class.getSimpleName();
    long current_milliseconds = 0;
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
        Utils.Log(TAG,"Start");
        mCountDownTimer = new CountDownTimer(value, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                current_milliseconds = current_milliseconds-1;
                Long serverUptimeSeconds =
                        (millisUntilFinished - current_milliseconds) / 1000;
                String secondsLeft = String.format("%d", ((serverUptimeSeconds % 86400) % 3600) % 60);
                Utils.Log(TAG,secondsLeft);
            }
            @Override
            public void onFinish() {
                Utils.Log(TAG,"Finish :");
                mCountDownTimer= null;
                ServiceManager.getInstance().onSendEmail();
                ServiceManager.getInstance().setIsWaitingSendMail(false);
            }
        }.start();
    }

    public void onStop(){
        if (mCountDownTimer!=null){
            mCountDownTimer.cancel();
            mCountDownTimer.onFinish();
            mCountDownTimer = null;
        }
    }

}
