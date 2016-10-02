package com.example.youssef.list.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Youssef on 8/17/2016.
 */

public class Company implements Serializable{
    private String mId;
    private String mName;
    private String mListId;
    private String mCreatorId;

    public Company(JSONObject json){
        this.initFromJson(json);
    }
    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmListId() {
        return mListId;
    }

    public void setmListId(String mListId) {
        this.mListId = mListId;
    }
    public Company(){

    }
    public Company(String name, String creatorId){
        this.mName = name;
        this.mCreatorId = creatorId;
    }
    public JSONObject toJSOnObject(){
        JSONObject json = new JSONObject();
        try {
            if(this.mId!=null)
                json.put("_id",mId);
            if(this.mListId!=null)
                json.put("listId",mListId);
            json.put("name",this.mName);
            json.put("creatorId",this.mCreatorId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public String getmCreatorId() {
        return mCreatorId;
    }

    public void setmCreatorId(String mCreatorId) {
        this.mCreatorId = mCreatorId;
    }

    private String getIdSub(String id){

        String idSub = id.substring(9,id.length()-2);
        return idSub;
    }
    public void initFromJson(JSONObject json){
        try {
//            JSONObject oid = new JSONObject(json.getString("_id"));
            if(json.getString(("_id")).contains("$oid"))
                this.mId = getIdSub(json.getString("_id"));
            else
                this.mId = json.getString("_id");
            this.mName = json.getString("name");
            if(json.has("listId"))
                this.mListId= json.getString("listId");
            this.mCreatorId = json.getString("creatorId");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
