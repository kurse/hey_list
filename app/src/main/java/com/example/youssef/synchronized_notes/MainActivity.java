package com.example.youssef.synchronized_notes;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.youssef.synchronized_notes.adapters.DrawerAdapter;
import com.example.youssef.synchronized_notes.fragments.CompanyCreationFragment;
import com.example.youssef.synchronized_notes.fragments.ObjectListFragment;
import com.example.youssef.synchronized_notes.models.User;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class MainActivity extends AppCompatActivity {
    private User mCurUser;
    private ListView mDrawerList;
    private DrawerAdapter mAdapter;
    private String mToken;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar tb = (Toolbar)findViewById(R.id.main_tb);
        setSupportActionBar(tb);
        mDrawerList = (ListView)findViewById(R.id.navList);
        if(mCurUser == null)
            mCurUser= new User();
        addDrawerItems();
        String response = getIntent().getExtras().getString("response");
        JSONObject jsonResponse;
        try {
            jsonResponse  = new JSONObject(response);
            if(jsonResponse.has("user")){
                JSONObject userJson = new JSONObject(jsonResponse.getString("user"));
                mCurUser.initFromJsonObject(userJson);
            }
            if(jsonResponse.has("token"))
                mToken = jsonResponse.getString("token");
            if(jsonResponse.has("company"))
                mCurUser.setCompany(jsonResponse.getString("company"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment list = new ObjectListFragment();
        Bundle data = new Bundle();
        data.putString("token",mToken);
        data.putSerializable("user",mCurUser);
//        data.putString("userId",mCurUser.getId());
//        if(mCurUser.g
        list.setArguments(data);
        ft.replace(R.id.fragments_view,list).commit();
        addDrawerItems();

    }

    private void addDrawerItems() {
        mAdapter = new DrawerAdapter(MainActivity.this);
        mDrawerList.setAdapter(mAdapter);
        if(mCurUser.getCompany()==null) {
            mAdapter.addDrawerITem("Créer son entreprise");
        }
        else{
            mAdapter.clearAll();
            if(mCurUser.isCreator())
                mAdapter.addDrawerITem("Ajouter un utilisateur");
        }
        mAdapter.addDrawerITem("Se Déconnecter");
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    if(!mCurUser.isCreator()){
                        FragmentManager fm = getFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        Fragment companyCreation = new CompanyCreationFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("id", mCurUser.getId());
                        companyCreation.setArguments(bundle);
                        ft.replace(R.id.fragments_view, companyCreation).commit();
                        DrawerLayout dl = (DrawerLayout) findViewById(R.id.activity_main);
                        dl.closeDrawer(Gravity.LEFT);
                    } else {

                    }

                }
                else if(position == mAdapter.getCount()-1){
                    Intent returnToStart = new Intent(getApplicationContext(),LoginActivity.class);
                    disconnect();
                    startActivity(returnToStart);
                    finish();
                }
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
                String url = "http://192.168.1.11:8080/disconnect";

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
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar,menu);
        return super.onCreateOptionsMenu(menu);
    }


}
