package com.example.youssef.synchronized_notes.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.youssef.synchronized_notes.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Youssef on 9/24/2016.
 */

public class AddUserFragment extends Fragment{


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

    private void addUserServ(final String userName){
        Runnable r = new Runnable() {
            @Override
            public void run() {

                RestTemplate restTemplate = new RestTemplate();
                String url = "http://137.74.44.134:8080/addUserGroup";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("authToken",mToken);
                String response="";
                try {
                    JSONObject json = new JSONObject();
                    json.put("orgId",mCompanyId);
                    json.put("listId",mListId);
                    json.put("userName",userName);
                    String listIdJson = json.toString();
                    HttpEntity<String> entity = new HttpEntity<>(listIdJson,headers);

                    response = restTemplate.postForObject(url, entity, String.class);
                    Log.d("response", response);
                    if (!response.equals("exists")) {
                        JSONObject jsonResponse = new JSONObject(response);
                        if(jsonResponse.has("result"))
                            if(jsonResponse.getString("result").equals("ok")) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mContext, "L'utilisateur a été bien ajouté", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        else {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mContext, "Erreur", Toast.LENGTH_LONG).show();
                                    }
                                });                            }
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
                    else {
                        Toast.makeText(mContext, "Erreur", Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext,"Erreur ",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    private void showAddExistingDlg(){
        final AlertDialog.Builder db = new AlertDialog.Builder(mContext);
        db.setTitle("Ajout");
        db.setMessage("Nom de l'utilisateur");
        final EditText objectT = new EditText(mContext);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        objectT.setLayoutParams(lp);
        db.setView(objectT);
        db.setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                addUserServ(objectT.getText().toString());
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

    private void initViews(View view) {

        mToken = this.getArguments().getString("token");
        mCompanyId = this.getArguments().getString("orgId");
        mListId = this.getArguments().getString("listId");
        mContext = view.getContext();
        mNewUser = (Button) view.findViewById(R.id.add_user_new);
        mExistingUser = (Button) view.findViewById(R.id.add_user_existing);
        mNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Fragment registerFragment = new RegisterFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("mode",ADD_NEW_USER);
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
                showAddExistingDlg();
            }
        });

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
