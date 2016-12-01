package com.example.youssef.list.models;

import com.example.youssef.list.MyDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by Youssef on 12/1/2016.
 */

@Table(database = MyDatabase.class)
public class Item extends BaseModel {

    @Column
    @PrimaryKey
    String itemName;

}
