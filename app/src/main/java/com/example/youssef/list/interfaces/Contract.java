package com.example.youssef.list.interfaces;

import android.app.Fragment;

import java.util.ArrayList;

/**
 * Created by Youssef on 10/9/2016.
 */

public class Contract {

    public interface View<T> {
        public void onNext(T t);
        public void showError(String error);
    }

    public interface Presenter<T>{
        public void publish();
        public void takeView(T t);
    }

}
