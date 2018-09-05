package co.tpcreative.suppersafe.common.network;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;

import java.lang.reflect.Modifier;
import java.util.HashMap;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

@SuppressWarnings("SpellCheckingInspection")
public class Dependencies<T> extends BaseDependencies  {
    private static final int DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final int TIMEOUT_MAXIMUM = 30;
    public static Object serverAPI;
    private static Dependencies sInstance;
    private  Retrofit.Builder retrofitInstance;
    private Context context;
    private String URL ;
    private DependenciesListener dependenciesListener;
    private int  mTimeOut = 1 ;
    public static final String TAG = Dependencies.class.getSimpleName();

    private Dependencies(Context context){

    }

    public static Dependencies getsInstance(Context context, String url){
        if (sInstance == null){
            sInstance = new Dependencies(context);
        }
        sInstance.context = context ;
        sInstance.URL = url;
        return sInstance;
    }

    public static Dependencies getsInstance(Context context){
        if (sInstance == null){
            sInstance = new Dependencies(context);
        }
        sInstance.context = context ;
        return sInstance;
    }

    public void dependenciesListener(DependenciesListener dependenciesListener){
        sInstance.dependenciesListener = dependenciesListener;
    }

    public void setTimeOutByMinute(int minute){
        if (minute>0) {
            this.mTimeOut = minute;
        }
    }

    public void setRootURL(String url){
        this.URL = url;
    }

    public void changeApiBaseUrl(String newApiBaseUrl) {
        this.URL = newApiBaseUrl;
        if (serverAPI==null){
            init();
        }
        else {
            serverAPI = (T)sInstance.reUse(dependenciesListener.onObject(),dependenciesListener.isXML());
        }
    }

    public T reUse(Class<T> tClass, boolean isXML){
        OkHttpClient okHttpClient = provideOkHttpClientDefault();
        Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.FINAL , Modifier.TRANSIENT , Modifier.STATIC).create();

        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);

        retrofitInstance
                .baseUrl(URL)
                .client(okHttpClient)
                .addConverterFactory(isXML ? SimpleXmlConverterFactory.create(serializer): GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        return retrofitInstance.build().create(tClass);
    }

    public void init(){
        if (serverAPI == null){
            OkHttpClient okHttpClient = provideOkHttpClientDefault();
            serverAPI = (T) sInstance.provideRestApi(okHttpClient,dependenciesListener.onObject(),dependenciesListener.isXML());
        }
    }

    private T provideRestApi(@NonNull OkHttpClient okHttpClient, Class<T> tClass, boolean isXML){
        Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.FINAL , Modifier.TRANSIENT , Modifier.STATIC).create();

        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);

        retrofitInstance = new Retrofit.Builder()
                .baseUrl(URL)
                .client(okHttpClient)
                .addConverterFactory(isXML ? SimpleXmlConverterFactory.create(serializer): GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        return  retrofitInstance.build().create(tClass);
    }

    @Override
    protected HashMap<String, String> getHeaders() {
        HashMap<String, String> hashMap = new HashMap<>();
        String oauthToken = null;
        if (dependenciesListener!=null){
            if (dependenciesListener.onCustomHeader()!=null){
                hashMap.putAll(dependenciesListener.onCustomHeader());
            }
            oauthToken = dependenciesListener.onAuthorToken();
        }
        if (oauthToken != null){
            hashMap.put("Authorization" ,oauthToken);
        }
        return hashMap;
    }

    @Override
    protected int getTimeOut() {
        return mTimeOut;
    }

    public interface DependenciesListener <T>{
        Class<T> onObject();
        String onAuthorToken();
        HashMap<String,String> onCustomHeader();
        boolean isXML();
    }

}
