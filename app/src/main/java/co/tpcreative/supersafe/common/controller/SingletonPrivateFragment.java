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

    public interface SingletonPrivateFragmentListener{
        void onUpdateView();
    }

}
