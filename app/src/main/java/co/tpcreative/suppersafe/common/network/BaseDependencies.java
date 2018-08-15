package co.tpcreative.suppersafe.common.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

import static java.util.concurrent.TimeUnit.MINUTES;

public class BaseDependencies {

    private static int DEFAULT_TIMEOUT = 5;
    protected OkHttpClient provideOkHttpClientDefault() {
        OkHttpClient okBuilder = new OkHttpClient.Builder()
                .addInterceptor(
                        new Interceptor() {
                            @Override
                            public Response intercept(Chain chain) throws IOException {
                                Request request = chain.request();
                                Request.Builder builder = request.newBuilder();
                                HashMap<String, String> headers = getHeaders();
                                if (headers != null && headers.size() > 0) {
                                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                                        Timber.d("%s : %s",entry.getKey(), entry.getValue());
                                        builder.addHeader(entry.getKey(), entry.getValue());
                                    }
                                }
                                return chain.proceed(builder.build());
                            }
                        }).build();
        int timeout = getTimeOut();
        okBuilder.newBuilder().connectTimeout(timeout, MINUTES);
        okBuilder.newBuilder().readTimeout(timeout, MINUTES);
        okBuilder.newBuilder().writeTimeout(timeout, MINUTES);
        return okBuilder;
    }

    protected HashMap<String, String> getHeaders() {
        return null;
    }

    protected int getTimeOut() {
        return DEFAULT_TIMEOUT;
    }


}
