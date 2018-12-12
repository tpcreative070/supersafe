package co.tpcreative.supersafe.common.controller;
import android.os.CountDownTimer;

import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EmailToken;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;

public class SingletonScreenLock {

    private static SingletonScreenLock instance ;
    private static final String TAG = SingletonScreenLock.class.getSimpleName();
    long current_milliseconds = 0;
    public static SingletonScreenLock getInstance(){
        if (instance==null){
            instance = new SingletonScreenLock();
        }
        return instance;
    }

    private CountDownTimer mCountDownTimer;
    private SingletonScreenLock.SingletonScreenLockListener ls;

    public void setListener(SingletonScreenLock.SingletonScreenLockListener ls){
        this.ls = ls;
    }

    public void onStartTimer(long value){
        Utils.Log(TAG,"onStartTimer");
        if (mCountDownTimer!=null){
            Utils.Log(TAG,"Running............");
            return;
        }
        Utils.Log(TAG,"Start");
        mCountDownTimer = new CountDownTimer(value, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                current_milliseconds = current_milliseconds-1;
                Long serverUptimeSeconds =
                        (millisUntilFinished - current_milliseconds) / 1000;
                String secondsLeft = String.format("%d", ((serverUptimeSeconds % 86400) % 3600) % 60);
                onAttemptTimer(secondsLeft);
            }
            @Override
            public void onFinish() {
                Utils.Log(TAG,"Finish :");
                mCountDownTimer= null;
                onAttemptTimer("0");
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

    public void onAttemptTimer(final String seconds){
        if (ls!=null){
            ls.onAttemptTimer(seconds);
        }
    }

    public interface SingletonScreenLockListener{
        void onAttemptTimer(String seconds);
    }

}
