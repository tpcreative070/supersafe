package co.tpcreative.supersafe.common.views.progressing
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.DialogInterface
import android.content.res.TypedArray
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import kotlinx.android.synthetic.main.dmax_spots_dialog.*

class SpotsDialog  private constructor(context: Context?, dotColorId: Int, message: String, theme: Int, cancelable: Boolean, cancelListener: DialogInterface.OnCancelListener?) : AlertDialog(context!!, theme) {
    private var mFillDrawable = 0
    private var typedArray: TypedArray? = null
    class Builder {
        private var context: Context? = null
        private var message: String? = null
        private var messageId = 0
        private var themeId = 0
        private var dotColorId = 0
        private var cancelable = true // default dialog behaviour
        private var cancelListener: DialogInterface.OnCancelListener? = null
        fun setContext(context: Context?): Builder {
            this.context = context
            return this
        }

        fun setMessage(message: String?): Builder {
            this.message = message
            return this
        }

        fun setDotColor(color: Int): Builder {
            dotColorId = color
            return this
        }

        fun setMessage(@StringRes messageId: Int): Builder {
            this.messageId = messageId
            return this
        }

        fun setTheme(@StyleRes themeId: Int): Builder {
            this.themeId = themeId
            return this
        }

        fun setCancelable(cancelable: Boolean): Builder {
            this.cancelable = cancelable
            return this
        }

        fun setCancelListener(cancelListener: DialogInterface.OnCancelListener?): Builder {
            this.cancelListener = cancelListener
            return this
        }

        fun build(): AlertDialog {
            return SpotsDialog(
                    context,
                    dotColorId,
                    (if (messageId != 0) context?.getString(messageId) else message)!!,
                    if (themeId != 0) themeId else R.style.SpotsDialogDefault,
                    cancelable,
                    cancelListener
            )
        }
    }

    private var size = 0
    private lateinit var spots: Array<AnimatedView?>
    private var animator: AnimatorPlayer? = null
    private var message: CharSequence?
    private val dorColorId: Int
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dmax_spots_dialog)
        setCanceledOnTouchOutside(false)
        typedArray = context.obtainStyledAttributes(R.styleable.Dialog)
        mFillDrawable = typedArray!!.getResourceId(R.styleable.Dialog_DialogSpotColor, R.drawable.dot_empty)
        initMessage()
        initProgress()
    }

    override fun onStart() {
        super.onStart()
        for (view in spots) view?.visibility = View.VISIBLE
        animator = AnimatorPlayer(createAnimations())
        animator?.play()
    }

    override fun onStop() {
        super.onStop()
        animator?.stop()
    }

    override fun setMessage(message: CharSequence) {
        this.message = message
        if (isShowing) initMessage()
    }

    private fun initMessage() {
        if (message != null && message!!.isNotEmpty()) {
           dmax_spots_title.text = message
        }
    }

    private fun initProgress(){
        val progress: ProgressLayout? = dmax_spots_progress
        size = progress!!.spotsCount
        spots = arrayOfNulls(size)
        val size = context.resources.getDimensionPixelSize(R.dimen.spot_size)
        val progressWidth = context.resources.getDimensionPixelSize(R.dimen.progress_width)
        for (i in spots.indices) {
            val v = AnimatedView(context)
            val shape = GradientDrawable()
            shape.cornerRadius = 10f
            shape.setColor(ContextCompat.getColor(context, dorColorId))
            v.background = shape
            v.target = progressWidth
            v.xFactor = -1f
            v.visibility = View.INVISIBLE
            progress.addView(v, size, size)
            spots[i] = v
        }
    }

    private fun createAnimations(): Array<Animator?> {
        val animators = arrayOfNulls<Animator>(size)
        for (i in spots.indices) {
            val animatedView = spots[i]
            val move: Animator = ObjectAnimator.ofFloat(animatedView, "xFactor", 0f, 1f)
            move.duration = DURATION.toLong()
            move.interpolator = HesitateInterpolator()
            move.startDelay = DELAY * i.toLong()
            move.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    animatedView?.visibility = View.INVISIBLE
                }
                override fun onAnimationStart(animation: Animator) {
                    animatedView?.visibility = View.VISIBLE
                }
            })
            animators[i] = move
        }
        return animators
    }

    companion object {
        private const val DELAY = 150
        private const val DURATION = 1500
    }

    init {
        this.message = message
        dorColorId = dotColorId
        setCancelable(cancelable)
        cancelListener?.let { setOnCancelListener(it) }
    }
}