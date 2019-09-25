package org.iw11.driver.network;

import org.iw11.driver.network.api.IBusPoolingApi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RestServiceFactory {

    private static final String API_BASE_URL = "http://10.16.102.69:8080/api/";

    private static Retrofit RETROFIT;

    private static synchronized Retrofit getRetrofit() {
        if (RETROFIT == null) {
            RETROFIT = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return RETROFIT;
    }

    public static IBusPoolingApi getApiService() {
        return getRetrofit().create(IBusPoolingApi.class);
    }

    private RestServiceFactory() { }
}
