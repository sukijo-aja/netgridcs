package com.androidstarter.app.data.remote;

import android.content.Context;

import com.androidstarter.app.utils.DnsHelper;
import com.androidstarter.app.utils.SessionManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit client with SessionManager-aware OkHttp interceptors:
 * - Auto-injects Bearer token into every request.
 * - Intercepts 401 responses and triggers SessionManager.logout().
 */
public class RetrofitClient {

    private static Retrofit retrofit;
    private static final String BASE_URL = "https://api.aladhan.com/v1/";

    /** Returns a session-aware Retrofit instance. */
    public static Retrofit getRetrofitInstance(Context context) {
        if (retrofit == null) {
            retrofit = buildRetrofit(context.getApplicationContext());
        }
        return retrofit;
    }

    /** Force rebuild (e.g. after login with a new token). */
    public static void reset() {
        retrofit = null;
    }

    private static Retrofit buildRetrofit(Context appContext) {
        OkHttpClient client = new OkHttpClient.Builder()
                .dns(DnsHelper.createGoogleDns())
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                // Auth interceptor: inject token header
                .addInterceptor(chain -> {
                    String token = SessionManager.getToken(appContext);
                    Request.Builder requestBuilder = chain.request().newBuilder();
                    if (token != null && !token.isEmpty()) {
                        requestBuilder.header("Authorization", "Bearer " + token);
                    }
                    return chain.proceed(requestBuilder.build());
                })
                // Response interceptor: handle 401 Unauthorized
                .addInterceptor(chain -> {
                    Response response = chain.proceed(chain.request());
                    if (response.code() == 401) {
                        SessionManager.logout(appContext);
                    }
                    return response;
                })
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
