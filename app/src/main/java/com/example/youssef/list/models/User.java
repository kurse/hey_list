package com.example.youssef.list.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

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

public class User implements Serializable{

    private Company company;
    private String curOrgId;
    private String id;
    private String username;
    private String password;
    private String emailAddress;
    private boolean isCreator=false;
    public User(){

    }

    public String getCurOrgId() {
        return curOrgId;
    }

    public void setCurOrgId(String curOrgId) {
        this.curOrgId = curOrgId;
    }

    public User(String username, String password){
        this.username = username;
        this.password = password;
    }
    public User(String username, String password, String emailAddress){
        this.username = username;
        this.password = password;
        this.emailAddress = emailAddress;
    }
    public User(String username, String password, String emailAddress, String orgId){
        this.username = username;
        this.password = password;
        this.emailAddress = emailAddress;
        this.curOrgId = orgId;
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


    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();
        if(this.id!=null)
            json.put("_id",id);
        json.put("username",username);
        json.put("password",password);
        json.put("email_address",emailAddress);
        if(this.curOrgId!=null) {
            json.put("orgID", curOrgId);
            if(this.company!=null)
                json.put("listId", company.getmListId());
        }
        return json;
    }

    public String chnouSmytek(){
        return username;
    }
    public Company getCompany() {
        return company;
    }

    public void setCompany(String json) {
        try {
            this.company = new Company(new JSONObject(json));
            curOrgId = company.getmId();
            if(company.getmCreatorId().equals(id))
                isCreator=true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isCreator() {
        return isCreator;
    }

    public void setCreator(boolean creator) {
        isCreator = creator;
    }

    public void initFromJsonObject(JSONObject json) throws JSONException {
        this.id = json.get("_id").toString();
        this.username = json.getString("username");
        this.password = json.getString("password");
        this.emailAddress = json.getString("email_address");
        if(json.has("company")){
            company = new Company(json);
            this.curOrgId = company.getmId();
        }

    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
