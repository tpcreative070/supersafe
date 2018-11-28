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
import com.google.gson.Gson;
import com.snatik.storage.Storage;
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
import co.tpcreative.supersafe.model.ImportFiles;
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
    private Storage storage;
    private final List<ImportFiles> mListImport = new ArrayList<>();
    private int count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_files);
        onDrawOverLay(this);
        storage = new Storage(this);
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
                    Utils.Log(TAG,"Path existing "+ storage.isFileExist(imageUri.getPath()));
                    response = PathUtil.getRealPathFromUri(this,imageUri);
                    if (response==null){
                        response = PathUtil.getFilePathFromURI(this,imageUri);
                    }
                }
                else{
                    response = PathUtil.getPath(this,imageUri);
                    if (response==null){
                        response = PathUtil.getFilePathFromURI(this,imageUri);
                    }
                }

                if (response==null){
                    onStopProgressing();
                    Utils.showGotItSnackbar(imgChecked, R.string.error_occurred, new ServiceManager.ServiceManagerSyncDataListener() {
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
                }else {
                    final File mFile = new File(response);
                    if (mFile.exists()) {
                        final String path = mFile.getAbsolutePath();
                        final String name = mFile.getName();
                        final String fileExtension = Utils.getFileExtension(path);
                        final String mimeType = intent.getType();
                        Log.d(TAG, "file extension " + fileExtension);

                        MimeTypeFile mimeTypeFile = Utils.mediaTypeSupport().get(fileExtension);

                        if (mimeTypeFile == null) {
                            mimeTypeFile = new MimeTypeFile("." + fileExtension, EnumFormatType.FILES, mimeType);
                            mimeTypeFile.name = name;
                        }

                        mimeTypeFile.name = name;
                        count +=1;
                        ImportFiles importFiles = new ImportFiles(mainCategories,mimeTypeFile,path,0,false);
                        mListImport.add(importFiles);
                        ServiceManager.getInstance().setmListImport(mListImport);
                        ServiceManager.getInstance().onImportingFiles();
                    } else {
                        onStopProgressing();
                        finish();
                    }
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
                        if (response==null){
                            response = PathUtil.getFilePathFromURI(this,imageUris.get(i));
                        }
                    }
                    else{
                        response = PathUtil.getPath(this,imageUris.get(i));
                        if (response==null){
                            response = PathUtil.getFilePathFromURI(this,imageUris.get(i));
                        }
                    }
                    if (response!=null){
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
                            count +=1;
                            ImportFiles importFiles = new ImportFiles(mainCategories,mimeTypeFile,path,i,false);
                            mListImport.add(importFiles);
                            Utils.Log(TAG,new Gson().toJson(mimeTypeFile));
                        }
                        else{
                            onStopProgressing();
                            finish();
                        }
                    }
                    else{
                        onStopProgressing();
                        Utils.showGotItSnackbar(imgChecked, R.string.error_occurred, new ServiceManager.ServiceManagerSyncDataListener() {
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
                    }
                }
                ServiceManager.getInstance().setmListImport(mListImport);
                ServiceManager.getInstance().onImportingFiles();
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
    protected void onDestroy() {
        super.onDestroy();
        Utils.onDeleteTemporaryFile();
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
            onStopProgressing();
            onShowUI(View.VISIBLE);
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

    @Override
    public void onCompletedDownload(EnumStatus status) {

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
