package co.tpcreative.suppersafe.common.presenter;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import java.util.concurrent.TimeUnit;

import co.tpcreative.suppersafe.common.util.Utils;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class Presenter<V>{



    @Nullable
    private volatile V view;
    protected CompositeDisposable subscriptions;
    @CallSuper
    public void bindView(@NonNull V view) {
        this.view = view;
        this.subscriptions = new CompositeDisposable();
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
        if (subscriptions != null) {
            if (!subscriptions.isDisposed()) {
                subscriptions.dispose();
            }
            if (subscriptions.isDisposed()) {
                subscriptions.clear();
            }
            subscriptions = null;
        }

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

    protected  void  onDelay(){
       Observable.fromArray(0,5)
               .concatMap(i-> Observable.just(i).delay(6000, TimeUnit.MILLISECONDS))
                .doOnNext(i->{
                    /*Do something here*/
                })
               .doOnComplete(() -> Log.d("",""))
                .subscribe();
    }

    protected void initRxJavaLoader(){
        Flowable.create((FlowableEmitter<Object> emitter) -> {
            emitter.onNext(1);
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER).observeOn(Schedulers.io()).subscribe(response ->{

        });
    }


}

