/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.tpcreative.supersafe.common.services.upload;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import co.tpcreative.supersafe.common.api.request.UploadingFileRequest;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * @author PC
 */
public class UploadService {

    private long totalSize = 0;
    private UploadServiceListener listener;
    private String url;

    public Disposable getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Disposable subscriptions) {
        this.subscriptions = subscriptions;
    }

    private Disposable subscriptions;

    public static final String TAG = UploadService.class.getSimpleName();

    public void setListener(UploadServiceListener listener, String url) {
        this.listener = listener;
        this.url = url;
    }

    public void postSpecificFile(UploadingFileRequest upload, String key) {
        subscriptions = Observable.create(subscriber -> {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            String responseString = null;
            try {
                Iterator var4 = upload.mapHeader.entrySet().iterator();
                while (var4.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry) var4.next();
                    post.addHeader(entry.getKey(), entry.getValue());
                    Log.d(TAG, "adding header : " + entry.getKey() + "   value " + entry.getValue());
                }

                for (File index : upload.list) {
                    File sourceFile = new File(index.getAbsolutePath());
                    builder.addPart(key, new FileBody(sourceFile));
                }

                for (Map.Entry<String, String> entry : upload.mapBody.entrySet()) {
                    builder.addTextBody(entry.getKey(), entry.getValue());
                    Log.d(TAG, "adding text body : " + entry.getKey());
                }

                final HttpEntity yourEntity = builder.build();

                ProgressiveEntity myEntity = new ProgressiveEntity(yourEntity, new ProgressiveEntity.ProgressListener() {
                    @Override
                    public void transferred(long num) {
                        int percent = (int) ((num / (float) totalSize) * 100);
                        listener.onProgressing(percent, totalSize);
                    }
                    @Override
                    public void transferSpeed(double speed) {
                        listener.onSpeed(speed);
                    }
                });

                totalSize = myEntity.getContentLength();
                post.setEntity(myEntity);
                HttpResponse response = client.execute(post);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: " + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }
            subscriber.onNext(responseString);
            subscriber.onComplete();
        }).observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .subscribe(response -> {
                    listener.onUploadCompleted((String) response,upload);
                });

    }


    public void postUploadFileMulti(UploadingFileRequest upload) {

        subscriptions = Observable.create(subscriber -> {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);


            String responseString = null;
            try {
                Iterator var4 = upload.mapHeader.entrySet().iterator();
                while (var4.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry) var4.next();
                    post.addHeader(entry.getKey(), entry.getValue());
                    Log.d(TAG, "adding header : " + entry.getKey() + "   value " + entry.getValue());
                }




                for (File index : upload.list) {
                    File sourceFile = new File(index.getAbsolutePath());
                    //builder.addPart("file", new FileBody(sourceFile));
                    Log.d(TAG,"get name :"+ sourceFile.getName());
                    ContentBody cbFile = new FileBody(sourceFile, ContentType.create("image/jpeg"),sourceFile.getName());
                    builder.addPart("data",cbFile);
                }

                for (Map.Entry<String, String> entry : upload.mapBody.entrySet()) {
                    builder.addTextBody(entry.getKey(), entry.getValue());
                    Log.d(TAG, "adding text body : " + entry.getKey());
                }




                final HttpEntity yourEntity = builder.build();


                ProgressiveEntity myEntity = new ProgressiveEntity(yourEntity, new ProgressiveEntity.ProgressListener() {
                    @Override
                    public void transferred(long num) {
                        int percent = (int) ((num / (float) totalSize) * 100);
                        listener.onProgressing(percent, totalSize);
                    }
                    @Override
                    public void transferSpeed(double speed) {
                        listener.onSpeed(speed);
                    }
                });

                totalSize = myEntity.getContentLength();


                post.setEntity(myEntity);
                HttpResponse response = client.execute(post);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: " + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }
            subscriber.onNext(responseString);
            subscriber.onComplete();
        }).observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .subscribe(response -> {
                    listener.onUploadCompleted((String) response,upload);
                });

    }

    public interface UploadServiceListener {
        void onUploadCompleted(String response, UploadingFileRequest request);
        void onProgressing(int percent, long total);
        void onSpeed(double speed);
    }
}
