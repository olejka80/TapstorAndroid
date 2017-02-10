package com.iproject.tapstor.adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iproject.tapstor.R;
import com.iproject.tapstor.helper.TapstorData;
import com.iproject.tapstor.objects.Results;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class EnterprisesAdapter extends ArrayAdapter<Results> {
    private final String TAG = "EnterprisesAdapter";
    int width, height;
    private Context context;
    private List<Results> values;

    public EnterprisesAdapter(Context context, List<Results> values, boolean nearMeTabActive, int layoutId) {
        super(context, layoutId, values);
        this.context = context;
        this.values = values;

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        int dens = 3;

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            width = (metrics.widthPixels / dens) - (int) ((2 * metrics.density) + 0.5);
            height = (int) (width / 1.31) + (int) ((41 * metrics.density) + 0.5);
        } else {
            width = (metrics.heightPixels / dens) - (int) ((2 * metrics.density) + 0.5);
            height = (int) (width / 1.31) + (int) ((41 * metrics.density) + 0.5);
        }


    }

    public List<Results> getAdapterList() {
        return values;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolderItem viewHolder;
        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // get layout from mobile.xml
            convertView = inflater.inflate(R.layout.row_gridview_enterprises, parent, false);

            convertView.getLayoutParams().height = height;
            convertView.getLayoutParams().width = width;

            // well set up the ViewHolder
            viewHolder = new ViewHolderItem();

            viewHolder.textView = (TextView) convertView.findViewById(R.id.grid_item_label);

            viewHolder.image = (ImageView) convertView.findViewById(R.id.grid_item_image);
            viewHolder.newsCount = (TextView) convertView.findViewById(R.id.news_count);

            viewHolder.newRibbon = (ImageView) convertView.findViewById(R.id.new_ribbon);

            viewHolder.yellowRibbon = (ImageView) convertView.findViewById(R.id.yellow_ribbon);
            // store the holder with the view.
            convertView.setTag(viewHolder);
        } else {
            // we've just avoided calling findViewById() on resource everytime
            // just use the viewHolder
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        viewHolder.textView.setText(values.get(position).name);

        String url = values.get(position).avatar;

        viewHolder.image.setImageResource(android.R.color.transparent);

        if (url == null || url.equals("")) {

            viewHolder.image.setImageResource(R.drawable.tapstor_icon);

        } else {

            Picasso.with(context).load(url)
                    .into(viewHolder.image, new Callback() {

                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                        }
                    });

        }

        if (values.get(position).news != 0) {
            viewHolder.newsCount.setVisibility(View.VISIBLE);
            viewHolder.newsCount.setText(String.format(context.getResources().getString(R.string.placeholder_string), values.get(position).news));
        } else {
            viewHolder.newsCount.setVisibility(View.GONE);
        }

        if (values.get(position).isNew == 0) {
            viewHolder.newRibbon.setVisibility(View.INVISIBLE);
        } else {
            if (TapstorData.getInstance().getTab() != 3)
                viewHolder.newRibbon.setVisibility(View.VISIBLE);
        }

        if (values.get(position).has_offers == 0) {
            viewHolder.yellowRibbon.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.yellowRibbon.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public Results getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return values.get(position).hashCode();
    }

    // our ViewHolder.
    static class ViewHolderItem {

        TextView textView;
        ImageView image;
        TextView newsCount;
        ImageView newRibbon;
        ImageView yellowRibbon;

    }

}