package org.iw11.driver.network.api;

import org.iw11.driver.network.model.BusCredentials;
import org.iw11.driver.network.model.BusRoute;
import org.iw11.driver.network.model.LocationUpdate;
import org.iw11.driver.network.model.TokenModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface IBusPoolingApi {

    @POST("bus/location")
    Call<Void> postLocation(@Body LocationUpdate locationUpdate, @Header("Access-token") String token);

    @POST("bus/login")
    Call<TokenModel> postLogin(@Body BusCredentials busCredentials);

    @GET("bus/route")
    Call<BusRoute> getRoute(@Header("Access-token") String token);

    @POST("bus/route/complete")
    Call<Void> postRouteComplete(@Header("Access-token") String token);
}
