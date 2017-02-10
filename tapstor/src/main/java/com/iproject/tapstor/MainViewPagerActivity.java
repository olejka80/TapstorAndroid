package com.iproject.tapstor;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iproject.tapstor.fragments.TabFragmentEnterprises;
import com.iproject.tapstor.fragments.TabFragmentFavorites;
import com.iproject.tapstor.fragments.TabFragmentSettings;
import com.iproject.tapstor.helper.Helper;
import com.iproject.tapstor.helper.TapstorActivity;
import com.iproject.tapstor.helper.TapstorData;
import com.iproject.tapstor.library.FusedLocationAccess;
import com.iproject.tapstor.library.Log;
import com.iproject.tapstor.objects.Cat;
import com.iproject.tapstor.rest.Messages;
import com.iproject.tapstor.rest.RestResponse;
import com.iproject.tapstor.rest.RestServices;
import com.iproject.tapstor.rest.Result_user_messages;

import java.util.ArrayList;
import java.util.List;

/**
 * The Activity that displays Listings based on popularity
 *
 * @author Dimitris Touzloudis <dimitris.touzloudis@gmail.com>
 */
public class MainViewPagerActivity extends TapstorActivity {

    private final static String TAG = "MainViewPagerActivity";
    boolean firstLoad = true;
    private ActionBar actionBar;
    private ViewPager mViewPager;
    private Context context;
    private TabFragmentEnterprises mTabFragmentEnterprises;
    private TabFragmentFavorites mTabFragmentFavorites;
    private TabFragmentSettings mTabFragmentSettings;
    private Menu menu;
    private SubMenu subMenu;
    private MenuItem itemMenu;
    private TabLayout mTabLayout;
    private ViewPagerAdapter viewPagerAdapter;


