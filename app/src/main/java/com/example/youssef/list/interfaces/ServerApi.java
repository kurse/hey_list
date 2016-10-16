package com.example.youssef.list.interfaces;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by Youssef on 10/16/2016.
 */

public interface ServerApi {

    @Headers("Content-Type: application/json")
    @POST("/auth")
    Observable<String> auth(@Body String user);

    @Headers("Content-Type: application/json")
    @POST("/getList")
    Observable<String> getList(@Header("token") String token, @Body String json);


}
