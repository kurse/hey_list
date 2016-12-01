package com.example.youssef.list;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.youssef.list.fireBase.FireBaseService;
import com.example.youssef.list.adapters.DrawerAdapter;
import com.example.youssef.list.fragments.AddUserFragment;
import com.example.youssef.list.fragments.CompanyCreationFragment;
import com.example.youssef.list.fragments.GroupsListFragment;
import com.example.youssef.list.fragments.ObjectListFragment;
import com.example.youssef.list.fragments.UsersListFragment;
import com.example.youssef.list.models.User;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.messaging.FirebaseMessaging;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;

public class MainActivity extends AppCompatActivity{

    public static int FRAGMENT_LIST = 10;
    public static int FRAGMENT_COMPANY_CREATION = 11;
    public static int FRAGMENT_GROUPS = 12;
//    public static String SERVER_URL = "http://192.168.178.22:8080";
    public static String SERVER_URL = "http://137.74.44.134:8080";
    private User mCurUser;
    boolean multigroup =false;

    @BindView(R.id.navList) ListView mDrawerList;
    private DrawerAdapter mAdapter;
    private boolean confirmation;
    public String mToken;
    public FireBaseService fbs;
//    FirebaseController fbc;

    public void setCurGroup(String curGroupId, String curGroupName, String curGroupListId, boolean isCreator){
        mCurUser.getCompany().switchCompany(curGroupId,curGroupName,curGroupListId);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment list = new ObjectListFragment();
        Bundle data = new Bundle();
        data.putString("token",mToken);
        data.putSerializable("user",mCurUser);
        list.setArguments(data);
        ft.replace(R.id.fragments_view,list)
                .addToBackStack("objectsList")
                .commit();
        mCurUser.setCreator(isCreator);
        addDrawerItems();
        TextView titleText = (TextView) findViewById(R.id.title_actionbar);
        titleText.setText(curGroupName);
        SharedPreferences prefs = getApplication().getSharedPreferences("account", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("orgId", mCurUser.getCompany().getmId());

        editor.commit();
        EventBus.getDefault().post("refresh");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("curUser",mCurUser);
        outState.putString("token",mToken);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);



        Toolbar tb = (Toolbar)findViewById(R.id.main_tb);
        fbs = new FireBaseService();
//        fbc = new FirebaseController(this);
//        mDrawerList = (ListView)findViewById(R.id.navList);
        if(mCurUser == null){
            if(savedInstanceState != null && savedInstanceState.containsKey("curUser")){
                mCurUser = (User)savedInstanceState.getSerializable("curUser");
                mToken = savedInstanceState.getString("token");
            }
            else
                mCurUser= new User();
        }
        addDrawerItems();
        try{
        if(getIntent().getExtras().containsKey("response")) {
            String response = getIntent().getExtras().getString("response");
            JSONObject jsonResponse;
            try {
                jsonResponse = new JSONObject(response);
                getIntent().removeExtra("response");
                if(jsonResponse.has("multiGroup")){
                    multigroup = true;
                }
                if (jsonResponse.has("user")) {
                    JSONObject userJson = new JSONObject(jsonResponse.getString("user"));
                    mCurUser.initFromJsonObject(userJson);
                    SharedPreferences prefs = getApplication().getSharedPreferences("account", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("username", mCurUser.getUsername());
                    if (jsonResponse.has("company")) {
                        mCurUser.setCompany(jsonResponse.getString("company"));

                        editor.putString("orgId", mCurUser.getCompany().getmId());
                    }
                    editor.commit();
                }
                if (jsonResponse.has("token"))
                    mToken = jsonResponse.getString("token");

//                try {
//                    String addresponse = fbc.addNotificationKey("321808001729",mCurUser.getCompany().getmName(),mCurUser.getId(),
//                            fbc.getFirebaseToken());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }}catch (Exception e){
            Log.d("error", e.getMessage());
        }
        String title;
        if(mCurUser.getCompany()!=null)
            title = mCurUser.getCompany().getmName();
        else
            title = "Hey!List";
        TextView titleText = (TextView) findViewById(R.id.title_actionbar);
        titleText.setText(title);
//        tb.setTitle(title);
//        tb.setTitleTextColor(Color.WHITE);
        setSupportActionBar(tb);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment list = new ObjectListFragment();
        Bundle data = new Bundle();
        data.putString("token",mToken);
        data.putSerializable("user",mCurUser);
        list.setArguments(data);
        ft.replace(R.id.fragments_view,list)
                .addToBackStack("objectsList")
                .commit();
        addDrawerItems();
        AppRate.with(this)
                .setInstallDays(0) // default 10, 0 means install day.
                .setLaunchTimes(5) // default 10
                .setRemindInterval(2) // default 1
                .setShowLaterButton(true) // default true
                .setDebug(false) // default false
                .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.
                    @Override
                    public void onClickButton(int which) {
//                        System.exit(0);
                    }
                })
                .monitor();
        AppRate.showRateDialogIfMeetsConditions(this);
    }

    private void showConfirmationDialog(String text){
        confirmation = false;
        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setTitle(getString(R.string.warning_title));
        dlg.setMessage(text);
        dlg.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Bundle bundle = new Bundle();
                Fragment companyCreation = new CompanyCreationFragment();
                bundle.putString("id", mCurUser.getId());
                companyCreation.setArguments(bundle);
                ft.addToBackStack(null);
                ft.replace(R.id.fragments_view, companyCreation)
                        .addToBackStack("companyCreation")
                        .commit();
            }
        });
        dlg.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dlg.show();
