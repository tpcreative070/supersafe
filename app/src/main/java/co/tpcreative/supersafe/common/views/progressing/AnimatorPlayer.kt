package co.tpcreative.supersafe.common.views.progressing
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
internal class AnimatorPlayer(private val animators: Array<Animator?>) : AnimatorListenerAdapter() {
    private var interrupted = false
    override fun onAnimationEnd(animation: Animator) {
        if (!interrupted) animate()
    }
    fun play() {
        animate()
    }
    fun stop() {
        interrupted = true
    }
    private fun animate() {
        val set = AnimatorSet()
        set.playTogether(*animators)
        set.addListener(this)
        set.start()
    }
}