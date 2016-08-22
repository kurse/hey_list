package com.example.youssef.synchronized_notes;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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

public class MainActivity extends AppCompatActivity {
    private User mCurUser;
    private ListView mDrawerList;
    private DrawerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment list = new ObjectListFragment();
        ft.replace(R.id.fragments_view,list).commit();
        Toolbar tb = (Toolbar)findViewById(R.id.main_tb);
        setSupportActionBar(tb);
        mDrawerList = (ListView)findViewById(R.id.navList);
        mCurUser = new User();
        addDrawerItems();
        String response = getIntent().getExtras().getString("response");
        JSONObject jsonResponse;
        try {
            jsonResponse  = new JSONObject(response);
            if(jsonResponse.has("user")){
                JSONObject userJson = new JSONObject(jsonResponse.getString("user"));
                mCurUser.initFromJsonObject(userJson);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addDrawerItems() {
        mAdapter = new DrawerAdapter(MainActivity.this);
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    Fragment companyCreation = new CompanyCreationFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("id",mCurUser.getId());
                    companyCreation.setArguments(bundle);
                    ft.replace(R.id.fragments_view,companyCreation).commit();
                    DrawerLayout dl = (DrawerLayout) findViewById(R.id.activity_main);
                    dl.closeDrawer(Gravity.LEFT);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar,menu);
        return super.onCreateOptionsMenu(menu);
    }


}
