package co.tpcreative.supersafe.common.controller;
import co.tpcreative.supersafe.model.EnumStatus;

public class SingletonPrivateFragment {

    private static SingletonPrivateFragment instance;
    private SingletonPrivateFragmentListener ls;

    public static SingletonPrivateFragment getInstance() {
        if (instance==null){
            instance = new SingletonPrivateFragment();
        }
        return instance;
    }

    public void setListener(SingletonPrivateFragmentListener ls){
        this.ls = ls;
    }

    public void onUpdateView(){
        if (ls!=null){
            ls.onUpdateView();
        }
    }

//    public void onPremiumTimer(int days,int months,int minutes,int seconds){
//        if (ls!=null){
//            ls.onPremiumTimer(days,months,minutes,seconds);
//        }
//    }

    public interface SingletonPrivateFragmentListener{
        void onUpdateView();
        //void onPremiumTimer(int days,int months,int minutes,int seconds);
    }

}
