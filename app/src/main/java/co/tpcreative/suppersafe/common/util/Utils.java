package co.tpcreative.suppersafe.common.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.snatik.storage.Storage;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import co.tpcreative.suppersafe.R;
import de.mrapp.android.dialog.MaterialDialog;

/**
 * Created by pc on 07/16/2017.
 */

public class Utils {
    // utility function
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

    public static void hideSoftKeyboard(Activity context) {
        View view = context.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }






}
