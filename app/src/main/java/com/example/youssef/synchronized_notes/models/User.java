package com.example.youssef.synchronized_notes.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Youssef on 8/17/2016.
 */

//package main.models
//
//        import org.bson.types.ObjectId
//
//        case class User(_id:Option[ObjectId] = None, username: String, var password: String, var orgID : Option[Int] = None)
//        import spray.json.{DefaultJsonProtocol, _}
//
//
//        object MyJsonProtocol extends DefaultJsonProtocol {
//        implicit val userJsonFormat = DefaultJsonProtocol.jsonFormat4(User.apply)
//        }

public class User {
    private String id;
    private String username;
    private String password;
    private String orgId;

    public User(){

    }
    public User(String username, String password){
        this.username = username;
        this.password = password;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();
        if(this.id!=null)
            json.put("_id",id);
        json.put("username",username);
        json.put("password",password);
        if(this.orgId!=null)
            json.put("orgID",orgId);
        return json;
    }

    public void initFromJsonObject(JSONObject json) throws JSONException {
        this.id = json.get("_id").toString();
        this.username = json.getString("username");
        this.password = json.getString("password");
        this.orgId = json.getString("orgID");
    }
}
