package co.tpcreative.supersafe.ui.sharefiles;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.GalleryCameraMediaManager;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.util.PathUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.MimeTypeFile;
import dmax.dialog.SpotsDialog;


public class ShareFilesActivity extends BaseActivity implements GalleryCameraMediaManager.AlbumDetailManagerListener{

    private static final String TAG = ShareFilesActivity.class.getSimpleName();
    final List<Integer> mListFile = new ArrayList<>();
    private AlertDialog dialog;
    @BindView(R.id.imgChecked)
    ImageView imgChecked;
    @BindView(R.id.btnGotIt)
    Button btnGotIt;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.rlProgress)
    RelativeLayout rlProgress;
    int count =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_files);
        onDrawOverLay(this);
        onHandlerIntent();
        onShowUI(View.GONE);
    }

    void onHandlerIntent(){
        try {
            Intent intent = getIntent();
            String action = intent.getAction();
            String type = intent.getType();
            Utils.Log(TAG,"original type :"+ type);
            if (Intent.ACTION_SEND.equals(action) && type != null ) {
                handleSendSingleItem(intent,EnumFormatType.FILES);
            } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
                handleSendMultipleFiles(intent,EnumFormatType.FILES);
            } else {
                Utils.Log(TAG,"Sending items is not existing");
            }
        }
        catch (Exception e){
            finish();
            e.printStackTrace();
        }
    }

    void handleSendSingleItem(Intent intent,EnumFormatType enumFormatType) {
        try {
            Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            final List<MainCategories> list = MainCategories.getInstance().getList();
            final MainCategories mainCategories  = list.get(0);
            if (imageUri != null && mainCategories!=null) {
                mListFile.clear();
                onStartProgressing();
                String response = "";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    Utils.Log(TAG,"value path :"+ imageUri.getPath());
                    response = PathUtil.getRealPathFromUri(this,imageUri);
                }
                else{
                    response = PathUtil.getPath(this,imageUri);
                }
                final File mFile = new File(response);
                if (mFile.exists()){
                    final String path = mFile.getAbsolutePath();
                    final String name = mFile.getName();
                    final String fileExtension = Utils.getFileExtension(path);
                    final String mimeType = intent.getType();
                    Log.d(TAG, "file extension " + fileExtension);

                    MimeTypeFile mimeTypeFile = Utils.mediaTypeSupport().get(fileExtension);

                    if (mimeTypeFile==null){
                        mimeTypeFile = new MimeTypeFile("."+fileExtension,EnumFormatType.FILES,mimeType);
                        mimeTypeFile.name = name;
                    }

                    mimeTypeFile.name = name;
                    mListFile.add(0);
                    count = 1;
                    ServiceManager.getInstance().onSaveDataOnGallery(mimeTypeFile,mListFile, path,mainCategories);

                }
                else{
                    onStopProgressing();
                    finish();
                }
            }
            else {
                Utils.Log(TAG,"Nothing to do at single item");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    void handleSendMultipleFiles(Intent intent, EnumFormatType enumFormatType) {
        try {
            ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            final List<MainCategories> list = MainCategories.getInstance().getList();
            final MainCategories mainCategories  = list.get(0);
            if (imageUris != null) {
                mListFile.clear();
                onStartProgressing();
                for (int i = 0 ;i<imageUris.size();i++){
                    String response = "";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        response = PathUtil.getRealPathFromUri(this,imageUris.get(i));
                    }
                    else{
                        response = PathUtil.getPath(this,imageUris.get(i));
                    }
                    final File mFile = new File(response);
                    if (mFile.exists()){
                        final String path = mFile.getAbsolutePath();
                        final String name = mFile.getName();
                        final String mimeType = intent.getType();
                        String fileExtension = Utils.getFileExtension(path);
                        Log.d(TAG, "file extension " + fileExtension);


                        MimeTypeFile mimeTypeFile = Utils.mediaTypeSupport().get(fileExtension);
                        if (mimeTypeFile==null){
                            mimeTypeFile = new MimeTypeFile("."+fileExtension,EnumFormatType.FILES,mimeType);
                            mimeTypeFile.name = name;
                        }
                        mimeTypeFile.name = name;
                        mListFile.add(i);
                        count = imageUris.size();
                        ServiceManager.getInstance().onSaveDataOnGallery(mimeTypeFile,mListFile, path, mainCategories);
                    }
                    else{
                        onStopProgressing();
                        finish();
                    }
                }
            }
            else{
                finish();
                Utils.Log(TAG,"Nothing to do at multiple items");
            }
        }
        catch (Exception e){
            finish();
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        GalleryCameraMediaManager.getInstance().setListener(this);
    }

    @Override
    public void onNotifier(EnumStatus status) {

    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {

    }

    @Override
    public void onUpdatedView() {

    }

    @Override
    public void onStartProgress() {

    }

    @Override
    public void onStopProgress() {
        try {
            if (mListFile.size()==0){
                onStopProgressing();
                onShowUI(View.VISIBLE);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void onStartProgressing(){
        try{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (dialog==null){
                        dialog = new SpotsDialog.Builder()
                                .setContext(ShareFilesActivity.this)
                                .setMessage(getString(R.string.importing))
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
        Utils.Log(TAG,"onStopProgressing");
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

    public void onShowUI(int res){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvTitle.setVisibility(res);
                imgChecked.setVisibility(res);
                btnGotIt.setVisibility(res);
                rlProgress.setVisibility(res);
                tvTitle.setText(String.format(getString(R.string.imported_file_successful),""+count));
            }
        });
    }

    @OnClick(R.id.btnGotIt)
    public void onClickedGotIt(View view){
        finish();
    }
}
