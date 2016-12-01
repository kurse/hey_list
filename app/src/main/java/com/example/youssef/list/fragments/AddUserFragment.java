package com.example.youssef.list.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.youssef.list.MainActivity;
import com.example.youssef.list.R;
import com.example.youssef.list.interfaces.ServerApi;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Youssef on 9/24/2016.
 */

public class AddUserFragment extends Fragment{

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(MainActivity.SERVER_URL)
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    ServerApi serverApi = retrofit.create(ServerApi.class);
    public static int ADD_NEW_USER = 1;
    private String mToken;
    private String mCompanyId;
    private String mListId;
    private Button mNewUser;
    private Button mExistingUser;
    private Context mContext;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_user,container,false);
    }

    private void addUserRetrofit(String userName){
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
                                    Toast.makeText(mContext, getString(R.string.add_user_success), Toast.LENGTH_LONG).show();
                                    getFragmentManager().popBackStack("addUser",0);
                                }
                            });
                        }else if(jsonResponse.getString("result").equals("missing")){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext, getString(R.string.error_user_not_exist), Toast.LENGTH_LONG).show();
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

    private void showAddExistingDlg(){
        final AlertDialog.Builder db = new AlertDialog.Builder(mContext);
        db.setTitle(getString(R.string.add_title));
        db.setMessage(getString(R.string.username));
        final EditText objectT = new EditText(mContext);
        objectT.setImeOptions(EditorInfo.IME_ACTION_DONE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        objectT.setLayoutParams(lp);
        db.setView(objectT);
        db.setPositiveButton(getString(R.string.action_add), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                addUserRetrofit(objectT.getText().toString());
            }
        });
        db.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog dba = db.create();
        objectT.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dba.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        dba.show();
    }

    private void initViews(View view) {

        mToken = this.getArguments().getString("token");
        mCompanyId = this.getArguments().getString("orgId");
        mListId = this.getArguments().getString("listId");
        mContext = view.getContext();
        mNewUser = (Button) view.findViewById(R.id.add_user_new);
        mExistingUser = (Button) view.findViewById(R.id.add_user_existing);
        Button back = (Button)view.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        mNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Fragment registerFragment = new RegisterFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("mode",ADD_NEW_USER);
                bundle.putString("adderName",getArguments().getString("adderName"));
                bundle.putString("groupName",getArguments().getString("groupName"));
                bundle.putString("orgId",mCompanyId);
                bundle.putString("listId",mListId);
                bundle.putString("token",mToken);
                registerFragment.setArguments(bundle);
                ft.replace(R.id.fragments_view,registerFragment);
                ft.addToBackStack("registerNew").commit();
                DrawerLayout dl = (DrawerLayout) getActivity().findViewById(R.id.activity_main);
                dl.closeDrawer(Gravity.LEFT);
            }
        });
        mExistingUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout dl = (DrawerLayout) getActivity().findViewById(R.id.activity_main);
                dl.closeDrawer(Gravity.LEFT);
                final SharedPreferences sharedPref = getActivity().getSharedPreferences("account",Context.MODE_PRIVATE);
                if(!sharedPref.contains("firstLogin"))
                {
                    AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity());
                    dlg.setTitle(getString(R.string.facebook_tip_title));
                    dlg.setMessage(getString(R.string.facebook_tip_msg));
                    dlg.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showAddExistingDlg();
                            dialog.dismiss();
                        }
                    });
                    dlg.setNegativeButton(getString(R.string.stop_showing), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean("firstLogin",true);
                            editor.commit();
                            showAddExistingDlg();
                            dialog.dismiss();

                        }
                    });
                    dlg.show();
                }else
                    showAddExistingDlg();
            }
        });

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
        initViews(view);
    }

    @Override
    public void onStop() {
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
