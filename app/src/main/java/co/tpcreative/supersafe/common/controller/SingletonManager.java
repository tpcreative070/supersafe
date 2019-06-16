package co.tpcreative.supersafe.common.controller;
public class SingletonManager {
    private static SingletonManager instance ;
    public boolean isAnimation() {
        return isAnimation;
    }
    public void setAnimation(boolean isAnimation) {
        this.isAnimation = isAnimation;
    }
    private boolean isAnimation;
    public boolean isVisitLockScreen() {
        return isVisitLockScreen;
    }

    public void setVisitLockScreen(boolean visitLockScreen) {
        isVisitLockScreen = visitLockScreen;
    }

    public boolean isVisitFakePin() {
        return isVisitFakePin;
    }

    public void setVisitFakePin(boolean visitFakePin) {
        isVisitFakePin = visitFakePin;
    }

    public boolean isReloadMainTab() {
        return isReloadMainTab;
    }

    public void setReloadMainTab(boolean reloadMainTab) {
        isReloadMainTab = reloadMainTab;
    }

    private boolean isVisitLockScreen;
    private boolean isVisitFakePin;
    private boolean isReloadMainTab;

    public static SingletonManager getInstance(){
        if (instance==null){
            synchronized (SingletonManager.class){
                if (instance==null){
                    instance = new SingletonManager();
                }
            }
        }
        return instance;
    }
}
