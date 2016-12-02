package com.example.youssef.list.modules;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.youssef.list.MainActivity;
import com.example.youssef.list.MyApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by Youssef on 12/2/2016.
 */

@Module
public class DaggerModule {

    private MyApplication app;

    public DaggerModule(MyApplication app){
        this.app= app;
    }
    @Provides
    @Singleton
    MyApplication provideApplicationContext(){
        return app;
    }

    @Provides
    @Singleton
    Retrofit providesRetrofit(MyApplication application){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MainActivity.SERVER_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }
    @Provides
    @Singleton
    SharedPreferences providesSharedPreferences(MyApplication application) {
        return application.getSharedPreferences("account", Context.MODE_PRIVATE);
    }
}
