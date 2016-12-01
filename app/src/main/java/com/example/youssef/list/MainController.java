package com.example.youssef.list;

import com.example.youssef.list.presenters.ListPresenter;

/**
 * Created by Youssef on 11/27/2016.
 */

public class MainController {

    public static class MessageEvent{
        public String message;
        public static ListPresenter lp;

        public MessageEvent(String message){
            this.message = message;
        }
    }

}
