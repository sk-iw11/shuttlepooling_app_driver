package org.iw11.driver.network.api;

import org.iw11.driver.network.model.BusCredentials;
import org.iw11.driver.network.model.LocationUpdate;
import org.iw11.driver.network.model.TokenModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface IBusPoolingApi {

    @POST("bus/location")
    Call<Void> postLocation(@Header("Access-token") String token, @Body LocationUpdate locationUpdate);

    @POST("bus/login")
    Call<TokenModel> postLogin(@Body BusCredentials busCredentials);
}