    public void onPause() {
        super.onPause();
        FusedLocationAccess.getInstance(this).disableLocationListener();
        try {
            if (mViewPager.getCurrentItem() == 0) {
                if (mTabFragmentEnterprises != null)
                    mTabFragmentEnterprises.closeSearch();
            } else if (mViewPager.getCurrentItem() == 1) {

                if (mTabFragmentFavorites != null)
                    mTabFragmentFavorites.closeCategoryMenu();

            }
        } catch (Exception ignored) {

        }
        // Helper.closeSearch(ll1, categorySelection);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        if (mViewPager.getCurrentItem() == 0) {
            Log.w(TAG, "log:" + 01);
            TapstorData.getInstance().setSelection(-99, 1);
            TapstorData.getInstance().setSelectedCategoryId(-99, 1);
        } else if (mViewPager.getCurrentItem() == 1) {
            Log.w(TAG, "log:" + 02);
            TapstorData.getInstance().setSelection(-99, 2);
            TapstorData.getInstance().setSelectedCategoryId(-99, 2);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mViewPager.getCurrentItem() == 0) {
            Log.w(TAG, "log:" + 01);
            TapstorData.getInstance().setSelection(-99, 1);
            TapstorData.getInstance().setSelectedCategoryId(-99, 1);
        } else if (mViewPager.getCurrentItem() == 1) {
            Log.w(TAG, "log:" + 02);
            TapstorData.getInstance().setSelection(-99, 2);
            TapstorData.getInstance().setSelectedCategoryId(-99, 2);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FusedLocationAccess.getInstance(this).enableLocationListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FusedLocationAccess.getInstance(this).enableLocationListener();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        try {
            Log.e(TAG, "popular 1");
            Cat cat = null;
            if (mViewPager.getCurrentItem() == 0) {
                cat = Helper.getSelectedCategory(1);
            } else {
                cat = Helper.getSelectedCategory(2);
            }

            if (cat != null) {
                if (mViewPager.getCurrentItem() == 0) {
                    if (mTabFragmentEnterprises != null)
                        mTabFragmentEnterprises.setCategorySelectionRow(cat);
                } else if (mViewPager.getCurrentItem() == 1) {
                    if (mTabFragmentFavorites != null)
                        mTabFragmentFavorites.setCategorySelectionRow(cat);
                }
            }

            if (!firstLoad) {
                if (mTabFragmentFavorites != null) {
                    Log.e(TAG, "AAAAAA 3");
                    mTabFragmentFavorites.fragmentForceRefresh();

                }
            } else {
                firstLoad = false;
            }
        } catch (Exception ignored) {

        }

        new FetchNotifications().execute();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        try {
            changeBottomBarOnClick(intent.getExtras().getInt("TAB", 1));
        } catch (Exception e) {
            Log.e(TAG, e);
        }
        super.onNewIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_view_pager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        actionBar = getSupportActionBar();

        context = MainViewPagerActivity.this;
        boolean notify = false;
        if (getIntent().getExtras() != null) {
            notify = getIntent().getExtras().getBoolean("go_to_notifications", false);
        }

        FusedLocationAccess.getInstance(this).addOnLocationChangedListener(new FusedLocationAccess.OnLocationChangedListener() {
            @Override
            public void onLocationChanged(Location location) {
                TapstorData.getInstance().setLocation(location);
                mTabFragmentEnterprises.locationFound();
            }
        });


        TapstorData.getInstance().setTab(1);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        mTabFragmentEnterprises = TabFragmentEnterprises.newInstance();
        viewPagerAdapter.addFrag(mTabFragmentEnterprises, getResources().getString(R.string.tab_enterprises));

        mTabFragmentFavorites = TabFragmentFavorites.newInstance();
        viewPagerAdapter.addFrag(mTabFragmentFavorites, getResources().getString(R.string.tab_favorites));

        mTabFragmentSettings = TabFragmentSettings.newInstance();
        viewPagerAdapter.addFrag(mTabFragmentSettings, getResources().getString(R.string.tab_settings));


        mViewPager.setAdapter(viewPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        setupTabIcons();

        mTabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
                int pos = tab.getPosition();

                int color;

                if (pos == 0) {
                    color = getResources().getColor(R.color.blue);
                    toggleActionBarButtons(false);
                    toggleBottomTabBar(true);
                    mTabFragmentEnterprises.fragmentForceRefresh();
                } else if (pos == 1) {
                    color = getResources().getColor(R.color.purple);
                    toggleActionBarButtons(false);
                    toggleBottomTabBar(true);
                    mTabFragmentFavorites.fragmentForceRefresh();
                } else {
                    color = getResources().getColor(R.color.yellow);
                    (menu.findItem(R.id.search)).collapseActionView();
                    toggleActionBarButtons(true);
                    toggleBottomTabBar(false);
                }
                mTabLayout.setSelectedTabIndicatorColor(color);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mTabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.blue));
        mTabLayout.setSelectedTabIndicatorHeight(Helper.convertDpToPixel(6, MainViewPagerActivity.this));

        actionBar.setTitle("");



