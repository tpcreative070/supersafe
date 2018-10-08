package co.tpcreative.supersafe.common.controller;

public class SingletonFakePinComponent {

    private static SingletonFakePinComponent instance;
    private SingletonPrivateFragmentListener ls;

    public static SingletonFakePinComponent getInstance() {
        if (instance==null){
            instance = new SingletonFakePinComponent();
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
