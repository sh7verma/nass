package app.com.esenatenigeria.network;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import app.com.esenatenigeria.utils.eSenateApplication;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

//        private static final String BASE_URL = "http://192.168.1.149:3000/";// Development
    private static final String BASE_URL = "http://52.28.171.36:3000/";// LIVE
    private static final String YOUTUBE_BASE_URL = "https://www.googleapis.com/";
    private static final String CACHE_CONTROL = "Cache-Control";
    private static Retrofit retrofit = null;
    private static Retrofit retrofitYoutube = null;
    private static ApiInterface apiInterface = null;
    private static ApiInterface apiInterfaceYoutube = null;

    public static ApiInterface getInstance() {

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(provideOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        if (apiInterface == null) {
            apiInterface = retrofit.create(ApiInterface.class);
        }
        return apiInterface;
    }

    public static ApiInterface getYoutubeInstance() {

        if (retrofitYoutube == null) {
            retrofitYoutube = new Retrofit.Builder()
                    .baseUrl(YOUTUBE_BASE_URL)
                    .client(provideOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        if (apiInterfaceYoutube == null) {
            apiInterfaceYoutube = retrofitYoutube.create(ApiInterface.class);
        }
        return apiInterfaceYoutube;
    }

    //Creating OKHttpClient
    private static OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor(provideOfflineCacheInterceptor())
                .addNetworkInterceptor(provideCacheInterceptor())
                .cache(provideCache())
                .readTimeout(120, TimeUnit.SECONDS)
                .connectTimeout(120, TimeUnit.SECONDS)
                .build();
    }

    //Creating Cache
    private static Cache provideCache() {
        Cache cache = null;
        try {
            cache = new Cache(new File(eSenateApplication.getInstance().getCacheDir(), "http-cache"),
                    10 * 1024 * 1024); // 10 MB
        } catch (Exception ignored) {

        }
        return cache;
    }

    private static Interceptor provideCacheInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());

                // re-write response header to force use of cache
                CacheControl cacheControl = new CacheControl.Builder()
                        .maxAge(2, TimeUnit.MINUTES)
                        .build();

                return response.newBuilder()
                        .header(CACHE_CONTROL, cacheControl.toString())
                        .build();
            }
        };
    }

    //Provides offline cache
    private static Interceptor provideOfflineCacheInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();

                if (!eSenateApplication.hasNetwork()) {
                    CacheControl cacheControl = new CacheControl.Builder()
                            .maxStale(7, TimeUnit.DAYS)
                            .build();

                    request = request.newBuilder()
                            .cacheControl(cacheControl)
                            .build();
                }

                return chain.proceed(request);
            }
        };
    }


}
