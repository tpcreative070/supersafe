package co.tpcreative.supersafe.common.services.download
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by PC on 9/5/2017.
 */
object Sequence {
    private val counter: AtomicInteger? = AtomicInteger()
    fun nextValue(): Int {
        return Sequence.counter!!.getAndIncrement()
    }
}