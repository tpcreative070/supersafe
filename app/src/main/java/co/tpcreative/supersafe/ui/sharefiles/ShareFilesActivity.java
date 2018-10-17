package co.tpcreative.supersafe.ui.sharefiles;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.GalleryCameraMediaManager;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.util.PathUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.MimeTypeFile;
import dmax.dialog.SpotsDialog;


public class ShareFilesActivity extends BaseActivity implements GalleryCameraMediaManager.AlbumDetailManagerListener{

    private static final String TAG = ShareFilesActivity.class.getSimpleName();
    final List<Integer> mListFile = new ArrayList<>();
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_files);
        onDrawOverLay(this);
        onHandlerIntent();
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
                    Log.d(TAG, "file extension " + Utils.getFileExtension(path));
                    String fileExtension = Utils.getFileExtension(path);
                    final MimeTypeFile mimeTypeFile = Utils.mediaTypeSupport().get(fileExtension);
                    mimeTypeFile.name = name;
                    mListFile.add(0);
                    ServiceManager.getInstance().onSaveDataOnGallery(mimeTypeFile,mListFile, path,mainCategories);
                }
                else{
                    onStopProgressing();
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
                        Log.d(TAG, "file extension " + Utils.getFileExtension(path));
                        String fileExtension = Utils.getFileExtension(path);
                        final MimeTypeFile mimeTypeFile = Utils.mediaTypeSupport().get(fileExtension);
                        mimeTypeFile.name = name;
                        mListFile.add(i);
                        ServiceManager.getInstance().onSaveDataOnGallery(mimeTypeFile,mListFile, path, mainCategories);
                    }
                    else{
                        onStopProgressing();
                    }
                }
            }
            else{
                Utils.Log(TAG,"Nothing to do at multiple items");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        GalleryCameraMediaManager.getInstance().setListener(this);
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
                                .setMessage(getString(R.string.progressing))
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
                        finish();
                    }
                }
            });
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
    }
}
