package com.example.youssef.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
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

    public void addItem(final String item) {
        data.add(item);
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
        TextView text = (TextView) vi.findViewById(R.id.object_item_text);
        text.setText(data.get(position));
        Button remove = (Button)vi.findViewById(R.id.remove_object_item);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.removeItemRetrofit(data.get(position));
//                data.remove(position);
//                notifyDataSetChanged();
            }
        });
        return vi;
    }
}
