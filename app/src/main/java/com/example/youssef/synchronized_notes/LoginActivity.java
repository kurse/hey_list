package com.example.youssef.synchronized_notes;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.youssef.synchronized_notes.fragments.LoginFragment;
import com.example.youssef.synchronized_notes.fragments.ObjectListFragment;

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment list = new LoginFragment();
        ft.replace(R.id.fragment_holder,list).commit();

    }


}
