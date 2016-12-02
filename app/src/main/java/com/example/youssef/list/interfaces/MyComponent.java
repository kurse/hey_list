package com.example.youssef.list.interfaces;

import android.content.Context;

import com.example.youssef.list.MainActivity;
import com.example.youssef.list.MyApplication;
import com.example.youssef.list.modules.DaggerModule;
import com.example.youssef.list.presenters.ListPresenter;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Youssef on 12/2/2016.
 */

@Singleton
@Component(modules={DaggerModule.class})
public interface MyComponent {



//    Context context();
    void inject(MainActivity activity);
}
