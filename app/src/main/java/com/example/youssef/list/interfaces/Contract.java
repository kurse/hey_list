package com.example.youssef.list.interfaces;

import android.app.Fragment;

import java.util.ArrayList;

/**
 * Created by Youssef on 10/9/2016.
 */

public class Contract {

    public interface View<T, T2> {
        public void onNext(T t, T2 t2);
        public void showError(String error);
    }

    public interface Presenter<T>{
        public void publish();
        public void takeView(T t);
    }

}
