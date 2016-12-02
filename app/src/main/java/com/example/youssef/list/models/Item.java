package com.example.youssef.list.models;

import com.example.youssef.list.MyDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by Youssef on 12/1/2016.
 */

@Table(database = MyDatabase.class, name = "Item")
public class Item extends BaseModel {

    public static final String NAME = "Item";
    @Column
    public int occurences = 0;

    @Column
    @PrimaryKey
    public String itemName;

    public void increment(){
        occurences++;
        this.save();
    }

    public void setOccurences(int occurences) {
        this.occurences = occurences;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getOccurences() {
        return occurences;
    }

    public String getItemName() {
        return itemName;
    }

    @Override
    public void save() {
        if(this.exists()){
            Item existing = new Select().from(Item.class).where(Item_Table.itemName.eq(itemName)).querySingle();
            occurences += existing.occurences +1;

        }
        super.save();
    }



}
