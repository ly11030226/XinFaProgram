package com.ads.xinfa.application;

import android.app.Application;
import android.content.Context;

import com.ads.xinfa.net.APIService;
import com.ads.xinfa.net.HttpLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class XinFaApplication extends Application {

    private static final String TAG = "XinFaApplication";
    private static Context Instance;
    private OkHttpClient client;
    public static APIService apiService;
    public static HashMap<String,String> COMMAND_HASHMAP = new HashMap<>();
    @Override
    public void onCreate() {
        super.onCreate();
        Instance = getApplicationContext();
        try {
//            initOkhttp();
//            initRetrofit();
            //访问共享文件用到
            System.setProperty("jcifs.smb.client.dfs.disabled", "true");
            System.setProperty("jcifs.smb.client.soTimeout", "1000000");
            System.setProperty("jcifs.smb.client.responseTimeout", "30000");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(API.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) //rx支持
                .client(client)
                .build();
        apiService = retrofit.create(APIService.class);
    }

    private void initOkhttp() {
        HttpLogger httpLogger = new HttpLogger();
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(httpLogger);
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        //网络请求 Retrofit2
        client = new OkHttpClient.Builder()
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

    public static APIService getApiService(){
        return apiService;
    }

    public static Context getInstance(){
        return Instance;
    }
}
