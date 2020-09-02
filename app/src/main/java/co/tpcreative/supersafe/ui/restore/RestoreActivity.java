package co.tpcreative.supersafe.ui.restore;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import com.google.gson.Gson;
import com.rengwuxian.materialedittext.MaterialEditText;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.listener.Listener;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.services.SuperSafeReceiver;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.ThemeApp;
import dmax.dialog.SpotsDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RestoreActivity extends BaseActivity implements TextView.OnEditorActionListener, BaseView {

    @BindView(R.id.edtPreviousPIN)
    MaterialEditText edtPreviousPIN;
    @BindView(R.id.btnForgotPin)
    Button btnForgotPin;
    @BindView(R.id.btnRestoreNow)
    Button btnRestoreNow;
    @BindView(R.id.tvWrongPin)
    TextView tvWrongPin;
    private AlertDialog dialog;
    private boolean isNext;
    private RestorePresenter presenter;
    private static final String TAG = RestoreActivity.class.getSimpleName();
    private int count = 0;
    private Disposable subscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        edtPreviousPIN.addTextChangedListener(mTextWatcher);
        edtPreviousPIN.setOnEditorActionListener(this);
        presenter = new RestorePresenter();
        presenter.bindView(this);
        presenter.onGetData();
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {

    }

    private void onStartProgressing() {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (dialog == null) {
                        ThemeApp themeApp = ThemeApp.getInstance().getThemeInfo();
                        dialog = new SpotsDialog.Builder()
                                .setContext(RestoreActivity.this)
                                .setDotColor(themeApp.getAccentColor())
                                .setMessage(getString(R.string.progressing))
                                .setCancelable(true)
                                .build();
                    }
                    if (!dialog.isShowing()) {
                        dialog.show();
                        Utils.Log(TAG, "Showing dialog...");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onStopProgressing() {
        Utils.Log(TAG, "onStopProgressing");
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });
        } catch (Exception e) {
            Utils.Log(TAG, e.getMessage());
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (!SuperSafeReceiver.isConnected()) {
                Utils.showDialog(this, getString(R.string.internet));
                return false;
            }
            if (isNext) {
                String pin = SuperSafeApplication.getInstance().readKey();
                if (pin.equals(edtPreviousPIN.getText().toString())) {
                    Utils.hideKeyboard(getCurrentFocus());
                    onStartProgressing();
                    Utils.onObserveData(2000, new Listener() {
                        @Override
                        public void onStart() {
                            onRestore();
                        }
                    });
                } else {
                    edtPreviousPIN.setText("");
                    tvWrongPin.setVisibility(View.VISIBLE);
                    shake();
                    Utils.hideKeyboard(getCurrentFocus());
                    count += 1;
                    if (count >= 4) {
                        btnForgotPin.setVisibility(View.VISIBLE);
                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }

    @OnClick(R.id.btnRestoreNow)
    public void onRestoreNow(View view) {
        String pin = SuperSafeApplication.getInstance().readKey();
        if (pin.equals(edtPreviousPIN.getText().toString())) {
            Utils.hideKeyboard(getCurrentFocus());
            onStartProgressing();
            Utils.onObserveData(2000, new Listener() {
                @Override
                public void onStart() {
                    onRestore();
                }
            });

        } else {
            edtPreviousPIN.setText("");
            tvWrongPin.setVisibility(View.VISIBLE);
            shake();
            Utils.hideKeyboard(getCurrentFocus());
            count += 1;
            if (count >= 4) {
                btnForgotPin.setVisibility(View.VISIBLE);
            }
        }
    }

    @OnClick(R.id.btnForgotPin)
    public void onForgotPIN(View view) {
        Navigator.onMoveToForgotPin(this, true);
    }

    public void onRestore() {
        subscriptions = Observable.create(subscriber -> {
            Utils.onExportAndImportFile(SuperSafeApplication.getInstance().getSupersafeBackup(), SuperSafeApplication.getInstance().getSupersafeDataBaseFolder(), new ServiceManager.ServiceManagerSyncDataListener() {
                @Override
                public void onCompleted() {
                    subscriber.onNext(true);
                    subscriber.onComplete();
                    Utils.Log(TAG, "Exporting successful");
                    Utils.setUserPreShare(presenter.mUser);
                    Navigator.onMoveToMainTab(RestoreActivity.this);
                }
                @Override
                public void onError() {
                    Utils.Log(TAG, "Exporting error");
                    subscriber.onNext(true);
                    subscriber.onComplete();
                }
                @Override
                public void onCancel() {
                    subscriber.onNext(true);
                    subscriber.onComplete();
                }
            });
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(response -> {
                    ServiceManager.getInstance().onStartService();
                    onStopProgressing();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscriptions!=null){
            subscriptions.dispose();
        }
    }

    @Override
    protected void onStopListenerAWhile() {
    }

    /*Detecting textWatch*/

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String value = s.toString().trim();
            if (Utils.isValid(value)) {
                btnRestoreNow.setBackground(getResources().getDrawable(R.drawable.bg_button_rounded));
                btnRestoreNow.setTextColor(getResources().getColor(R.color.white));
                isNext = true;
                tvWrongPin.setVisibility(View.INVISIBLE);
            } else {
                btnRestoreNow.setBackground(getResources().getDrawable(R.drawable.bg_button_disable_rounded));
                btnRestoreNow.setTextColor(getResources().getColor(R.color.colorDisableText));
                isNext = false;
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStartLoading(EnumStatus status) {

    }

    @Override
    public void onStopLoading(EnumStatus status) {

    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onError(String message, EnumStatus status) {

    }

    @Override
    public void onSuccessful(String message) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, Object object) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List list) {

    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    private void shake() {
        ObjectAnimator objectAnimator = new ObjectAnimator().ofFloat(edtPreviousPIN, "translationX",
                0, 25, -25, 25, -25, 15, -15, 6, -6, 0).setDuration(1000);
        objectAnimator.start();
    }

}
