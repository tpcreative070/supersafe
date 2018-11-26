package co.tpcreative.supersafe.common.controller;
import co.tpcreative.supersafe.model.EnumStatus;

public class SingletonManagerTab {

    private static SingletonManagerTab instance ;
    private SingleTonResponseListener listener;

    public static SingletonManagerTab getInstance(){
        if (instance==null){
            synchronized (SingletonManagerTab.class){
                if (instance==null){
                    instance = new SingletonManagerTab();
                }
            }
        }
        return instance;
    }

    public void setListener(SingleTonResponseListener listener){
        this.listener = listener;
    }

    public void onAction(EnumStatus status){
        if (listener!=null){
            listener.onAction(status);
        }
    }

    public void onRequestAccessToken(){
        if (listener!=null){
            listener.onRequestAccessToken();
        }
    }

    public void onSyncDone(){
        if (listener!=null){
            listener.onSyncDone();
        }
    }

    public void setVisetFloatingButton(final int isVisit){
        if (listener!=null){
            listener.visitFloatingButton(isVisit);
        }
    }

    public interface SingleTonResponseListener{
        void visitFloatingButton(int isVisit);
        void onAction(EnumStatus enumStatus);
        void onSyncDone();
        void onRequestAccessToken();
    }

}
