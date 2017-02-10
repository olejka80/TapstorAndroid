package com.iproject.tapstor.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iproject.tapstor.R;
import com.iproject.tapstor.helper.Helper;
import com.iproject.tapstor.library.CircleTransform;
import com.iproject.tapstor.library.Log;
import com.iproject.tapstor.objects.News;
import com.iproject.tapstor.objects.Rating;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends ArrayAdapter<Object> {
    private final Context context;
    boolean isNews;
    private List<News> newsList = new ArrayList<>();
    private List<Rating> ratingsList = new ArrayList<>();
    private String TAG = "ListAdapter";

    public ListAdapter(Context context, List<News> newsList, List<Rating> ratingsList, boolean isNews) {
        super(context, R.layout.row_comment_item);

        this.context = context;
        this.newsList = newsList;
        this.ratingsList = ratingsList;
        this.isNews = isNews;

    }

    public View getView(int position, View convertView, ViewGroup parent) {

        // Inflate the layout with an XML layout for the row view
        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            // The row view to be returned
            convertView = inflater.inflate(isNews ? R.layout.row_news_item : R.layout.row_comment_item, parent, false);
        }

        try {

            if (isNews) {
                TextView title = (TextView) convertView.findViewById(R.id.textView_title);
                TextView content = (TextView) convertView.findViewById(R.id.textView_content);
                TextView stamp = (TextView) convertView.findViewById(R.id.textView_stamp);
                // set the title and content of the news object
                title.setText(newsList.get(position).title);
                content.setText(newsList.get(position).content);
                // set the stamp verbally
                stamp.setText(Helper.calculateAndDisplayTime(newsList.get(position).stamp, context));

                ImageView avatar = (ImageView) convertView.findViewById(R.id.imageView1);
                try {

                    // if (isNews) {
                    if (!(newsList.get(position).image == null || newsList.get(position).image.equals(""))) {
                        Picasso.with(context).load(newsList.get(position).image).into(avatar);
                    } else {
                        Picasso.with(context).load(R.drawable.tapstor_icon).into(avatar);
                    }

                } catch (Exception e) {
                    Log.e(TAG, e);
                }

            } else {

                TextView title = (TextView) convertView.findViewById(R.id.textView1);
                TextView content = (TextView) convertView.findViewById(R.id.textView2);
                TextView stamp = (TextView) convertView.findViewById(R.id.textView_stamp);

                title.setText(ratingsList.get(position).name);
                content.setText(ratingsList.get(position).content);
                stamp.setText(Helper.calculateAndDisplayTime(ratingsList.get(position).stamp, context));
                ImageView avatar = (ImageView) convertView.findViewById(R.id.imageView1);

                try {
                    if (!(ratingsList.get(position).image == null || ratingsList.get(position).image.equals(""))) {
                        Picasso.with(context).load(ratingsList.get(position).image).fit().transform(new CircleTransform()).into(avatar);

                    } else {
                        Picasso.with(context).load(R.drawable.tapstor_icon).fit().transform(new CircleTransform()).into(avatar);
                    }

                } catch (Exception e) {
                    Log.e(TAG, e);
                }

                // set the store star rating
                ImageView img1, img2, img3, img4, img5;
                img1 = (ImageView) convertView.findViewById(R.id.star1);
                img2 = (ImageView) convertView.findViewById(R.id.star2);
                img3 = (ImageView) convertView.findViewById(R.id.star3);
                img4 = (ImageView) convertView.findViewById(R.id.star4);
                img5 = (ImageView) convertView.findViewById(R.id.star5);

                setStoreStarRating(ratingsList.get(position).rating, img1,
                        img2, img3, img4, img5);

                if (ratingsList.get(position).reply == null || ratingsList.get(position).reply.length() == 0) {
                    convertView.findViewById(R.id.reply).setVisibility(View.GONE);
                } else {
                    convertView.findViewById(R.id.reply).setVisibility(View.VISIBLE);
                }

            }

        } catch (Exception e) {
            Log.e(TAG, e);

        }

        return convertView;
    }

    /**
     * Sets the images for the store rating
     *
     * @param rating store average rating
     */
    public void setStoreStarRating(double rating, ImageView img1,
                                   ImageView img2, ImageView img3, ImageView img4, ImageView img5) {
        try {

            if (rating > 0.0 && rating < 1.0) {
                img1.setBackgroundResource(R.drawable.star_half);
            } else if (rating >= 1.0 && rating < 2.0) {

                img1.setBackgroundResource(R.drawable.star_full);
                if (rating != 1.0) {
                    img2.setBackgroundResource(R.drawable.star_half);
                }

            } else if (rating >= 2.0 && rating < 3.0) {
                img1.setBackgroundResource(R.drawable.star_full);
                img2.setBackgroundResource(R.drawable.star_full);
                if (rating != 2.0) {
                    img3.setBackgroundResource(R.drawable.star_half);
                }

            } else if (rating >= 3.0 && rating < 4.0) {

                img1.setBackgroundResource(R.drawable.star_full);
                img2.setBackgroundResource(R.drawable.star_full);
                img3.setBackgroundResource(R.drawable.star_full);
                if (rating != 3.0) {
                    img4.setBackgroundResource(R.drawable.star_half);
                }

            } else if (rating >= 4.0 && rating < 5.0) {
                img1.setBackgroundResource(R.drawable.star_full);
                img2.setBackgroundResource(R.drawable.star_full);
                img3.setBackgroundResource(R.drawable.star_full);
                img4.setBackgroundResource(R.drawable.star_full);
                if (rating != 4.0) {
                    img5.setBackgroundResource(R.drawable.star_half);
                }

            } else if (rating == 5.0) {
                img1.setBackgroundResource(R.drawable.star_full);
                img2.setBackgroundResource(R.drawable.star_full);
                img3.setBackgroundResource(R.drawable.star_full);
                img4.setBackgroundResource(R.drawable.star_full);
                img5.setBackgroundResource(R.drawable.star_full);

            }
        } catch (Exception e) {
            Log.e(TAG, e);
        }

    }

    @Override
    public int getCount() {

        if (isNews) {
            return newsList.size();
        } else {
            return ratingsList.size();
        }
    }

    @Override
    public Object getItem(int position) {

        if (isNews) {
            return newsList.get(position);
        } else {
            return ratingsList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        if (isNews) {
            return newsList.get(position).hashCode();
        } else {
            return ratingsList.get(position).hashCode();
        }

    }

}