package com.example.youssef.synchronized_notes.fragments;

import android.app.Fragment;
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

public class RegisterFragment extends Fragment {

    private Context mContext;
    private Button mCreateGroup;
    private Button mRegister;
    private EditText mUserName;
    private EditText mPassword;
    private EditText mGroupName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register,container,false);
    }
    private void register(){
        Runnable r = new Runnable() {
            @Override
            public void run() {

                User user = new User(mUserName.getText().toString(), BCrypt.hashpw(mPassword.getText().toString(),BCrypt.gensalt()));
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

                String url = "http://192.168.0.20:8080/register";
                String requestJson = null;
                try {
                    requestJson = user.toJsonObject().toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> entity = new HttpEntity<>(requestJson,headers);
                String response="";
                try {
                    response = restTemplate.postForObject(url, entity, String.class);
                    Log.d("response", response);
                    if (!response.equals("exists")) {

                        Intent main = new Intent(mContext, MainActivity.class);
                        main.putExtra("response", response);
                        startActivity(main);
                        getActivity().finish();
                    }
                    else
                        Toast.makeText(mContext,"Ce nom d'utilisateur est déjà pris",Toast.LENGTH_LONG).show();
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
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPassword = (EditText) view.findViewById(R.id.password);
        mUserName = (EditText) view.findViewById(R.id.username);
        mRegister = (Button) view.findViewById(R.id.create_account);
        mContext = view.getContext();
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }
}
