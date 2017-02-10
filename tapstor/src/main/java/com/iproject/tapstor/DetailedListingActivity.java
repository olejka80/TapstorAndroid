package com.iproject.tapstor;

import android.app.ActionBar.LayoutParams;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.iproject.tapstor.helper.Helper;
import com.iproject.tapstor.helper.TapstorActivity;
import com.iproject.tapstor.helper.TapstorData;
import com.iproject.tapstor.library.AutoResizeTextView;
import com.iproject.tapstor.library.CircleTransform;
import com.iproject.tapstor.library.Log;
import com.iproject.tapstor.library.StorageAccess;
import com.iproject.tapstor.objects.Availability;
import com.iproject.tapstor.objects.DaySchedule;
import com.iproject.tapstor.objects.Element;
import com.iproject.tapstor.objects.ElementResult;
import com.iproject.tapstor.objects.News;
import com.iproject.tapstor.objects.Rating;
import com.iproject.tapstor.objects.Store;
import com.iproject.tapstor.rest.RestResponse;
import com.iproject.tapstor.rest.RestServices;
import com.iproject.tapstor.rest.Result_do_favorite;
import com.iproject.tapstor.rest.Result_do_rating;
import com.iproject.tapstor.rest.SendPostValueRating;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DetailedListingActivity extends TapstorActivity {

    protected static final String TAG = "DetailedListingActivity";
    boolean isFromNotifications;
    boolean goToProducts;
    int companyId, productId;
    int ratingCounter = 0;
    private Context context;
    private boolean isFavorite = false;
    private String USER_PREFS = "USER_PREFS";
    private String USER_NAME = "USER_NAME";
    private String USER_SURNAME = "USER_SURNAME";
    private String AVATAR_PATH = "AVATAR_PATH";
    private TextView name;
    private TextView surname;
    private EditText comment;
    private ImageView avatarPictureHolder;
    private TextView title;
    private TextView distance;
    private String descText;
    private int lastVisibleLineNumber = 0;
    private TextView description;
    private AlertDialog.Builder chooseAction;
    private AlertDialog choose;
    private List<Rating> ratings;
    private List<News> news;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_listing);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        findViewById(R.id.tabBar).setVisibility(View.INVISIBLE);
        findViewById(R.id.companyNewsSection).setVisibility(View.GONE);
        context = DetailedListingActivity.this;

        if (getIntent().getExtras() != null) {
            isFromNotifications = getIntent().getExtras().getBoolean("fromNotifications", false);
            companyId = getIntent().getExtras().getInt("CompanyId");
            if (isFromNotifications) {

                goToProducts = getIntent().getExtras().getBoolean("openProduct", false);
                if (goToProducts) {
                    productId = getIntent().getExtras().getInt("ProductId");
                }
            }
        }

        if (!isFromNotifications) {
            setUpActionBar();
        } else {

            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);

            ActionBar actionBar = getSupportActionBar();
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle("");
            actionBar.setLogo(android.R.color.transparent);
            actionBar.setIcon(android.R.color.transparent);
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_left);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.grey));
        }

        fixTopImageViewHeights();

        new GetDetails().execute();

    }

    private void fixTopImageViewHeights() {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int width, height;
        width = metrics.widthPixels - (int) ((90 * metrics.density) + 0.5);

        height = (int) (width / 1.31);

        Log.e(TAG, "w " + width + " h" + height);
        android.widget.RelativeLayout.LayoutParams relParams =
                (android.widget.RelativeLayout.LayoutParams) findViewById(R.id.imageView1).getLayoutParams();
        relParams.height = height;
        findViewById(R.id.imageView1).setLayoutParams(relParams);

        android.widget.RelativeLayout.LayoutParams linParams =
                (android.widget.RelativeLayout.LayoutParams) findViewById(R.id.right_placeholder).getLayoutParams();
        linParams.height = height;

        findViewById(R.id.right_placeholder).setLayoutParams(linParams);

    }

    private void setUpActionBar() {

        // Inflate the custom view
        title = (TextView) findViewById(R.id.header_title);
        if (isFromNotifications) {
            title.setText(TapstorData.getInstance().getSelectedElement().title);
        } else {
            title.setText(TapstorData.getInstance().getSelectedEnterprise().name);
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("");

        actionBar.setHomeAsUpIndicator(R.drawable.arrow_left);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.white));

    }

    /**
     * calculate users distance to closest store
     */
    private void calcDistance() {

        if (TapstorData.getInstance().getLatitude() != 0.0 && TapstorData.getInstance().getLongitude() != 0.0) {

            LatLng me = new LatLng(TapstorData.getInstance().getLatitude(),
                    TapstorData.getInstance().getLongitude());
            LatLng company = null;

            try {
                Helper.findNearestStore(TapstorData.getInstance().getSelectedElement());

                company = new LatLng(Double.parseDouble(TapstorData
                        .getInstance().getSelectedElement().closestStore.lat),
                        Double.parseDouble(TapstorData.getInstance()
                                .getSelectedElement().closestStore.lng));

            } catch (Exception e) {
                company = new LatLng(0.0, 0.0);
            }

            findViewById(R.id.distance_container).setVisibility(View.VISIBLE);

            ((TextView) findViewById(R.id.distance_container)).setText(Helper.distanceBetweenTwoPoints(me, company, context));

        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showMessage(@StringRes int message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Set up the store detailed View with the selected store's info and details
     *
     * @param response the Web service response object for the selected store
     */
    private void setUpLayoutForStore(ElementResult response) {
        // set logo of the store
        ImageView storeImage = (ImageView) findViewById(R.id.imageView1);

        if (!(response.element.avatar == null || response.element.avatar
                .equals(""))) {
            Picasso.with(context).load(response.element.avatar)
                    .into(storeImage);
        } else {
            Picasso.with(context).load(R.drawable.tapstor_icon)
                    .into(storeImage);
        }
        // Set rating info of store
        TextView storeRating = (TextView) findViewById(R.id.store_rating);
        storeRating.setText(response.element.average_rating + "");

        // set the store star rating
        ImageView img1, img2, img3, img4, img5;
        img1 = (ImageView) findViewById(R.id.star1);
        img2 = (ImageView) findViewById(R.id.star2);
        img3 = (ImageView) findViewById(R.id.star3);
        img4 = (ImageView) findViewById(R.id.star4);
        img5 = (ImageView) findViewById(R.id.star5);
        setStoreStarRating(Double.parseDouble(response.element.average_rating),
                img1, img2, img3, img4, img5);

        setTheDescriptionSection(response);

        isFavorite = response.element.favorite != 0;

        // Set the news section of the store
        setTheNewsSection(response);

        // Set the Comments section of the store
        setTheCommentsSection(response);

        // Set all the listeners and actions
        setTheListeners(response);

    }

    private void setTheDescriptionSection(ElementResult response) {

        /** set description of store **/

        String desc = "";
        if (response.element.description != null) {
            desc = response.element.description;
        }

        descText = desc;

        // Fix the description text
        description = (TextView) findViewById(R.id.description_text);
        description.setText(desc);

        description.post(new Runnable() {

            @Override
            public void run() {

                lastVisibleLineNumber = description.getLineCount();
                Log.e(TAG, "lastVisibleLineNumber " + lastVisibleLineNumber);
                setDescriptionLayout();
            }
        });

    }

    private void setDescriptionLayout() {
        final TextView descHeader = (TextView) findViewById(R.id.description);

        if (lastVisibleLineNumber > 2) {
            descHeader.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    R.drawable.more_desc_arrow, 0);
            // }

            description.setMaxLines(2);
            description.setEllipsize(TruncateAt.END);
            description.invalidate();

            descHeader.setOnClickListener(new OnClickListener() {
                boolean clicked = false;

                @Override
                public void onClick(View v) {

                    try {
                        if (!clicked) {

                            descHeader.setCompoundDrawablesWithIntrinsicBounds(
                                    0, 0, R.drawable.more_desc_arrow_up, 0);

                            clicked = true;
                            description.setMaxLines(Integer.MAX_VALUE);
                            description.setEllipsize(null);
                            description.setText(descText);
                            description.invalidate();

                        } else {
                            descHeader.setCompoundDrawablesWithIntrinsicBounds(
                                    0, 0, R.drawable.more_desc_arrow, 0);

                            clicked = false;
                            description.setMaxLines(2);
                            description.setEllipsize(TruncateAt.END);
                            description.setText(descText);
                            description.invalidate();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e);
                    }
                }
            });

        }

    }

    // SET THE VIEW LISTENERS WITH TOP BOTTOM HIERARCHY
    private void setTheListeners(final ElementResult response) {

        // Listener for the rating layout of store to prompt the user to rate
        // or create profile in order to rate
        findViewById(R.id.store_rating_relavite_layout).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (Helper.checkIfUserHasProfile(context)) {
                            showCommentDialog();
                        } else {
                            // go to profile but first show choice dialog
                            showProfileDialog();

                        }

                    }
                });

        // Share listener to initiate Android share Intent
        findViewById(R.id.share_layout).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        final Element elemement = TapstorData.getInstance().getSelectedElement();


                        chooseAction = new AlertDialog.Builder(context);

                        chooseAction.setCancelable(true);
                        chooseAction.setItems(R.array.share_options,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Helper.shareOption(context, elemement.title,
                                                elemement.description, elemement.avatar, which);
                                    }

                                });

                        choose = chooseAction.create();
                        // choose.setCanceledOnTouchOutside(true);
                        choose.show();

                    }
                });

        // Listener for the plus sign layout to prompt the user to rate
        // or create profile in order to rate
        findViewById(R.id.add_comment).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (Helper.checkIfUserHasProfile(context)) {
                            showCommentDialog();
                        } else {
                            // go to profile but first show choice dialog
                            showProfileDialog();
                        }

                    }
                });

        /** BOTTOM TAB BAR HERE **/

        // tab for products
        findViewById(R.id.tab1).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // send tracker.
                Helper.sendGoogleAnalyticsAction("Tab choice", "Products", TapstorData.getInstance().getSelectedEnterprise().id, context);

                Intent prod = new Intent(DetailedListingActivity.this,
                        ProductsForStoreActivity.class);

                prod.putExtra("offers", false);

                startActivity(prod);

            }
        });

        // tab for offers
        findViewById(R.id.tab2).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // send tracker.
                Helper.sendGoogleAnalyticsAction("Tab choice", "Offers", TapstorData.getInstance().getSelectedEnterprise().id, context);

                Intent prod = new Intent(DetailedListingActivity.this,
                        ProductsForStoreActivity.class);

                prod.putExtra("offers", true);

                startActivity(prod);

            }
        });

        // tab for contact info
        findViewById(R.id.tab3).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // send tracker.
                Helper.sendGoogleAnalyticsAction("Tab choice", "Contact Info", TapstorData.getInstance().getSelectedEnterprise().id, context);

                setContactInfo(response);
            }
        });

        final ImageView fav = (ImageView) findViewById(R.id.fav_image);
        if (isFavorite) {
            fav.setImageResource(R.drawable.tab_fav_selected);
        }
        // tab for Favorite
        findViewById(R.id.tab4).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (isFavorite) {
                    isFavorite = false;
                    fav.setImageResource(R.drawable.tab_fav);
                } else {
                    // send tracker.
                    Helper.sendGoogleAnalyticsAction("Tab choice", "Favorite", TapstorData.getInstance().getSelectedEnterprise().id, context);
                    isFavorite = true;
                    fav.setImageResource(R.drawable.tab_fav_selected);
                }

                new DoFavorite().execute();

            }
        });

        // tab for AR
        findViewById(R.id.tab5).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // send tracker.
                Helper.sendGoogleAnalyticsAction("Tab choice", "AR Company", TapstorData.getInstance().getSelectedEnterprise().id, context);

                Helper.checkCameraAugmented(DetailedListingActivity.this, true);


            }
        });
        try {
            setupBottomBar(response.element);
        } catch (Exception e) {
            Log.e(TAG, e);
        }

    }

    private void setupBottomBar(final Element element) {
        if (!element.hasProducts()) {
            findViewById(R.id.tab1).setVisibility(View.GONE);
            findViewById(R.id.tab2).setVisibility(View.GONE);
            findViewById(R.id.tab3).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    openStoreMap(getClosestStore(element.stores));
                }
            });
        } else {
            findViewById(R.id.companyNewsSection).setVisibility(View.VISIBLE);
        }
        findViewById(R.id.tabBar).setVisibility(View.VISIBLE);
    }


    protected void showCommentDialog() {
        if (!StorageAccess.getInstance(this).isPermissionGranted()) {
            StorageAccess.getInstance(this).checkPermission();
            return;
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
                    new PostComment(dialog).execute();
                } else {
                    Toast.makeText(context, R.string.fill_in_all_fields, Toast.LENGTH_SHORT).show();
                }

            }
        });

        dialog.show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        StorageAccess.getInstance(this).onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
    private void setTheStarRatingListeners(final ImageView star1, final ImageView star2,
                                           final ImageView star3, final ImageView star4,
                                           final ImageView star5) {

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

    /**
     * check for comments and stars before posting rating
     *
     * @return true if comment and stars are set false if not
     */
    protected boolean fieldsAreFilled() {
        return ratingCounter != 0;
    }

    protected void showProfileDialog() {

        new AlertDialog.Builder(context)
                .setMessage(R.string.dialog_text)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                startActivityForResult(new Intent(context,
                                        UserProfileActivity.class), 1234);
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        })

                .show();

    }

    public void refreshView() {

        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        startActivity(intent);

    }

    /**
     * Set the comment section with the comments rows
     *
     * @param response
     */
    private void setTheCommentsSection(final ElementResult response) {
        ratings = response.ratings;
        if (ratings != null) {

            if (ratings.size() > 0) {
                ((TextView) findViewById(R.id.num_of_comments)).setText(String.format(getResources().getString(R.string.placeholder_string), ratings.size()));

                final LinearLayout commentsLayout = (LinearLayout) findViewById(R.id.linear_layout_comments);

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
                    final TextView expandComments = (TextView) findViewById(R.id.expand_comments);


                    expandComments.setVisibility(View.VISIBLE);
                    expandComments.setOnClickListener(
                            new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    expandComments.setVisibility(View.GONE);

                                    commentsLayout.removeAllViews();
                                    int counter = 0;
                                    for (int i = ratings.size() - 1; i >= 0; i--) {
                                        counter++;

                                        inflateCommentsRow(ratings.get(i), commentsLayout);
                                    }


                                }
                            });
                }

            } else {

                ((TextView) findViewById(R.id.num_of_comments)).setText("0");

            }
        }

    }

    /**
     * Inflates a comment row inside the layout
     *
     * @param rating         object
     * @param commentsLayout the layout to put comments into
     */
    private void inflateCommentsRow(final Rating rating, LinearLayout commentsLayout) {

        View child = getLayoutInflater().inflate(R.layout.row_comment_item,
                new LinearLayout(this), false);

        final TextView title = (TextView) child.findViewById(R.id.textView1);
        final TextView content = (TextView) child.findViewById(R.id.textView2);
        final TextView stamp = (TextView) child
                .findViewById(R.id.textView_stamp);

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

        if (rating.reply == null || rating.reply.length() == 0) {
            child.findViewById(R.id.reply).setVisibility(View.GONE);
        } else {
            child.findViewById(R.id.reply).setVisibility(View.VISIBLE);
        }

        child.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showPopUpDetailDialog(rating.image, stamp.getText().toString(),
                        rating.name, rating.content, rating.reply, false);

            }

        });

        commentsLayout.addView(child);

    }

    /**
     * set the news section of the store
     */
    private void setTheNewsSection(final ElementResult response2) {
        news = response2.news;
        if (news != null) {
            final LinearLayout newsLayout = (LinearLayout) findViewById(R.id.linear_layout_news);

            if (news.size() > 0) {
                // Make the view visible


                findViewById(R.id.no_news).setVisibility(View.GONE);

                int counter = 0;

                for (int i = 0; i < news.size(); i++) {
                    inflateNewsRow(news.get(i), newsLayout);
                    counter++;
                    if (counter == 3) {
                        break;
                    }

                }

            } else {
                findViewById(R.id.no_news).setVisibility(View.VISIBLE);
            }

            if (news.size() > 3) {

                final TextView expandNews = (TextView) findViewById(R.id.expand_news);

                expandNews.setVisibility(View.VISIBLE);
                expandNews.setOnClickListener(
                        new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                newsLayout.removeAllViews();
                                for (int i = 0; i < news.size(); i++) {
                                    inflateNewsRow(news.get(i), newsLayout);
                                    expandNews.setVisibility(View.GONE);
                                }
                            }
                        });
            }

        }

    }

    /**
     * Inflate each row with the layout
     *
     * @param news       news object
     * @param newsLayout the layout to put into
     */
    private void inflateNewsRow(final News news, LinearLayout newsLayout) {

        View child = getLayoutInflater().inflate(R.layout.row_news_item,
                new LinearLayout(this), false);

        final TextView title = (TextView) child
                .findViewById(R.id.textView_title);
        final TextView content = (TextView) child
                .findViewById(R.id.textView_content);
        final TextView stamp = (TextView) child
                .findViewById(R.id.textView_stamp);
        // set the title and content of the news object
        title.setText(news.title);
        content.setText(news.content);
        // set the stamp verbally
        stamp.setText(Helper.calculateAndDisplayTime(news.stamp, context));

        ImageView avatar = (ImageView) child.findViewById(R.id.imageView1);
        try {
            if (news.image == null || news.image.equals("")) {
                Picasso.with(context).load(R.drawable.tapstor_icon).into(avatar);
            } else {
                Picasso.with(context).load(news.image).into(avatar);
            }
        } catch (Exception e) {
            Log.e(TAG, e);
        }

        child.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showPopUpDetailDialog(news.image, stamp.getText().toString(),
                        news.title, news.content, null, true);

            }

        });

        newsLayout.addView(child);

    }

    /**
     * Iterates the store list and finds the closest store to the user's
     * location
     *
     * @param stores the list of stores related to this Brand
     * @return A store for the closest store
     */
    private Store getClosestStore(List<Store> stores) {

        Store closestStore = stores.get(0);
        Log.e(TAG, "getClosestStore");
        try {
            if (TapstorData.getInstance().getLatitude() != 0.0
                    || TapstorData.getInstance().getLongitude() != 0.0) {

                Location myLocation = new Location("myLocation");
                myLocation.setLatitude(TapstorData.getInstance().getLatitude());
                myLocation.setLongitude(TapstorData.getInstance()
                        .getLongitude());

                float distanceToClosestStore = 0.0f;

                for (Store store : stores) {

                    try {

                        Location location = new Location("Store");
                        location.setLatitude(Double.parseDouble(store.lat));
                        location.setLongitude(Double.parseDouble(store.lng));

                        float dist = myLocation.distanceTo(location) / 1000;

                        if (distanceToClosestStore == 0) {
                            Log.e(TAG, "closest store found");
                            distanceToClosestStore = dist;
                            closestStore = store;

                        } else if (distanceToClosestStore > dist) {
                            Log.e(TAG, "closest store found new");
                            distanceToClosestStore = dist;
                            closestStore = store;

                        } else {
                            Log.e(TAG, "nothing found");

                        }
                    } catch (Exception e) {
                        // Log.e(TAG, e);
                    }
                }

            }
        } catch (Exception e) {
            Log.e(TAG, e);
        }
        return closestStore;
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

    private void openStoreMap(Store closestStore) {
        TapstorData.getInstance().getSelectedEnterprise().lat = closestStore.lat;
        TapstorData.getInstance().getSelectedEnterprise().lng = closestStore.lng;
        startActivity(new Intent(DetailedListingActivity.this, MapViewActivity.class));
    }


    private void setContactInfo(ElementResult response) {

        // custom dialog
        final Dialog dialog = new Dialog(this, R.style.ThemeFullScreen);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(R.layout.inflate_dialog_contact);

        dialog.setCancelable(true);

        // But first find closest Store
        final Store closestStore = getClosestStore(response.element.stores);

        Log.i(TAG, "======");
        Log.i(TAG, new Gson().toJson(response));
        Log.i(TAG, "======");
        Log.i(TAG, new Gson().toJson(response.element));

        String phone = "";
        if (closestStore.phone != null) {
            phone = closestStore.phone;
            Log.i(TAG, "closestStore.phone: " + closestStore.phone);
        } else if (response.element.phone != null) {
            phone = response.element.phone;
            Log.i(TAG, "response.element.phone: " + response.element.phone);
        }

        String address = "";
        if (closestStore.address != null) {
            address = closestStore.address;
        } else if (response.element.address != null) {
            address = response.element.address;
        }

        AutoResizeTextView addressTextView = (AutoResizeTextView) dialog.findViewById(R.id.textView3);
        addressTextView.setText(address);

        final AutoResizeTextView phoneTextView = (AutoResizeTextView) dialog.findViewById(R.id.textView5);
        Log.i(TAG, "----> " + phone);
        phoneTextView.setText(phone);

        try {
            setTheTimeScheduleView(response.element.availability, dialog);

        } catch (Exception e) {
            Log.e(TAG, e);
        }
        dialog.findViewById(R.id.textView2).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        openStoreMap(closestStore);
                        dialog.dismiss();

                    }
                });

        TextView makeCallTextView = (TextView) dialog.findViewById(R.id.textView4);

        makeCallTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {

                    if (!phoneTextView.getText().toString().equals("")) {

                        Intent dial = new Intent();
                        dial.setAction("android.intent.action.DIAL");
                        dial.setData(Uri.parse("tel:" + phoneTextView.getText().toString()));
                        context.startActivity(dial);

                    }

                } catch (Exception e) {
                    Log.e(TAG, e);
                }
                dialog.dismiss();

            }
        });

        dialog.findViewById(R.id.close_button).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();

                    }
                });

        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();

    }

    private List<AvailabilityTimes> getStoreTimes(Availability availability) {
        List<AvailabilityTimes> result = new ArrayList<>(7);
        result.add(new AvailabilityTimes(context.getResources().getString(R.string.monday), availability.mon));
        result.add(new AvailabilityTimes(context.getResources().getString(R.string.tuesday), availability.tue));
        result.add(new AvailabilityTimes(context.getResources().getString(R.string.wednesday), availability.wed));
        result.add(new AvailabilityTimes(context.getResources().getString(R.string.thursday), availability.thu));
        result.add(new AvailabilityTimes(context.getResources().getString(R.string.friday), availability.fri));
        result.add(new AvailabilityTimes(context.getResources().getString(R.string.saturday), availability.sat));
        result.add(new AvailabilityTimes(context.getResources().getString(R.string.sunday), availability.sun));
        return result;

    }

    private void setTheTimeScheduleView(Availability availability, Dialog dialog) {
        List<AvailabilityTimes> availabilityTimes = getStoreTimes(availability);


        LinearLayout linearLayoutTimes = (LinearLayout) dialog.findViewById(R.id.linearLayoutTimes);

        for (AvailabilityTimes availabilityTime : availabilityTimes) {

            View child = getLayoutInflater().inflate(R.layout.inflate_store_times_item, linearLayoutTimes, false);

            TextView day = (TextView) child.findViewById(R.id.day);
            day.setText(availabilityTime.day);

            TextView firstTimes = (TextView) child.findViewById(R.id.firstTime);
            TextView secondTimes = (TextView) child.findViewById(R.id.secondTime);
            TextView closed = (TextView) child.findViewById(R.id.closed);

            if (availabilityTime.isClosed()) {
                firstTimes.setVisibility(View.GONE);
                secondTimes.setVisibility(View.GONE);
                closed.setVisibility(View.VISIBLE);
            } else {
                firstTimes.setText(availabilityTime.firstTimes);
                secondTimes.setText(availabilityTime.secondTimes);

                if (availabilityTime.firstTimes.equals("")) {
                    firstTimes.setVisibility(View.GONE);
                }

                if (availabilityTime.secondTimes.equals("")) {
                    secondTimes.setVisibility(View.GONE);
                }

                closed.setVisibility(View.GONE);
            }

            linearLayoutTimes.addView(child);
        }


    }

    private void showPopUpDetailDialog(final String imageURL, final String timestamp, final String title,
                                       final String content, final String response, final boolean isNews) {

        // custom dialog
        final Dialog masterDialog = new Dialog(this, R.style.ThemeFullScreen);

        masterDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        masterDialog.setContentView(R.layout.inflate_dialog_detail_pop_up);

        masterDialog.setCancelable(true);
        try {
            if (isNews) {
                masterDialog.findViewById(R.id.share_layout).setVisibility(
                        View.VISIBLE);

                masterDialog.findViewById(R.id.share_it).setOnClickListener(
                        new OnClickListener() {

                            @Override
                            public void onClick(View v) {


                                chooseAction = new AlertDialog.Builder(context);

                                chooseAction.setCancelable(true);
                                //chooseAction.setTitle("Μοιράσου το:");

                                chooseAction.setItems(R.array.share_options,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                Helper.shareOption(context, title, content, imageURL, which);
                                            }
                                        });

                                choose = chooseAction.create();
                                // choose.setCanceledOnTouchOutside(true);

                                masterDialog.dismiss();
                                choose.show();

                            }
                        });
            } else if (response != null && response.length() != 0) {

                masterDialog.findViewById(R.id.response_layout).setVisibility(
                        View.VISIBLE);
                ((TextView) masterDialog.findViewById(R.id.response))
                        .setText(response);
            }
        } catch (Exception e) {
            Log.e(TAG, e);
        }

        final TextView titleTextView = (TextView) masterDialog
                .findViewById(R.id.title);
        final TextView contentTextView = (TextView) masterDialog
                .findViewById(R.id.content);
        final TextView timeStampTextView = (TextView) masterDialog
                .findViewById(R.id.timestamp);

        final ImageView closeButton = (ImageView) masterDialog
                .findViewById(R.id.close_button);

        final ImageView image = (ImageView) masterDialog
                .findViewById(R.id.post_image);
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
                masterDialog.dismiss();
            }
        });

        masterDialog.show();

    }

    private class AvailabilityTimes {

        public String day;
        public String firstTimes;
        public String secondTimes;

        public AvailabilityTimes(String day, DaySchedule daySchedule) {
            this.day = day;

            if (daySchedule.from1 == null) {
                this.firstTimes = "";
            } else {
                this.firstTimes = String.format(getResources().getString(R.string.placeholder_dash), daySchedule.from1, daySchedule.to1);
            }

            if (daySchedule.from2 == null) {
                this.secondTimes = "";
            } else {
                this.secondTimes = String.format(getResources().getString(R.string.placeholder_dash), daySchedule.from2, daySchedule.to2);
            }
        }

        public boolean isClosed() {
            return firstTimes.equals("") && secondTimes.equals("");
        }


    }

    /**
     * web service to get all details for Store
     *
     * @author Grassos Konstantinos <grassos.konstantinos@gmail.com>
     */
    private class GetDetails extends AsyncTask<String, Void, String> {

        private RestResponse rest;
        private ElementResult response;

        protected String doInBackground(final String... args) {

            try {

                Gson gson = new Gson();

                String type = "1";
                String id = "";
                if (isFromNotifications) {
                    id = "" + companyId;
                } else {
                    id = TapstorData.getInstance().getSelectedEnterprise().id;

                }

                String token = TapstorData.getInstance().getUserToken();
                // Call to web service
                String reader = RestServices.getInstance().getOperation(
                        RestServices.getInstance().GET_COMPANY_DETAILS + type
                                + "/" + id + "/" + token + "/" + Helper.getLanguageToken(context));
                Log.i(TAG + ">", reader);
                rest = gson.fromJson(reader, RestResponse.class);
                response = rest.result_get_element;

            } catch (Exception e) {
                Log.e(TAG, e);
            }

            return null;
        }

        protected void onPostExecute(final String result) {

            try {

                if (response.error.equals("")) {

                    TapstorData.getInstance().setSelectedElement(response.element);

                    setUpLayoutForStore(response);
                    if (isFromNotifications) {
                        setUpActionBar();
                        if (goToProducts) {
                            Intent prod = new Intent(
                                    DetailedListingActivity.this,
                                    ProductsForStoreActivity.class);

                            prod.putExtra("offers", false);
                            prod.putExtra("goToPosition", true);
                            prod.putExtra("position", "" + productId);

                            startActivity(prod);
                        }
                    }

                    // calculate and display store distance to user
                    calcDistance();

                } else {

                    // MESSAGE TO DISPLAY IN CASE OF ERROR
                    showMessage(R.string.connection_error);
                    finish();

                }

            } catch (Exception e) {
                // MESSAGE TO DISPLAY IN CASE OF EXCEPTION
                Log.e(TAG, e);
                showMessage(R.string.connection_error);
                finish();

            }

        }

    }

    private class PostComment extends AsyncTask<String, Void, String> {

        Result_do_rating result_do_rating;
        Dialog dialog;

        public PostComment(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                Gson gson = new Gson();
                String token = TapstorData.getInstance().getUserToken();
                String type = "1";
                String id = TapstorData.getInstance().getSelectedEnterprise().id;
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
                    refreshView();

                } else {
                    showMessage(R.string.already_commented);
                }

            } catch (Exception e) {
                showMessage(R.string.already_commented);
                Log.e(TAG, e);
            }

        }

    }

    private class DoFavorite extends AsyncTask<String, Void, String> {

        RestResponse rest;
        Result_do_favorite response;

        @Override
        protected String doInBackground(String... params) {

            Gson gson = new Gson();

            // OS number for android devices
            String type = "1";
            // String deviceId = Helper.getUniqueId(context);
            String userToken = TapstorData.getInstance().getUserToken();
            String id = TapstorData.getInstance().getSelectedEnterprise().id;

            // Call to web service
            String reader = RestServices.getInstance().getOperation(
                    RestServices.getInstance().DO_FAVORITE + userToken + "/"
                            + type + "/" + id);
            rest = gson.fromJson(reader, RestResponse.class);

            response = rest.result_do_favorite;

            return null;

        }

        @Override
        protected void onPostExecute(String result) {
            if (response.error.equals("")) {

            }

        }

    }

}