//        return confirmation;
    }
    private void addDrawerItems() {
        mAdapter = new DrawerAdapter(MainActivity.this);
        mDrawerList.setAdapter(mAdapter);
        if(mCurUser.getCompany()==null) {
            mAdapter.addDrawerITem(getString(R.string.create_group));
        }
        else{
            mAdapter.clearAll();
            if(mCurUser.isCreator())
                mAdapter.addDrawerITem(getString(R.string.add_users));
            if(multigroup)
                mAdapter.addDrawerITem(getString(R.string.switch_group));
            mAdapter.addDrawerITem(getString(R.string.users_list));
            mAdapter.addDrawerITem(getString(R.string.create_group));
//            else
//                mAdapter.addDrawerITem(getString(R.string.leave_group));

        }
//        mAdapter.addDrawerITem(getString(R.string.tutorial));
        mAdapter.addDrawerITem(getString(R.string.disconnect));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String optionItem = mDrawerList.getItemAtPosition(position).toString().trim();
//                if (position == 0) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Bundle bundle = new Bundle();
//                    String optionItem = mDrawerList.getItemAtPosition(position).toString().trim();
                    if(optionItem.equals(getString(R.string.create_group))){
//                        showConfirmationDialog(getString(R.string.warning_leave_group_creation));
                        Fragment companyCreation = new CompanyCreationFragment();
                        bundle.putString("id", mCurUser.getId());
                        companyCreation.setArguments(bundle);
                        ft.addToBackStack(null);
                        ft.replace(R.id.fragments_view, companyCreation)
                                .addToBackStack("companyCreation")
                                .commit();

                    }else if(optionItem.equals(getString(R.string.switch_group))){

                        bundle.putSerializable("curUser",mCurUser);
                        bundle.putString("curOrgId", mCurUser.getCompany().getmId());
                        bundle.putString("token",mToken);
                        Fragment orgsListFragment = new GroupsListFragment();
                        orgsListFragment.setArguments(bundle);
                        ft.replace(R.id.fragments_view,orgsListFragment)
                                .addToBackStack("getGroups")
                                .commit();

                    }else if(optionItem.equals(getString(R.string.users_list))){

                        Fragment usersList = new UsersListFragment();
                        bundle.putSerializable("curUser",mCurUser);
                        bundle.putString("token",mToken);
                        usersList.setArguments(bundle);
                        ft.addToBackStack(null);
                        ft.replace(R.id.fragments_view, usersList)
                                .addToBackStack("usersList")
                                .commit();
                    }else if(optionItem.equals(getString(R.string.add_users))) {

                        bundle.putString("adderName",mCurUser.getUsername());
                        bundle.putString("groupName",mCurUser.getCompany().getmName());
                        bundle.putString("listId",mCurUser.getCompany().getmListId());
                        bundle.putString("orgId", mCurUser.getCompany().getmId());
                        bundle.putString("token",mToken);
                        Fragment addUserFragment = new AddUserFragment();
                        addUserFragment.setArguments(bundle);
                        ft.replace(R.id.fragments_view,addUserFragment)
                                .addToBackStack("addUser")
                                .commit();
                    }
                    else if(optionItem.equals(getString(R.string.disconnect))){
                        Intent returnToStart = new Intent(getApplicationContext(),LoginActivity.class);
                        disconnect();
                        startActivity(returnToStart);
                        finish();
                    }
                    DrawerLayout dl = (DrawerLayout) findViewById(R.id.activity_main);
                    dl.closeDrawer(Gravity.LEFT);
//                }else if(optionItem.equals(getString(R.string.tutorial))){
////                    Intent tutorialActivity = new Intent(getApplication(),TutorialActivity.class);
////                    startActivity(tutorialActivity);
//                }else if(position == mAdapter.getCount()-1){
//                    Intent returnToStart = new Intent(getApplicationContext(),LoginActivity.class);
//                    disconnect();
//                    startActivity(returnToStart);
//                    finish();
//                }

            }
        });
    }
    public void disconnect(){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                String url = "http://137.74.44.134:8080/disconnect";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                try {
                    JSONObject json = new JSONObject();
                    json.put("token", mToken);
                    String listIdJson = json.toString();
                    HttpEntity<String> entity = new HttpEntity<>(listIdJson, headers);

                    restTemplate.postForObject(url, entity, String.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
        getSharedPreferences("account",MODE_PRIVATE).edit().clear().commit();
        if(mCurUser.getCompany()!=null)
            FirebaseMessaging.getInstance().unsubscribeFromTopic(mCurUser.getCompany().getmListId());

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.action_bar,menu);
        return super.onCreateOptionsMenu(menu);
    }


}
