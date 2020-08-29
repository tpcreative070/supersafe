package co.tpcreative.supersafe.common.util;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.room.Ignore;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.util.Base64;
import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.snatik.storage.Storage;
import com.snatik.storage.helpers.OnStorageListener;
import com.snatik.storage.helpers.SizeUnit;
import com.snatik.storage.security.SecurityUtil;

import org.apache.commons.io.FilenameUtils;
import org.greenrobot.eventbus.EventBus;
import org.solovyev.android.checkout.Purchase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import co.tpcreative.supersafe.BuildConfig;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.SingletonManager;
import co.tpcreative.supersafe.common.entities.InstanceGenerator;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;
import co.tpcreative.supersafe.common.helper.SQLHelper;
import co.tpcreative.supersafe.common.listener.Listener;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.model.EnumFileType;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.ImportFilesModel;
import co.tpcreative.supersafe.model.ItemModel;
import co.tpcreative.supersafe.model.MainCategoryModel;
import co.tpcreative.supersafe.model.MimeTypeFile;
import co.tpcreative.supersafe.model.SyncItemModel;
import co.tpcreative.supersafe.model.ThemeApp;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.ui.lockscreen.EnterPinActivity;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by pc on 07/16/2017.
 */
public class Utils {
    // utility function
    public  static String  FORMAT_TIME = "yyyy-MM-dd HH:mm:ss";
    public static String FORMAT_TIME_FILE_NAME = "yyyyMMdd_HHmmss";
    final public static int COUNT_RATE = 9;
    final public static long START_TIMER = 5000;
    private  static  Storage storage = new Storage(SuperSafeApplication.getInstance());
    private static final String TAG = Utils.class.getSimpleName();
    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public static boolean isValid(CharSequence target) {
        return (!TextUtils.isEmpty(target));
    }

