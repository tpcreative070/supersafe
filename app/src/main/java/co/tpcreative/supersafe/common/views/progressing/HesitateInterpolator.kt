package co.tpcreative.supersafe.common.views.progressing
import android.view.animation.Interpolator
internal class HesitateInterpolator : Interpolator {
    override fun getInterpolation(input: Float): Float {
        return if (input < 0.5) Math.pow(input * 2.toDouble(), POW).toFloat() * 0.5f else Math.pow((1 - input) * 2.toDouble(), POW).toFloat() * -0.5f + 1
    }
    companion object {
        private const val POW = 1.0 / 2.0
    }
}