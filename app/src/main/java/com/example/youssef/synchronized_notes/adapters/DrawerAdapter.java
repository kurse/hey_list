package com.example.youssef.synchronized_notes.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.youssef.synchronized_notes.R;

/**
 * Created by Youssef on 6/4/2016.
 */

public class DrawerAdapter extends BaseAdapter {

    private Context context;
    String[] menuItems;
    int[] itemsID;
    public DrawerAdapter(Context context){
        this.context = context;
        menuItems = new String[]{"Créer son entreprise", "Se déconnecter"};
        itemsID = new int[]{4567,54697,};
    }

    @Override
    public int getCount() {
        return menuItems.length;
    }

    @Override
    public Object getItem(int position) {
        return menuItems[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = null;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.drawer_menu_item, parent, false);
        }
        else{
            row = convertView;
        }
        TextView itemText = (TextView) row.findViewById(R.id.item_text);
        ImageView itemIcon = (ImageView) row.findViewById(R.id.item_icon);
        itemText.setText(menuItems[position]);
//        itemIcon.setImageResource(itemsID[position]);
        return row;
    }
}
