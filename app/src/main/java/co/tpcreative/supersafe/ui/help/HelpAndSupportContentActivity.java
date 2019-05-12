package co.tpcreative.supersafe.ui.help;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.gson.Gson;
import com.rengwuxian.materialedittext.MaterialEditText;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.List;
import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeReceiver;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.common.views.AdvancedWebView;
import co.tpcreative.supersafe.model.EmailToken;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;
import dmax.dialog.SpotsDialog;

public class HelpAndSupportContentActivity extends BaseActivity implements BaseView ,TextView.OnEditorActionListener{

    private static final String TAG = HelpAndSupportContentActivity.class.getSimpleName();
    private HelpAndSupportPresenter presenter;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.webview)
    AdvancedWebView webview;
    @BindView(R.id.llEmail)
    LinearLayout llEmail;
    @BindView(R.id.tvEmail)
    TextView tvEmail;
    @BindView(R.id.edtSupport)
    MaterialEditText edtSupport;
    private boolean isNext ;
    private User mUser;
    private AlertDialog dialog;
    private MenuItem menuItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_and_support_content);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        onDrawOverLay(this);
        presenter = new HelpAndSupportPresenter();
        presenter.bindView(this);
        presenter.onGetDataIntent(this);

        mUser = User.getInstance().getUserInfo();
        tvEmail.setText(mUser.email);

        edtSupport.addTextChangedListener(mTextWatcher);
        edtSupport.setOnEditorActionListener(this);

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
        Utils.hideSoftKeyboard(this);
        onRegisterHomeWatcher();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"OnDestroy");
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
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
        switch (status){
            case SEND_EMAIL:{
                onStopProgressing();
                Utils.showGotItSnackbar(getCurrentFocus(),R.string.send_email_failed);
                edtSupport.setText("");
                break;
            }
        }
    }

    @Override
    public void onSuccessful(String message) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {
        Utils.Log(TAG,new Gson().toJson(presenter.content));
        switch (status){
            case RELOAD:{
                if (presenter.content.content.equals(getString(R.string.contact_support_content))){
                    llEmail.setVisibility(View.VISIBLE);
                    edtSupport.setVisibility(View.VISIBLE);
                    webview.setVisibility(View.GONE);
                }
                else{
                    tvTitle.setText(presenter.content.title);
                    llEmail.setVisibility(View.GONE);
                    edtSupport.setVisibility(View.GONE);
                    webview.setVisibility(View.VISIBLE);
                    webview.loadUrl(presenter.content.content);
                }
                break;
            }
            case SEND_EMAIL:{
                onStopProgressing();
                Utils.showInfoSnackbar(getCurrentFocus(),R.string.thank_you,true);
                edtSupport.setText("");
                break;
            }
        }
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


    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_ACTION_DONE) {
            if (!SuperSafeReceiver.isConnected()){
                Utils.showDialog(this,getString(R.string.internet));
                return false;
            }
            if (isNext){
                onStartProgressing();
                String content = edtSupport.getText().toString();

                final EmailToken emailToken = EmailToken.getInstance().convertTextObject(mUser,content);
                presenter.onSendMail(emailToken,content);
                Utils.hideKeyboard(edtSupport);
                return true;
            }
            return false;
        }
        return false;
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String value = s.toString().trim();
            if (Utils.isValid(value)){
                isNext = true;
            }
            else{
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_help_support, menu);
        menuItem = menu.findItem(R.id.menu_item_send);
        if (presenter!=null){
            if (presenter.content.content.equals(getString(R.string.contact_support_content))){
                menuItem.setVisible(true);
            }
            else{
                menuItem.setVisible(false);
            }
        }

        Utils.Log(TAG,"Menu.............");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_send:{
                if (isNext){
                    String content = edtSupport.getText().toString();
                    final EmailToken emailToken = EmailToken.getInstance().convertTextObject(mUser,content);
                    presenter.onSendMail(emailToken,content);
                    onStartProgressing();
                    Utils.hideKeyboard(edtSupport);
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void onStartProgressing(){
        try{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (dialog==null){
                        dialog = new SpotsDialog.Builder()
                                .setContext(HelpAndSupportContentActivity.this)
                                .setMessage(getString(R.string.Sending))
                                .setCancelable(true)
                                .build();
                    }
                    if (!dialog.isShowing()){
                        dialog.show();
                        Utils.Log(TAG,"Showing dialog...");
                    }
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void onStopProgressing(){
        try{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (dialog!=null){
                        dialog.dismiss();
                    }
                }
            });
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
    }
}
