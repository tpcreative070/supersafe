package co.tpcreative.suppersafe.common.presenter;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


public class Presenter<V> {

    @Nullable
    private volatile V view;
    @CallSuper
    public void bindView(@NonNull V view) {
        this.view = view;
    }

    @Nullable
    protected V view() {
        return view;
    }

    @Nullable
    public void setView(V view) {
       this.view = view;
    }

    @CallSuper
    private void unbindView(@NonNull V view) {
        this.view = null;
    }

    @CallSuper
    public void unbindView() {
        unbindView(view);
    }

    public boolean isViewAttached() {
        return view != null;
    }

    public void checkViewAttached() {
        if (!isViewAttached()) throw new MvpViewNotAttachedException();
    }
    private static class MvpViewNotAttachedException extends RuntimeException {
        MvpViewNotAttachedException() {
            super(
                    "Please call Presenter.attachView(MvpView) before"
                            + " requesting data to the Presenter");
        }
    }

}

