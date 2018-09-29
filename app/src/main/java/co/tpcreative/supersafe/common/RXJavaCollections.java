package co.tpcreative.supersafe.common;
import co.tpcreative.supersafe.common.util.Utils;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RXJavaCollections {

    private static final String TAG = RXJavaCollections.class.getSimpleName();


    private Observable<Integer> getObservableItems() {
        return Observable.create(subscriber -> {
            for (int i = 0;i<10;i++) {
                subscriber.onNext(i);
            }
            subscriber.onComplete();
        });
    }

    public void getObservable(){
        getObservableItems().
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(new Observer<Integer>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        Utils.Log(TAG,"complete");
                    }
                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(Integer pojoObject) {
                        // Show Progress
                        Utils.Log(TAG,"next" + pojoObject);
                    }
                });
    }


}
