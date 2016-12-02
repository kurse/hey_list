package com.example.youssef.list.presenters;

import com.example.youssef.list.MainController.MessageEvent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.youssef.list.MainActivity;
import com.example.youssef.list.MainController;
import com.example.youssef.list.R;
import com.example.youssef.list.fragments.ObjectListFragment;
import com.example.youssef.list.interfaces.Contract;
import com.example.youssef.list.interfaces.ServerApi;
import com.example.youssef.list.models.Item;
import com.example.youssef.list.models.User;
import com.google.firebase.messaging.FirebaseMessaging;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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
    private ArrayList<String> orgs;
    private ArrayList<Boolean> checkedItems;
    RestTemplate restTemplate = new RestTemplate();
    private Boolean justLoggedIn = true;
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(MainActivity.SERVER_URL)
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    ServerApi serverApi = retrofit.create(ServerApi.class);

    Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.getData().getString("caller").equals("fetchList"))
                fetchListRetrofit();
            else if(msg.getData().getString("caller").equals("addItem")){
                addItemRetrofit(msg.getData().getString("item"));
            }
            else if(msg.getData().getString("caller").equals("removeItem")){
                removeItemRetrofit(msg.getData().getString("item"));
            }
            return true;
        }
    };

    Runnable mServerRunnable;
    Thread mServerThread;

