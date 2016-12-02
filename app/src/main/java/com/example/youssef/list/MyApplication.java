package com.example.youssef.list;

import android.app.Application;

//import com.example.youssef.list.interfaces.DaggerMyComponent;
import com.example.youssef.list.interfaces.DaggerMyComponent;
import com.example.youssef.list.interfaces.MyComponent;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

/**
 * Created by Youssef on 12/1/2016.
 */

public class MyApplication extends Application {

    MyComponent dagger;
    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(new FlowConfig.Builder(this).build());
        dagger = DaggerMyComponent.builder().build();
    }
    public MyComponent getComponent() {
        return dagger;
    }
}
