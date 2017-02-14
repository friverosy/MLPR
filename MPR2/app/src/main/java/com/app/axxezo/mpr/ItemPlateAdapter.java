package com.app.axxezo.mpr;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by nicolasmartin on 24-01-17.
 */

public class ItemPlateAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ListPlateItem> items;

    public ItemPlateAdapter(Context context, ArrayList<ListPlateItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    @Override
    public Object getItem(int position) {
        return this.items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;

        if (convertView == null) {
            // Create a new view into the list.
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.item_list_plate, parent, false);
        }

        // Set data into the view.
        TextView tvNumber = (TextView) rowView.findViewById(R.id.t_list_plate_number);
        TextView tvDate = (TextView) rowView.findViewById(R.id.t_list_plate_date);
        TextView tvId = (TextView) rowView.findViewById(R.id.t_list_plate_id);

        ListPlateItem item = this.items.get(position);
        tvNumber.setText(item.get_number());
        tvDate.setText(item.get_date());
        tvId.setText(item.get_id());

        return rowView;
    }
}