//    private String mToken;
    private User mCurUser;

    private Throwable error;
    private ObjectListFragment mFragment;
    private boolean registered = false;
    public void removeItemRetrofit(final String item){
        if(!isRequest) {
            error = null;
            orgs = null;
            isRequest = true;
            try {
                JSONObject json = new JSONObject();
                json.put("listId",mCurUser.getCompany().getmListId());
                json.put("item",item);
                json.put("username",mCurUser.getUsername());
                final Observable<String> removeObservable = serverApi.removeItem(getToken(), json.toString());
                Observer removeObserver = new Observer() {
                    @Override
                    public void onCompleted() {
                        isRequest=false;
                        removeObservable.unsubscribeOn(Schedulers.newThread());
                    }

                    @Override
                    public void onError(Throwable e) {
                        isRequest=false;
                        if(e.getMessage().contains("ailed to connect")){
                            error = new Error(mFragment.getString(R.string.error_not_connected));
                        } else{
                            error = new Error(mFragment.getString(R.string.error_title_generic));
                        }
                        publish();
                        isRequest = false;
                    }

                    @Override
                    public void onNext(Object o) {
                        try {
                            error = null;
                            String response = o.toString();
                            isRequest = false;
                            Log.d("response", response);
                            if (!response.equals(ERROR_TOKEN_DETECT)) {
                                JSONObject jsonResponse = new JSONObject(response);
                                final JSONArray itemsArray = new JSONArray(jsonResponse.getString("list"));
                                orgs = new ArrayList<>();
                                checkedItems = new ArrayList<>();
                                for (int i = 0; i < itemsArray.length(); i++)
                                    try {
                                        JSONObject item = new JSONObject(itemsArray.getString(i));
                                        orgs.add(item.getString("name"));

                                        if(item.getBoolean("checked"))
                                            checkedItems.add(true);
                                        else
                                            checkedItems.add(false);
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
                                loginRetrofit(username, password, msg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                removeObservable.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(removeObserver);
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }


    public void addItemRetrofit(final String item){
        if(!isRequest) {
            error = null;
            orgs = null;
            isRequest = true;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("authToken", getToken());
            String response = "";
            try {
                JSONObject json = new JSONObject();
                json.put("listId",mCurUser.getCompany().getmListId());
                json.put("item",item);
                json.put("username",mCurUser.getUsername());
                final Observable<String> addObservable = serverApi.addItem(getToken(), json.toString());
                Observer listObserver = new Observer() {
                    @Override
                    public void onCompleted() {
                        isRequest=false;
                        addObservable.unsubscribeOn(Schedulers.newThread());
                    }

                    @Override
                    public void onError(Throwable e) {
                        isRequest=false;
                        if(e.getMessage().contains("ailed to connect")){
                            error = new Error(mFragment.getString(R.string.error_not_connected));
                        } else{
                            error = new Error(mFragment.getString(R.string.error_title_generic));
                        }
                        publish();
                        isRequest = false;
                    }

                    @Override
                    public void onNext(Object o) {
                        try {
                            error = null;
                            String response = o.toString();
                            isRequest = false;
                            Log.d("response", response);
                            if (!response.equals(ERROR_TOKEN_DETECT)) {

                                JSONObject jsonResponse = new JSONObject(response);
                                final JSONArray itemsArray = new JSONArray(jsonResponse.getString("list"));

                                orgs = new ArrayList<>();
                                checkedItems = new ArrayList<>();
                                for (int i = 0; i < itemsArray.length(); i++)
                                    try {
                                        JSONObject item = new JSONObject(itemsArray.getString(i));
                                        orgs.add(item.getString("name"));
                                        if(item.getBoolean("checked"))
                                            checkedItems.add(true);
                                        else
                                            checkedItems.add(false);
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
                                loginRetrofit(username, password, msg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                addObservable.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(listObserver);
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public void addItemDB(final String item){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                error = null;
                orgs = null;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("authToken",getToken());
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

                        orgs = new ArrayList<>();
                        for (int i = 0; i < itemsArray.length(); i++)
                            try {
                                orgs.add(itemsArray.getString(i));
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
                        loginRetrofit(username, password, msg);
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
                        loginRetrofit(username, password, msg);

                    }
//                        error = new Error(mFragment.getString(R.string.error_title_generic));
                    publish();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MainController.MessageEvent event) {
            if(event.message!=null){
                if(event.message.equals("refresh")){
                    fetchListRetrofit();
                }
            }

    }
    @Override
    public void takeView(ObjectListFragment fragment){
        if(fragment != null){
            if(mFragment != fragment) {
                if(!registered){
                    registered = true;
                    EventBus.getDefault().register(this);
                }
                MessageEvent.lp = this;
                UNKNOWN = fragment.getString(R.string.error_title_generic);
                NOT_CONNECTED = fragment.getString(R.string.error_not_connected);
                FAILED_LOGIN = fragment.getString(R.string.error_auth_failed);
                mFragment = fragment;
                if (mCurUser == null) {
                    mCurUser = mFragment.mCurUser;
                    if(justLoggedIn){
                        justLoggedIn = false;
                        Toast.makeText(fragment.getActivity(),fragment.getResources().getString(R.string.welcome_message)+mCurUser.getUsername(),Toast.LENGTH_LONG).show();
                    }
                }
                if(mCurUser.getCompany()!=null) {
                    fetchListRetrofit();
                    FirebaseMessaging.getInstance().subscribeToTopic(mCurUser.getCompany().getmListId());
                }
            }
        }
        else {
            mFragment = null;
            mCurUser = null;
        }
//        publish();
    }
    public void checkRetrofit(final String item, final boolean checked){
            if(!isRequest) {
                error = null;
                orgs = null;
                isRequest = true;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("authToken", getToken());
                try {
                    JSONObject json = new JSONObject();
                    json.put("listId",mCurUser.getCompany().getmListId());
                    json.put("item",item);
                    json.put("checked",checked);
                    json.put("username",mCurUser.getUsername());
                    final Observable<String> checkObservable = serverApi.checkItem(getToken(), json.toString());
                    Observer listObserver = new Observer() {
                        @Override
                        public void onCompleted() {
                            isRequest=false;
                            checkObservable.unsubscribeOn(Schedulers.newThread());
                        }

                        @Override
                        public void onError(Throwable e) {
                            isRequest=false;
                            if(e.getMessage().contains("ailed to connect")){
                                error = new Error(mFragment.getString(R.string.error_not_connected));
                            } else{
                                error = new Error(mFragment.getString(R.string.error_title_generic));
                            }
                            publish();
                            isRequest = false;
                        }

                        @Override
                        public void onNext(Object o) {
                            try {
                                error = null;
                                String response = o.toString();
                                isRequest = false;
                                Log.d("response", response);
                                if (!response.equals(ERROR_TOKEN_DETECT)) {

                                    JSONObject jsonResponse = new JSONObject(response);
                                    final JSONArray itemsArray = new JSONArray(jsonResponse.getString("list"));

                                    orgs = new ArrayList<>();
                                    checkedItems = new ArrayList<>();
                                    for (int i = 0; i < itemsArray.length(); i++)
                                        try {
                                            JSONObject item = new JSONObject(itemsArray.getString(i));
                                            orgs.add(item.getString("name"));
                                            if(item.getBoolean("checked"))
                                                checkedItems.add(true);
                                            else
                                                checkedItems.add(false);
                                        } catch (JSONException e) {
                                            error = new Error(mFragment.getString(R.string.error_title_generic));
                                            publish();
                                        }
                                    publish();
                                }
                                else{
                                    Message msg = new Message();
                                    msg.getData().putString("caller","checkItem");
                                    msg.getData().putString("item",item);
                                    msg.getData().putBoolean("checked",checked);
                                    SharedPreferences sharedPref = mFragment.getActivity().getSharedPreferences("account", Context.MODE_PRIVATE);
                                    String username = sharedPref.getString("username","");
                                    String password = new String(Base64.decode(sharedPref.getString("password",""),Base64.DEFAULT));
                                    loginRetrofit(username, password, msg);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    checkObservable.subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(listObserver);
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }

    }
    public void fetchListRetrofit(){
        if(mCurUser.getCompany()!=null)
            if(!isRequest) {
                error = null;
                orgs = null;
                isRequest = true;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("authToken", getToken());
                String response = "";
                try {
                    JSONObject json = new JSONObject();
                    json.put("listId", mCurUser.getCompany().getmListId());
//                    String listIdJson = json.toString();

                    final Observable<String> listObservable = serverApi.getList(getToken(), json.toString());
                    Observer listObserver = new Observer() {
                        @Override
                        public void onCompleted() {
                            isRequest=false;
                            if(mFragment != null && mFragment.mObjectsListLayout.isRefreshing())
                                mFragment.mObjectsListLayout.setRefreshing(false);
                            listObservable.unsubscribeOn(Schedulers.newThread());
                        }

                        @Override
                        public void onError(Throwable e) {
                            isRequest=false;
                            if(mFragment.mObjectsListLayout.isRefreshing())
                                mFragment.mObjectsListLayout.setRefreshing(false);
                            if(e.getMessage().contains("ailed to connect")){
                                error = new Error(mFragment.getString(R.string.error_not_connected));
                            } else{
                                error = new Error(mFragment.getString(R.string.error_title_generic));
                            }
                            publish();
                        }

                        @Override
                        public void onNext(Object o) {
                            try {
                                error = null;
                                String response = o.toString();
                                isRequest = false;
                                Log.d("response", response);
                                if (!response.equals("invalidToken")) {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    JSONArray itemsArray = new JSONArray(jsonResponse.getString("list"));
                                    checkedItems = new ArrayList<>();
                                    orgs = new ArrayList<>();
                                    for (int i = 0; i < itemsArray.length(); i++)
                                        try {
                                            JSONObject item = new JSONObject(itemsArray.getString(i));
                                            orgs.add(item.getString("name"));
                                            Item curItem = new Item();
                                            curItem.setItemName(item.getString("name"));
                                            curItem.save();
                                            if(item.getBoolean("checked"))
                                                checkedItems.add(true);
                                            else
                                                checkedItems.add(false);
                                        } catch (JSONException e) {
                                            error = new Error(mFragment.getString(R.string.error_title_generic));
                                            publish();
                                        }
                                    publish();

                                } else {
                                    SharedPreferences sharedPref = mFragment.getActivity().getSharedPreferences("account", Context.MODE_PRIVATE);
                                    String username = sharedPref.getString("username", "");
                                    String password = new String(Base64.decode(sharedPref.getString("password", ""), Base64.DEFAULT));
                                    Message msg = new Message();
                                    msg.getData().putString("caller", "fetchList");
                                    loginRetrofit(username, password, msg);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    listObservable.subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(listObserver);
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
    }

    public void loginRetrofit(String username, String password, final Message callAfter){
        if(!isRequest){
            error = null;
            User user = new User(username,password,"");

            try {
//                    HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//                    interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//                    OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
                JSONObject json = new JSONObject();
                json.put("username",username);
                json.put("password",password);
                if(user.getCompany()!=null)
                    json.put("orgID",user.getCompany().getmId());
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(MainActivity.SERVER_URL)
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                ServerApi serverApi = retrofit.create(ServerApi.class);
                final Observable<String> auth = serverApi.auth(json.toString());
                Observer responseObserver = new Observer() {
                    @Override
                    public void onCompleted() {
                        auth.unsubscribeOn(Schedulers.newThread());
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(e.getMessage().contains("ailed to connect")){
                            error = new Error(mFragment.getString(R.string.error_not_connected));
                        } else{
                            error = new Error(mFragment.getString(R.string.error_title_generic));
                        }
                        publish();
                    }
                    @Override
                    public void onNext(Object o) {
                        if( mFragment!= null && mFragment.dbRetry!=null && mFragment.dbRetry.isShowing())
                            mFragment.dbRetry.dismiss();
                        error = null;
                        String responseStr = o.toString();
                        JSONObject responseJson = null;
                        try {
                            responseJson = new JSONObject(responseStr);
                            if (responseJson.has("token")) {

                                setToken(responseJson.getString("token"));
                                callback.handleMessage(callAfter);
                            } else {
                                error = new Error(FAILED_LOGIN);
                                publish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                };
                auth.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(responseObserver);


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public void setToken(String token){
        ((MainActivity)mFragment.getActivity()).mToken = token;
    }
    public String getToken(){
        return ((MainActivity)mFragment.getActivity()).mToken;
    }
    @Override
    public void publish()
    {
        if (mFragment != null) {
            if (error != null)
                mFragment.showError(error.getMessage());
            else if (orgs != null) {
                mFragment.onNext(orgs, checkedItems);
            }
        }
    }

    public void uncheckItem(int position) {

    }

    public void checkItem(int position) {

    }
}
