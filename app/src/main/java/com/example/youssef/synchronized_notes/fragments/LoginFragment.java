package com.example.youssef.synchronized_notes.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.youssef.synchronized_notes.BCrypt;
import com.example.youssef.synchronized_notes.MainActivity;
import com.example.youssef.synchronized_notes.R;
import com.example.youssef.synchronized_notes.models.User;

import org.json.JSONException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Youssef on 8/20/2016.
 */

public class LoginFragment extends Fragment {

    private Context mContext;
    private Button mLoginButton;
    private Button mRegisterButton;
    private EditText mUserNameT;
    private EditText mPasswordT;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = view.getContext();
        mLoginButton = (Button)view.findViewById(R.id.login);
        mRegisterButton = (Button)view.findViewById(R.id.register);
        mUserNameT = (EditText)view.findViewById(R.id.name);
        mPasswordT = (EditText)view.findViewById(R.id.password);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Fragment list = new RegisterFragment();
                ft.addToBackStack(null);
                ft.replace(R.id.fragment_holder,list).commit();

            }
        });
    }
    private void login(){
//        String encryptedPwd = Encryption.encrypt(mPasswordT.getText().toString(),"");


// Add the Jackson and String message converters
//        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
//        MediaType mt = new MediaType("*", "json", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET);
//        MediaType mt2 = new MediaType("*", "javascript", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET);
//        jsonConverter.setSupportedMediaTypes(new List().);


// Make the HTTP POST request, marshaling the request to JSON, and the response to a String

        Runnable r = new Runnable() {
            @Override
            public void run() {

//                User user = new User(mUserNameT.getText().toString(), mPasswordT.getText().toString());
                User user = new User("test1","1234");
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

                String url = "http://192.168.0.20:8080/auth";
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
                    if (!response.equals("fail")) {
//                JSONObject json = new JSONObject(response);
//                User loggedInUser = new User();
//                loggedInUser.initFromJsonObject(json);
                        Intent main = new Intent(mContext, MainActivity.class);
                        main.putExtra("response", response);
                        startActivity(main);
                        getActivity().finish();
                    }
                }catch (Exception e){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext,"Erreur de connexion, veuillez vérifier votre connexion et réessayer plus tard",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        Thread t = new Thread(r);
        t.start();


    }
}
