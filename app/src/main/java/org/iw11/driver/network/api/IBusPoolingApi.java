package org.iw11.driver.network.api;

import org.iw11.driver.network.model.LocationUpdate;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IBusPoolingApi {

    @POST("bus/location")
    Call<Void> postLocation(@Body LocationUpdate locationUpdate);
}
