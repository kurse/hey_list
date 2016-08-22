package com.example.youssef.synchronized_notes.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.youssef.synchronized_notes.R;

/**
 * Created by Youssef on 8/22/2016.
 */

public class CompanyCreationFragment extends Fragment {
    private EditText mCompanyName;
    private Button mButtonCreate;
    private String mUserId;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_company_creation,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUserId = this.getArguments().getString("id");
        mCompanyName = (EditText) view.findViewById(R.id.company_name);
        mButtonCreate = (Button) view.findViewById(R.id.create_company_button);
        mButtonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
