package co.tpcreative.suppersafe.common.util;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.support.media.ExifInterface;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;

import com.google.android.gms.common.util.Hex;
import com.google.common.base.Charsets;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import co.tpcreative.suppersafe.BuildConfig;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;
import de.mrapp.android.dialog.MaterialDialog;

/**
 * Created by pc on 07/16/2017.
 */

public class Utils {
    // utility function
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
        builder.setTitle(activity.getString(R.string.confirm));
        builder.setMessage(message);
        builder.setPositiveButton(activity.getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
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
            InputMethodManager inputManager = (InputMethodManager) SupperSafeApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static  int dpToPx(int dp) {
        Resources r = SupperSafeApplication.getInstance().getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, r.getDisplayMetrics());
        return px;
    }

    public static void showKeyboard(View view) {
        // Check if no view has focus:
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager)  SupperSafeApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public boolean saveFile(Context context, String mytext){
        Log.i("TESTE", "SAVE");
        try {
            FileOutputStream fos = context.openFileOutput("file_name"+".txt",Context.MODE_PRIVATE);
            Writer out = new OutputStreamWriter(fos);
            out.write(mytext);
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String load(Context context){
        Log.i("TESTE", "FILE");
        try {
            FileInputStream fis = context.openFileInput("file_name"+".txt");
            BufferedReader r = new BufferedReader(new InputStreamReader(fis));
            String line= r.readLine();
            r.close();
            return line;
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("TESTE", "FILE - false");
            return null;
        }
    }

    public static Bitmap getThumbnail(final byte[]data){
        final int THUMBSIZE_HEIGHT = 600;
        final int THUMBSIZE_WIDTH = 400;
        Bitmap thumbImage = ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeByteArray(data,0,data.length),
                THUMBSIZE_HEIGHT,
                THUMBSIZE_WIDTH);
        InputStream in = null;
        try {
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
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {}
            }
        }
        return thumbImage;
    }

    public static Bitmap getThumbnail(File file){
        final int THUMBSIZE_HEIGHT = 600;
        final int THUMBSIZE_WIDTH = 400;
        Bitmap thumbImage = ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeFile(file.getAbsolutePath()),
                THUMBSIZE_HEIGHT,
                THUMBSIZE_WIDTH);
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
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
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return thumbImage;
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

    public static String getHexCode(String value){
        String hexString = Hex.bytesToStringUppercase(value.getBytes(Charsets.UTF_8));
        return hexString;
    }

}
