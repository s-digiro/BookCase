package edu.temple.bookcase.adapters;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StringArrayAdapter extends BaseAdapter {

    private Context context;
    private String[] strings;

    public StringArrayAdapter(Context context, String[] strings) {
        this.context = context;
        this.strings = strings;
    }
    @Override
    public int getCount() {
        return strings.length;
    }

    @Override
    public Object getItem(int position) {
        return strings[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = new TextView(context);
        view.setText(this.strings[position]);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        return view;
    }
}
