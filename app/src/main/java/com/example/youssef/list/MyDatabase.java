package com.example.youssef.list;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by Youssef on 12/1/2016.
 */

@Database(name = MyDatabase.NAME, version = MyDatabase.VERSION)
public class MyDatabase {
    public static final String NAME = "MyDataBase";

    public static final int VERSION = 1;
}