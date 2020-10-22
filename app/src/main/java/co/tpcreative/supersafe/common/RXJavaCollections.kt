package co.tpcreative.supersafe.common
import co.tpcreative.supersafe.common.api.response.BaseResponse
import co.tpcreative.supersafe.common.util.Utils
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers

class RXJavaCollections {
    private fun getObservableItems(): Observable<Int?>? {
        return Observable.create(ObservableOnSubscribe<Int?> { subscriber: ObservableEmitter<Int?>? ->
            for (i in 0..9) {
                subscriber?.onNext(i)
            }
            subscriber?.onComplete()
        })
    }

    fun getObservable() {
        getObservableItems()?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())?.subscribe(object : Observer<Int?> {
            override fun onSubscribe(d: Disposable) {}
            override fun onComplete() {
                Utils.Log(TAG, "complete")
            }

            override fun onError(e: Throwable) {}
            override fun onNext(pojoObject: Int) {
                // Show Progress
                Utils.Log(TAG, "next$pojoObject")
            }
        })
    }

    fun onUI() {
        Observable.create<Any?>(ObservableOnSubscribe<Any?> { subscriber: ObservableEmitter<Any?>? -> })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe { response: Any? -> }
    }

    fun login(email: String?, password: String?): Observable<BaseResponse>? {
        return null
    }

    companion object {
        private val TAG = RXJavaCollections::class.java.simpleName
    }
}