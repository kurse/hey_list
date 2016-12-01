package com.example.youssef.list.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.example.youssef.list.MainActivity;
import com.example.youssef.list.MainController;
import com.example.youssef.list.R;
import com.example.youssef.list.adapters.UsersAdapter;
import com.example.youssef.list.interfaces.ServerApi;
import com.example.youssef.list.models.User;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


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
 * Created by Youssef on 11/30/2016.
 */

public class UsersListFragment extends Fragment {

    private boolean isRequest = false;
    private String mToken;
    @BindView(R.id.back) Button back;
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(MainActivity.SERVER_URL)
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    ServerApi serverApi = retrofit.create(ServerApi.class);


    UsersAdapter mUsersListAdapter;
    User mCurUser;
    @BindView(R.id.users_list) ListView mUsersList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users_list,container,false);
    }
    public void fetchUsers(){
        if(mCurUser.getCompany()!=null)
            if(!isRequest) {
                isRequest = true;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("authToken", mToken);
                try {
                    JSONObject json = new JSONObject();
                    json.put("orgID", mCurUser.getCompany().getmId());

                    final Observable<String> listObservable = serverApi.getUsersList(mToken, json.toString());
                    Observer listObserver = new Observer() {
                        @Override
                        public void onCompleted() {
                            isRequest=false;
                            listObservable.unsubscribeOn(Schedulers.newThread());
                        }

                        @Override
                        public void onError(Throwable e) {
                            isRequest=false;
//                            if(e.getMessage().contains("ailed to connect")){
//                                error = new Error(mFragment.getString(R.string.error_not_connected));
//                            } else{
//                                error = new Error(mFragment.getString(R.string.error_title_generic));
//                            }
//                            publish();
                        }

                        @Override
                        public void onNext(Object o) {
                            try {
//                                error = null;
                                String response = o.toString();
                                isRequest = false;
                                Log.d("response", response);
                                if (!response.equals("invalidToken")) {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    JSONArray users = new JSONArray(jsonResponse.getString("usersList"));

                                    for (int i = 0; i < users.length(); i++)
                                        try {
                                            mUsersListAdapter.addUser(users.getString(i));
                                        } catch (JSONException e) {
                                            Log.d("error : ", e.getMessage());
//                                            error = new Error(mFragment.getString(R.string.error_title_generic));
//                                            publish();
                                        }
                                    mUsersListAdapter.notifyDataSetChanged();
                                } else {
                                    EventBus.getDefault().post(new MainController.MessageEvent("refresh"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    listObservable.subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(listObserver);
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
    }
    @Override
    public void onStop() {
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        super.onStop();
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack("objectsList",0);
            }
        });
        mCurUser = (User)getArguments().getSerializable("curUser");
        mToken = getArguments().getString("token");
        mUsersListAdapter = new UsersAdapter(view.getContext());
        mUsersList.setAdapter(mUsersListAdapter);
        fetchUsers();

    }
}
