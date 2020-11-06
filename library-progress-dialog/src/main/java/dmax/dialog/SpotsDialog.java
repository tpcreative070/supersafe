package dmax.dialog;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 13.01.15 at 14:22
 */
public class SpotsDialog extends AlertDialog {

    private int mFillDrawable;
    private TypedArray typedArray;

    public static class Builder {

        private Context context;
        private String message;
        private int messageId;
        private int themeId;
        private int dotColorId;
        private boolean cancelable = true; // default dialog behaviour
        private OnCancelListener cancelListener;


        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setDotColor(int color) {
            this.dotColorId = color;
            return this;
        }

        public Builder setMessage(@StringRes int messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder setTheme(@StyleRes int themeId) {
            this.themeId = themeId;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        public Builder setCancelListener(OnCancelListener cancelListener) {
            this.cancelListener = cancelListener;
            return this;
        }

        public AlertDialog build() {
            return new SpotsDialog(
                    context,
                    dotColorId,
                    messageId != 0 ? context.getString(messageId) : message,
                    themeId != 0 ? themeId : R.style.SpotsDialogDefault,

                    cancelable,
                    cancelListener
            );
        }
    }

    private static final int DELAY = 150;
    private static final int DURATION = 1500;

    private int size;
    private AnimatedView[] spots;
    private AnimatorPlayer animator;
    private CharSequence message;
    private int dorColorId;

    private SpotsDialog(Context context,int dotColorId, String message, int theme, boolean cancelable, OnCancelListener cancelListener) {
        super(context, theme);
        this.message = message;
        this.dorColorId = dotColorId;

        setCancelable(cancelable);
        if (cancelListener != null) setOnCancelListener(cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dmax_spots_dialog);
        setCanceledOnTouchOutside(false);
        typedArray = getContext().obtainStyledAttributes(R.styleable.Dialog);
        mFillDrawable = typedArray.getResourceId(R.styleable.Dialog_DialogSpotColor,R.drawable.dot_empty);
        initMessage();
        initProgress();
    }

    @Override
    protected void onStart() {
        super.onStart();

        for (AnimatedView view : spots) view.setVisibility(View.VISIBLE);

        animator = new AnimatorPlayer(createAnimations());
        animator.play();
    }

    @Override
    protected void onStop() {
        super.onStop();

        animator.stop();
    }

    @Override
    public void setMessage(CharSequence message) {
        this.message = message;
        if (isShowing()) initMessage();
    }

    //~

    private void initMessage() {
        if (message != null && message.length() > 0) {
            ((TextView) findViewById(R.id.dmax_spots_title)).setText(message);
        }
    }

    private void initProgress() {
        ProgressLayout progress = findViewById(R.id.dmax_spots_progress);
        size = progress.getSpotsCount();

        spots = new AnimatedView[size];
        int size = getContext().getResources().getDimensionPixelSize(R.dimen.spot_size);
        int progressWidth = getContext().getResources().getDimensionPixelSize(R.dimen.progress_width);
        for (int i = 0; i < spots.length; i++) {
            AnimatedView v = new AnimatedView(getContext());
            GradientDrawable shape =  new GradientDrawable();
            shape.setCornerRadius(10);
            shape.setColor(getContext().getResources().getColor(dorColorId));
            v.setBackground(shape);
            v.setTarget(progressWidth);
            v.setXFactor(-1f);
            v.setVisibility(View.INVISIBLE);
            progress.addView(v, size, size);
            spots[i] = v;
        }
    }

    private Animator[] createAnimations() {
        Animator[] animators = new Animator[size];
        for (int i = 0; i < spots.length; i++) {
            final AnimatedView animatedView = spots[i];
            Animator move = ObjectAnimator.ofFloat(animatedView, "xFactor", 0, 1);
            move.setDuration(DURATION);
            move.setInterpolator(new HesitateInterpolator());
            move.setStartDelay(DELAY * i);
            move.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animatedView.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    animatedView.setVisibility(View.VISIBLE);
                }
            });
            animators[i] = move;
        }
        return animators;
    }
}
