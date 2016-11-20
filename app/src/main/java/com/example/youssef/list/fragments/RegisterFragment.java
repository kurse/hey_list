package com.example.youssef.list.fragments;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
import com.example.youssef.list.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

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
    @BindView(R.id.create_account) Button mRegister;
    @BindView(R.id.register_username) EditText mUserName;
    @BindView(R.id.register_password) EditText mPassword;
    @BindView(R.id.email_address) EditText mEmailAddress;
    @BindView(R.id.password_confirm) EditText mPassConfirm;

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(MainActivity.SERVER_URL)
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    ServerApi serverApi = retrofit.create(ServerApi.class);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register,container,false);
    }
    private void registerRetrofit(){

            User user = new User(mUserName.getText().toString(), mPassword.getText().toString(), mEmailAddress.getText().toString());
            String json = null;
            try {
                json = user.toJsonObject().toString();
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
            final Observable<String> registerObservable = serverApi.register(json.toString());
            Observer registerObserver = new Observer() {
                @Override
                public void onCompleted() {
                    registerObservable.unsubscribeOn(Schedulers.newThread());
                }

                @Override
                public void onError(Throwable e) {
                    Toast.makeText(mContext, getString(R.string.error_not_connected), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onNext(Object o) {
                        String response = o.toString();
                        if (!response.equals("exists")) {
                            if (addingNewMode) {
                                addUserRetrofit(mUserName.getText().toString());
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
                                    mUserName.setError(getString(R.string.error_user_exists));
                                }
                            });
                        }
                }
            };
            registerObservable.subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(registerObserver);

    }

    private void addUserRetrofit(String userName){
        try {
            JSONObject json = new JSONObject();

            json.put("orgId", mCompanyId);
            json.put("listId", mListId);
            json.put("userName", userName);

            final Observable<String> addUserObservable = serverApi.addUser(mToken, json.toString());
            Observer addUserObserver = new Observer() {
                @Override
                public void onCompleted() {
                    addUserObservable.unsubscribeOn(Schedulers.newThread());
                }

                @Override
                public void onError(Throwable e) {
                    Toast.makeText(mContext, getString(R.string.error_not_connected), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onNext(Object o) {
                    String response = o.toString();
                    JSONObject jsonResponse = null;
                    try {
                        jsonResponse = new JSONObject(response);
                        if(jsonResponse.getString("result").equals("ok")) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext, getString(R.string.add_user_success), Toast.LENGTH_LONG).show();
                                    getFragmentManager().popBackStack("objectsList",0);
                                }
                            });
                        }
                        else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext, getString(R.string.error_title_generic), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            };
            addUserObservable.subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(addUserObserver);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public final static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
//        mPassword = (EditText) view.findViewById(R.id.password);
//        mPassConfirm = (EditText) view.findViewById(R.id.password_confirm);
//        mUserName = (EditText) view.findViewById(R.id.username);
//        mRegister = (Button) view.findViewById(R.id.create_account);
        mContext = view.getContext();
        if(getArguments()!=null) {
            if (!getArguments().containsKey("mode")) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
                mRegister.setText(getString(R.string.create));
                addingNewMode = true;
                mCompanyId = getArguments().getString("orgId");
                mListId = getArguments().getString("listId");
                mToken = getArguments().getString("token");
            }else {
            }

        }
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean register = true;
                if(mUserName.getText().length()==0){
                    register = false;
                    mUserName.setError(getResources().getString(R.string.empty_error));
                    mUserName.requestFocus();
                }
                else if(mPassword.getText().toString().length()<8){
                    register = false;
                    mPassword.setError(getResources().getString(R.string.error_password_short));
                    mPassword.requestFocus();
                }
                else if(!mPassword.getText().toString().equals(mPassConfirm.getText().toString())){
                    register = false;
                    mPassword.setError(getResources().getString(R.string.error_password_mismatch));
                    mPassword.requestFocus();
                }
                else if(!isValidEmail(mEmailAddress.getText().toString())){
                    register = false;
                    mEmailAddress.setError(getResources().getString(R.string.error_email_invalid));
                    mEmailAddress.requestFocus();
                }
                if(register){
                    AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
                    ab.setTitle("E-mail Validation");
                    ab.setMessage("The e-mail is used to send you a reminder of your login informations, do you confirm it's a valid e-mail?");
                    ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            registerRetrofit();
                        }
                    });
                    ab.setNegativeButton(getResources().getString(R.string.back), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    ab.show();
                }
//                    registerRetrofit();
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
    @Override
    public void onStop() {
        if(getArguments()!=null)
            ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        super.onStop();
    }
}
