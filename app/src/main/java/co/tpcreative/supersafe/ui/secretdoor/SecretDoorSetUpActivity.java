package co.tpcreative.supersafe.ui.secretdoor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;


public class SecretDoorSetUpActivity extends BaseActivity {

    private static final String TAG = SecretDoorSetUpActivity.class.getSimpleName();

    @BindView(R.id.imgLauncher)
    ImageView imgLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret_door_set_up);
        final Drawable droid = ContextCompat.getDrawable(this,R.mipmap.ic_launcher);
        // You don't always need a sequence, and for that there's a single time tap target
        final SpannableString spannedDesc = new SpannableString(getString(R.string.long_press_the_log));
        TapTargetView.showFor(this, TapTarget.forView(imgLauncher, getString(R.string.try_it_now), spannedDesc)
                .cancelable(false)
                .icon(droid)
                .targetCircleColor(R.color.colorButton)
                .titleTextDimen(R.dimen.text_size_title)
                .titleTypeface(Typeface.DEFAULT_BOLD)
                .tintTarget(true), new TapTargetView.Listener() {
            @Override
            public void onTargetClick(TapTargetView view) {
                super.onTargetClick(view);
                // .. which evidently starts the sequence we defined earlier
            }

            @Override
            public void onTargetLongClick(TapTargetView view) {
                super.onTargetLongClick(view);
                onShowDialog();
            }

            @Override
            public void onOuterCircleClick(TapTargetView view) {
                super.onOuterCircleClick(view);
                Toast.makeText(view.getContext(), "You clicked the outer circle!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                Log.d("TapTargetViewSample", "You dismissed me :(");
            }

        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EnumStatus event) {
        switch (event){
            case FINISH:{
                finish();
                break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        onRegisterHomeWatcher();
        //SuperSafeApplication.getInstance().writeKeyHomePressed(SecretDoorSetUpActivity.class.getSimpleName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"OnDestroy");
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {

    }

    public void onShowDialog(){
        MaterialDialog.Builder builder =  new MaterialDialog.Builder(this)
                .title(getString(R.string.enable_secret_door))
                .content(getString(R.string.enable_secret_door_detail))
                .theme(Theme.LIGHT)
                .titleColor(getResources().getColor(R.color.black))
                .inputType(InputType.TYPE_CLASS_TEXT)
                .negativeText(getString(R.string.cancel))
                .positiveText(getString(R.string.ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        PrefsController.putBoolean(getString(R.string.key_secret_door),true);
                        onBackPressed();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        PrefsController.putBoolean(getString(R.string.key_secret_door),false);
                        onBackPressed();
                    }
                });
        builder.show();
    }


}
