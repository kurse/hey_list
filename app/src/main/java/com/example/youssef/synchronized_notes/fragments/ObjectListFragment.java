package com.example.youssef.synchronized_notes.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
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
import com.example.youssef.synchronized_notes.R;

/**
 * Created by Youssef on 7/30/2016.
 */

public class ObjectListFragment extends Fragment {

    Context mContext;
    private ListView mObjectsList;
    private ItemsAdapter mObjectsAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_object_list,container,false);
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
                mObjectsAdapter.addItem(objectT.getText().toString());

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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = view.getContext();
        LinearLayout ll = (LinearLayout) view;
        mObjectsList = (ListView)ll.findViewById(R.id.objects_list);
        mObjectsAdapter = new ItemsAdapter(mContext);
        mObjectsList.setAdapter(mObjectsAdapter);

    }
}
