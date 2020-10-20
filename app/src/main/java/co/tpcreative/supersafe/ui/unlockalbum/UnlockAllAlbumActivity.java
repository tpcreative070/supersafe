package co.tpcreative.supersafe.ui.unlockalbum;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment;
import co.tpcreative.supersafe.common.helper.SQLHelper;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.request.VerifyCodeRequest;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.services.SuperSafeReceiver;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import fr.castorflex.android.circularprogressbar.CircularProgressDrawable;

public class UnlockAllAlbumActivity extends BaseActivity implements BaseView,TextView.OnEditorActionListener{

    @BindView(R.id.tvStep1)
    AppCompatTextView tvStep1;
    @BindView(R.id.edtCode)
    AppCompatEditText edtCode;
    @BindView(R.id.btnSendRequest)
    AppCompatButton btnSendRequest;
    @BindView(R.id.btnUnlock)
    AppCompatButton btnUnlock;
    @BindView(R.id.progressbar_circular)
    CircularProgressBar mCircularProgressBar;
    @BindView(R.id.progressbar_circular_unlock_albums)
    CircularProgressBar mCircularProgressBarUnlock;
    @BindView(R.id.tvPremiumDescription)
    AppCompatTextView tvPremiumDescription;
    private UnlockAllAlbumPresenter presenter;
    private boolean isNext;
    private static final String TAG = UnlockAllAlbumActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock_all_album);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        presenter = new UnlockAllAlbumPresenter();
        presenter.bindView(this);
        final User mUser = Utils.getUserInfo();
        if (mUser!=null){
            String email = mUser.email;
            if (email!=null){
                String result = Utils.getFontString(R.string.request_an_access_code,email);
                tvStep1.setText(Html.fromHtml(result));
            }
        }
        edtCode.addTextChangedListener(mTextWatcher);
        edtCode.setOnEditorActionListener(this);
        tvPremiumDescription.setText(getString(R.string.unlock_all_album_title));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EnumStatus event) {
        switch (event){
            case FINISH:{
                Navigator.onMoveToFaceDown(this);
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
        //SuperSafeApplication.getInstance().writeKeyHomePressed(UnlockAllAlbumActivity.class.getSimpleName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"OnDestroy");
        EventBus.getDefault().unregister(this);
        presenter.unbindView();
    }

    @Override
    protected void onStopListenerAWhile() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
    }


    public void setProgressValue(EnumStatus status){
        switch (status) {
            case REQUEST_CODE: {
                CircularProgressDrawable circularProgressDrawable;
                CircularProgressDrawable.Builder b = new CircularProgressDrawable.Builder(this)
                        .colors(getResources().getIntArray(R.array.gplus_colors))
                        .sweepSpeed(2)
                        .rotationSpeed(2)
                        .strokeWidth(Utils.dpToPx(3))
                        .style(CircularProgressDrawable.STYLE_ROUNDED);
                mCircularProgressBar.setIndeterminateDrawable(circularProgressDrawable = b.build());
                // /!\ Terrible hack, do not do this at home!
                circularProgressDrawable.setBounds(0,
                        0,
                        mCircularProgressBar.getWidth(),
                        mCircularProgressBar.getHeight());
                mCircularProgressBar.setVisibility(View.VISIBLE);
                break;
            }
            case UNLOCK_ALBUMS:{
                CircularProgressDrawable circularProgressDrawable;
                CircularProgressDrawable.Builder b = new CircularProgressDrawable.Builder(this)
                        .colors(getResources().getIntArray(R.array.gplus_colors))
                        .sweepSpeed(2)
                        .rotationSpeed(2)
                        .strokeWidth(Utils.dpToPx(3))
                        .style(CircularProgressDrawable.STYLE_ROUNDED);
                mCircularProgressBarUnlock.setIndeterminateDrawable(circularProgressDrawable = b.build());
                // /!\ Terrible hack, do not do this at home!
                circularProgressDrawable.setBounds(0,
                        0,
                        mCircularProgressBarUnlock.getWidth(),
                        mCircularProgressBarUnlock.getHeight());
                mCircularProgressBarUnlock.setVisibility(View.VISIBLE);
                break;
            }
        }

    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String value = s.toString().trim();
            if (Utils.isValid(value)){
                btnUnlock.setBackground(getResources().getDrawable(R.drawable.bg_button_rounded));
                btnUnlock.setTextColor(getResources().getColor(R.color.white));
                btnUnlock.setEnabled(true);
                isNext = true;
            }
            else{
                btnUnlock.setBackground(getResources().getDrawable(R.drawable.bg_button_disable_rounded));
                btnUnlock.setTextColor(getResources().getColor(R.color.colorDisableText));
                btnUnlock.setEnabled(false);
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

    public void onVerifyCode(){
        if (isNext){
            String code = edtCode.getText().toString().trim();
            final VerifyCodeRequest request = new VerifyCodeRequest();
            final User mUser = Utils.getUserInfo();
            if (mUser!=null){
                request.code = code;
                request.user_id = mUser.email;
                request._id =  mUser._id;
                request.device_id = SuperSafeApplication.getInstance().getDeviceId();
                presenter.onVerifyCode(request);
            }
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (!SuperSafeReceiver.isConnected()){
                Utils.showDialog(this,getString(R.string.internet));
                return false;
            }
            return false;
        }
        return false;
    }

    @OnClick(R.id.btnUnlock)
    public void onUnlockAlbums(View view){
        Utils.Log(TAG,"Action");
        if (isNext){
            btnUnlock.setText("");
            onStartLoading(EnumStatus.UNLOCK_ALBUMS);
            Utils.Log(TAG,"onUnlockAlbums");
            btnUnlock.setEnabled(false);
            onVerifyCode();
        }
    }

    @OnClick(R.id.btnSendRequest)
    public void onSentRequest(){
        btnSendRequest.setEnabled(false);
        btnSendRequest.setText("");
        onStartLoading(EnumStatus.REQUEST_CODE);
        final User mUser = Utils.getUserInfo();
        if (mUser!=null){
            if (mUser.email!=null){
                final VerifyCodeRequest request = new VerifyCodeRequest();
                request.user_id = mUser.email;
                presenter.onRequestCode(request);
            }
            else{
                Toast.makeText(this,"Email is null",Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this,"Email is null",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStartLoading(EnumStatus status) {
        setProgressValue(status);
    }

    @Override
    public void onStopLoading(EnumStatus status) {
        switch (status){
            case REQUEST_CODE:{
                if (mCircularProgressBar!=null){
                    mCircularProgressBar.progressiveStop();
                }
                break;
            }
            case UNLOCK_ALBUMS:{
                if (mCircularProgressBarUnlock!=null){
                    mCircularProgressBarUnlock.progressiveStop();
                }
                break;
            }
        }

    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void onError(String message, EnumStatus status) {
        onStopLoading(EnumStatus.REQUEST_CODE);
        onStopLoading(EnumStatus.UNLOCK_ALBUMS);
        switch (status){
            case REQUEST_CODE:{
                btnSendRequest.setText(getString(R.string.send_verification_code));
                btnSendRequest.setEnabled(true);
                btnUnlock.setText(getString(R.string.unlock_all_albums));
                btnUnlock.setEnabled(true);
                Utils.showGotItSnackbar(tvStep1,message);
                break;
            }
            case SEND_EMAIL:{
                Utils.showGotItSnackbar(tvStep1,message);
                break;
            }
            case VERIFY:{
                btnUnlock.setText(getString(R.string.unlock_all_albums));
                Utils.showGotItSnackbar(tvStep1,message);
                break;
            }
        }
    }

    @Override
    public void onSuccessful(String message) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {
        switch (status){
            case REQUEST_CODE:{
                btnSendRequest.setEnabled(false);
                break;
            }
            case SEND_EMAIL:{
                onStopLoading(EnumStatus.REQUEST_CODE);
                btnSendRequest.setText(getString(R.string.send_verification_code));
                Toast.makeText(this,"Sent the code to your email. Please check it",Toast.LENGTH_SHORT).show();
                break;
            }
            case VERIFY:{
                btnUnlock.setText(getString(R.string.unlock_all_albums));
                onStopLoading(EnumStatus.UNLOCK_ALBUMS);
                if (presenter.mListCategories!=null){
                    for (int i = 0;i< presenter.mListCategories.size();i++){
                        presenter.mListCategories.get(i).pin= "";
                        SQLHelper.updateCategory(presenter.mListCategories.get(i));
                    }
                }
                SingletonPrivateFragment.getInstance().onUpdateView();
                Utils.showGotItSnackbar(getCurrentFocus(), R.string.unlocked_successful, new ServiceManager.ServiceManagerSyncDataListener() {
                    @Override
                    public void onCompleted() {
                        finish();
                    }

                    @Override
                    public void onError() {

                    }

                    @Override
                    public void onCancel() {

                    }
                });
                break;
            }
            default:{
                btnSendRequest.setEnabled(true);
                btnUnlock.setEnabled(true);
                break;
            }
        }
    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, Object object) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List list) {

    }
}
