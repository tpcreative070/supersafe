package co.tpcreative.suppersafe.common.controller;
import android.graphics.Bitmap;
import java.io.File;
import java.io.InputStream;

import co.tpcreative.suppersafe.common.JealousSky;

public class SingletonEncryptData {

    private static SingletonEncryptData instance ;
    private SingleTonResponseListener listener;

    public static SingletonEncryptData getInstance(){
        if (instance==null){
            synchronized (SingletonEncryptData.class){
                if (instance==null){
                    instance = new SingletonEncryptData();
                }
            }
        }
        return instance;
    }

    public void setListener(SingleTonResponseListener listener){
        this.listener = listener;
    }


    public File onEncryptData(final InputStream inputStream,final String outPut){
        try{
            JealousSky jealousSky = JealousSky.getInstance();
            jealousSky.initialize(
                    "123",
                    "FFD7BADF2FBB1999");
           File file = jealousSky.encryptToFile(inputStream,outPut);
           if (file==null){
               return null;
           }
           return file;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap onDecryptData(final InputStream inputStream){
        try{
            JealousSky jealousSky = JealousSky.getInstance();
            jealousSky.initialize(
                    "123",
                    "FFD7BADF2FBB1999");
            Bitmap bitmap = jealousSky.decryptToBitmap(inputStream);
            if (bitmap==null){
                return null;
            }
            return bitmap;

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    public File onDecryptData(final InputStream inputStream,String output){
        try{
            JealousSky jealousSky = JealousSky.getInstance();
            jealousSky.initialize(
                    "123",
                    "FFD7BADF2FBB1999");
            File file = jealousSky.decryptToFile(inputStream,output);
            if (file==null){
                return null;
            }
            return file;

        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public interface SingleTonResponseListener{
        void onEncryptSuccessful(boolean data,File file);
    }


}
