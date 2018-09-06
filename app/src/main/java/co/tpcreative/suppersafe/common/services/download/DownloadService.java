package co.tpcreative.suppersafe.common.services.download;
import android.graphics.Bitmap;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.snatik.storage.Storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import co.tpcreative.suppersafe.common.api.request.DownloadFileRequest;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;
import co.tpcreative.suppersafe.common.util.Utils;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by PC on 9/1/2017.
 */

public class DownloadService  implements ProgressResponseBody.ProgressResponseBodyListener{
    public static final String TAG = DownloadService.class.getSimpleName();
    private Object context;
    private String ip ;
    private DownLoadServiceListener listener;
    private Storage storage;
    
    
    public DownloadService(Object context) {
        this.context = context;
    }
    
    public void intDownLoadPOST(DownloadFileRequest request){
        downloadFileByPost(request);
    }
    
    public void intDownLoadGET(DownloadFileRequest request){
        downloadFileByGET(request);
    }
    
    public void onProgressingDownload(DownLoadServiceListener downLoadServiceListener,String ip){
        this.listener  = downLoadServiceListener;
        this.ip = ip;
        storage = new Storage(SupperSafeApplication.getInstance());
    }

    @Override
    public void onAttachmentDownloadUpdate(int percent) {
        //Log.d(TAG,"Downloading : " + percent );
        this.listener.onProgressingDownloading(percent);
    }

    @Override
    public void onAttachmentElapsedTime(long elapsed) {
        this.listener.onAttachmentElapsedTime(elapsed);
    }

    @Override
    public void onAttachmentAllTimeForDownloading(long all) {
        this.listener.onAttachmentAllTimeForDownloading(all);
    }

    @Override
    public void onAttachmentRemainingTime(long all) {
        this.listener.onAttachmentRemainingTime(all);
    }

    @Override
    public void onAttachmentSpeedPerSecond(double all) {
        this.listener.onAttachmentSpeedPerSecond(all);
    }

    @Override
    public void onAttachmentTotalDownload(long totalByte, long totalByteDownloaded) {
        this.listener.onAttachmentTotalDownload(totalByte,totalByteDownloaded);
    }

    @Override
    public void onAttachmentDownloadedError(String message) {
        this.listener.onDownLoadError("Error occurred downloading from body : " + message);
    }

    @Override
    public void onAttachmentDownloadedSuccess() {
      
    }
    
    public synchronized void downloadFileByPost(final DownloadFileRequest reqest){
        RetrofitInterface downloadService = createService(RetrofitInterface.class, this.ip,reqest.mapHeader);
        downloadService.downloadFile(reqest.api_name,reqest)
                .flatMap(processResponse(reqest))
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .subscribe(handleResult(reqest));
    }


    public synchronized void downloadFileByGET(final DownloadFileRequest reqest){
        RetrofitInterface downloadService = createService(RetrofitInterface.class, this.ip,reqest.mapHeader);
        downloadService.downloadFile(reqest.api_name)
                .flatMap(processResponse(reqest))
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .subscribe(handleResult(reqest));
    }

    public synchronized void downloadDriveFileByGET(final DownloadFileRequest reqest){
        RetrofitInterface downloadService = createService(RetrofitInterface.class, this.ip,reqest.mapHeader);
        downloadService.downloadDriveFile(reqest.api_name,reqest.Authorization)
                .flatMap(processResponse(reqest))
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .subscribe(handleResult(reqest));
    }

    private synchronized Function<Response<ResponseBody>, Observable<File>> processResponse(final DownloadFileRequest request) {
        return new Function<Response<ResponseBody>, Observable<File>>() {
            @Override
            public Observable<File> apply(Response<ResponseBody> responseBodyResponse) throws Exception {
                if (responseBodyResponse==null) {
                    Log.d(TAG, "response Body is null");
                }
                if (responseBodyResponse!=null && listener!=null){
                    listener.onCodeResponse(responseBodyResponse.code(),request);
                }
                return saveToDisk(responseBodyResponse,request);
            }
        };
    }

