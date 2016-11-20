package com.example.youssef.list;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.youssef.list.fragments.ObjectListFragment;
import com.example.youssef.list.presenters.ListPresenter;

import java.util.ArrayList;

/**
 * Created by Youssef on 7/30/2016.
 */

public class ItemsAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> data = new ArrayList<>();
    ArrayList<Boolean> checkedItems = new ArrayList<>();
    private static LayoutInflater inflater = null;
    private ObjectListFragment mOLFragment;
    private ListPresenter presenter;
    public boolean contains(String object){
        if(data.contains(object))
            return true;
        else
            return false;
    }
    public void clear(){
        data.clear();
        checkedItems.clear();
        notifyDataSetChanged();
    }
    public ItemsAdapter(Context context, ListPresenter presenter){
        this.context = context;
//        this.mOLFragment = fragment;
        this.presenter = presenter;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
//    public ItemsAdapter(Context context, String[] data) {
//        // TODO Auto-generated constructor stub
//        this.context = context;
//        this.data = data;
//        inflater = (LayoutInflater) context
//                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//    }

    public void addItem(final String item, boolean checked) {
        data.add(item);
        checkedItems.add(checked);
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.fragment_object, null);
        final TextView text = (TextView) vi.findViewById(R.id.object_item_text);
        text.setText(data.get(position));
        final AppCompatCheckBox checkBox = (AppCompatCheckBox)vi.findViewById(R.id.check);
        checkBox.setChecked(checkedItems.get(position));
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkedItems.set(position,!checkedItems.get(position));
                if(checkedItems.get(position))
                    text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                else
                    text.setPaintFlags(text.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                presenter.checkRetrofit(data.get(position),checkedItems.get(position));
            }
        });
        if(checkedItems.get(position))
            text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            text.setPaintFlags(text.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        Button remove = (Button)vi.findViewById(R.id.remove_object_item);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.removeItemRetrofit(data.get(position));
//                data.remove(position);
//                notifyDataSetChanged();
            }
        });
        AppCompatCheckBox cross = (AppCompatCheckBox) vi.findViewById(R.id.check);
        cross.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    checkItem(position);
                }
                else{
                    unCheckItem(position);
                }
            }
        });
        return vi;
    }

    private void unCheckItem(int position) {
        presenter.checkItem(position);
    }

    private void checkItem(int position) {
        presenter.uncheckItem(position);
    }
}
