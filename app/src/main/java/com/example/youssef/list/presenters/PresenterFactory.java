package com.example.youssef.list.presenters;

import android.support.annotation.NonNull;

/**
 * Created by Youssef on 10/9/2016.
 */

public interface PresenterFactory<T extends Presenter> {

    /**
     * Create a new instance of a Presenter
     *
     * @return The Presenter instance
     */
    @NonNull
    T createPresenter();
}