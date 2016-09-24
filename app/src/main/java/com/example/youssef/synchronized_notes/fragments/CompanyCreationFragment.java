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

import com.example.youssef.synchronized_notes.MainActivity;
import com.example.youssef.synchronized_notes.R;
import com.example.youssef.synchronized_notes.models.Company;

import org.json.JSONException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Youssef on 8/22/2016.
 */

public class CompanyCreationFragment extends Fragment {
    RestTemplate restTemplate = new RestTemplate();

    private EditText mCompanyName;
    private Button mButtonCreate;
    private String mUserId;
    private Context mContext;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_company_creation,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        mUserId = this.getArguments().getString("id");
        this.mContext = view.getContext();
        mCompanyName = (EditText) view.findViewById(R.id.company_name);
        mButtonCreate = (Button) view.findViewById(R.id.create_company_button);
        mButtonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }
    private void register(){
        Runnable r = new Runnable() {
            @Override
            public void run() {

                Company company = new Company(mCompanyName.getText().toString(),mUserId);

                String url = "http://192.168.1.11:8080/newgroup";
                String requestJson = company.toJSOnObject().toString();

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
                        Toast.makeText(mContext,"Cette entreprise existe déjà",Toast.LENGTH_LONG).show();
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
