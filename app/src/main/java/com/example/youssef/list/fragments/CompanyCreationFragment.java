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
import com.example.youssef.list.interfaces.ServerApi;
import com.example.youssef.list.models.Company;
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
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Youssef on 8/22/2016.
 */

public class CompanyCreationFragment extends Fragment {
    RestTemplate restTemplate = new RestTemplate();
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(MainActivity.SERVER_URL)
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    ServerApi serverApi = retrofit.create(ServerApi.class);
    @BindView(R.id.company_name) EditText mCompanyName;
    @BindView(R.id.create_company_button) Button mButtonCreate;
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
        ButterKnife.bind(this,view);
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        mUserId = this.getArguments().getString("id");
        this.mContext = view.getContext();
//        mCompanyName = (EditText) view.findViewById(R.id.company_name);
//        mButtonCreate = (Button) view.findViewById(R.id.create_company_button);
        mButtonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCompanyName.getText().toString().length()==0){

                    mCompanyName.setError(getResources().getString(R.string.empty_error));
                    mCompanyName.requestFocus();
                }
                else
                    newGroupRetrofit();
            }
        });
        Button back = (Button)view.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
    }
    private void newGroupRetrofit(){

        Company company = new Company(mCompanyName.getText().toString(),mUserId);
        String json = company.toJSOnObject().toString();

        final Observable<String> createObservable = serverApi.newGroup(json.toString());
        Observer createObserver = new Observer() {
            @Override
            public void onCompleted() {
                createObservable.unsubscribeOn(Schedulers.newThread());
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(mContext, getString(R.string.error_not_connected), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNext(Object o) {
                String response = o.toString();
                if (!response.equals("exists")) {
                    Intent main = new Intent(mContext, MainActivity.class);
                    main.putExtra("response", response);
                    startActivity(main);
                    getActivity().finish();
                }
                else
                    Toast.makeText(mContext,getString(R.string.error_group_exists),Toast.LENGTH_LONG).show();

            }
        };
        createObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(createObserver);

    }
    @Override
    public void onStop() {
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        super.onStop();
    }


}
