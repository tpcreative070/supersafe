package co.tpcreative.supersafe.common.controller;
import android.os.CountDownTimer;

import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EmailToken;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;

public class SingletonScreenLock {

    private static SingletonScreenLock instance ;
    private static final String TAG = SingletonScreenLock.class.getSimpleName();
    public static SingletonScreenLock getInstance(){
        synchronized (SingletonScreenLock.class){
            if (instance==null){
                instance = new SingletonScreenLock();
            }
            return instance;
        }
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
                long secondsRemaining = millisUntilFinished / 1000;
                SingletonScreenLock.getInstance().onAttemptTimer(""+secondsRemaining);
            }
            @Override
            public void onFinish() {
                Utils.Log(TAG,"Finish :");
                mCountDownTimer= null;
                SingletonScreenLock.getInstance().onAttemptTimerFinish();
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

    public void onAttemptTimerFinish(){
        if (ls!=null){
            ls.onAttemptTimerFinish();
        }
    }

    public interface SingletonScreenLockListener{
        void onAttemptTimer(String seconds);
        void onAttemptTimerFinish();
    }

}
