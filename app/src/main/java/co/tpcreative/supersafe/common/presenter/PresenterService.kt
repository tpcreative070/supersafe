package co.tpcreative.supersafe.common.presenter
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.annotation.CallSuper
import io.reactivex.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

open class PresenterService<V> : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @Volatile
    private var view: V? = null
    protected var subscriptions: CompositeDisposable? = null

    @CallSuper
    fun bindView(view: V) {
        this.view = view
        subscriptions = CompositeDisposable()
    }

    protected fun view(): V? {
        return view
    }

    fun setView(view: V?) {
        this.view = view
    }

    @CallSuper
    private fun unbindView(view: V) {
        if (subscriptions != null) {
            if (!subscriptions!!.isDisposed()) {
                subscriptions!!.dispose()
            }
            if (subscriptions!!.isDisposed()) {
                subscriptions!!.clear()
            }
            subscriptions = null
        }
        this.view = null
    }

    @CallSuper
    fun unbindView() {
        unbindView(view!!)
    }

    fun isViewAttached(): Boolean {
        return view != null
    }

    fun checkViewAttached() {
        if (!isViewAttached()) throw PresenterService.MvpViewNotAttachedException()
    }

    private class MvpViewNotAttachedException internal constructor() : RuntimeException("Please call Presenter.attachView(MvpView) before"
            + " requesting data to the Presenter")

    protected fun onDelay() {
        Observable.fromArray<Int?>(0, 5)
                .concatMap { i: Int? -> Observable.just(i).delay(6000, TimeUnit.MILLISECONDS) }
                .doOnNext { i: Int? -> }
                .doOnComplete { Log.d("", "") }
                .subscribe()
    }

    protected fun initRxJavaLoader() {
        Flowable.create(FlowableOnSubscribe<Any?> { emitter: FlowableEmitter<Any?>? ->
            emitter?.onNext(1)
            emitter?.onComplete()
        }, BackpressureStrategy.BUFFER).observeOn(Schedulers.io()).subscribe(Consumer { response: Any? -> })
    }
}