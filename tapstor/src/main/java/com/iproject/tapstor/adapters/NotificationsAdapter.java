package com.iproject.tapstor.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iproject.tapstor.R;
import com.iproject.tapstor.helper.TapstorData;
import com.iproject.tapstor.rest.Messages;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NotificationsAdapter extends ArrayAdapter<Messages> {
    private final static String IMAGE_URL = "http://www.tapstorbusiness.com/echo/images/users/";// id.jpg
    private final Context context;
    private List<Messages> values;
    private SparseBooleanArray mSelectedItemsIds;

    public NotificationsAdapter(Context context, List<Messages> values) {
        super(context, R.layout.row_notifications, values);
        this.context = context;
        this.values = values;
        mSelectedItemsIds = new SparseBooleanArray();

    }

    public List<Messages> getValues() {
        return values;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        // Inflate the layout with an XML layout for the row view
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            // The row view to be returned
            convertView = inflater.inflate(R.layout.row_notifications, parent, false);
        }

        Messages message = values.get(position);
        try {
            if (TapstorData.getInstance().notificationIdsofReadList
                    .contains(message.id)) {
                // convertView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                convertView.setAlpha(0.5f);
            } else {
                convertView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                convertView.setAlpha(1f);
            }

        } catch (Exception e) {
            convertView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            convertView.setAlpha(1f);
        }

        if (mSelectedItemsIds.get(position)) {
            convertView.setBackgroundColor(Color.parseColor("#C4C4C4"));
            convertView.setAlpha(1f);
        }

        TextView notificationMessage = (TextView) convertView
                .findViewById(R.id.textView1);

//		notificationMessage.setMinTextSize(11f);
//		notificationMessage.setMaxTextSize(25f);

        TextView stamp = (TextView) convertView.findViewById(R.id.textView3);
        ImageView image = (ImageView) convertView.findViewById(R.id.imageView1);

        notificationMessage.setText(message.message);
        stamp.setText(message.stamp);

        // Log.e("PICASSO", IMAGE_URL + message.company_id + "m.jpg");
        Picasso.with(context).load(IMAGE_URL + message.company_id + "m.jpg")
                .into(image);

        if (message.type == 3) {

            convertView.findViewById(R.id.imageViewMessage).setVisibility(
                    View.VISIBLE);
        } else {
            convertView.findViewById(R.id.imageViewMessage).setVisibility(
                    View.GONE);
        }

        return convertView;

    }

    @Override
    public int getCount() {

        return values != null ? values.size() : 0;

    }

    @Override
    public Messages getItem(int position) {

        return values.get(position);

    }

    @Override
    public long getItemId(int position) {
        return values.get(position).hashCode();
    }

    @Override
    public void remove(Messages object) {

        values.remove(object);
        notifyDataSetChanged();

    }

    public List<Messages> getMessagesPopulation() {
        return values;
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value) {
        if (value) {

            mSelectedItemsIds.put(position, value);

        } else {

            mSelectedItemsIds.delete(position);

        }

        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

}