package co.tpcreative.supersafe.common.controller;

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

    public void setVisetFloatingButton(final int isVisit){
        if (listener!=null){
            listener.visitFloatingButton(isVisit);
        }
    }

    public interface SingleTonResponseListener{
        void visitFloatingButton(int isVisit);
    }


}
