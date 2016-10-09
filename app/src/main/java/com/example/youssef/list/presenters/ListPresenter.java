package com.example.youssef.list.presenters;

import android.util.Log;
import android.widget.Toast;

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

    public static String TAG = "ListPresenter";
    public static String FETCH_LIST_ADDRESS = "http://137.74.44.134:8080/getList";
    public static String ADD_ITEM_ADDRESS = "http://137.74.44.134:8080/addItem";
    public static String REMOVE_ITEM_ADDRESS = "http://137.74.44.134:8080/removeItem";
    public static String AUTH_ADDRESS = "http://137.74.44.134:8080/auth";
    private Boolean isRequest = false;
    private ArrayList<String> items;
    RestTemplate restTemplate = new RestTemplate();

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
                    if (!response.equals("exists")) {
                        JSONObject jsonResponse = new JSONObject(response);
                        final JSONArray itemsArray = new JSONArray(jsonResponse.getString("list"));
                        items = new ArrayList<>();
                        for (int i = 0; i < itemsArray.length(); i++)
                            try {
                                items.add(itemsArray.getString(i));
                            } catch (JSONException e) {
                                error = new Error("Erreur serveur, veuillez réessayer");
                                publish();
                            }
                        publish();
                    }
                    else{
                        error = new Error("Erreur");
                        publish();
                    }
//                        Toast.makeText(mContext,"Erreur de connexion",Toast.LENGTH_LONG).show();
                }catch (Exception e){
                    error = new Error("Erreur serveur, veuillez réessayer");
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
                    if (!response.equals("exists")) {

                        JSONObject jsonResponse = new JSONObject(response);
                        final JSONArray itemsArray = new JSONArray(jsonResponse.getString("list"));

                        items = new ArrayList<>();
                        for (int i = 0; i < itemsArray.length(); i++)
                            try {
                                items.add(itemsArray.getString(i));
                            } catch (JSONException e) {
                                error = e;
                                publish();
                            }
                        publish();
                    }
                    else{
                        error = new Error(response);
                        publish();
                    }
//                        Toast.makeText(mContext,"Erreur de connexion",Toast.LENGTH_LONG).show();
                }catch (Exception e){
                    error = e;
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
                mFragment = fragment;
                if (mCurUser == null) {
                    mCurUser = mFragment.mCurUser;
                    mToken = mFragment.mToken;
                }
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
                                        error = e;
                                        publish();
                                    }
                                publish();

                            } else {
//                                SharedPreferences sharedPref = mFragment.getActivity().getSharedPreferences("account", Context.MODE_PRIVATE);
//                                String username = sharedPref.getString("username", "");
//                                String password = new String(Base64.decode(sharedPref.getString("password", ""), Base64.DEFAULT));
                                error = new Error(response);
//                                login(username, password);
                            }
                        } catch (Exception e) {
                            isRequest = false;
                            error = e;
                            publish();
                        }
                    }
                };
                mServerThread = new Thread(mServerRunnable);
                mServerThread.start();
            }

    }
    public void login(final String username, final String password){
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
                                fetchList();
                            } else {
                                error = new Error(response);
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
                        error = e;
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
