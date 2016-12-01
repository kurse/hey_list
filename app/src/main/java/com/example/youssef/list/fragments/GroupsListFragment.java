package com.example.youssef.list.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.youssef.list.MainActivity;
import com.example.youssef.list.MainController;
import com.example.youssef.list.R;
import com.example.youssef.list.interfaces.ServerApi;
import com.example.youssef.list.models.User;
import com.example.youssef.list.presenters.ListPresenter;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
 * Created by Youssef on 11/26/2016.
 */

public class GroupsListFragment extends Fragment {

    @BindView(R.id.orgsList)  Spinner orgsList;
    @BindView(R.id.back)  Button back;
    @BindView(R.id.choose)  Button choose;
    private String curOrgId, curListId, curName;
    private ArrayAdapter<String> orgsListAdapter;
    private ArrayList<String> mOrgsId, mListIds, mOrgsCreators;
    private boolean isCreator = false;
    private User mCurUser;
    boolean isRequest = false;
    private String mToken;
    RestTemplate restTemplate = new RestTemplate();
    private Boolean justLoggedIn = true;
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(MainActivity.SERVER_URL)
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    ServerApi serverApi = retrofit.create(ServerApi.class);
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
        mCurUser = (User)getArguments().getSerializable("curUser");
        curOrgId = mCurUser.getCompany().getmId();
        mToken = getArguments().getString("token");
        orgsListAdapter = new ArrayAdapter<String>(getActivity(), R.layout.orgs_item);
        orgsList.setAdapter(orgsListAdapter);
        fetchGroups();
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).setCurGroup(curOrgId, curName, curListId, isCreator);
                getFragmentManager().popBackStack("objectsList",0);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack("objectsList",0);
            }
        });

    }
    public void fetchGroups(){
        if(mCurUser.getCompany()!=null)
            if(!isRequest) {
                isRequest = true;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("authToken", mToken);
                try {
                    JSONObject json = new JSONObject();
                    json.put("userId", mCurUser.getId());

                    final Observable<String> listObservable = serverApi.getGroupsList(mToken, json.toString());
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
                                    JSONArray orgsId = new JSONArray(jsonResponse.getString("orgsIds"));
                                    JSONArray orgsNames = new JSONArray(jsonResponse.getString("orgsNames"));
                                    JSONArray orgsCreators = new JSONArray(jsonResponse.getString("orgsCreators"));
                                    JSONArray listsIds = new JSONArray(jsonResponse.getString("listsIds"));
                                    mOrgsId = new ArrayList<>();
                                    mOrgsCreators = new ArrayList<>();
                                    mListIds = new ArrayList<>();
                                    for (int i = 0; i < orgsId.length(); i++)
                                        try {
                                            orgsListAdapter.add(orgsNames.getString(i));
                                            mOrgsCreators.add(orgsCreators.getString(i));
                                            mOrgsId.add(orgsId.getString(i));
                                            mListIds.add(listsIds.getString(i));
                                        } catch (JSONException e) {
                                            Log.d("error : ", e.getMessage());
//                                            error = new Error(mFragment.getString(R.string.error_title_generic));
//                                            publish();
                                        }
                                    orgsListAdapter.notifyDataSetChanged();
                                    orgsList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                            if(mOrgsCreators.get(position).equals(mCurUser.getId()))
                                                isCreator = true;
                                            else
                                                isCreator = false;
                                            curOrgId = mOrgsId.get(position);
                                            curListId = mListIds.get(position);
                                            curName =  (String)orgsList.getItemAtPosition(position);
                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> parent) {

                                        }
                                    });
                                    orgsList.setSelection(mOrgsId.indexOf(curOrgId));

//                                    publish();

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
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_groups_list,container,false);
    }
}
