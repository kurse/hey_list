package com.example.youssef.list.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.youssef.list.ItemsAdapter;
import com.example.youssef.list.R;
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

/**
 * Created by Youssef on 7/30/2016.
 */

public class ObjectListFragment extends Fragment {
    RestTemplate restTemplate = new RestTemplate();
    AlertDialog dba;
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
        objectT.setImeOptions(EditorInfo.IME_ACTION_DONE);
        dba.show();
    }
    private void initList(){

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_refresh:
                fetchList();
                return true;
            case R.id.action_add:
                if(mCurUser.getCompany()!=null)
                    showDialogText();
                else
                    Toast.makeText(getActivity(),"Vous n'avez pas encore de groupe pour ajouter des objets synchronisés",Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_menu:
                DrawerLayout dl = (DrawerLayout) getActivity().findViewById(R.id.activity_main);
                if(dl.isDrawerOpen(Gravity.LEFT))
                    dl.closeDrawer(Gravity.LEFT);
                else
                    dl.openDrawer(Gravity.LEFT);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
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
                    if (!response.equals("invalidToken")) {
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
                    else{
                        SharedPreferences sharedPref = getActivity().getSharedPreferences("account",Context.MODE_PRIVATE);
                        String username = sharedPref.getString("username","");
                        String password = new String(Base64.decode(sharedPref.getString("password",""),Base64.DEFAULT));
                        login(username, password);
                    }
                }catch (Exception e){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext,"Erreur de connexion, veuillez vérifier votre connexion et réessayer plus tard",Toast.LENGTH_LONG).show();
                            showRetryDlg();
                        }
                    });
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
    }
    private void login(final String username, final String password){

        Runnable r = new Runnable() {
            @Override
            public void run() {

                User user = new User(username, password);


                String url = "http://137.74.44.134:8080/auth";
                String requestJson = null;
                try {
                    requestJson = user.toJsonObject().toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> entity = new HttpEntity<>(requestJson,headers);
//                    String answer = restTemplate.postForObject(url, entity, String.class);
                String response="";
                try {
                    response = restTemplate.postForObject(url, entity, String.class);
                    Log.d("response", response);
                    if (!response.equals("fail")) {

                        JSONObject responseJson = new JSONObject(response);
                        if(responseJson.has("token")) {
                            mToken = responseJson.getString("token");
                            dba.dismiss();
                            fetchList();
                        }
                        else
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext,"Erreur de connexion, veuillez vérifier votre connexion et réessayer plus tard",Toast.LENGTH_LONG).show();
                                    showRetryDlg();
                                }
                            });
                    }
                }catch (Exception e){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext,"Erreur de connexion, veuillez vérifier votre connexion et réessayer plus tard",Toast.LENGTH_LONG).show();
                            showRetryDlg();
                        }
                    });
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
        showLoadingDlg();
    }
    private void showLoadingDlg(){
        final AlertDialog.Builder db = new AlertDialog.Builder(mContext);
        db.setTitle("Connexion en cours");
        db.setMessage("Veuillez patienter");
        dba = db.create();
        dba.show();
        TextView title = (TextView)dba.findViewById(android.R.id.title);
        if(title!=null)
            title.setGravity(Gravity.CENTER);
        TextView msg = (TextView)dba.findViewById(android.R.id.message);
        if(msg!=null)
            msg.setGravity(Gravity.CENTER);
    }
    private void showRetryDlg(){
        if(dba!=null && dba.isShowing())
            dba.dismiss();
        final AlertDialog.Builder db = new AlertDialog.Builder(mContext);
        db.setTitle("Réessayer");
        db.setMessage("Réessayez quand vous êtes connectés");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.retry_dialog_layout, null);
        db.setView(dialogView);
        final Dialog dbRetry = db.create();
        dbRetry.show();
        Button retryButton = (Button)dbRetry.findViewById(R.id.retry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = getActivity().getSharedPreferences("account",Context.MODE_PRIVATE);
                String username = sharedPref.getString("username","");
                String password = new String(Base64.decode(sharedPref.getString("password",""),Base64.DEFAULT));
                login(username, password);
                dbRetry.dismiss();
            }
        });
        TextView title = (TextView)dbRetry.findViewById(android.R.id.title);
        if(title!=null)
            title.setGravity(Gravity.CENTER);
        TextView msg = (TextView)dbRetry.findViewById(android.R.id.message);
        if(msg!=null)
            msg.setGravity(Gravity.CENTER);
    }
    @Override
    public void onDestroy() {
        if(dba!=null && dba.isShowing())
            dba.dismiss();
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
