package com.ads.xinfa.net;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttp3Manager {
    private static OkHttpClient okHttpClient;
    public static OkHttpClient getOkHttpClient(){
        if (okHttpClient == null) {
            synchronized (OkHttp3Manager.class){
                if (okHttpClient == null) {
                    HttpLogger httpLogger = new HttpLogger();
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor(httpLogger);
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                    okHttpClient = new OkHttpClient.Builder()
                            .retryOnConnectionFailure(true)
                            .connectTimeout(1800, TimeUnit.SECONDS)
                            .readTimeout(1800, TimeUnit.SECONDS)
                            .writeTimeout(1800, TimeUnit.SECONDS)
                            .addNetworkInterceptor(logging)
                            .addInterceptor(new Interceptor() {
                                @Override
                                public Response intercept(Chain chain) throws IOException {
                                    Request request = chain.request().newBuilder()
                                            .header("is_app", "1")
                                            .build();
                                    return chain.proceed(request);
                                }
                            }).build();
                }
            }
        }
        return okHttpClient;
    }
}
