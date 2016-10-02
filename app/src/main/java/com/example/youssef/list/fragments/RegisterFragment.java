package com.example.youssef.list.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.youssef.list.MainActivity;
import com.example.youssef.list.R;
import com.example.youssef.list.models.User;

import org.json.JSONException;
import org.json.JSONObject;
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

    public static int ADD_NEW_USER = 1;

    RestTemplate restTemplate = new RestTemplate();
    private String mToken;
    private String mCompanyId;
    private String mListId;
    private Context mContext;
    private boolean addingNewMode = false;
    private Button mCreateGroup;
    private Button mRegister;
    private EditText mUserName;
    private EditText mPassword;
    private EditText mPassConfirm;
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
        if(mPassword.getText().toString().length()>=8) {
            if(mPassword.getText().toString().equals(mPassConfirm.getText().toString())) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {

                        User user = new User(mUserName.getText().toString(), mPassword.getText().toString());


                        String url = "http://137.74.44.134:8080/register";
                        String requestJson = null;
                        try {
                            requestJson = user.toJsonObject().toString();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);

                        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
                        String response = "";
                        try {
                            response = restTemplate.postForObject(url, entity, String.class);
                            Log.d("response", response);
                            if (!response.equals("exists")) {
                                if (addingNewMode) {
                                    addUserServ(mUserName.getText().toString());
                                } else {
                                    Intent main = new Intent(mContext, MainActivity.class);
                                    SharedPreferences prefs = getActivity().getSharedPreferences("account", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("username", mUserName.getText().toString());
                                    editor.putString("password", Base64.encodeToString(mPassword.getText().toString().getBytes(), Base64.DEFAULT));
                                    editor.putBoolean("connected", true);
                                    editor.commit();
                                    main.putExtra("response", response);
                                    startActivity(main);
                                    getActivity().finish();
                                }

                            } else {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mUserName.setError("Ce nom d'utilisateur est déjà pris");
                                    }
                                });
                            }
                        } catch (Exception e) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext, "Erreur de connexion, veuillez vérifier votre connexion et réessayer plus tard", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                };
                Thread t = new Thread(r);
                t.start();
            }
            else
                mPassConfirm.setError("Les mots de passes ne correspondent pas");
        }
        else
            mPassword.setError("Mot de passe trop court (8 caractères minimum)");
    }
    private void addUserServ(final String userName){
        Runnable r = new Runnable() {
            @Override
            public void run() {

                RestTemplate restTemplate = new RestTemplate();
                String url = "http://137.74.44.134:8080/addUserGroup";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("authToken",mToken);
                String response="";
                try {
                    JSONObject json = new JSONObject();
                    json.put("orgId",mCompanyId);
                    json.put("listId",mListId);
                    json.put("userName",userName);
                    String listIdJson = json.toString();
                    HttpEntity<String> entity = new HttpEntity<>(listIdJson,headers);

                    response = restTemplate.postForObject(url, entity, String.class);
                    Log.d("response", response);
                    if (!response.equals("exists")) {
                        JSONObject jsonResponse = new JSONObject(response);
                        if(jsonResponse.getString("result").equals("ok")) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext, "L'utilisateur a été bien ajouté", Toast.LENGTH_LONG).show();
                                    getFragmentManager().popBackStack("objectsList",0);
                                }
                            });
                        }
                        else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext, "Erreur", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                for(int i=0;i<array.length();i++)
//                                    try {
//                                        if(!mObjectsAdapter.contains(array.getString(i)))
//                                            mObjectsAdapter.addItem(array.getString(i));
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//                            }
//                        });
                    }
                    else
                        Toast.makeText(mContext,"Erreur",Toast.LENGTH_LONG).show();
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
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        mPassword = (EditText) view.findViewById(R.id.password);
        mPassConfirm = (EditText) view.findViewById(R.id.password_confirm);
        mUserName = (EditText) view.findViewById(R.id.username);
        mRegister = (Button) view.findViewById(R.id.create_account);
        mContext = view.getContext();
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
        Button back = (Button)view.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        if(getArguments()!=null){
            ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
            mRegister.setText("Enregistrer");
            addingNewMode = true;
            mCompanyId = getArguments().getString("orgId");
            mListId = getArguments().getString("listId");
            mToken = getArguments().getString("token");
        }

    }
    @Override
    public void onStop() {
        if(getArguments()!=null)
            ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        super.onStop();
    }
}
