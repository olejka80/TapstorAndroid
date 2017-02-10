package com.iproject.tapstor.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iproject.tapstor.DetailedListingActivity;
import com.iproject.tapstor.R;
import com.iproject.tapstor.TapstorFragmentInterface;
import com.iproject.tapstor.adapters.EnterprisesAdapter;
import com.iproject.tapstor.adapters.SliderAdapter;
import com.iproject.tapstor.adapters.SliderAdapter.OnRemoveClickedListener;
import com.iproject.tapstor.helper.Helper;
import com.iproject.tapstor.helper.TapstorActivity;
import com.iproject.tapstor.helper.TapstorData;
import com.iproject.tapstor.library.FusedLocationAccess;
import com.iproject.tapstor.library.Log;
import com.iproject.tapstor.objects.Cat;
import com.iproject.tapstor.objects.Results;
import com.iproject.tapstor.objects.SearchChoice;
import com.iproject.tapstor.rest.RestResponse;
import com.iproject.tapstor.rest.RestServices;
import com.iproject.tapstor.rest.Result_get_results;
import com.iproject.tapstor.rest.SendPostValueGetResults;

import java.util.ArrayList;
import java.util.List;

public class TabFragmentEnterprises extends Fragment implements OnRemoveClickedListener,
        TapstorFragmentInterface, View.OnTouchListener {

    private final String TAG = "TabFragmentEnterprises";
    ViewGroup _root;
    boolean isExecuting = false;
    private GridView gridView;
    private View rootView;
    private EnterprisesAdapter adapter;
    private boolean search = false;
    private boolean stopRefreshing = false;
    private boolean stopSearchRefreshing = false;
    private int currentPage = 0;
    private int currentSearchPage = 0;
    private int previousTotal = 0;
    private boolean loading = true;
    private ArrayList<Results> oldresults = new ArrayList<>();
    private SliderAdapter sliderAdapter;
    private List<Cat> featuredCompanyValues = new ArrayList<>();
    private List<Cat> allCompanyValues = new ArrayList<>();
    private ListView list;
    private LinearLayout ll1;
    private RelativeLayout categorySelection;
    private float px;

    // *******//
    private int actualHeight;
    private SearchChoice sChoice;

    // *******//
    private int _yDelta;
    private AsyncTask<String, Void, String> mTask;
    private float top;


    public TabFragmentEnterprises() {

    }


    public static TabFragmentEnterprises newInstance() {
        return new TabFragmentEnterprises();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        if (sliderAdapter != null) {
            Log.e(TAG, "enterprise resume");
            Cat cat = Helper.getSelectedCategory(1);
            if (cat != null) {
                updateTheSliderListView(cat, -1);
                sliderAdapter.notifyDataSetChanged();
            }
        }

        FusedLocationAccess.getInstance((TapstorActivity) getContext()).enableLocationListener();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_tab_1, container, false);

        Resources r = getResources();
        px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 305, r.getDisplayMetrics());
        actualHeight = (int) px;

        if (TapstorData.getInstance().getTab() != 2) {
            newAllCategoryList();
            newCategoryList();

            sChoice = TapstorData.getInstance().getSearchChoice(1);

            // Load the data asynchronously
            Log.e(TAG, "callAsyncTaskToLoadResults 3");
            callAsyncTaskToLoadResults();
            // set the search choices listeners
            setOnClickListenerForCategoriesFiltering();
        }

        _root = (ViewGroup) rootView.findViewById(R.id.root);
        // For the slider menu
        rootView.findViewById(R.id.shadow).setOnTouchListener(this);
        rootView.findViewById(R.id.emptyView).setOnTouchListener(this);
        return rootView;
    }

    /**
     * A way to keep track and cancel previous task and start a new one
     */
    private void starAsyncTask() {

        if (mTask != null) {
            try {
                // Log.eAlways("mTask", "STOPPED TASK");
                mTask.cancel(true);
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }

        // set The Active Tab
        final int activeType = 1;

        if (TapstorData.getInstance().getTab() != 2) {

            Log.i(TAG, "1");
            mTask = new FetchEnterprises(TapstorData.getInstance()
                    .getUserToken(), "" + activeType, ""
                    + TapstorData.getInstance().getTab(), "" + getPage(),
                    sChoice.getCat(), sChoice.getSorting(),
                    sChoice.getSorting_val(), sChoice.getKeyword(), "", "",
                    search);

        } else {

            mTask = new FetchEnterprises(TapstorData.getInstance()
                    .getUserToken(), "" + activeType, ""
                    + TapstorData.getInstance().getTab(), "" + getPage(),
                    sChoice.getCat(), sChoice.getSorting(),
                    sChoice.getSorting_val(), sChoice.getKeyword(), ""
                    + TapstorData.getInstance().getLatitude(), ""
                    + TapstorData.getInstance().getLongitude(), search);

        }

        mTask.execute();

    }

    /**
     * Does the call to the Web service to get the required results and populate
     * the grid view
     */
    private void callAsyncTaskToLoadResults() {
        starAsyncTask();
    }

    /**
     * Show an alpha channel imageview to make blur effect when loading
     *
     * @param blur
     */
    public void blurView(boolean blur) {

        if (blur) {
            rootView.findViewById(R.id.blur).setVisibility(View.VISIBLE);
        } else {
            rootView.findViewById(R.id.blur).setVisibility(View.GONE);
        }

    }

    /**
     * Keep track of wether to try to load more resutls
     *
     * @return true stop refreshing reached an end/ false continue
     */
    private boolean getStopRefreshing() {
        if (search) {
            return stopSearchRefreshing;
        } else {
            return stopRefreshing;
        }
    }

    private void setStopRefreshing(boolean value) {
        if (search) {
            stopSearchRefreshing = value;
        } else {
            stopRefreshing = value;
        }
    }

    /**
     * adds the new ElementResult data to the adapter list
     *
     * @param data a list of Results
     */
    private void addNewData(List<Results> data) {
        try {
            Log.i(TAG, "adapter old size: " + adapter.getAdapterList().size());

            if (data.size() != 0) {
                adapter.addAll(data);
                adapter.notifyDataSetChanged();
                Log.i(TAG, "adapter new size: "
                        + adapter.getAdapterList().size());
                // loading = false;
            } else {
                loading = true;
            }

        } catch (Exception e) {
            Log.e(TAG, e);
        }

    }

    /**
     * adds one page to the page counter for parent results or search results
     * accordingly
     */
    private void addOnePage() {
        if (search) {
            currentSearchPage = currentSearchPage + 1;
            Log.i(TAG, "current page " + currentSearchPage);
        } else {
            currentPage = currentPage + 1;
            Log.i(TAG, "current page " + currentPage);
        }
    }

    /**
     * remove one page to the page counter for parent results or search results
     * accordingly
     */
    private void removeOnePage() {
        if (search) {
            currentSearchPage = currentSearchPage - 1;
            Log.i(TAG, "current page " + currentSearchPage);
        } else {
            currentPage = currentPage - 1;
            Log.i(TAG, "current page " + currentPage);
        }
    }

    /**
     * get the page counter for parent results or search results accordingly
     */
    private int getPage() {
        if (search) {
            Log.i(TAG, "current page " + currentSearchPage);
            return currentSearchPage;
        } else {
            Log.i(TAG, "current page " + currentPage);
            return currentPage;
        }
    }

    /**
     * Set the click listeners and animation for the top category filtering list
     * menu
     */
    private void setOnClickListenerForCategoriesFiltering() {
        ll1 = (LinearLayout) rootView.findViewById(R.id.menu_container);
        categorySelection = (RelativeLayout) rootView
                .findViewById(R.id.selected_row_category);

        rootView.findViewById(R.id.shadow).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        menuItemClicked();

                    }
                });

        list = (ListView) ll1.findViewById(R.id.listView1);

        AnimationSet set = new AnimationSet(true);
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(100);
        set.addAnimation(animation);
        // Animation sc = new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f);
        Animation sc = new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f);
        sc.setDuration(150);
        set.addAnimation(sc);
        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(200);
        set.addAnimation(animation);
        final LayoutAnimationController controller = new LayoutAnimationController(
                set, 0.5f);
        list.setLayoutAnimation(controller);

        sliderAdapter = new SliderAdapter(getActivity(), getCategoryList(), this, 1);

        list.setAdapter(sliderAdapter);

        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                Log.i(TAG, "clicked " + position);

                Animation anim = AnimationUtils.loadAnimation(getContext(),
                        android.R.anim.slide_out_right);
                anim.setDuration(500);

                int removePosition = position - list.getFirstVisiblePosition();

                Log.i(TAG, "remove visible pos " + removePosition);
                list.getChildAt(removePosition).invalidate();
                list.getChildAt(removePosition).startAnimation(anim);
                list.getChildAt(removePosition).invalidate();
                anim.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                        new Handler().post(new Runnable() {
                            public void run() {
                                Cat cat = getCategoryList().get(position);

                                updateTheSliderListView(cat, position);
                            }
                        });

                    }
                });

            }
        });

    }

    /**
     * Update the list view. Bring selected on TOP
     *
     * @param cat      category
     * @param position of listview to display selected
     */
    private void updateTheSliderListView(Cat cat, int position) {
        if (cat != null) {
            Log.i("CAT ID", "CAT ID:" + cat.id);

            if (isExecuting) {
                return;
            }

            if (cat.id < 0) {
                sChoice.setCat("");
            } else {
                sChoice.setCat("" + cat.id);
            }

            if (cat.id == -1) {

                TapstorData.getInstance().setSelectedCategoryId(cat.id, 1);

                setCategoryList(getAllCategoryList());
                getCategoryList().add(0, cat);
                getCategoryList().remove(position + 1);

                sliderDraweLayoutCallback(true);

                sliderAdapter = new SliderAdapter(getContext(), getCategoryList(),
                        TabFragmentEnterprises.this, 1);

                list.setAdapter(sliderAdapter);
                Log.i(TAG, "SET 0, 1");
                Log.w(TAG, "log:" + 4);
                sliderAdapter.setSelectedRow(0, 1);

                list.setSelection(0);


            } else {

                getCategoryList().add(0, cat);
                getCategoryList().remove(position + 1);
                TapstorData.getInstance().setSelectedCategoryId(cat.id, 1);
                Log.i(TAG, "SET 0, 1");
                Log.w(TAG, "log:" + 5);
                sliderAdapter.setSelectedRow(0, 1);
                list.setSelection(0);


                Log.i(TAG, "clicked " + position);

                Helper.closeCategoryMenu(ll1, categorySelection, 1);


                setTheCategoryWithTheListenerCallback();

            }
            clearAllDataForSearchAndPaging();
            callAsyncTaskToLoadResults();
        }
    }

    /**
     * Function to store old data in an array list
     */
    private void storePreviousData() {
        Log.i(TAG, "store data");
        oldresults = new ArrayList<Results>(adapter.getAdapterList());

    }

    /**
     * function to clear all data
     */
    private void clearAllDataForSearchAndPaging() {
        Log.i(TAG, "CLEAR ALL data");
        stopRefreshing = false;
        stopSearchRefreshing = false;
        currentPage = 0;
        currentSearchPage = 0;
    }

    /**
     * Loads the previous data before the search/filter action
     */
    private void loadPreviousData() {
        Log.i(null, "load previous data");

        if (adapter != null && oldresults != null) {
            Log.i(null, "not null");


            Log.i(null, "reload old");
            boolean nearMeTabActive = false;

            if (TapstorData.getInstance().getTab() == 2) {
                nearMeTabActive = true;
            }

            adapter = new EnterprisesAdapter(getContext(), oldresults, nearMeTabActive, R.layout.row_gridview_enterprises);
            gridView.setAdapter(adapter);

            adapter.notifyDataSetChanged();
            if (oldresults.size() != 0) {
                Log.i(null, "oldresults not 0");
                rootView.findViewById(R.id.empty).setVisibility(View.GONE);

            }
        }

    }

    /**
     * Reset the list to the default values
     *
     * @param display whether to display or hide the slide menu
     */
    private void resetTheListsToDefault(boolean display) {

        newAllCategoryList();
        newCategoryList();

        sliderAdapter = new SliderAdapter(getContext(), getCategoryList(),
                TabFragmentEnterprises.this, 1);

        AnimationSet set = new AnimationSet(true);
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(100);
        set.addAnimation(animation);
        // Animation sc = new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f);
        Animation sc = new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f);
        sc.setDuration(150);
        set.addAnimation(sc);
        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(200);
        set.addAnimation(animation);
        final LayoutAnimationController controller = new LayoutAnimationController(
                set, 0.5f);
        list.setLayoutAnimation(controller);

        if (display) {
            sliderDraweLayoutCallback(false);
        }

        ll1.invalidate();
        list.setAdapter(sliderAdapter);
        sliderAdapter.setSelectedRow(-99, 1);
        list.setSelection(0);

    }

    /**
     * Search functionality. Sets the necessary values and calls async task
     *
     * @param arg0 String to search
     */
    public void performTheSearchFunctionality(String arg0) {
        Log.i(TAG, "search for: " + arg0);

        currentSearchPage = 0;

        if (arg0.equals("")) {
            stopSearchRefreshing = false;
            search = false;
            loadPreviousData();
        } else {
            search = true;
            sChoice.setKeyword(arg0);
            Log.i(TAG, "callAsyncTaskToLoadResults 2");
            callAsyncTaskToLoadResults();
        }
    }

    @Override
    public void onRemoveClicked(boolean display) {
        sChoice.setCat("");
        Log.i(TAG, "clicked ");
        resetTheListsToDefault(display);
        clearAllDataForSearchAndPaging();
        callAsyncTaskToLoadResults();

    }

    private List<Cat> getCategoryList() {
        return featuredCompanyValues;
    }

    private void setCategoryList(List<Cat> list) {
        // TODO clone list
        this.featuredCompanyValues = list;
    }

    private void newCategoryList() {

        featuredCompanyValues = new ArrayList<>();

        for (Cat item : TapstorData.getInstance().getCloneFeatured()) {
            try {
                featuredCompanyValues.add((Cat) item.clone());
            } catch (CloneNotSupportedException e) {

                Log.i(TAG, e);

            }
        }
    }

    private List<Cat> getAllCategoryList() {
        return allCompanyValues;
    }

    private void newAllCategoryList() {
        try {
            allCompanyValues = new ArrayList<>(TapstorData.getInstance().getCloneAll().size());

            for (Cat item : TapstorData.getInstance().getCloneAll()) {
                try {
                    allCompanyValues.add((Cat) item.clone());
                } catch (CloneNotSupportedException e) {

                    Log.e(TAG, e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e);
        }
    }

    @Override
    public void fragmentForceRefresh() {

        Log.i(TAG, "frag enterprise became visible active tab" + TapstorData.getInstance().getTab());

        search = false;
        sChoice.setKeyword("");
        stopRefreshing = false;
        stopSearchRefreshing = false;
        currentPage = 0;
        currentSearchPage = 0;
        TapstorData.getInstance().setSearchChoice(new SearchChoice(), 1);
        // Load the data asynchronously
        Log.i(TAG, "callAsyncTaskToLoadResults 5 refresh");
        callAsyncTaskToLoadResults();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void locationFound() {
        if (TapstorData.getInstance().getTab() == 2) {
            newAllCategoryList();
            newCategoryList();
            TapstorData.getInstance().setSearchChoice(new SearchChoice(), 1);
            // Load the data asynchronously
            Log.i(TAG, "callAsyncTaskToLoadResults 6 loc found");
            clearAllDataForSearchAndPaging();
            callAsyncTaskToLoadResults();
            // set the search choices listeners
            try {
                setOnClickListenerForCategoriesFiltering();
            } catch (Exception ignored) {

            }
        }

    }

    public void setCategorySelectionRow(Cat category) {
        if (category != null) {
            categorySelection.setVisibility(View.VISIBLE);
            ((TextView) categorySelection.findViewById(R.id.cat_name)).setText(category.title);
            categorySelection.findViewById(R.id.check_row_categories).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO clean the layout
                    categorySelection.setVisibility(View.GONE);
                    Log.i(TAG, "setSelectedCategoryId 1");
                    TapstorData.getInstance().setSelectedCategoryId(-99, 1);
                    actualHeight = (int) px;

                    onRemoveClicked(false);

                }
            });

        }
    }

    public void closeSearch() {
        Helper.closeCategoryMenu(ll1, categorySelection, 1);

    }

    public void menuItemClicked() {
        if (!TapstorData.getInstance().isMenuStatusOpen()) {

            rootView.findViewById(R.id.frame).setVisibility(View.VISIBLE);

            TapstorData.getInstance().setMenuStatusOpen(true);

            ani a = new ani(0, actualHeight);
            a.setDuration(500);
            ll1.startAnimation(a);

        } else {

            Helper.closeCategoryMenu(ll1, categorySelection, 1);

        }

        Cat cat = Helper.getSelectedCategory(1);

        try {
            Helper.sendGoogleAnalyticsAction("Category Click", cat.title, null, getContext());
        } catch (Exception e) {
            Log.e(TAG, e);
        }

        if (cat != null) {
            setCategorySelectionRow(cat);
        }
    }

    /**
     * @param fullScreen
     */

    public void sliderDraweLayoutCallback(boolean fullScreen) {

        if (fullScreen) {

            int endHeight = (int) (getActivity().findViewById(R.id.tabBar).getY() - ll1.getY());

            actualHeight = endHeight;
            ll1.getLayoutParams().height = endHeight;
            ll1.requestLayout();

        } else {
            actualHeight = (int) px;
            ll1.getLayoutParams().height = actualHeight;
            ll1.requestLayout();
        }
    }

    public void setTheCategoryWithTheListenerCallback() {
        Log.i(TAG, "popular 3");
        Cat cat = Helper.getSelectedCategory(1);
        if (cat != null)
            setCategorySelectionRow(cat);

    }

    public boolean onTouch(View view, MotionEvent event) {

        final int Y = (int) event.getRawY();
        View Rootview = rootView.findViewById(R.id.menu_container);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:

                FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) rootView
                        .findViewById(R.id.menu_container).getLayoutParams();

                // Touch Y event
                int actionDown = Y;

                // Save current time of touch event to calculate speed of gesture
                double timerDown = System.currentTimeMillis();

                _yDelta = Y - lParams.topMargin;

                top = lParams.height;

                break;

            case MotionEvent.ACTION_UP:

                FrameLayout.LayoutParams layoutParamsUp = (FrameLayout.LayoutParams) Rootview
                        .getLayoutParams();
                if (layoutParamsUp.topMargin < 0) {

                    SlideToAbove(Rootview);

                }

                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i(TAG, (Y - _yDelta) + "");
                if (Y - _yDelta < 0 && Y - _yDelta > -top) {
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) Rootview
                            .getLayoutParams();
                    // layoutParams.leftMargin = X - _xDelta;
                    layoutParams.topMargin = Y - _yDelta;

                    Rootview.setLayoutParams(layoutParams);
                } else {
                    if (Y - _yDelta < 0) {
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) Rootview
                                .getLayoutParams();
                        // layoutParams.leftMargin = X - _xDelta;
                        layoutParams.topMargin = -(int) top;

                        Rootview.setLayoutParams(layoutParams);
                    } else {
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) Rootview
                                .getLayoutParams();
                        // layoutParams.leftMargin = X - _xDelta;
                        layoutParams.topMargin = 0;

                        Rootview.setLayoutParams(layoutParams);
                    }
                }
                break;
        }
        _root.invalidate();
        return true;
    }

    public void SlideToAbove(final View v) {
        Animation slide = null;
        slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, -1.0f);

        slide.setDuration(400);
        slide.setFillAfter(true);
        slide.setFillEnabled(true);
        v.startAnimation(slide);

        slide.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                FrameLayout.LayoutParams layoutParamsUp = (FrameLayout.LayoutParams) v
                        .getLayoutParams();

                v.clearAnimation();

                closeSearch();

                layoutParamsUp.topMargin = 0;

                v.setLayoutParams(layoutParamsUp);

            }

        });

    }

    /**
     * Get near by results Based on user location
     *
     * @author Grassos Konstantinos <grassos.konstantinos@gmail.com>
     */
    private class FetchEnterprises extends AsyncTask<String, Void, String> {

        private Result_get_results result_get_results;

        private String token;
        private String type;
        private String tab;
        private String page;
        private String cat;
        private String sorting;
        private String sorting_val;
        private String keyword;
        private String lat;
        private String lng;
        private boolean search;

        /**
         * @param token       (do_login token)
         * @param type        1:companies 2:products 3:services
         * @param tab         1:popular 2:near_me 3:new
         * @param page        paging number
         * @param cat         category id
         * @param sorting     1:percentage 2:price_difference 3:price_range 4:near_me
         * @param sorting_val price range ex.90,00:100,00
         * @param keyword     text to search
         * @param lat         latitude
         * @param lng         longitude
         */
        public FetchEnterprises(String token, String type, String tab,
                                String page, String cat, String sorting, String sorting_val,
                                String keyword, String lat, String lng, boolean search) {

            this.token = token;
            this.type = type;
            this.tab = tab;
            this.page = page;
            this.cat = cat;
            this.sorting = sorting;
            this.sorting_val = sorting_val;
            this.keyword = keyword;
            this.lat = lat;
            this.lng = lng;
            this.search = search;

        }

        @Override
        protected void onPreExecute() {
            isExecuting = true;

            if (getPage() != 0) {
                gridView.setSelection(gridView.getCount() - 1);
            }

            try {
                blurView(true);
                rootView.findViewById(R.id.progressBar1).setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Log.i(TAG, e);
            }
        }

        @Override
        protected String doInBackground(final String... args) {

            try {

                Gson gson = new Gson();
                String searchKeyword = "";

                if (search) {
                    searchKeyword = keyword;
                }

                SendPostValueGetResults sendValue = new SendPostValueGetResults(
                        token, type, tab, page, cat, sorting, sorting_val,
                        searchKeyword, lat, lng);


                String reader = RestServices.getInstance().postOperation(
                        sendValue, RestServices.getInstance().GET_RESULTS);


                RestResponse response = gson.fromJson(reader,
                        RestResponse.class);
                result_get_results = response.result_get_results;

            } catch (Exception e) {
                Log.i(TAG, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final String result) {

            try {
                Log.i(TAG, "inside post execute");

                blurView(false);
                rootView.findViewById(R.id.progressBar1).setVisibility(View.GONE);

                if (!(!result_get_results.error.equals("") || result_get_results.results == null
                        || result_get_results.results.size() == 0)) {
                    rootView.findViewById(R.id.empty).setVisibility(View.GONE);
                    if (getPage() == 0) {

                        gridView = (GridView) rootView.findViewById(R.id.gridView1);

                        gridView.setOnScrollListener(new EndlessScrollListener(0));

                        boolean nearMeTabActive = false;

                        if (TapstorData.getInstance().getTab() == 2) {
                            nearMeTabActive = true;
                        }

                        Log.i(TAG, "first size: " + result_get_results.results.size());

                        adapter = new EnterprisesAdapter(getContext(),
                                result_get_results.results, nearMeTabActive,
                                R.layout.row_gridview_enterprises);

                        gridView.setAdapter(adapter);
                        gridView.setOnItemClickListener(new OnItemClickListener() {
                            public void onItemClick(AdapterView<?> parent,
                                                    View v, int position, long id) {

                                Results item = (Results) parent
                                        .getItemAtPosition(position);
                                item.news = 0;

                                TapstorData.getInstance()
                                        .setSelectedEnterprise(item);

                                // send tracker.
                                Helper.sendGoogleAnalyticsAction(
                                        "Company click",
                                        TapstorData.getInstance()
                                                .getSelectedEnterprise().company,
                                        TapstorData.getInstance()
                                                .getSelectedEnterprise().id,
                                        getContext());

                                startActivity(new Intent(getContext(),
                                        DetailedListingActivity.class));

                            }

                        });
                    } else {

                        if (result_get_results.results.size() != 0) {
                            Log.i(TAG, "add new data with size: "
                                    + result_get_results.results.size());
                            addNewData(result_get_results.results);
                        } else {

                            if (getPage() != 0) {

                                Log.i(TAG, "stopRefreshing");
                                setStopRefreshing(true);
                                removeOnePage();

                            }
                        }

                    }

                    if (!search) {
                        storePreviousData();
                    }

                } else {
                    if (search) {
                        Log.i(TAG, "stopRefreshing");
                        setStopRefreshing(true);
                        if (getPage() != 0) {
                            removeOnePage();
                        }
                        Toast.makeText(getContext(), R.string.no_results, Toast.LENGTH_LONG).show();

                    } else {
                        if (getPage() != 0) {

                            Log.i(TAG, "stopRefreshing");
                            setStopRefreshing(true);
                            removeOnePage();

                        }
                    }
                }

                addOnePage();

                loading = false;

            } catch (Exception e) {

                Log.i(TAG, "no favorites for account");

            }

            isExecuting = false;

        }

    }

    /**
     * Class to implement a scroll listener to load more results when reaching
     * near the end of grid view
     *
     * @author Grassos Konstantinos
     */
    private class EndlessScrollListener implements OnScrollListener {
        private int visibleThreshold = 0;

        public EndlessScrollListener(int visibleThreshold) {
            this.visibleThreshold = visibleThreshold;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (!getStopRefreshing()) {
                if (loading) {
                    if (totalItemCount > previousTotal) {
                        previousTotal = totalItemCount;
                    }
                } else {

                    if ((totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)
                            && (totalItemCount - visibleItemCount != 0)) {
                        // load the next page using a background task,
                        loading = true;
                        Log.i(TAG, "callAsyncTaskToLoadResults 1");
                        callAsyncTaskToLoadResults();

                    }

                }
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }
    }

    private class ani extends Animation {

        int startHeight, endHeight;

        public ani(int startHeight, int endHeight) {
            this.startHeight = startHeight;
            this.endHeight = endHeight;

        }

        @Override
        protected void applyTransformation(float interpolatedTime,
                                           Transformation t) {

            int newHeight;

            // Log.d(TAG, "height: " + actualHeight);
            newHeight = (int) (startHeight * interpolatedTime);

            ll1.getLayoutParams().height = newHeight;
            ll1.requestLayout();

        }

        @Override
        public void initialize(int width, int height, int parentWidth,
                               int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);

            startHeight = endHeight;

        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }

    }

}
