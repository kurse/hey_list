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
import android.text.Html;
import android.text.TextUtils;
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

public class RegisterFragment extends Fragment {

    public static int ADD_NEW_USER = 1;

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
    private String mCompanyName, mAdderName;
    private boolean waitingForShare = false;
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
            String json;
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

                            }else{
                                Intent main = new Intent(mContext, MainActivity.class);
                                SharedPreferences prefs = getActivity().getSharedPreferences("account", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("username", username);
                                editor.putString("password", Base64.encodeToString(password.getBytes(), Base64.DEFAULT));
                                editor.putBoolean("connected", true);
                                editor.commit();
                                main.putExtra("response", response);
                                startActivity(main);
                                getActivity().finish();
                            }

                        } else {
                            if(facebookRegister){
                                facebookRegister=false;
                                Toast.makeText(getActivity(),getResources().getString(R.string.facebook_already_registered),Toast.LENGTH_LONG).show();
                            }else
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mUserName.setError(getString(R.string.error_user_exists));
                                }
                            });
                            if(dba != null && dba.isShowing())
                                dba.dismiss();

                        }
                }
            };
            registerObservable.subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(registerObserver);

    }
    private void showShare(final String username, final String group, final String adder){
        android.app.AlertDialog.Builder shareDialog = new android.app.AlertDialog.Builder(getActivity());
        shareDialog.setTitle(getString(R.string.notify_add));
        shareDialog.setMessage(getString(R.string.notify_add_message));
        shareDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);

                sendIntent.setType("text/plain");
                String notificationMessage = adder;
                notificationMessage += getString(R.string.notification_p1);
                notificationMessage += group;
                notificationMessage += getString(R.string.notification_p2);
                notificationMessage += username;
                notificationMessage += getString(R.string.notification_p3);
                sendIntent.putExtra(Intent.EXTRA_TEXT, notificationMessage);
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
                waitingForShare = true;
            }
        });
        shareDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        shareDialog.show();
    }
    private void addUserRetrofit(final String userName){
        try {
            JSONObject json = new JSONObject();

            json.put("orgId", mCompanyId);
            json.put("listId", mListId);
            json.put("username", userName);

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
                                    showShare(userName, mCompanyName, mAdderName);
                                    Toast.makeText(mContext, getString(R.string.add_user_success), Toast.LENGTH_LONG).show();
                                }
                            });
                        }else {
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
        mFacebookRegister = (LoginButton) view.findViewById(R.id.login_fb);
        if(getArguments()!=null) {
            if (getArguments().containsKey("mode")) {
                int mode = getArguments().getInt("mode");
                if(mode == LoginFragment.LOGIN_FACEBOOK){
                    mFacebookRegister.setVisibility(View.INVISIBLE);
                    separator.setVisibility(View.INVISIBLE);
                    mEmailRegister.setVisibility(View.INVISIBLE);
                    mUserName.setVisibility(View.INVISIBLE);
                    mEmailAddress.setVisibility(View.INVISIBLE);
                    mPassword.setVisibility(View.INVISIBLE);
                    mPassConfirm.setVisibility(View.INVISIBLE);
                    mRegister.setVisibility(View.INVISIBLE);
                    back.setVisibility(View.INVISIBLE);
                    TextView title = (TextView)view.findViewById(R.id.title_register) ;
                    title.setText("");
                    registerRetrofit(getArguments().getString("username"),getArguments().getString("password"),getArguments().getString("email"));
                }else{
                    mEmailRegister.setVisibility(View.GONE);
                    mFacebookRegister.setVisibility(View.GONE);
                    separator.setVisibility(View.GONE);
                    mUserName.setVisibility(View.VISIBLE);
                    mEmailAddress.setVisibility(View.GONE);
                    mPassword.setVisibility(View.GONE);
                    mPassConfirm.setVisibility(View.GONE);
                    mRegister.setVisibility(View.VISIBLE);
                    ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
                    mRegister.setText(getString(R.string.create));
                    mCompanyName = getArguments().getString("groupName");
                    mAdderName = getArguments().getString("adderName");
                    addingNewMode = true;
                    mCompanyId = getArguments().getString("orgId");
                    mListId = getArguments().getString("listId");
                    mToken = getArguments().getString("token");
                }
            }
        }else {
            mEmailRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mUserName.setVisibility(View.VISIBLE);
                    mEmailAddress.setVisibility(View.VISIBLE);
                    mPassword.setVisibility(View.VISIBLE);
                    mPassConfirm.setVisibility(View.VISIBLE);
                    mRegister.setVisibility(View.VISIBLE);
                }
            });
            mFacebookRegister.setReadPermissions("email");
            // If using in a fragment
            mFacebookRegister.setFragment(this);
            // Other app specific specialization
            // Callback registration
            cbm = CallbackManager.Factory.create();
            mFacebookRegister.registerCallback(cbm, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
//                loginResult.getRecentlyGrantedPermissions().
                    setFacebookData(loginResult);
                    Log.d("loginResult : ", loginResult.toString());
                }

                @Override
                public void onCancel() {
                    // App code
                }

                @Override
                public void onError(FacebookException exception) {
                    Log.i("error fb: ", exception.toString());
                    if(exception.getMessage().contains("FAILURE")) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), getString(R.string.error_not_connected), Toast.LENGTH_LONG).show();
                            }
                        });
                    }                    }
            });
//            mFacebookRegister.setVisibility(View.INVISIBLE);
//            separator.setVisibility(View.INVISIBLE);
//            mEmailRegister.setVisibility(View.INVISIBLE);
//            mUserName.setVisibility(View.INVISIBLE);
//            mEmailAddress.setVisibility(View.INVISIBLE);
//            mPassword.setVisibility(View.INVISIBLE);
//            mPassConfirm.setVisibility(View.INVISIBLE);
//            mRegister.setVisibility(View.INVISIBLE);
//            back.setVisibility(View.INVISIBLE);
//            TextView title = (TextView)view.findViewById(R.id.title_register) ;
//            title.setText("");
//            registerRetrofit(getArguments().getString("username"),getArguments().getString("password"),getArguments().getString("email"));
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
                else if(mPassword.getText().toString().length()<8 && getArguments()==null){
                    register = false;
                    mPassword.setError(getResources().getString(R.string.error_password_short));
                    mPassword.requestFocus();
                }
                else if(!mPassword.getText().toString().equals(mPassConfirm.getText().toString())&& getArguments()==null){
                    register = false;
                    mPassword.setError(getResources().getString(R.string.error_password_mismatch));
                    mPassword.requestFocus();
                }
                else if(!isValidEmail(mEmailAddress.getText().toString())&& getArguments()==null){
                    register = false;
                    mEmailAddress.setError(getResources().getString(R.string.error_email_invalid));
                    mEmailAddress.requestFocus();
                }
                if(register && getArguments()==null){
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
                else if(register){
                    registerRetrofit(mUserName.getText().toString(), "", "");
                }
//                    registerRetrofit();
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
    public void onResume() {
        super.onResume();
        if(waitingForShare == true)
            getFragmentManager().popBackStack("addUser",0);

    }

    @Override
    public void onStop() {
        if(dba!=null && dba.isShowing())
            dba.dismiss();
        super.onStop();
    }
}
