package com.example.youssef.list.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.youssef.list.MainActivity;
import com.example.youssef.list.R;
import com.example.youssef.list.models.User;

import org.json.JSONException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Youssef on 8/20/2016.
 */

public class LoginFragment extends Fragment {
    RestTemplate restTemplate = new RestTemplate();
    AlertDialog dba;
    private Context mContext;
    @BindView(R.id.login) Button mLoginButton;
    @BindView(R.id.register) Button mRegisterButton;
    @BindView(R.id.username) EditText mUserNameT;
    @BindView(R.id.password) EditText mPasswordT;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        mContext = getActivity();
        ButterKnife.bind(this,view);
//        mLoginButton = (Button) view.findViewById(R.id.login);
//        mRegisterButton = (Button) view.findViewById(R.id.register);
//        mUserNameT = (EditText) view.findViewById(R.id.name);
//        mPasswordT = (EditText) view.findViewById(R.id.password);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(mUserNameT.getText().toString(), mPasswordT.getText().toString());
            }
        });
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Fragment list = new RegisterFragment();
                ft.addToBackStack(null);
                ft.replace(R.id.fragment_holder, list)
                        .addToBackStack("register")
                        .commit();

            }
        });
        SharedPreferences sharedPref = getActivity().getSharedPreferences("account",Context.MODE_PRIVATE);
        boolean connected = sharedPref.getBoolean("connected",false);
        if(connected) {
            String username = sharedPref.getString("username","");
            String password = new String(Base64.decode(sharedPref.getString("password",""),Base64.DEFAULT));
            login(username,password);
        }


    }
    private void login(final String username, final String password){

        Runnable r = new Runnable() {
            @Override
            public void run() {

                User user = new User(username, password);


                String url = "http://137.74.44.134:8080/auth";
                String requestJson = null;
                try {
                    requestJson = user.toJsonObject().toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> entity = new HttpEntity<>(requestJson,headers);
//                    String answer = restTemplate.postForObject(url, entity, String.class);
                String response="";
                try {
                    response = restTemplate.postForObject(url, entity, String.class);
                    Log.d("response", response);
                    if(response.equals("none"))
                    {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext,getString(R.string.error_user_not_exist),Toast.LENGTH_LONG).show();
                                dba.dismiss();
                            }
                        });
                    }
                    else{
                        if (!response.equals("fail")) {
//                JSONObject json = new JSONObject(response);
//                User loggedInUser = new User();
//                loggedInUser.initFromJsonObject(json);
                            Intent main = new Intent(mContext, MainActivity.class);
                            main.putExtra("response", response);
                            SharedPreferences prefs = getActivity().getSharedPreferences("account",Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("username", username);
                            editor.putString("password", Base64.encodeToString(password.getBytes(), Base64.DEFAULT));
                            editor.putBoolean("connected",true);
                            editor.commit();
                            dba.dismiss();
                            startActivity(main);
                            getActivity().finish();
                        } else{
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mPasswordT.setError(getString(R.string.error_wrong_password));
                                    dba.dismiss();
                                }
                            });
                        }
                    }
                }catch (Exception e){
                    if(getActivity() != null)
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext,getString(R.string.error_not_connected),Toast.LENGTH_LONG).show();
                            dba.dismiss();
                        }
                    });
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
        showLoadingDlg();
    }
    private void showLoadingDlg(){
        final AlertDialog.Builder db = new AlertDialog.Builder(mContext);
        db.setTitle(getString(R.string.connecting));
        db.setMessage(getString(R.string.wait_msg));
        dba = db.create();
        dba.show();
        TextView title = (TextView)dba.findViewById(android.R.id.title);
        if(title!=null)
            title.setGravity(Gravity.CENTER);
        TextView msg = (TextView)dba.findViewById(android.R.id.message);
        if(msg!=null)
            msg.setGravity(Gravity.CENTER);
    }

    @Override
    public void onDestroy() {
        try {
            if (dba != null && dba.isShowing()) {
                dba.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
