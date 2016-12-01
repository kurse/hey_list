package com.example.youssef.list.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.youssef.list.R;
import com.example.youssef.list.fragments.ObjectListFragment;
import com.example.youssef.list.presenters.ListPresenter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by Youssef on 7/30/2016.
 */

public class UsersAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> users = new ArrayList<>();

    private static LayoutInflater inflater = null;


    public boolean contains(String object){
        if(users.contains(object))
            return true;
        else
            return false;
    }
    public void clear(){
        users.clear();
        notifyDataSetChanged();
    }
    public UsersAdapter(Context context){
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public void addUser(final String item) {
        String textStr= item;
        try {
            textStr = new String(item.getBytes(),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        users.add(textStr);
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return users.get(position);
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
            vi = inflater.inflate(R.layout.user_item, null);
        TextView text = (TextView) vi.findViewById(R.id.userItem);

        text.setText(users.get(position));





        return vi;
    }


}