    private synchronized Observable<File> saveToDisk(final Response<ResponseBody> response, final DownloadFileRequest request) {
        return Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(ObservableEmitter<File> subscriber) throws Exception {
                try {
                    new File(request.path_folder_output).mkdirs();
                    File destinationFile = new File(request.path_folder_output,request.file_name);
                    if (!destinationFile.exists()) {
                        destinationFile.createNewFile();
                        Log.d(TAG, "created file");
                    }
                    BufferedSink bufferedSink = Okio.buffer(Okio.sink(destinationFile));
                    bufferedSink.writeAll(response.body().source());
                    if (listener!=null){
                        listener.onSavedCompleted();
                    }
                    bufferedSink.close();
                    subscriber.onNext(destinationFile);
                    subscriber.onComplete();
                } catch (IOException e) {
                    e.printStackTrace();
                    if (listener!=null){
                        File destinationFile = new File(request.path_folder_output,request.file_name);
                        if (destinationFile.isFile() && destinationFile.exists()){
                            destinationFile.delete();
                        }
                        HashMap<String,Object> response = new HashMap<>();
                        response.put("message","Downloading occurred error on save file: " + e.getMessage());
                        response.put("request",new Gson().toJson(request));
                        listener.onErrorSave(new Gson().toJson(response));
                    }
                    subscriber.onError(e);
                }
            }
        });
    }
    
    private Observer<File> handleResult(DownloadFileRequest mFileName) {
        return new Observer<File>() {
            File file_name ;
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                Log.d(TAG,"Download completed");
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                File destinationFile = new File(mFileName.path_folder_output,mFileName.file_name);
                if (destinationFile.isFile() && destinationFile.exists()){
                    destinationFile.delete();
                }
                HashMap<String,Object> response = new HashMap<>();
                response.put("message","Downloading occurred error on save file: " + e.getMessage());
                response.put("request",new Gson().toJson(mFileName));
                listener.onDownLoadError(new Gson().toJson(response));
            }
            @Override
            public void onNext(File file) {
                this.file_name = file;
                listener.onDownLoadCompleted(file,mFileName);
                Log.d(TAG,"File onNext to " + file.getAbsolutePath());
            }
        };
    }

    private synchronized <T> T createService(Class<T> serviceClass, String baseUrl, Map<String,String> mapHeader) {
        Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.FINAL , Modifier.TRANSIENT , Modifier.STATIC).create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(getOkHttpDownloadClientBuilder(this,mapHeader))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
        return retrofit.create(serviceClass);
    }

    private synchronized OkHttpClient getOkHttpDownloadClientBuilder(final ProgressResponseBody.ProgressResponseBodyListener progressListener, final Map<String,String> mapHeader) {
        OkHttpClient httpClientBuilder = new OkHttpClient.Builder()
                .connectTimeout(10 , TimeUnit.MINUTES)
                .writeTimeout(10 , TimeUnit.MINUTES)
                .readTimeout(10 , TimeUnit.MINUTES).addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Request.Builder builder = request.newBuilder();
                Iterator var4 = mapHeader.entrySet().iterator();
                while(var4.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry)var4.next();
                    builder.addHeader(entry.getKey(),entry.getValue());
                }
                if(progressListener == null)
                    return chain.proceed(builder.build());
                okhttp3.Response originalResponse = chain.proceed(builder.build());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();
            }
        }).build();
        return httpClientBuilder;
    }

    public interface DownLoadServiceListener {
        void onDownLoadCompleted(File file_name, DownloadFileRequest request);
        void onDownLoadError(String error);
        void onProgressingDownloading(int percent);
        void onAttachmentElapsedTime(long elapsed);
        void onAttachmentAllTimeForDownloading(long all);
        void onAttachmentRemainingTime(long all);
        void onAttachmentSpeedPerSecond(double all);
        void onAttachmentTotalDownload(long totalByte, long totalByteDownloaded);
        void onSavedCompleted();
        void onErrorSave(String name);
        void onCodeResponse(int code, DownloadFileRequest request);
    }


}