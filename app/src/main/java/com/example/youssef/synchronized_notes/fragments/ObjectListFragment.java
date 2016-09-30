package com.example.youssef.synchronized_notes.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.youssef.synchronized_notes.ItemsAdapter;
import com.example.youssef.synchronized_notes.MainActivity;
import com.example.youssef.synchronized_notes.R;
import com.example.youssef.synchronized_notes.models.Company;
import com.example.youssef.synchronized_notes.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Youssef on 7/30/2016.
 */

public class ObjectListFragment extends Fragment {
    RestTemplate restTemplate = new RestTemplate();

    private String mToken;
    private User mCurUser;
    Context mContext;
    private ListView mObjectsList;
    private ItemsAdapter mObjectsAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_object_list,container,false);
    }

    private void addItemDB(final String item){
        Runnable r = new Runnable() {
            @Override
            public void run() {



                String url = "http://137.74.44.134:8080/addItem";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("authToken",mToken);
                String response="";
                try {
                    JSONObject json = new JSONObject();
                    json.put("listId",mCurUser.getCompany().getmListId());
                    json.put("item",item);
                    String listIdJson = json.toString();
                    HttpEntity<String> entity = new HttpEntity<>(listIdJson,headers);

                    response = restTemplate.postForObject(url, entity, String.class);
                    Log.d("response", response);
                    if (!response.equals("exists")) {
                        JSONObject jsonResponse = new JSONObject(response);
                        final JSONArray array = new JSONArray(jsonResponse.getString("list"));

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for(int i=0;i<array.length();i++)
                                    try {
                                        if(!mObjectsAdapter.contains(array.getString(i)))
                                            mObjectsAdapter.addItem(array.getString(i));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                            }
                        });
                    }
                    else
                        Toast.makeText(mContext,"Erreur de connexion",Toast.LENGTH_LONG).show();
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
    public void removeItemDB(final String item){
        Runnable r = new Runnable() {
            @Override
            public void run() {

                String url = "http://137.74.44.134:8080/removeItem";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("authToken",mToken);
                String response="";
                try {
                    JSONObject json = new JSONObject();
                    json.put("listId",mCurUser.getCompany().getmListId());
                    json.put("item",item);
                    String listIdJson = json.toString();
                    HttpEntity<String> entity = new HttpEntity<>(listIdJson,headers);

                    response = restTemplate.postForObject(url, entity, String.class);
                    Log.d("response", response);
                    if (!response.equals("exists")) {
                        JSONObject jsonResponse = new JSONObject(response);
                        final JSONArray array = new JSONArray(jsonResponse.getString("list"));

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
                        Toast.makeText(mContext,"Erreur de connexion",Toast.LENGTH_LONG).show();
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
    private void showDialogText(){
        final AlertDialog.Builder db = new AlertDialog.Builder(mContext);
        db.setTitle("Ajout");
        db.setMessage("Nouvel Objet");
        final EditText objectT = new EditText(mContext);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        objectT.setLayoutParams(lp);
        db.setView(objectT);
        db.setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String item = objectT.getText().toString();
                mObjectsAdapter.addItem(item);
                addItemDB(item);
            }
        });
        db.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
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
    private void initList(){

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_add:
                showDialogText();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mCurUser.getCompany() != null)
            fetchList();
    }

    private void fetchList(){
        Runnable r = new Runnable() {
            @Override
            public void run() {



                String url = "http://137.74.44.134:8080/getList";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("authToken",mToken);
                String response="";
                try {
                    JSONObject json = new JSONObject();
                    json.put("listId",mCurUser.getCompany().getmListId());
                    String listIdJson = json.toString();
                    HttpEntity<String> entity = new HttpEntity<>(listIdJson,headers);

                    response = restTemplate.postForObject(url, entity, String.class);
                    Log.d("response", response);
                    if (!response.equals("exists")) {
                        JSONObject jsonResponse = new JSONObject(response);
                        final JSONArray array = new JSONArray(jsonResponse.getString("list"));

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    for(int i=0;i<array.length();i++)
                                        try {
                                            if(!mObjectsAdapter.contains(array.getString(i)))
                                                mObjectsAdapter.addItem(array.getString(i));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                }
                            });
                    }
                    else
                        Toast.makeText(mContext,"Erreur de connexion",Toast.LENGTH_LONG).show();
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
    public void onDestroy() {
        getActivity().finish();
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        mContext = view.getContext();
        LinearLayout ll = (LinearLayout) view;
        mObjectsList = (ListView)ll.findViewById(R.id.objects_list);
        mObjectsAdapter = new ItemsAdapter(mContext, this);
        mObjectsList.setAdapter(mObjectsAdapter);
        mCurUser = (User) this.getArguments().getSerializable("user");
        mToken = this.getArguments().getString("token");
    }
}
