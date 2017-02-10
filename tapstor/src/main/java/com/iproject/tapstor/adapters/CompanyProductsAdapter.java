package com.iproject.tapstor.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iproject.tapstor.ProductsForStoreActivity;
import com.iproject.tapstor.R;
import com.iproject.tapstor.UserProfileActivity;
import com.iproject.tapstor.helper.Helper;
import com.iproject.tapstor.helper.TapstorData;
import com.iproject.tapstor.library.CircleTransform;
import com.iproject.tapstor.library.Log;
import com.iproject.tapstor.library.StorageAccess;
import com.iproject.tapstor.objects.Products;
import com.iproject.tapstor.objects.Rating;
import com.iproject.tapstor.rest.RestResponse;
import com.iproject.tapstor.rest.RestServices;
import com.iproject.tapstor.rest.Result_do_rating;
import com.iproject.tapstor.rest.SendPostValueRating;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CompanyProductsAdapter extends ArrayAdapter<Products> {

    private static final String TAG = "CompanyProductsAdapter";
    private Context context;
    private List<Products> values;
    private AlertDialog.Builder chooseAction;
    private AlertDialog choose;
    private String USER_PREFS = "USER_PREFS";
    private String USER_NAME = "USER_NAME";
    private String USER_SURNAME = "USER_SURNAME";
    private String AVATAR_PATH = "AVATAR_PATH";
    private TextView name;
    private TextView surname;
    private EditText comment;
    private ImageView avatarPictureHolder;
    private int ratingCounter = 0;

    public CompanyProductsAdapter(Context context, List<Products> values) {
        super(context, R.layout.row_product_item);
        this.context = context;
        this.values = values;
        Log.e(TAG, "set adapter");

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        // Inflate the layout with an XML layout for the row view
        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Log.e(TAG, "row " + position);

        if (convertView == null) {
            // The row view to be returned
            convertView = inflater.inflate(R.layout.row_product_item, parent, false);
        }

        final Products product = values.get(position);

        String title = product.title;

        ImageView imageContainer = (ImageView) convertView
                .findViewById(R.id.imageView1);
        String imageURL = product.image;
        try {
            Picasso.with(context).load(imageURL).into(imageContainer);
        } catch (Exception e) {
            Log.e(TAG, e);
            Picasso.with(context).load(R.drawable.tapstor_icon);
        }

        String description = product.description;
        String sellPrice = product.offer.sell_price;
        String offerPrice = product.offer.offer_price;

        String dealFrom = product.offer.deal_from;
        String dealTo = product.offer.deal_to;

        double averageRating = product.average_rating;

        TextView titleTextView = (TextView) convertView.findViewById(R.id.textView3);

        titleTextView.setText(title);

        TextView titleOfferPerCent = (TextView) convertView
                .findViewById(R.id.textView5);

        String offerPerCent = "-";
        String percent = setOfferPerCent(offerPerCent, product);
        if (percent.length() > 0) {
            titleOfferPerCent.setText(String.format(context.getResources().getString(R.string.discount_percentage), percent));
        }

        TextView titleOfferNewPrice = (TextView) convertView
                .findViewById(R.id.textView8);
        if (!(offerPrice == null || offerPrice.equals("0.00"))) {
            titleOfferNewPrice.setText(String.format(context.getResources().getString(R.string.currency), offerPrice));
        }

        TextView titleOfferOldPrice = (TextView) convertView
                .findViewById(R.id.textView9);

        if (!(sellPrice == null || sellPrice.equals("0.00"))) {
            titleOfferOldPrice.setText(String.format(context.getResources().getString(R.string.currency), sellPrice));
            if (!(offerPrice == null || offerPrice.equals("0.00"))) {
                titleOfferOldPrice.setPaintFlags(titleOfferOldPrice
                        .getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                titleOfferOldPrice.setTextColor(Color.parseColor("#FFFFFF"));
            }
        } else {
            titleOfferOldPrice.setText(R.string.no_price);
            titleOfferOldPrice.setTextColor(Color.parseColor("#FFFFFF"));
        }

        TextView storeRating = (TextView) convertView.findViewById(R.id.store_rating);

        if (product.average_rating != 0.0) {
            storeRating.setText(String.format(context.getResources().getString(R.string.placeholder_string), product.average_rating));
        } else {
            storeRating.setText("-");
        }

        // set the store star rating
        ImageView img1, img2, img3, img4, img5;
        img1 = (ImageView) convertView.findViewById(R.id.star1);
        img2 = (ImageView) convertView.findViewById(R.id.star2);
        img3 = (ImageView) convertView.findViewById(R.id.star3);
        img4 = (ImageView) convertView.findViewById(R.id.star4);
        img5 = (ImageView) convertView.findViewById(R.id.star5);

        setStoreStarRating(averageRating, img1, img2, img3, img4, img5);


        TextView descriptionTextView = (TextView) convertView.findViewById(R.id.descriprtion_TextView);
        descriptionTextView.setText(Html.fromHtml(description).toString());

        ScrollView childScrollView = (ScrollView) convertView.findViewById(R.id.scroll_area);

        ListView parentListView = (ListView) parent.findViewById(R.id.listView_products);

        final View currentRowView = convertView;
        parentListView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                currentRowView.findViewById(R.id.scroll_area).getParent()
                        .requestDisallowInterceptTouchEvent(false);
                return false;

            }
        });

        childScrollView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        setUpCommentsLayout(convertView, values.get(position).ratings);

        // Listener for the rating layout of store to prompt the user to rate
        // or create profile in order to rate
        convertView.findViewById(R.id.store_rating_relavite_layout)
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (Helper.checkIfUserHasProfile(context)) {
                            showCommentDialog(values.get(position), position);
                        } else {

                            // go to profile but first show choice dialog
                            showProfileDialog();

                        }

                    }
                });

        convertView.findViewById(R.id.add_comment).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (Helper.checkIfUserHasProfile(context)) {
                            showCommentDialog(values.get(position), position);
                        } else {

                            // go to profile but first show choice dialog

                            showProfileDialog();

                        }
                    }
                });

        convertView.findViewById(R.id.share_layout).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        chooseAction = new AlertDialog.Builder(context);

                        chooseAction.setCancelable(true);
                        chooseAction.setItems(R.array.share_options,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Helper.shareOption(context, product.title,
                                                Html.fromHtml(product.description).toString(),
                                                product.image, which);
                                    }
                                });

                        choose = chooseAction.create();
                        // choose.setCanceledOnTouchOutside(true);
                        choose.show();

                    }
                });

        return convertView;

    }

    protected void showProfileDialog() {

        new AlertDialog.Builder(context)
                .setMessage(
                        context.getResources().getString(R.string.dialog_text))
                .setPositiveButton(
                        context.getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                ((Activity) context).startActivityForResult(
                                        new Intent(context,
                                                UserProfileActivity.class),
                                        1234);
                                dialog.cancel();
                            }
                        })
                .setNegativeButton(
                        context.getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.cancel();
                            }
                        })

                .show();

    }

    private void setUpCommentsLayout(View convertView, final List<Rating> ratings) {

        final LinearLayout commentsLayout = (LinearLayout) convertView.findViewById(R.id.linear_layout_comments);

        commentsLayout.removeAllViews();

        if (ratings != null) {

            if (ratings.size() > 0) {
                ((TextView) convertView.findViewById(R.id.num_of_comments)).setText(
                        String.format(context.getResources().getString(R.string.placeholder_string), ratings.size()));

                int counter = 0;
                for (int i = ratings.size() - 1; i >= 0; i--) {
                    counter++;
                    // inflate each new row inside the newsLayout layout
                    inflateCommentsRow(ratings.get(i), commentsLayout);

                    if (counter == 3) {
                        break;
                    }

                }

                if (ratings.size() > 3) {

                    final TextView expandComments = (TextView) convertView.findViewById(R.id.expand_comments);

                    expandComments.findViewById(R.id.expand_comments).setVisibility(View.VISIBLE);
                    expandComments.findViewById(R.id.expand_comments).setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            commentsLayout.removeAllViews();

                            for (int i = ratings.size() - 1; i >= 0; i--) {
                                inflateCommentsRow(ratings.get(i), commentsLayout);
                            }

                            expandComments.setVisibility(View.GONE);

                        }
                    });
                } else {
                    convertView.findViewById(R.id.expand_comments).setVisibility(View.GONE);
                }

            } else {

                ((TextView) convertView.findViewById(R.id.num_of_comments)).setText("0");

                convertView.findViewById(R.id.expand_comments).setVisibility(View.GONE);

            }
        } else {
            convertView.findViewById(R.id.expand_comments).setVisibility(View.GONE);
        }

    }

    /**
     * Inflates a comment row inside the layout
     *
     * @param rating         object
     * @param commentsLayout the layout to put comments into
     */
    private void inflateCommentsRow(final Rating rating,
                                    LinearLayout commentsLayout) {

        View child = ((Activity) context).getLayoutInflater().inflate(
                R.layout.row_comment_item, new LinearLayout(context), false);

        TextView title = (TextView) child.findViewById(R.id.textView1);
        TextView content = (TextView) child.findViewById(R.id.textView2);
        TextView stamp = (TextView) child.findViewById(R.id.textView_stamp);

        title.setText(rating.name);
        content.setText(rating.content);
        stamp.setText(Helper.calculateAndDisplayTime(rating.stamp, context));
        ImageView avatar = (ImageView) child.findViewById(R.id.imageView1);

        try {
            if (!(rating.image == null || rating.image.equals(""))) {

                // Picasso.with(context).setIndicatorsEnabled(true);
                Log.e(TAG, rating.image);
                Picasso.with(context).load(rating.image).fit()
                        .transform(new CircleTransform()).into(avatar);

            } else {
                Picasso.with(context).load(R.drawable.tapstor_icon).fit()
                        .transform(new CircleTransform()).into(avatar);
            }

        } catch (Exception e) {
            Log.e(TAG, e);
        }

        // set the store star rating
        ImageView img1, img2, img3, img4, img5;
        img1 = (ImageView) child.findViewById(R.id.star1);
        img2 = (ImageView) child.findViewById(R.id.star2);
        img3 = (ImageView) child.findViewById(R.id.star3);
        img4 = (ImageView) child.findViewById(R.id.star4);
        img5 = (ImageView) child.findViewById(R.id.star5);

        setStoreStarRating(rating.rating, img1, img2, img3, img4, img5);

        child.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showPopUpDetailDialog(rating.image,
                        Helper.calculateAndDisplayTime(rating.stamp, context),
                        rating.name, rating.content, rating.reply);

            }
        });

        commentsLayout.addView(child);

    }

    private void showPopUpDetailDialog(String imageURL, String timestamp,
                                       String title, String content, String response) {

        // custom dialog
        final Dialog dialog = new Dialog(context, R.style.ThemeFullScreen);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(R.layout.inflate_dialog_detail_pop_up);

        dialog.setCancelable(true);
        try {
            if (response != null && response.length() != 0) {

                dialog.findViewById(R.id.response_layout).setVisibility(
                        View.VISIBLE);
                ((TextView) dialog.findViewById(R.id.response))
                        .setText(response);
            }
        } catch (Exception e) {
            Log.e(TAG, e);
        }

        final TextView titleTextView = (TextView) dialog.findViewById(R.id.title);
        final TextView contentTextView = (TextView) dialog.findViewById(R.id.content);
        final TextView timeStampTextView = (TextView) dialog.findViewById(R.id.timestamp);

        final ImageView closeButton = (ImageView) dialog.findViewById(R.id.close_button);

        final ImageView image = (ImageView) dialog.findViewById(R.id.post_image);
        if (!(imageURL == null || imageURL.length() == 0)) {
            Picasso.with(context).load(imageURL).memoryPolicy(MemoryPolicy.NO_CACHE).into(image);
        } else {
            image.setVisibility(View.GONE);
        }

        titleTextView.setText(title);
        contentTextView.setText(content);
        timeStampTextView.setText(timestamp);

        closeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                dialog.dismiss();

            }
        });

        dialog.show();

    }

    private String setOfferPerCent(String offerPerCent, Products product) {
        // 100-80/120*100
        try {

            if (product.offer.sell_price != null
                    && product.offer.offer_price != null) {
                Log.e(null, "" + product.offer.offer_price + " "
                        + product.offer.sell_price);
                int perCent = (int) (100 - ((Double
                        .parseDouble(product.offer.offer_price) / Double
                        .parseDouble(product.offer.sell_price)) * 100));

                offerPerCent = "" + perCent;
            } else {
                offerPerCent = "";
            }

        } catch (Exception e) {

            offerPerCent = "";
            Log.e(TAG, e);

        }

        return offerPerCent;
    }

    /**
     * Sets the images for the store rating
     *
     * @param rating store average rating
     */
    public void setStoreStarRating(double rating, ImageView img1,
                                   ImageView img2, ImageView img3, ImageView img4, ImageView img5) {
        try {

            if (rating == 0.0) {
                img1.setBackgroundResource(R.drawable.star_empty);
                img2.setBackgroundResource(R.drawable.star_empty);
                img3.setBackgroundResource(R.drawable.star_empty);
                img4.setBackgroundResource(R.drawable.star_empty);
                img5.setBackgroundResource(R.drawable.star_empty);
            } else if (rating > 0.0 && rating < 1.0) {
                img1.setBackgroundResource(R.drawable.star_half);
                img2.setBackgroundResource(R.drawable.star_empty);
                img3.setBackgroundResource(R.drawable.star_empty);
                img4.setBackgroundResource(R.drawable.star_empty);
                img5.setBackgroundResource(R.drawable.star_empty);
            } else if (rating >= 1.0 && rating < 2.0) {

                img1.setBackgroundResource(R.drawable.star_full);
                if (rating != 1.0) {
                    img2.setBackgroundResource(R.drawable.star_half);
                } else {
                    img2.setBackgroundResource(R.drawable.star_empty);
                }

                img3.setBackgroundResource(R.drawable.star_empty);
                img4.setBackgroundResource(R.drawable.star_empty);
                img5.setBackgroundResource(R.drawable.star_empty);

            } else if (rating >= 2.0 && rating < 3.0) {
                img1.setBackgroundResource(R.drawable.star_full);
                img2.setBackgroundResource(R.drawable.star_full);
                if (rating != 2.0) {
                    img3.setBackgroundResource(R.drawable.star_half);
                } else {
                    img3.setBackgroundResource(R.drawable.star_empty);
                }
                img4.setBackgroundResource(R.drawable.star_empty);
                img5.setBackgroundResource(R.drawable.star_empty);

            } else if (rating >= 3.0 && rating < 4.0) {

                img1.setBackgroundResource(R.drawable.star_full);
                img2.setBackgroundResource(R.drawable.star_full);
                img3.setBackgroundResource(R.drawable.star_full);
                if (rating != 3.0) {
                    img4.setBackgroundResource(R.drawable.star_half);
                } else {
                    img4.setBackgroundResource(R.drawable.star_empty);
                }

                img5.setBackgroundResource(R.drawable.star_empty);

            } else if (rating >= 4.0 && rating < 5.0) {
                img1.setBackgroundResource(R.drawable.star_full);
                img2.setBackgroundResource(R.drawable.star_full);
                img3.setBackgroundResource(R.drawable.star_full);
                img4.setBackgroundResource(R.drawable.star_full);
                if (rating != 4.0) {
                    img5.setBackgroundResource(R.drawable.star_half);
                } else {
                    img5.setBackgroundResource(R.drawable.star_empty);
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

    protected void showCommentDialog(final Products selectedProduct, final int position) {
        if (!StorageAccess.getInstance(((ProductsForStoreActivity) context)).isPermissionGranted()) {
            StorageAccess.getInstance(((ProductsForStoreActivity) context)).checkPermission();
        }


        final Dialog dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.inflate_dialog_comment);

        // Fill up info and avatar
        name = (TextView) dialog.findViewById(R.id.textView2);
        surname = (TextView) dialog.findViewById(R.id.textView3);

        comment = (EditText) dialog.findViewById(R.id.editText1);
        avatarPictureHolder = (ImageView) dialog.findViewById(R.id.imageView1);

        SharedPreferences sharedPrefs = context.getSharedPreferences(
                USER_PREFS, Context.MODE_PRIVATE);

        String userName = sharedPrefs.getString(USER_NAME, null);
        String userSurname = sharedPrefs.getString(USER_SURNAME, null);
        String avatarPath = sharedPrefs.getString(AVATAR_PATH, null);

        name.setText(userName);
        surname.setText(userSurname);

        // try to get image if exists
        try {

            if (avatarPath != null) {
                Log.e(TAG, "avatar path in disk " + avatarPath);
                if (!avatarPath.equals("")) {

                    Bitmap imageBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(avatarPath), 200, 200, true);

                    avatarPictureHolder.setImageResource(R.drawable.avatar_circle);

                    imageBitmap = Helper.rotateMediaFile(avatarPath, imageBitmap);

                    avatarPictureHolder.setImageBitmap(Helper.getCroppedBitmap(imageBitmap));
                }
            } else {
                Log.e(TAG, "avatar path is null ");
            }

        } catch (Exception e) {
            Log.e(TAG, e);
        }

        // STAR LISTENERS
        ImageView img1, img2, img3, img4, img5;
        img1 = (ImageView) dialog.findViewById(R.id.star1);
        img2 = (ImageView) dialog.findViewById(R.id.star2);
        img3 = (ImageView) dialog.findViewById(R.id.star3);
        img4 = (ImageView) dialog.findViewById(R.id.star4);
        img5 = (ImageView) dialog.findViewById(R.id.star5);

        setTheStarRatingListeners(img1, img2, img3, img4, img5);

        // Button Listeners

        Button cancel, submit;
        cancel = (Button) dialog.findViewById(R.id.button1);
        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        submit = (Button) dialog.findViewById(R.id.button2);
        submit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (fieldsAreFilled()) {
                    new PostComment(dialog, selectedProduct, position)
                            .execute();
                } else {
                    Toast.makeText(
                            context,
                            context.getResources().getString(
                                    R.string.fill_in_all_fields),
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

        dialog.show();

    }

    /**
     * check for comments and stars before posting rating
     *
     * @return true if comment and stars are set false if not
     */
    protected boolean fieldsAreFilled() {
        return ratingCounter != 0;
    }

    /**
     * set the listeners for the start to change star images and save the rating
     *
     * @param star1 ImageView for first star
     * @param star2 ImageView for second star
     * @param star3 ImageView for third star
     * @param star4 ImageView for fourth star
     * @param star5 ImageView for fifth star
     */
    private void setTheStarRatingListeners(final ImageView star1, final ImageView star2, final ImageView star3,
                                           final ImageView star4, final ImageView star5) {

        star1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                star1.setBackgroundResource(R.drawable.star_full);
                star2.setBackgroundResource(R.drawable.star_empty);
                star3.setBackgroundResource(R.drawable.star_empty);
                star4.setBackgroundResource(R.drawable.star_empty);
                star5.setBackgroundResource(R.drawable.star_empty);

                ratingCounter = 1;
            }
        });

        star2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                star1.setBackgroundResource(R.drawable.star_full);
                star2.setBackgroundResource(R.drawable.star_full);
                star3.setBackgroundResource(R.drawable.star_empty);
                star4.setBackgroundResource(R.drawable.star_empty);
                star5.setBackgroundResource(R.drawable.star_empty);

                ratingCounter = 2;
            }
        });

        star3.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                star1.setBackgroundResource(R.drawable.star_full);
                star2.setBackgroundResource(R.drawable.star_full);
                star3.setBackgroundResource(R.drawable.star_full);
                star4.setBackgroundResource(R.drawable.star_empty);
                star5.setBackgroundResource(R.drawable.star_empty);

                ratingCounter = 3;
            }
        });

        star4.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                star1.setBackgroundResource(R.drawable.star_full);
                star2.setBackgroundResource(R.drawable.star_full);
                star3.setBackgroundResource(R.drawable.star_full);
                star4.setBackgroundResource(R.drawable.star_full);
                star5.setBackgroundResource(R.drawable.star_empty);

                ratingCounter = 4;
            }
        });

        star5.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                star1.setBackgroundResource(R.drawable.star_full);
                star2.setBackgroundResource(R.drawable.star_full);
                star3.setBackgroundResource(R.drawable.star_full);
                star4.setBackgroundResource(R.drawable.star_full);
                star5.setBackgroundResource(R.drawable.star_full);

                ratingCounter = 5;
            }
        });

    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public Products getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return values.get(position).hashCode();
    }

    private class PostComment extends AsyncTask<String, Void, String> {

        int position;
        private Result_do_rating result_do_rating;
        private Dialog dialog;
        private Products selectedProduct;

        public PostComment(Dialog dialog, Products selectedProduct, int position) {
            this.dialog = dialog;
            this.selectedProduct = selectedProduct;
            this.position = position;
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                Gson gson = new Gson();
                String token = TapstorData.getInstance().getUserToken();
                String type = "2";
                String id = selectedProduct.id;
                String commentText = comment.getText().toString();
                String ratingValue = "" + ratingCounter;

                SendPostValueRating rating = new SendPostValueRating(type, id,
                        ratingValue, commentText, token);

                // Call to web service
                String reader = RestServices.getInstance().postOperation(
                        rating, RestServices.getInstance().DO_RATING);
                RestResponse rest = gson.fromJson(reader, RestResponse.class);

                result_do_rating = rest.result_do_rating;
            } catch (Exception e) {
                Log.e(TAG, e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            try {

                if (result_do_rating.error.equals("")) {

                    dialog.dismiss();

                    ((ProductsForStoreActivity) context).refreshView(position);

                    notifyDataSetChanged();

                } else {

                    Toast.makeText(
                            context,
                            context.getResources().getString(
                                    R.string.already_commented),
                            Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                Toast.makeText(
                        context,
                        context.getResources().getString(
                                R.string.already_commented), Toast.LENGTH_LONG)
                        .show();
                Log.e(TAG, e);
            }

        }
    }

}