    public static void showDialog(Activity activity,String message){
        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity);
        builder.title(R.string.confirm);
        builder.content(message);
        builder.positiveText(R.string.ok);
        builder .show();
    }

    public static void showDialog(Activity activity, String message, ServiceManager.ServiceManagerSyncDataListener ls){
        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity);
        builder.title(R.string.confirm);
        builder.content(message);
        builder.positiveText(R.string.ok);
        builder.negativeText(R.string.cancel);
        builder.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                ls.onCancel();
            }
        });
        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                ls.onCompleted();
            }
        });
        builder .show();
    }

    public static boolean mCreateAndSaveFileOverride(String fileName,String path_folder_name,String responseJson, boolean append) {
        Utils.Log(TAG,"path "+ path_folder_name);
        final String newLine = System.getProperty("line.separator");
        try{
            File root = new File(path_folder_name+ "/" + fileName);
            double saved  = storage.getSize(root,SizeUnit.MB);
            if (saved>=1){
                storage.deleteFile(root.getAbsolutePath());
            }
            if (!root.exists()){
                File parentFolder = new File(path_folder_name);
                if (!parentFolder.exists()) {
                    parentFolder.mkdirs();
                }
                root.createNewFile();
            }

            FileWriter file = new FileWriter(root,append);
            file.write("\r\n");
            file.write(responseJson);
            file.write("\r\n");

            file.flush();
            file.close();
            return true ;
        } catch (IOException e) {
            Utils.Log(TAG,e.getMessage());
            return false ;
        }
    }

    public static void hideSoftKeyboard(Activity context) {
        View view = context.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void hideKeyboard(View view) {
        // Check if no view has focus:
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) SuperSafeApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static  int dpToPx(int dp) {
        Resources r = SuperSafeApplication.getInstance().getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, r.getDisplayMetrics());
        return px;
    }

    public static void showKeyboard(View view) {
        // Check if no view has focus:
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager)  SuperSafeApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static File getPackagePath(Context context){
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                ".temporary.jpg");
        return file;
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static String getFileExtension(String url){
        String fileExt = FilenameUtils.getExtension(url).toLowerCase();
        return fileExt;
    }

    public static void Log(final String TAG,final String message){
        if (BuildConfig.DEBUG){
            Log.d(TAG,message);
        }
    }

    public static String getUUId(){
        try {
            return UUID.randomUUID().toString();
        }
        catch (Exception e){
            return ""+System.currentTimeMillis();
        }
    }

    public static String getCurrentDateTime() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String result = dateFormat.format(date);
        return result;
    }

    public static String getCurrentDateTime(String formatName) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(formatName, Locale.getDefault());
        String result = dateFormat.format(date);
        return result;
    }

    public static String getCurrentDate(String value) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        try {
            Date mDate = sdf.parse(value);
            SimpleDateFormat dateFormat = new SimpleDateFormat("EE dd MMM, yyyy", Locale.getDefault());
            String result = dateFormat.format(mDate);
            return result;
        } catch (ParseException e) {
            e.printStackTrace();
        }
      return "";
    }

    public static String getCurrentDateTimeFormat() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String result = dateFormat.format(date);
        return result;
    }

    public static String getHexCode(String value){
        return Base64.encodeBase64String(value.toUpperCase().getBytes(Charsets.UTF_8));
    }

    public static void onExportAndImportFile(String input,String output,ServiceManager.ServiceManagerSyncDataListener ls){
        final Storage storage = new Storage(SuperSafeApplication.getInstance());
        final List<File> mFile = storage.getFiles(input);
        try {
        for (File index : mFile){
            if (storage.isFileExist(index.getAbsolutePath())){
                storage.createFile(new File(output+index.getName()), new File(index.getAbsolutePath()), new OnStorageListener() {
                    @Override
                    public void onSuccessful() {

                    }
                    @Override
                    public void onFailed() {
                        ls.onError();
                    }

                    @Override
                    public void onSuccessful(String path) {
                    }
                    @Override
                    public void onSuccessful(int position) {

                    }
                });
            }
        }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            ls.onCompleted();
        }
    }

    private Utils() {
        throw new AssertionError();
    }

    public static void showToast(Context context, @StringRes int text, boolean isLong) {
        showToast(context, context.getString(text), isLong);
    }

    public static void showToast(Context context, String text, boolean isLong) {
        Toast.makeText(context, text, isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    public static void showInfoSnackbar(final View view, final @StringRes int text, final boolean isLong) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                multilineSnackbar(
                        Snackbar.make(
                                view, text,
                                isLong ? BaseTransientBottomBar.LENGTH_LONG : BaseTransientBottomBar.LENGTH_SHORT)
                ).show();
            }
        }, 100);
    }

    public static void showGotItSnackbar(final View view, final @StringRes int text) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                multilineSnackbar(
                        Snackbar.make(
                                view, text, BaseTransientBottomBar.LENGTH_INDEFINITE)
                                .setAction(R.string.got_it, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                })
                ).show();
            }
        }, 200);
    }

    public static void showGotItSnackbar(final View view, final @StringRes String text) {
        onObserveData(START_TIMER, new Listener() {
            @Override
            public void onStart() {
                multilineSnackbar(
                        Snackbar.make(
                                view, text, BaseTransientBottomBar.LENGTH_INDEFINITE)
                                .setAction(R.string.got_it, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                })
                ).show();
            }
        });
    }

    public static void showGotItSnackbar(final View view, final @StringRes int text, ServiceManager.ServiceManagerSyncDataListener ls) {
        onObserveData(START_TIMER, new Listener() {
            @Override
            public void onStart() {
                multilineSnackbar(
                        Snackbar.make(
                                view, text, BaseTransientBottomBar.LENGTH_INDEFINITE)
                                .setAction(R.string.got_it, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        ls.onCompleted();
                                    }
                                })
                ).show();
            }
        });
    }

    private static Snackbar multilineSnackbar(Snackbar snackbar) {
        TextView textView = (TextView) snackbar.getView().findViewById(R.id.snackbar_text);
        textView.setMaxLines(5);
        return snackbar;
    }

    public static void slideToRight(View view){
        TranslateAnimation animate = new TranslateAnimation(0,view.getWidth(),0,0);
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }
    // To animate view slide out from right to left
    public static void slideToLeft(View view){
        TranslateAnimation animate = new TranslateAnimation(0,-view.getWidth(),0,0);
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }

    // To animate view slide out from top to bottom
    public static void slideToBottomHeader(View view){
        TranslateAnimation animate = new TranslateAnimation(0,0,-view.getHeight(), 0);
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // To animate view slide out from bottom to top
    public static void slideToTopHeader(View view){
        Utils.Log(TAG," "+ view.getHeight());
        TranslateAnimation animate = new TranslateAnimation(0,0,0,-view.getHeight());
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // To animate view slide out from top to bottom
    public static void slideToBottomFooter(View view){
        TranslateAnimation animate = new TranslateAnimation(0,0,0, view.getHeight());
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // To animate view slide out from bottom to top
    public static void slideToTopFooter(View view){
        Utils.Log(TAG," "+ view.getHeight());
        TranslateAnimation animate = new TranslateAnimation(0,0,view.getHeight(),0);
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    public static String stringToHex(String content){
        return Base64.encodeBase64String(content.getBytes(Charsets.UTF_8));
    }

    public static String hexToString(String hex){
        try {
            byte[] data= Base64.decodeBase64(hex.getBytes());
            return new String(data,Charsets.UTF_8);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static HashMap<String,MimeTypeFile> mediaTypeSupport(){
        HashMap<String,MimeTypeFile> hashMap = new HashMap<>();
        hashMap.put("mp4",new MimeTypeFile(".mp4", EnumFormatType.VIDEO,"video/mp4"));
        hashMap.put("3gp",new MimeTypeFile(".3gp", EnumFormatType.VIDEO,"video/3gp"));
        hashMap.put("wmv",new MimeTypeFile(".wmv", EnumFormatType.VIDEO,"video/wmv"));
        hashMap.put("mkv",new MimeTypeFile(".mkv", EnumFormatType.VIDEO,"video/mkv"));
        hashMap.put("m4a",new MimeTypeFile(".m4a", EnumFormatType.AUDIO,"audio/m4a"));
        hashMap.put("aac",new MimeTypeFile(".aac", EnumFormatType.AUDIO,"audio/aac"));
        hashMap.put("mp3",new MimeTypeFile(".mp3", EnumFormatType.AUDIO,"audio/mp3"));
        hashMap.put("wav",new MimeTypeFile(".wav", EnumFormatType.AUDIO,"audio/wav"));
        hashMap.put("jpg",new MimeTypeFile(".jpg", EnumFormatType.IMAGE,"image/jpeg"));
        hashMap.put("jpeg",new MimeTypeFile(".jpeg", EnumFormatType.IMAGE,"image/jpeg"));
        hashMap.put("png",new MimeTypeFile(".png", EnumFormatType.IMAGE,"image/png"));
        hashMap.put("gif",new MimeTypeFile(".gif", EnumFormatType.IMAGE,"image/gif"));
        return hashMap;
    }

    public static HashMap<String,MimeTypeFile> mimeTypeSupport(){
        HashMap<String,MimeTypeFile> hashMap = new HashMap<>();
        hashMap.put("video/mp4",new MimeTypeFile(".mp4", EnumFormatType.VIDEO,"video/mp4"));
        hashMap.put("video/3gp",new MimeTypeFile(".3gp", EnumFormatType.VIDEO,"video/3gp"));
        hashMap.put("video/wmv",new MimeTypeFile(".wmv", EnumFormatType.VIDEO,"video/wmv"));
        hashMap.put("video/mkv",new MimeTypeFile(".mkv", EnumFormatType.VIDEO,"video/mkv"));
        hashMap.put("audio/m4a",new MimeTypeFile(".m4a", EnumFormatType.AUDIO,"audio/m4a"));
        hashMap.put("audio/aac",new MimeTypeFile(".aac", EnumFormatType.AUDIO,"audio/aac"));
        hashMap.put("audio/mp3",new MimeTypeFile(".mp3", EnumFormatType.AUDIO,"audio/mp3"));
        hashMap.put("audio/mpeg",new MimeTypeFile(".mp3", EnumFormatType.AUDIO,"audio/mpeg"));
        hashMap.put("audio/wav",new MimeTypeFile(".wav", EnumFormatType.AUDIO,"audio/wav"));
        hashMap.put("image/jpeg",new MimeTypeFile(".jpg", EnumFormatType.IMAGE,"image/jpeg"));
        hashMap.put("image/png",new MimeTypeFile(".png", EnumFormatType.IMAGE,"image/png"));
        hashMap.put("image/gif",new MimeTypeFile(".gif", EnumFormatType.IMAGE,"image/gif"));

        hashMap.put("application/msword",new MimeTypeFile(".doc", EnumFormatType.FILES,"application/msword"));
        hashMap.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document",new MimeTypeFile(".docx", EnumFormatType.FILES,"application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        hashMap.put("application/vnd.openxmlformats-officedocument.wordprocessingml.template",new MimeTypeFile(".dotx", EnumFormatType.FILES,"application/vnd.openxmlformats-officedocument.wordprocessingml.template"));
        hashMap.put("application/vnd.ms-word.document.macroEnabled.12",new MimeTypeFile(".dotm", EnumFormatType.FILES,"application/vnd.ms-word.document.macroEnabled.12"));
        hashMap.put("application/vnd.ms-excel",new MimeTypeFile(".xls", EnumFormatType.FILES,"application/vnd.ms-excel"));
        hashMap.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",new MimeTypeFile(".xlsx", EnumFormatType.FILES,"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        hashMap.put("application/vnd.openxmlformats-officedocument.spreadsheetml.template",new MimeTypeFile(".xltx", EnumFormatType.FILES,"application/vnd.openxmlformats-officedocument.spreadsheetml.template"));
        hashMap.put("application/vnd.ms-excel.sheet.macroEnabled.12",new MimeTypeFile(".xlsm", EnumFormatType.FILES,"application/vnd.ms-excel.sheet.macroEnabled.12"));
        hashMap.put("application/vnd.ms-excel.template.macroEnabled.12",new MimeTypeFile(".xltm", EnumFormatType.FILES,"application/vnd.ms-excel.template.macroEnabled.12"));
        hashMap.put("application/vnd.ms-excel.addin.macroEnabled.12",new MimeTypeFile(".xlam", EnumFormatType.FILES,"application/vnd.ms-excel.addin.macroEnabled.12"));
        hashMap.put("application/vnd.ms-excel.sheet.binary.macroEnabled.12",new MimeTypeFile(".xlsb", EnumFormatType.FILES,"application/vnd.ms-excel.sheet.binary.macroEnabled.12"));
        hashMap.put("application/vnd.ms-powerpoint",new MimeTypeFile(".ppt", EnumFormatType.FILES,"application/vnd.ms-powerpoint"));
        hashMap.put("application/vnd.openxmlformats-officedocument.presentationml.presentation",new MimeTypeFile(".pptx", EnumFormatType.FILES,"application/vnd.openxmlformats-officedocument.presentationml.presentation"));
        hashMap.put("application/vnd.openxmlformats-officedocument.presentationml.template",new MimeTypeFile(".potx", EnumFormatType.FILES,"application/vnd.openxmlformats-officedocument.presentationml.template"));
        hashMap.put("application/vnd.ms-powerpoint.addin.macroEnabled.12",new MimeTypeFile(".ppsx", EnumFormatType.FILES,"application/vnd.ms-powerpoint.addin.macroEnabled.12"));


        hashMap.put("application/vnd.ms-powerpoint.presentation.macroEnabled.12t",new MimeTypeFile(".pptm", EnumFormatType.FILES,"application/vnd.ms-powerpoint.presentation.macroEnabled.12"));
        hashMap.put("application/vnd.ms-powerpoint.template.macroEnabled.12",new MimeTypeFile(".potm", EnumFormatType.FILES,"application/vnd.ms-powerpoint.template.macroEnabled.12"));
        hashMap.put("application/vnd.ms-powerpoint.slideshow.macroEnabled.12",new MimeTypeFile(".ppsm", EnumFormatType.FILES,"application/vnd.ms-powerpoint.slideshow.macroEnabled.12"));
        hashMap.put("application/vnd.ms-access",new MimeTypeFile(".mdb", EnumFormatType.FILES,"application/vnd.ms-access"));


        return hashMap;
    }

    public static String DeviceInfo(){
        try {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            int version = Build.VERSION.SDK_INT;
            String versionRelease = Build.VERSION.RELEASE;
            return "manufacturer " + manufacturer
                    + " \n model " + model
                    + " \n version " + version
                    + " \n versionRelease " + versionRelease
                    + " \n app version name "+ BuildConfig.VERSION_NAME;

        }
        catch (Exception e){
            onWriteLog(e.getMessage(),EnumStatus.DEVICE_ABOUT);
        }
        return "Exception";
    }

    public static void onWriteLog(String message, EnumStatus status) {
        if (!BuildConfig.DEBUG){
            return;
        }
        if (status==null){
            Utils.mCreateAndSaveFileOverride("log.txt", SuperSafeApplication.getInstance().getSupersafeLog(), "----Time----" + Utils.getCurrentDateTimeFormat() +" ----Content--- :" + message, true);
        }
        else{
            Utils.mCreateAndSaveFileOverride("log.txt", SuperSafeApplication.getInstance().getSupersafeLog(), "----Time----" + Utils.getCurrentDateTimeFormat() + " ----Status---- :" + status.name() + " ----Content--- :" + message, true);
        }
    }

    private static void appendLog(String text) {
        File logFile = new File(SuperSafeApplication.getInstance().getFileLogs());
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text+"\n");
            buf.newLine();
            buf.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void onWriteLog(EnumStatus action,EnumStatus status,String value){
        if (!BuildConfig.DEBUG){
            return;
        }
        onCheck();
        appendLog("Version "+BuildConfig.VERSION_NAME+" ; created date time :"+Utils.getCurrentDateTime(Utils.FORMAT_TIME)+" ; Action :"+action.name() +" ; Status: "+status.name() + " ; message log: " +value);
    }

    public static void onCheck(){
        File file = new File(SuperSafeApplication.getInstance().getInstance().getFileLogs());
        if (file.exists()){
            long mSize = (long) +SuperSafeApplication.getInstance().getInstance().getStorage().getSize(file, SizeUnit.MB);
            if (mSize>2){
                SuperSafeApplication.getInstance().getStorage().deleteFile(file.getAbsolutePath());
            }
        }
    }

    public static void shareMultiple(List<File> files, Activity context){
        ArrayList<Uri> uris = new ArrayList<>();
        for(File file: files){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
                uris.add(uri);
            }
            else{
                uris.add(Uri.fromFile(file));
            }
        }
        final Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("*/*");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        context.startActivityForResult(Intent.createChooser(intent, context.getString(R.string.share)),Navigator.SHARE);
    }

    private static Point getScreenSize(Context activity) {
        Display display = ((Activity) activity).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static int getScreenWidth(Context activity) {
        return getScreenSize(activity).x;
    }

    public static int getScreenHeight(Context activity) {
        return getScreenSize(activity).y;
    }

    public static boolean isSensorAvailable() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return ActivityCompat.checkSelfPermission(SuperSafeApplication.getInstance(), Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED &&
                        SuperSafeApplication.getInstance().getSystemService(FingerprintManager.class).isHardwareDetected();
            } else {
                return FingerprintManagerCompat.from(SuperSafeApplication.getInstance()).isHardwareDetected();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static String getFontString(final int content,String value){
        ThemeApp themeApp = ThemeApp.getInstance().getThemeInfo();
        String sourceString = SuperSafeApplication.getInstance().getString(content, "<font color='"+ themeApp.getAccentColorHex()+"'>"+"<b>" + value +"</b>"+"</font>");
        return sourceString;
    }

    public static String getFontString(final int content,String value,int fontSize){
        ThemeApp themeApp = ThemeApp.getInstance().getThemeInfo();
        String sourceString = SuperSafeApplication.getInstance().getString(content, "<font size='"+fontSize+"' color='"+ themeApp.getAccentColorHex()+"'>"+"<b>" + value +"</b>"+"</font>");
        return sourceString;
    }

    public static boolean appInstalledOrNot(String uri) {
        PackageManager pm = SuperSafeApplication.getInstance().getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return false;
    }

    public static Map<String,Object> objectToHashMap(final Purchase items){
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> myMap = new Gson().fromJson(new Gson().toJson(items), type);
        return myMap;
    }

    public static void onDeleteTemporaryFile(){
        try {
            File rootDataDir = SuperSafeApplication.getInstance().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File []list = rootDataDir.listFiles();
            for (int i = 0; i< list.length;i++){
                Utils.Log(TAG,"File list :"+list[i].getAbsolutePath());
                SuperSafeApplication.getInstance().getStorage().deleteFile(list[i].getAbsolutePath());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean isLandscape(AppCompatActivity activity){
        boolean landscape;
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        int height = displaymetrics.heightPixels;

        if(width<height){
            landscape = false;
        }
        else{
            landscape = true;
        }
        return landscape;
    }

    /**
     * @return Number of bytes available on External storage
     */
    public static long getAvailableSpaceInBytes() {
        long availableSpace = -1L;
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();

        return availableSpace;
    }

    public static void writePinToSharedPreferences(String pin) {
        //PrefsController.putString(getString(R.string.key_pin),Utils.sha256(pin));
        SuperSafeApplication.getInstance().writeKey(pin);
    }

    public static String getPinFromSharedPreferences() {
        //PrefsController.getString(getString(R.string.key_pin), "");
        return SuperSafeApplication.getInstance().readKey();
    }

    public static void writeFakePinToSharedPreferences(String pin) {
        //PrefsController.putString(getString(R.string.key_pin),Utils.sha256(pin));
        SuperSafeApplication.getInstance().writeFakeKey(pin);
    }

    public static String getFakePinFromSharedPreferences() {
        //PrefsController.getString(getString(R.string.key_pin), "");
        return SuperSafeApplication.getInstance().readFakeKey();
    }

    public static boolean  isEnabledFakePin(){
       final boolean result =  PrefsController.getBoolean(SuperSafeApplication.getInstance().getString(R.string.key_fake_pin), false);
       return result;
    }

    public static boolean isExistingFakePin(String pin,String currentPin) {
        final String value = currentPin;
        if (pin.equals(value)) {
            return true;
        }
        return false;
    }

    public static boolean isExistingRealPin(String pin,String currentPin) {
        final String value = currentPin;
        if (pin.equals(value)) {
            return true;
        }
        return false;
    }

    public static void onCheckNewVersion(){
        final int current_code_version = PrefsController.getInt(SuperSafeApplication.getInstance().getString(R.string.current_code_version),0);
        if (current_code_version == BuildConfig.VERSION_CODE){
            Utils.Log(TAG,"Already install this version");
            return ;
        }
        else{
            PrefsController.putInt(SuperSafeApplication.getInstance().getString(R.string.current_code_version),BuildConfig.VERSION_CODE);
            PrefsController.putBoolean(SuperSafeApplication.getInstance().getString(R.string.we_are_a_team),false);
            Utils.Log(TAG,"New install this version");
        }
    }

    public static void onUpdatedCountRate(){
        int count = PrefsController.getInt(SuperSafeApplication.getInstance().getString(R.string.key_count_to_rate),0);
        if(count>999){
            PrefsController.putInt(SuperSafeApplication.getInstance().getString(R.string.key_count_to_rate),0);
        }else{
            PrefsController.putInt(SuperSafeApplication.getInstance().getString(R.string.key_count_to_rate),count+1);
        }
    }

    public static void onObserveData(long second, Listener ls){
        Completable.timer(second, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }
                    @Override
                    public void onComplete() {
                        Utils.Log(TAG,"Completed");
                        ls.onStart();
                    }
                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    public static void onHomePressed(){
        PrefsController.putInt(SuperSafeApplication.getInstance().getString(R.string.key_screen_status),EnumPinAction.SCREEN_LOCK.ordinal());
        Utils.Log(TAG,"Pressed home button");
        if (!SingletonManager.getInstance().isVisitLockScreen()){
            Navigator.onMoveToVerifyPin(SuperSafeApplication.getInstance().getActivity(),EnumPinAction.NONE);
            SingletonManager.getInstance().setVisitLockScreen(true);
            Utils.Log(TAG,"Verify pin");
        }else{
            Utils.Log(TAG,"Verify pin already");
        }
    }

    public static User getUserInfo(){
        try{
            String value = PrefsController.getString(SuperSafeApplication.getInstance().getString(R.string.key_user),null);
            if (value!=null){
                final User mUser = new Gson().fromJson(value,User.class);
                if (mUser!=null){
                    return mUser;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String getUserId(){
        try{
            String value = PrefsController.getString(SuperSafeApplication.getInstance().getString(R.string.key_user),null);
            if (value!=null){
                final User mUser = new Gson().fromJson(value,User.class);
                if (mUser!=null){
                    return mUser.email;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /*Checking allow sync data*/
    public static boolean isAllowSyncData(){
        return User.getInstance().isAllowRequestDriveApis();
    }


    public static boolean isCheckSyncSuggestion(){
        String name = SuperSafeApplication.getInstance().getString(R.string.key_count_sync);
        final int mCount = PrefsController.getInt(name,0);
        final boolean mSynced = getUserInfo().driveConnected;
        if (!mSynced){
            if (mCount==5) {
                PrefsController.putInt(name,0);
                return true;
            }else{
                PrefsController.putInt(name,mCount+1);
            }
        }
        return false;
    }

    public static String getAccessToken() {
        try {
            String value = PrefsController.getString(SuperSafeApplication.getInstance().getString(R.string.key_user), "");
            User user = new Gson().fromJson(value, User.class);
            if (user != null) {
               return user.author.session_token;
            }
            else{
                 return user.author.session_token;
            }
        } catch (Exception e) {
        }
        return SecurityUtil.DEFAULT_TOKEN;
    }

    public static void onPushEventBus(EnumStatus status){
        EventBus.getDefault().post(status);
    }

    /*Improved sync data*/
    /*Filter only item already synced*/
    public static List<ItemModel> filterOnlyGlobalOriginalId(List<ItemModel> list1) {
        List<ItemModel> mList = new ArrayList<>();
        for (ItemModel index : list1) {
            if (index.isSyncCloud){
                mList.add(index);
            }
        }
        return mList;
    }

    /*Remove duplicated item for download id*/
    public static List<ItemModel> clearListFromDuplicate(List<ItemModel> globalList,List<ItemModel> localList) {
        Map<String,ItemModel> modelMap = new HashMap<>();
        List<ItemModel>mList = new ArrayList<>();
        if (globalList!=null){
            if (globalList.size()==0){
                return mList;
            }
        }

        /*Merged local data*/
        final List<ItemModel> mLocalList = Utils.getMergedOriginalThumbnailList(localList);

        for (ItemModel index : mLocalList){
            modelMap.put(index.global_id,index);
        }

        /*Merged global data*/
        final List<ItemModel> mGlobalList = Utils.getMergedOriginalThumbnailList(globalList);

        Utils.Log(TAG,"onPreparingSyncData ==> Index download globalList "+ new Gson().toJson(globalList));
        Utils.Log(TAG,"onPreparingSyncData ==> Index download map "+ new Gson().toJson(modelMap));
        Utils.Log(TAG,"onPreparingSyncData ==> Index download list "+ new Gson().toJson(mGlobalList));

        for (ItemModel index : mGlobalList){
            final ItemModel item = modelMap.get(index.global_id);
            if (item!=null){
                if (!index.global_id.equals(item.global_id)){
                    mList.add(index);
                    Utils.Log(TAG,"onPreparingSyncData ==> Index download"+ new Gson().toJson(index));
                }
            }else{
                mList.add(index);
                Utils.Log(TAG,"onPreparingSyncData ==> Index download add "+ new Gson().toJson(index));
            }
        }
        return mList;
    }

    /*Merge list to hash map for upload, download and delete*/
    public static Map<String,ItemModel> mergeListToHashMap(List<ItemModel>mList){
        Map<String,ItemModel> map = new HashMap<>();
        for (ItemModel index : mList){
            map.put(index.unique_id,index);
        }
        return map;
    }

    /*Get the first of item data*/
    public static ItemModel getArrayOfIndexHashMap(Map<String,ItemModel> mMapDelete){
        if (mMapDelete!=null){
            if (mMapDelete.size()>0){
                final ItemModel model = mMapDelete.get(mMapDelete.keySet().toArray()[0]);
                Utils.Log(TAG,"Object need to be deleting " + new Gson().toJson(model));
                return  model;
            }
        }
        return null;
    }

    /*Get the first of category data*/
    public static MainCategoryModel getArrayOfIndexCategoryHashMap(Map<String, MainCategoryModel> mMapDelete){
        if (mMapDelete!=null){
            if (mMapDelete.size()>0){
                final MainCategoryModel model = mMapDelete.get(mMapDelete.keySet().toArray()[0]);
                Utils.Log(TAG,"Object need to be deleting " + new Gson().toJson(model));
                return  model;
            }
        }
        return null;
    }

    /*Delete hash map after delete Google drive or Server system*/
    public static boolean deletedIndexOfCategoryHashMap(MainCategoryModel itemModel, Map<String,MainCategoryModel>map){
        try {
            if (map!=null){
                if (map.size()>0){
                    map.remove(itemModel.unique_id);
                    return  true;
                }
            }
        }
        catch (Exception e){
            Utils.Log(TAG,"Could not delete hash map==============================>");
        }
        return  false;
    }

    /*Merge list to hash map for upload, download and delete*/
    public static Map<String,MainCategoryModel> mergeListToCategoryHashMap(List<MainCategoryModel>mList){
        Map<String,MainCategoryModel> map = new HashMap<>();
        for (MainCategoryModel index : mList){
            map.put(index.unique_id,index);
        }
        return map;
    }

    /*Merge list original and thumbnail as list*/
    public static List<ItemModel> getMergedOriginalThumbnailList(List<ItemModel> mDataList){
        List<ItemModel> mList = new ArrayList<>();
        for (ItemModel index : mDataList){
           if (!index.originalSync){
               mList.add(new ItemModel(index,true));
           }
           if (!index.thumbnailSync){
               mList.add(new ItemModel(index,false));
           }
        }
        return mList;
    }


    /*Delete hash map after delete Google drive and Server system*/
    public static boolean deletedIndexOfHashMap(ItemModel itemModel, Map<String,ItemModel>map){
        try {
            if (map!=null){
                if (map.size()>0){
                    map.remove(itemModel.unique_id);
                    return  true;
                }
            }
        }
        catch (Exception e){
            Utils.Log(TAG,"Could not delete hash map==============================>");
        }
        return  false;
    }

    /*------------------------Import area-------------------*/

    /*Add list to hash map for import*/
    public static Map<String, ImportFilesModel> mergeListToHashMapImport(List<ImportFilesModel>mList){
        Map<String,ImportFilesModel> map = new HashMap<>();
        for (ImportFilesModel index : mList){
            map.put(index.unique_id,index);
        }
        return map;
    }

    /*Get the first of data for import*/
    public static ImportFilesModel getArrayOfIndexHashMapImport(Map<String,ImportFilesModel> mMapDelete){
        if (mMapDelete!=null){
            if (mMapDelete.size()>0){
                final ImportFilesModel model = mMapDelete.get(mMapDelete.keySet().toArray()[0]);
                Utils.Log(TAG,"Object need to be deleting " + new Gson().toJson(model));
                return  model;
            }
        }
        return null;
    }

    /*Delete hash map after delete Google drive and Server system for import*/
    public static boolean deletedIndexOfHashMapImport(ImportFilesModel itemModel, Map<String,ImportFilesModel>map){
        try {
            if (map!=null){
                if (map.size()>0){
                    map.remove(itemModel.unique_id);
                    return  true;
                }
            }
        }
        catch (Exception e){
            Utils.Log(TAG,"Could not delete hash map==============================>");
        }
        return  false;
    }

    /*Check saver space*/
    public static boolean getSaverSpace(){
        final boolean isSaver = PrefsController.getBoolean(SuperSafeApplication.getInstance().getString(R.string.key_saving_space), false);
        return isSaver;
    }

    /*Delete folder*/
    public static void onDeleteItemFolder(String item_id){
        String path = SuperSafeApplication.getInstance().getSupersafePrivate()+item_id;
        Utils.Log(TAG,"Delete folder "+path);
        SuperSafeApplication.getInstance().getStorage().deleteDirectory(SuperSafeApplication.getInstance().getSupersafePrivate()+item_id);
    }

    /*Create folder*/
    public static String createDestinationDownloadItem(String items_id){
        String path = SuperSafeApplication.getInstance().getSupersafePrivate();
        String pathFolder = path + items_id + "/";
        return pathFolder;
    }

    public static String getOriginalPath(String currentTime,String items_id){
        String rootPath = SuperSafeApplication.getInstance().getSupersafePrivate();
        String pathContent = rootPath + items_id + "/";
        Utils.createDirectory(pathContent);
        String originalPath = pathContent + currentTime;
        return originalPath;
    }

    /*Create folder*/
    public static boolean createDirectory(String path) {
        File directory = new File(path);
        if (directory.exists()) {
            Log.w(TAG, "Directory '" + path + "' already exists");
            return false;
        }
        return directory.mkdirs();
    }


    public boolean onChangeCategories(MainCategoryModel mainCategories){
        try {
            String hex_name = Utils.getHexCode(mainCategories.categories_name);
            boolean mIsFakePin = mainCategories.isFakePin;
            MainCategoryModel response = SQLHelper.getCategoriesItemId(hex_name,mIsFakePin);
            if (response==null){
                mainCategories.categories_hex_name = hex_name;
                mainCategories.isChange = true;
                mainCategories.isSyncOwnServer = false;
               SQLHelper.updateCategory(mainCategories);
                return true;
            }
            Utils.Log(TAG,"value changed :"+ new Gson().toJson(response));
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isNotEmptyOrNull(String value) {
       if (value==null || value.equals("") || value.equals("null")){
           return false;
       }
       return  true;
    }



}