		/*
         * ALL THE TAB BAR BUTTONS BELOW
		 */
        findViewById(R.id.tab1).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Helper.sendGoogleAnalyticsAction("Tab choice", "Popular", null, context);
                changeBottomBarOnClick(1);
            }
        });
        findViewById(R.id.tab2).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Helper.sendGoogleAnalyticsAction("Tab choice", "Near me", null, context);
                changeBottomBarOnClick(2);

            }
        });

        findViewById(R.id.tab3).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Helper.sendGoogleAnalyticsAction("Tab choice", "New", null, context);
                changeBottomBarOnClick(3);

            }
        });

        findViewById(R.id.tab4).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Helper.sendGoogleAnalyticsAction("Tab choice", "Notifications", null, context);

                Intent i = new Intent(context, NotificationsActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        });

        findViewById(R.id.tab5).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Helper.sendGoogleAnalyticsAction("Tab choice", "AR", null, context);

                if (Helper.checkForSensors(context)) {
                    Helper.checkCameraAugmented(MainViewPagerActivity.this, false);
                } else {
                    Toast.makeText(context, R.string.not_supported, Toast.LENGTH_SHORT).show();
                }

            }
        });

		/*
         * ********************************
		 */

        if (notify) {
            startActivity(new Intent(MainViewPagerActivity.this, NotificationsActivity.class));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Used to put dark icons on light action bar
        try {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.options_menu, menu);
            this.menu = menu;
            // Associate Searchable configuration with the SearchView
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

            searchView.setOnQueryTextListener(new OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String arg0) {

                    Log.e(TAG, "text submit");
                    searchView.clearFocus();
                    performTheSearchFunctionality(arg0);

                    return false;

                }

                @Override
                public boolean onQueryTextChange(String arg0) {

                    if (arg0.length() == 0) {
                        Log.e(TAG, "text clear");
                        performTheSearchFunctionality(arg0);
                    }

                    return false;
                }

            });

            subMenu = menu.addSubMenu("Κατηγορίες");
            itemMenu = subMenu.getItem();
            itemMenu.setIcon(R.drawable.ic_action_more_details_btn_section);
            itemMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            itemMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    if (mViewPager.getCurrentItem() == 0) {
                        if (mTabFragmentEnterprises != null) {
                            mTabFragmentEnterprises.menuItemClicked();
                        }
                    } else if (mViewPager.getCurrentItem() == 1) {
                        if (mTabFragmentFavorites != null) {
                            mTabFragmentFavorites.menuItemClicked();
                        }
                    }

                    return false;
                }

            });

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private void toggleActionBarButtons(boolean isThirdTab) {
        menu.findItem(R.id.search).setEnabled(!isThirdTab);
        itemMenu.setEnabled(!isThirdTab);
    }

    private void toggleBottomTabBar(boolean enable) {
        findViewById(R.id.bottom_navigation_bar).setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    private void performTheSearchFunctionality(String arg0) {

        if (mViewPager.getCurrentItem() == 0) {
            if (mTabFragmentEnterprises != null)
                mTabFragmentEnterprises.performTheSearchFunctionality(arg0);
        } else if (mViewPager.getCurrentItem() == 1) {
            if (mTabFragmentFavorites != null)
                mTabFragmentFavorites.performtheSearchFunctionality(arg0);
        }

        try {

            if (arg0.length() > 0) {
                try {
                    Helper.sendGoogleAnalyticsAction("Search", arg0, null, context);
                } catch (Exception e) {
                    Log.e(TAG, e);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, e);
        }

    }

    public void changeBottomBarOnClick(int position) {

        if (TapstorData.getInstance().getTab() == position && position != 2) {
            return;
        }

        ImageView tab1 = (ImageView) findViewById(R.id.img1);
        ImageView tab2 = (ImageView) findViewById(R.id.img2);
        ImageView tab3 = (ImageView) findViewById(R.id.img3);

        TextView txt1 = (TextView) findViewById(R.id.txt1);
        TextView txt2 = (TextView) findViewById(R.id.txt2);
        TextView txt3 = (TextView) findViewById(R.id.txt3);
        if (position == 1) {

            // actionBar.setTitle("Δημοφιλείς");
            tab1.setImageResource(R.drawable.tabbar_popular_p);
            tab2.setImageResource(R.drawable.tabbar_near_me);
            tab3.setImageResource(R.drawable.tabbar_new);

            txt1.setTextAppearance(context, R.style.TextStyleBlack);
            txt2.setTextAppearance(context, R.style.TextStyleGray);
            txt3.setTextAppearance(context, R.style.TextStyleGray);

        } else if (position == 2) {

            FusedLocationAccess.getInstance(this).enableLocationListener();

            if (!Helper.checkProvider(context)) {
                Toast.makeText(context, R.string.gps_error, Toast.LENGTH_LONG).show();
            }

            // actionBar.setTitle("Κοντά μου");
            tab1.setImageResource(R.drawable.tabbar_popular);
            tab2.setImageResource(R.drawable.tabbar_near_me_p);
            tab3.setImageResource(R.drawable.tabbar_new);

            txt2.setTextAppearance(context, R.style.TextStyleBlack);
            txt1.setTextAppearance(context, R.style.TextStyleGray);
            txt3.setTextAppearance(context, R.style.TextStyleGray);
        } else {

            // actionBar.setTitle("Νέες");
            tab1.setImageResource(R.drawable.tabbar_popular);
            tab2.setImageResource(R.drawable.tabbar_near_me);
            tab3.setImageResource(R.drawable.tabbar_new_p);

            txt3.setTextAppearance(context, R.style.TextStyleBlack);
            txt2.setTextAppearance(context, R.style.TextStyleGray);
            txt1.setTextAppearance(context, R.style.TextStyleGray);

        }

        TapstorData.getInstance().setTab(position);

        if (mTabFragmentEnterprises != null) {
            Log.e(TAG, "AAAAAA 1");
            mTabFragmentEnterprises.fragmentForceRefresh();
        }
        if (mTabFragmentFavorites != null) {
            Log.e(TAG, "AAAAAA 2");
            mTabFragmentFavorites.fragmentForceRefresh();
        }

    }

    public void refreshMessagesPopUp() {
        TextView redBall = (TextView) findViewById(R.id.notification_num);
        if (TapstorData.getInstance().getUnreadMessages() != 0) {
            if (TapstorData.getInstance().getUnreadMessages() > 99) {
                redBall.setText(String.format(getResources().getString(R.string.placeholder_plus), String.valueOf(99)));
            } else {
                redBall.setText(String.format(getResources().getString(R.string.placeholder_string), String.valueOf(TapstorData.getInstance().getUnreadMessages())));
            }
            redBall.setVisibility(View.VISIBLE);
        } else {
            redBall.setVisibility(View.GONE);
        }
    }

    private void setupTab(int position, int resTitle) {
        RelativeLayout tab = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.tab_item_tabhost, null);
        TextView title = (TextView) tab.findViewById(R.id.tabText);
        title.setText(resTitle);
        mTabLayout.getTabAt(position).setCustomView(tab);
    }

    private void setupTabIcons() {
        setupTab(0, R.string.tab_enterprises);
        setupTab(1, R.string.tab_favorites);
        setupTab(2, R.string.tab_settings);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    /**
     * @author Grassos Konstantinos <grassos.konstantinos@gmail.com>
     */
    private class FetchNotifications extends AsyncTask<String, Void, String> {

        private Result_user_messages result_user_messages;

        @Override
        protected String doInBackground(final String... args) {

            try {

                Gson gson = new Gson();

                String reader = RestServices.getInstance().getOperation(
                        RestServices.getInstance().GET_USER_NOTIFICATIONS
                                + TapstorData.getInstance().getUserToken());

                RestResponse response = gson.fromJson(reader,
                        RestResponse.class);

                result_user_messages = response.result_user_messages;

            } catch (Exception e) {
                Log.e(TAG, e);
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void onPostExecute(final String result) {

            try {

                Log.e(TAG, "inside post execute");

                // findViewById(R.id.progressBar1).setVisibility(View.GONE);

                if (result_user_messages.error.equals("")) {

                    // load notifications list
                    TapstorData.getInstance().notificationIdsofReadList = Helper.readNotifications(context);

                    if (TapstorData.getInstance().notificationIdsofReadList == null) {
                        TapstorData.getInstance().notificationIdsofReadList = new ArrayList<>();
                    }

                    if (result_user_messages.messages.size() == 0) {

                        TapstorData.getInstance().setUnreadMessages(0);

                    } else {

                        int unRead = 0;
                        for (Messages message : result_user_messages.messages) {
                            if (!TapstorData.getInstance().notificationIdsofReadList
                                    .contains(message.id)) {
                                unRead += 1;
                            }
                        }

                        TapstorData.getInstance().setUnreadMessages(unRead);

                    }

                    refreshMessagesPopUp();

                }

            } catch (Exception e) {
                // findViewById(R.id.not_found).setVisibility(View.VISIBLE);
            }

            refreshMessagesPopUp();

        }
    }

}
