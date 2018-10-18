package co.tpcreative.supersafe.common.util;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.media.ExifInterface;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
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
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.api.client.util.Base64;
import com.google.common.base.Charsets;
import com.snatik.storage.Storage;
import com.snatik.storage.helpers.OnStorageListener;

import org.apache.commons.io.FilenameUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import co.tpcreative.supersafe.BuildConfig;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.MimeTypeFile;

/**
 * Created by pc on 07/16/2017.
 */

public class Utils {
    // utility function



    final public static int THUMB_SIZE_HEIGHT = 600;
    final public static int THUMB_SIZE_WIDTH = 400;


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

    // generate a hash
    public static String sha256(String s) {
        MessageDigest digest;
        String hash;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(s.getBytes());

            hash = bytesToHexString(digest.digest());

            return hash;
        } catch (NoSuchAlgorithmException e1) {
            return s;
        }
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

    public static boolean mCreateAndSaveFileOverride(String fileName,String path_folder_name,String responseJson, boolean append) {
        final String newLine = System.getProperty("line.separator");
        try{
            File root = new File(path_folder_name+ "/" + fileName);
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
            e.printStackTrace();
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


    public static Bitmap getThumbnail(final byte[]mData){
        InputStream in = null;
        Bitmap thumbImage = null;
        final byte[]data = mData;
        try {
        final int THUMBSIZE_HEIGHT = 600;
        final int THUMBSIZE_WIDTH = 400;
        thumbImage = ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeByteArray(data,0,data.length),
                THUMBSIZE_HEIGHT,
                THUMBSIZE_WIDTH);
            in = new ByteArrayInputStream(data);
            ExifInterface exifInterface = new ExifInterface(in);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }
            thumbImage = Bitmap.createBitmap(thumbImage, 0, 0, thumbImage.getWidth(), thumbImage.getHeight(), matrix, true); // rotating bitmap
        } catch (IOException e) {
            // Handle any errors
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {}
            }
        }
        return thumbImage;
    }

    public static Bitmap getThumbnail(final File file){
        InputStream in = null;
        Bitmap thumbImage = null;
        try {
            final int THUMBSIZE_HEIGHT = 600;
            final int THUMBSIZE_WIDTH = 400;
            thumbImage = ThumbnailUtils.extractThumbnail(
                    BitmapFactory.decodeFile(file.getAbsolutePath()),
                    THUMBSIZE_HEIGHT,
                    THUMBSIZE_WIDTH);
            ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }
            thumbImage = Bitmap.createBitmap(thumbImage, 0, 0, thumbImage.getWidth(), thumbImage.getHeight(), matrix, true); // rotating bitmap
        } catch (IOException e) {
            // Handle any errors
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {}
            }
        }
        return thumbImage;
    }


    /*Scale image before to save*/

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap getThumbnailScale(byte[] data) {
        // First decode with inJustDecodeBounds=true to check dimensions
        InputStream in = null;
        Bitmap thumbImage = null  ;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        try{
            options.inJustDecodeBounds = true;
            //options.inPurgeable = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, Utils.THUMB_SIZE_WIDTH, Utils.THUMB_SIZE_HEIGHT);

            // Decode bitmap with inSampleSize set
             options.inJustDecodeBounds = false;
            thumbImage =  ThumbnailUtils.extractThumbnail(BitmapFactory.decodeByteArray(data, 0, data.length, options),Utils.THUMB_SIZE_WIDTH,Utils.THUMB_SIZE_HEIGHT);


            in = new ByteArrayInputStream(data);
            ExifInterface exifInterface = new ExifInterface(in);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }
            thumbImage = Bitmap.createBitmap(thumbImage, 0, 0, thumbImage.getWidth(), thumbImage.getHeight(), matrix, true); // rotating bitmap
            return thumbImage;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try {
                if (in!=null){
                    in.close();
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        return thumbImage;
    }

    public static Bitmap getThumbnailScale(String  path) {
        // First decode with inJustDecodeBounds=true to check dimensions
        FileInputStream inputStream = null;
        Bitmap thumbImage = null  ;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        try{
            options.inJustDecodeBounds = true;
            //options.inPurgeable = true;
            BitmapFactory.decodeFile(path,options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, Utils.THUMB_SIZE_WIDTH, Utils.THUMB_SIZE_HEIGHT);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;

            thumbImage =  ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path, options),Utils.THUMB_SIZE_WIDTH,Utils.THUMB_SIZE_HEIGHT);

            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }
            thumbImage = Bitmap.createBitmap(thumbImage, 0, 0, thumbImage.getWidth(), thumbImage.getHeight(), matrix, true); // rotating bitmap
            return thumbImage;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {

        }
        return thumbImage;

    }

    public static Bitmap getThumbnailScaleRotate(String  path,int degrees) {
        // First decode with inJustDecodeBounds=true to check dimensions
        FileInputStream inputStream = null;
        Bitmap thumbImage = null  ;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        try{
            options.inJustDecodeBounds = true;
            //options.inPurgeable = true;
            BitmapFactory.decodeFile(path,options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, Utils.THUMB_SIZE_WIDTH, Utils.THUMB_SIZE_HEIGHT);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;

            thumbImage =  ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path, options),Utils.THUMB_SIZE_WIDTH,Utils.THUMB_SIZE_HEIGHT);

           // ExifInterface exifInterface = new ExifInterface(path);
          //  int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            //Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            thumbImage = Bitmap.createBitmap(thumbImage, 0, 0, thumbImage.getWidth(), thumbImage.getHeight(), matrix, true); // rotating bitmap
            return thumbImage;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {

        }
        return thumbImage;
    }

    public static File getPackagePath(Context context){
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                ".temporary.jpg");
        return file;
    }

    public static File getPackageFolderPath(Context context){
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        return file;
    }

    public static Bitmap saveByteArrayBitmap(final byte[] data,final int orientation){
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        try {
            Matrix matrix = new Matrix();
            matrix.setRotate(orientation+90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // rotating bitmap
            return bitmap;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap rotateBitmap(String src, Bitmap bitmap) {
        try {
            int orientation = getExifOrientation(src);

            if (orientation == 1) {
                return bitmap;
            }

            Matrix matrix = new Matrix();
            switch (orientation) {
                case 2:
                    matrix.setScale(-1, 1);
                    break;
                case 3:
                    matrix.setRotate(180);
                    break;
                case 4:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case 5:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case 6:
                    matrix.setRotate(90);
                    break;
                case 7:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case 8:
                    matrix.setRotate(-90);
                    break;
                default:
                    return bitmap;
            }

            try {
                Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return oriented;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private static int getExifOrientation(String src) throws IOException {
        int orientation = 1;
        try {
            /**
             * if your are targeting only api level >= 5
             * ExifInterface exif = new ExifInterface(src);
             * orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
             */
            if (Build.VERSION.SDK_INT >= 5) {
                Class<?> exifClass = Class.forName("android.media.ExifInterface");
                Constructor<?> exifConstructor = exifClass.getConstructor(new Class[] { String.class });
                Object exifInstance = exifConstructor.newInstance(new Object[] { src });
                Method getAttributeInt = exifClass.getMethod("getAttributeInt", new Class[] { String.class, int.class });
                Field tagOrientationField = exifClass.getField("TAG_ORIENTATION");
                String tagOrientation = (String) tagOrientationField.get(null);
                orientation = (Integer) getAttributeInt.invoke(exifInstance, new Object[] { tagOrientation, 1});
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return orientation;
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
        String fileExt = FilenameUtils.getExtension(url);
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

    public static String getDateIdentifier(){
        return ""+System.currentTimeMillis();
    }

    public static String getCurrentDateTime() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String result = dateFormat.format(date);
        return result;
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

    public static void exportDB() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            if (sd.canWrite()) {
                String currentDBPath = SuperSafeApplication.getInstance().getDatabasePath(SuperSafeApplication.getInstance().getString(R.string.key_database)).getAbsolutePath();
                String backupDBPath = SuperSafeApplication.getInstance().getSupersafeBackup()+SuperSafeApplication.getInstance().getString(R.string.key_database_backup);
                File currentDB = new File(currentDBPath);
                File backupDB = new File(backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(SuperSafeApplication.getInstance(), "Backup Successful!",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(SuperSafeApplication.getInstance(), "Backup Failed!", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public static void onBackUp(){
        try {
            String inFileName = SuperSafeApplication.getInstance().getDatabasePath(SuperSafeApplication.getInstance().getString(R.string.key_database)).getAbsolutePath();
            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);
            String outFileName = SuperSafeApplication.getInstance().getSupersafeBackup()+SuperSafeApplication.getInstance().getString(R.string.key_database_backup);

            if (!SuperSafeApplication.getInstance().getStorage().isFileExist(inFileName)){
                Utils.Log(TAG,"Path is not existing :" + SuperSafeApplication.getInstance().getPathDatabase());
                return;
            }
            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFileName);
            // Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer))>0){
                output.write(buffer, 0, length);
            }
            // Close the streams
            output.flush();
            output.close();
            fis.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        finally {
            Utils.Log(TAG,"Backup done");
        }
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


    public static void onRestore(ServiceManager.ServiceManagerSyncDataListener ls){
        try {
            String inFileName = SuperSafeApplication.getInstance().getSupersafeBackup()+SuperSafeApplication.getInstance().getString(R.string.key_database_backup);
            File dbFile = new File(inFileName);
            if (dbFile.isFile() && dbFile.exists()){
                Utils.Log(TAG,"File is existing");
            }
            Utils.Log(TAG,inFileName);
            FileInputStream fis = new FileInputStream(dbFile);
            String outFileName = SuperSafeApplication.getInstance().getDatabasePath(SuperSafeApplication.getInstance().getString(R.string.key_database)).getAbsolutePath();
            if (!SuperSafeApplication.getInstance().getStorage().isFileExist(inFileName)){
                Utils.Log(TAG,"Path is not existing :" + SuperSafeApplication.getInstance().getPathDatabase());
                return;
            }
            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFileName);
            // Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer))>0){
                Utils.Log(TAG,"Length :"+length);
                output.write(buffer, 0, length);
            }
            output.flush();
            output.close();
            fis.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        finally {
            ls.onCompleted();
        }
    }

    public void onWriteLargeFile(File input,File output){
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(input);
            int length = 0;
            FileOutputStream fOutputStream = new FileOutputStream(
                    output);
            //note the following line
            byte[] buffer = new byte[1024];
            while ((length = inputStream.read(buffer)) > 0) {
                fOutputStream.write(buffer, 0, length);
            }
            fOutputStream.flush();
            fOutputStream.close();
            inputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {}
            }
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

    public static void showGotItSnackbar(final View view, final @StringRes int text, ServiceManager.ServiceManagerSyncDataListener ls) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
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
        }, 200);
    }

    private static Snackbar multilineSnackbar(Snackbar snackbar) {
        TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
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


    public static String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
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
        hashMap.put("m4a",new MimeTypeFile(".m4a", EnumFormatType.AUDIO,"audio/m4a"));
        hashMap.put("jpg",new MimeTypeFile(".jpg", EnumFormatType.IMAGE,"image/jpeg"));
        hashMap.put("png",new MimeTypeFile(".png", EnumFormatType.IMAGE,"image/png"));
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
        if (status==null){
            Utils.mCreateAndSaveFileOverride("log.txt", SuperSafeApplication.getInstance().getSupersafeLog(), "----Time----" + Utils.getCurrentDateTimeFormat() +" ----Content--- :" + message, true);
        }
        else{
            Utils.mCreateAndSaveFileOverride("log.txt", SuperSafeApplication.getInstance().getSupersafeLog(), "----Time----" + Utils.getCurrentDateTimeFormat() + " ----Status---- :" + status.name() + " ----Content--- :" + message, true);
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

    public static File createTmpFile(Context context) {

        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            File pic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            String fileName = UUID.randomUUID().toString();
            return new File(pic, fileName + ".jpg");
        } else {
            File cacheDir = context.getCacheDir();
            String fileName = UUID.randomUUID().toString();
            return new File(cacheDir, fileName + ".jpg");
        }

    }

    public static void addMediaToGallery(Context context,Uri uri) {
        if (uri == null) {
            return;
        }
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(uri);
        context.sendBroadcast(mediaScanIntent);
    }

}
