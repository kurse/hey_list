package com.example.youssef.list.presenters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.youssef.list.R;
import com.example.youssef.list.fragments.ObjectListFragment;
import com.example.youssef.list.interfaces.Contract;
import com.example.youssef.list.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

/**
 * Created by Youssef on 10/8/2016.
 */

public class ListPresenter extends Presenter implements Contract.Presenter<ObjectListFragment>{

    public static String UNKNOWN;
    public static String NOT_CONNECTED;
    public static String FAILED_LOGIN;
    public static String TAG = "ListPresenter";
    public static String FETCH_LIST_ADDRESS = "http://137.74.44.134:8080/getList";
    public static String ADD_ITEM_ADDRESS = "http://137.74.44.134:8080/addItem";
    public static String REMOVE_ITEM_ADDRESS = "http://137.74.44.134:8080/removeItem";
    public static String AUTH_ADDRESS = "http://137.74.44.134:8080/auth";
    public static String ERROR_CONNECT_DETECT = "failed to connect";
    public static String ERROR_TOKEN_DETECT = "invalidToken";
    private Boolean isRequest = false;
    private ArrayList<String> items;
    RestTemplate restTemplate = new RestTemplate();

    Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.getData().getString("caller").equals("fetchList"))
                fetchList();
            else if(msg.getData().getString("caller").equals("addItem")){
                addItemDB(msg.getData().getString("item"));
            }
            else if(msg.getData().getString("caller").equals("removeItem")){
                removeItemDB(msg.getData().getString("item"));
            }
            return true;
        }
    };

    Runnable mServerRunnable;
    Thread mServerThread;

    private String mToken;
    private User mCurUser;

    private Throwable error;
    private ObjectListFragment mFragment;

    public void removeItemDB(final String item){
        Runnable r = new Runnable() {
            @Override
            public void run() {

                error = null;
                items = null;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("authToken",mToken);
                String response="";
                try {
                    JSONObject json = new JSONObject();
                    json.put("listId",mCurUser.getCompany().getmListId());
                    json.put("item",item);
                    String listIdJson = json.toString();
                    HttpEntity<String> entity = new HttpEntity<>(listIdJson,headers);

                    response = restTemplate.postForObject(REMOVE_ITEM_ADDRESS, entity, String.class);
                    Log.d("response", response);
                    if (!response.equals(ERROR_TOKEN_DETECT)) {
                        JSONObject jsonResponse = new JSONObject(response);
                        final JSONArray itemsArray = new JSONArray(jsonResponse.getString("list"));
                        items = new ArrayList<>();
                        for (int i = 0; i < itemsArray.length(); i++)
                            try {
                                items.add(itemsArray.getString(i));
                            } catch (JSONException e) {
                                error = new Error(mFragment.getString(R.string.error_title_generic));
                                publish();
                            }
                        publish();
                    } else{
                        Message msg = new Message();
                        msg.getData().putString("caller","removeItem");
                        msg.getData().putString("item",item);
                        SharedPreferences sharedPref = mFragment.getActivity().getSharedPreferences("account", Context.MODE_PRIVATE);
                        String username = sharedPref.getString("username","");
                        String password = new String(Base64.decode(sharedPref.getString("password",""),Base64.DEFAULT));
                        login(username, password, msg);
                    }
//                        Toast.makeText(mContext,"Erreur de connexion",Toast.LENGTH_LONG).show();
                }catch (Exception e){
                    error = new Error(mFragment.getString(R.string.error_not_connected));
                    publish();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    public void addItemDB(final String item){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                error = null;
                items = null;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("authToken",mToken);
                String response="";
                try {
                    JSONObject json = new JSONObject();
                    json.put("listId",mCurUser.getCompany().getmListId());
                    json.put("item",item);
                    String listIdJson = json.toString();
                    HttpEntity<String> entity = new HttpEntity<>(listIdJson,headers);

                    response = restTemplate.postForObject(ADD_ITEM_ADDRESS, entity, String.class);
                    Log.d("response", response);
                    if (!response.equals(ERROR_TOKEN_DETECT)) {

                        JSONObject jsonResponse = new JSONObject(response);
                        final JSONArray itemsArray = new JSONArray(jsonResponse.getString("list"));

                        items = new ArrayList<>();
                        for (int i = 0; i < itemsArray.length(); i++)
                            try {
                                items.add(itemsArray.getString(i));
                            } catch (JSONException e) {
                                error = new Error(mFragment.getString(R.string.error_title_generic));
                                publish();
                            }
                        publish();
                    }
                    else{
                        Message msg = new Message();
                        msg.getData().putString("caller","addItem");
                        msg.getData().putString("item",item);
                        SharedPreferences sharedPref = mFragment.getActivity().getSharedPreferences("account", Context.MODE_PRIVATE);
                        String username = sharedPref.getString("username","");
                        String password = new String(Base64.decode(sharedPref.getString("password",""),Base64.DEFAULT));
                        login(username, password, msg);
//                        publish();
                    }
//                        Toast.makeText(mContext,"Erreur de connexion",Toast.LENGTH_LONG).show();
                }catch (Exception e){
                    if(e.getMessage().contains(ERROR_CONNECT_DETECT)){
                        error = new Error(NOT_CONNECTED);
                    }else{
                        SharedPreferences sharedPref = mFragment.getActivity().getSharedPreferences("account", Context.MODE_PRIVATE);
                        String username = sharedPref.getString("username","");
                        String password = new String(Base64.decode(sharedPref.getString("password",""),Base64.DEFAULT));
                        Message msg = new Message();
                        msg.getData().putString("caller","addItem");
                        msg.getData().putString("item",item);
                        login(username, password, msg);

                    }
//                        error = new Error(mFragment.getString(R.string.error_title_generic));
                    publish();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
    }
    @Override
    public void takeView(ObjectListFragment fragment){
        if(fragment != null){
            if(mFragment != fragment) {
                UNKNOWN = fragment.getString(R.string.error_title_generic);
                NOT_CONNECTED = fragment.getString(R.string.error_not_connected);
                FAILED_LOGIN = fragment.getString(R.string.error_auth_failed);
                mFragment = fragment;
                if (mCurUser == null) {
                    mCurUser = mFragment.mCurUser;
                    mToken = mFragment.mToken;
                }
                if(mCurUser.getCompany()!=null)
                    fetchList();
            }
        }
        else {
            mFragment = null;
            mCurUser = null;
            mToken = null;
        }
//        publish();
    }
    public void fetchList(){
        if(mCurUser.getCompany()!=null)
            if(!isRequest) {
                error = null;
                items = null;
                mServerRunnable = new Runnable() {
                    @Override
                    public void run() {
                        isRequest = true;
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.add("authToken", mToken);
                        String response = "";
                        try {
                            JSONObject json = new JSONObject();
                            json.put("listId", mCurUser.getCompany().getmListId());
                            String listIdJson = json.toString();
                            HttpEntity<String> entity = new HttpEntity<>(listIdJson, headers);

                            response = restTemplate.postForObject(FETCH_LIST_ADDRESS, entity, String.class);
                            isRequest = false;
                            Log.d("response", response);
                            if (!response.equals("invalidToken")) {
                                JSONObject jsonResponse = new JSONObject(response);
                                JSONArray itemsArray = new JSONArray(jsonResponse.getString("list"));
                                items = new ArrayList<>();
                                for (int i = 0; i < itemsArray.length(); i++)
                                    try {
                                        items.add(itemsArray.getString(i));
                                    } catch (JSONException e) {
                                        error = new Error(mFragment.getString(R.string.error_title_generic));
                                        publish();
                                    }
                                publish();

                            } else {
                                SharedPreferences sharedPref = mFragment.getActivity().getSharedPreferences("account", Context.MODE_PRIVATE);
                                String username = sharedPref.getString("username","");
                                String password = new String(Base64.decode(sharedPref.getString("password",""),Base64.DEFAULT));
                                Message msg = new Message();
                                msg.getData().putString("caller","fetchList");
                                login(username, password,msg);
//                                SharedPreferences sharedPref = mFragment.getActivity().getSharedPreferences("account", Context.MODE_PRIVATE);
//                                String username = sharedPref.getString("username", "");
//                                String password = new String(Base64.decode(sharedPref.getString("password", ""), Base64.DEFAULT));
//                                error = new Error(response);
//                                login(username, password);
                            }
                        } catch (Exception e) {
                            if(e.getMessage().contains("failed to connect")){
                                error = new Error(mFragment.getString(R.string.error_not_connected));
                            } else{
                                error = new Error(mFragment.getString(R.string.error_title_generic));
                            }
                            publish();
                            isRequest = false;

                        }
                    }
                };
                mServerThread = new Thread(mServerRunnable);
                mServerThread.start();
            }else{
                error = new Error(mFragment.getString(R.string.error_no_group_sync));
                publish();
            }

    }
    public void login(final String username, final String password, final Message callAfter){
        if(!isRequest) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    isRequest = true;
                    User user = new User(username, password);

                    String requestJson = null;
                    try {
                        requestJson = user.toJsonObject().toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
//                    String answer = restTemplate.postForObject(url, entity, String.class);
                    String response = "";
                    try {
                        response = restTemplate.postForObject(AUTH_ADDRESS, entity, String.class);
                        Log.d("response", response);
                        isRequest = false;
                        if (!response.equals("fail")) {

                            JSONObject responseJson = new JSONObject(response);
                            if (responseJson.has("token")) {
                                mToken = responseJson.getString("token");
                                callback.handleMessage(callAfter);
                            } else {
                                error = new Error(FAILED_LOGIN);
                                publish();
                            }

//                            mFragment.getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(mContext,"Erreur de connexion, veuillez vérifier votre connexion et réessayer plus tard",Toast.LENGTH_LONG).show();
//                                    showRetryDlg();
//                                }
//                            });
                        }
                    } catch (Exception e) {
                        isRequest = false;
                        if(e.getMessage().contains("failed to connect")) {
                            error = new Error(NOT_CONNECTED);
                        } else{
                            error = new Error(UNKNOWN);
                        }
                        publish();
                    }
                }
            };
            Thread t = new Thread(r);
            t.start();
        }
//        showLoadingDlg();
    }

    @Override
    public void publish()
    {
        if (mFragment != null) {
            if (error != null)
                mFragment.showError(error.getMessage());
            else if (items != null)
                mFragment.onNext(items);
        }
    }

}
