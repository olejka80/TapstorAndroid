package com.iproject.tapstor.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.iproject.tapstor.R;
import com.iproject.tapstor.helper.TapstorData;
import com.iproject.tapstor.library.Log;
import com.iproject.tapstor.objects.Cat;

import java.util.List;

public class SliderAdapter extends ArrayAdapter<Cat> {
    private static final String TAG = "SliderAdapter";
    private final Context context;
    private List<Cat> values;
    private Fragment frag;
    private int topTabValue;

    /**
     * @param context
     * @param values  the list of categoriew
     */
    public SliderAdapter(Context context, List<Cat> values, Fragment frag, int topTabValue) {
        super(context, R.layout.row_slider_category, values);
        this.context = context;
        this.values = values;
        this.frag = frag;
        this.topTabValue = topTabValue;

    }

    public List<Cat> getValues() {
        return values;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {


        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_slider_category, parent, false);
        }

        Log.i("Slider Adapter", "Selected tab - " + topTabValue + " : " + TapstorData.getInstance().getSelection(topTabValue) + " : " + position);


        if (TapstorData.getInstance().getSelection(topTabValue) == position) {
            convertView.findViewById(R.id.check_row_categories).setVisibility(View.VISIBLE);

            convertView.findViewById(R.id.check_row_categories).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    ((OnRemoveClickedListener) frag).onRemoveClicked(true);
                }
            });

        } else {
            convertView.findViewById(R.id.check_row_categories).setVisibility(View.GONE);

            convertView.findViewById(R.id.check_row_categories).setOnClickListener(null);
        }

        try {

            TextView name = (TextView) convertView.findViewById(R.id.cat_name);
            name.setText(values.get(position).title);

        } catch (Exception e) {
            Log.e(TAG, e);

        }

        return convertView;
    }

    public void setSelectedRow(int selection, int tab) {
        Log.w(TAG, "log: tab:" + (tab) + " selection is " + selection);
        TapstorData.getInstance().setSelection(selection, tab);

        if (selection >= 0) {
            TapstorData.getInstance().setSelectedCategoryId(values.get(selection).id, tab);

        } else if (selection == -1) {
            TapstorData.getInstance().setSelectedCategoryId(-1, tab);
        } else {
            TapstorData.getInstance().setSelectedCategoryId(-99, tab);
        }
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {

        return values != null ? values.size() : 0;

    }

    @Override
    public Cat getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return values.get(position).hashCode();
    }

    public interface OnRemoveClickedListener {
        public void onRemoveClicked(boolean display);

    }

}