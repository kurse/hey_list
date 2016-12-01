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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.example.youssef.list.interfaces.ServerApi;
import com.example.youssef.list.models.User;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

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

public class CompleteRegistrationFragment extends Fragment {

    public static int ADD_NEW_USER = 1;
    private String oldUsername;
    boolean complete = false;
    CallbackManager cbm;
    private String facebookEmail;
    boolean facebookRegister=false;
    RestTemplate restTemplate = new RestTemplate();
    private String mToken;
    private String mCompanyId;
    private String mListId;
    private Context mContext;
    private boolean addingNewMode = false;
    android.app.AlertDialog dba;
    private String mOrgId;
    LoginButton mFacebookRegister;
    @BindView(R.id.show_email_register) Button mEmailRegister;
    @BindView(R.id.separator_register) View separator;
    @BindView(R.id.create_account) Button mRegister;
    @BindView(R.id.register_username) EditText mUserName;
    @BindView(R.id.register_password) EditText mPassword;
    @BindView(R.id.email_address) EditText mEmailAddress;
    @BindView(R.id.password_confirm) EditText mPassConfirm;
    @BindView(R.id.back) Button back;
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
    private void showLoadingDlg(){
        final android.app.AlertDialog.Builder db = new android.app.AlertDialog.Builder(mContext);
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

    private void registerRetrofit(final String username, final String password, String emailAddress){

        showLoadingDlg();
        User user = new User(username,password,emailAddress);
        user.setCurOrgId(mCompanyId);

//        String jsonStr;
        JSONObject json = new JSONObject();
        try {
            if(!oldUsername.equals(username))
                json.put("newUsername",username);
            json.put("username",oldUsername);
            json.put("password",password);
            json.put("email_address",emailAddress);
            json.put("orgID",mCompanyId);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        final Observable<String> registerObservable = serverApi.completeRegistration(json.toString());
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

                Intent main = new Intent(mContext, MainActivity.class);
                SharedPreferences prefs = getActivity().getSharedPreferences("account", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("username", username);
                editor.putString("password", Base64.encodeToString(password.getBytes(), Base64.DEFAULT));
                editor.putBoolean("connected", true);
                editor.commit();
                main.putExtra("response", response);
                startActivity(main);
                dba.dismiss();

                getActivity().finish();
            }
        };
        registerObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(registerObserver);

    }
    private void checkValidRetrofit(final String username){

        showLoadingDlg();


        final Observable<String> registerObservable = serverApi.checkNewlyAdded(username);
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
                if(!response.equals("no")){
                    complete = true;
                    mCompanyId = response;
                    mRegister.setText("Complete registration");
//                    mUserName.setEnabled(false);
                    oldUsername = username;


                    mEmailAddress.setVisibility(View.VISIBLE);
                    mPassword.setVisibility(View.VISIBLE);
                    mPassConfirm.setVisibility(View.VISIBLE);
                }else if(response.equals("no")){
                    mUserName.setError(getString(R.string.error_user_exists));
                }
                dba.dismiss();

            }
        };
        registerObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(registerObserver);

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


        mContext = view.getContext();
        mFacebookRegister = (LoginButton) view.findViewById(R.id.login_fb);
        mFacebookRegister.setVisibility(View.GONE);
        separator.setVisibility(View.GONE);
        mEmailRegister.setVisibility(View.GONE);
        mUserName.setVisibility(View.VISIBLE);
        mEmailAddress.setVisibility(View.GONE);
        mPassword.setVisibility(View.GONE);
        mPassConfirm.setVisibility(View.GONE);
        mRegister.setVisibility(View.VISIBLE);
        back.setVisibility(View.VISIBLE);
        TextView title = (TextView)view.findViewById(R.id.title_register) ;
        title.setText("");
//        registerRetrofit(getArguments().getString("username"),getArguments().getString("password"),getArguments().getString("email"));


        mRegister.setText("Check");
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (complete == false) {
                    if (mUserName.getText().length() == 0) {
                        mUserName.setError(getResources().getString(R.string.empty_error));
                        mUserName.requestFocus();
                    } else {
                        checkValidRetrofit(mUserName.getText().toString());
                    }
                }
                else{
                    boolean register = true;
                    if(mPassword.getText().toString().length()<8){
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
                        ab.setTitle(getString(R.string.email_validation_title));
                        ab.setMessage(getString(R.string.email_valid_warning));
                        ab.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                registerRetrofit(mUserName.getText().toString(), mPassword.getText().toString(), mEmailAddress.getText().toString());
                            }
                        });
                        ab.setNegativeButton(getString(R.string.back), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        ab.show();
                    }
                }
            }

        });

//        back = (Button)view.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });


    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        cbm.onActivityResult(requestCode, resultCode, data);
    }

    private void setFacebookData(final LoginResult loginResult)
    {
        if (loginResult.getRecentlyGrantedPermissions().size() > 0) {

            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            // Application code
                            try {
                                Log.i("Response", response.toString());
                                facebookEmail = response.getJSONObject().getString("email");
                                String firstName = response.getJSONObject().getString("first_name");
                                String lastName = response.getJSONObject().getString("last_name");
//                            String gender = response.getJSONObject().getString("gender");
//                            String bday= response.getJSONObject().getString("birthday");

                                Profile profile = Profile.getCurrentProfile();
                                String id = profile.getId();
                                LoginManager.getInstance().logOut();
                                facebookRegister = true;
                                registerRetrofit(firstName+" "+lastName,id,facebookEmail);
//                            String link = profile.getLinkUri().toString();
//                            Log.i("Link",link);
//                            if (Profile.getCurrentProfile()!=null)
//                            {
//                                Log.i("Login", "ProfilePic" + Profile.getCurrentProfile().getProfilePictureUri(200, 200));
//                            }

                                Log.i("Login" + "Email", facebookEmail);
                                Log.i("Login" + "FirstName", firstName);
                                Log.i("Login" + "LastName", lastName);
//                            Log.i("Login" + "Gender", gender);
//                            Log.i("Login" + "Bday", bday);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,email,first_name,last_name");
            request.setParameters(parameters);
            request.executeAsync();
        }else
            Toast.makeText(getActivity(),getString(R.string.facebook_permission_negative),Toast.LENGTH_LONG).show();
    }
    @Override
    public void onStop() {
//        if(getArguments()!=null)
//            ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        if(dba!=null && dba.isShowing())
            dba.dismiss();
        super.onStop();
    }
}
