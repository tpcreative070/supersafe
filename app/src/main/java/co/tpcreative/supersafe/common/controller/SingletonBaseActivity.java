package co.tpcreative.supersafe.common.controller;
public class SingletonBaseActivity {
    private static SingletonBaseActivity instance ;
    public boolean isAnimation() {
        return isAnimation;
    }
    public void setAnimation(boolean isAnimation) {
        this.isAnimation = isAnimation;
    }
    private boolean isAnimation;

    public static SingletonBaseActivity getInstance(){
        if (instance==null){
            synchronized (SingletonBaseActivity.class){
                if (instance==null){
                    instance = new SingletonBaseActivity();
                }
            }
        }
        return instance;
    }
}
