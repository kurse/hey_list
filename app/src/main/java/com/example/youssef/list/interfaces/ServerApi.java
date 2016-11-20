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
    @POST("/register")
    Observable<String> register(@Body String user);

    @Headers("Content-Type: application/json")
    @POST("/newgroup")
    Observable<String> newGroup(@Body String companyName);

    @Headers("Content-Type: application/json")
    @POST("/addUserGroup")
    Observable<String> addUser(@Header("authToken") String token, @Body String json);

    @Headers("Content-Type: application/json")
    @POST("/getList")
    Observable<String> getList(@Header("authToken") String token, @Body String json);

    @Headers("Content-Type: application/json")
    @POST("/addItem")
    Observable<String> addItem(@Header("authToken") String token, @Body String json);

    @Headers("Content-Type: application/json")
    @POST("/checkItem")
    Observable<String> checkItem(@Header("authToken") String token, @Body String json);

    @Headers("Content-Type: application/json")
    @POST("/removeItem")
    Observable<String> removeItem(@Header("authToken") String token, @Body String json);

}
