package com.iproject.tapstor;

import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.iproject.tapstor.adapters.CompanyProductsAdapter;
import com.iproject.tapstor.helper.Helper;
import com.iproject.tapstor.helper.TapstorActivity;
import com.iproject.tapstor.helper.TapstorData;
import com.iproject.tapstor.library.Log;
import com.iproject.tapstor.library.StorageAccess;
import com.iproject.tapstor.objects.Products;
import com.iproject.tapstor.rest.RestResponse;
import com.iproject.tapstor.rest.RestServices;
import com.iproject.tapstor.rest.Result_get_company_products;

import java.util.ArrayList;
import java.util.List;

public class ProductsForStoreActivity extends TapstorActivity {

    List<Products> listProducts = new ArrayList<>();
    private boolean isOffers;
    private ListView list;
    private CompanyProductsAdapter adapter;
    private String TAG = "ProductsForStoreActivity";

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_for_store);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");


        final ActionBar actionBar = getSupportActionBar();


        isOffers = getIntent().getExtras().getBoolean("offers");

        ((TextView) findViewById(R.id.header_title)).setText(TapstorData.getInstance().getSelectedElement().title);

        actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("");
        actionBar.setHomeAsUpIndicator(R.drawable.arrow_left);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.white));

        list = (ListView) findViewById(R.id.listView_products);
        adapter = new CompanyProductsAdapter(this, listProducts);

        list.setAdapter(adapter);

        new GetProductOffers(0).execute();

        StorageAccess.getInstance(this).checkPermission();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                onBackPressed();
                break;

        }

        return true;
    }

    public boolean productHasOffer(Products product) {
        return !(product.offer.offer_price == null || Double.parseDouble(product.offer.offer_price) == 0.0);
    }

    public void refreshView(int position) {
        new GetProductOffers(position).execute();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        StorageAccess.getInstance(this).onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * web service to get all products for Store
     *
     * @author Grassos Konstantinos <grassos.konstantinos@gmail.com>
     */
    public class GetProductOffers extends AsyncTask<String, Void, String> {

        int position = 0;
        private RestResponse rest;
        private Result_get_company_products response;

        public GetProductOffers(int position) {
            this.position = position;
        }

        @Override
        protected void onPreExecute() {
            findViewById(R.id.progressBar1).setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            try {

                Gson gson = new Gson();

                int id = TapstorData.getInstance().getSelectedElement().id;
                String token = TapstorData.getInstance().getUserToken();
                // Call to web service
                String reader = RestServices.getInstance().getOperation(
                        RestServices.getInstance().GET_COMPANY_PRODUCTS + token
                                + "/" + id + "/" + Helper.getLanguageToken(getApplicationContext()));

                rest = gson.fromJson(reader, RestResponse.class);

                response = rest.result_get_company_products;

            } catch (Exception e) {
                Log.e(TAG, e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(final String result) {

            try {

                if (response.error.equals("")) {

                    // IF FROM OFFERS REMOVE PRODUCTS WITH NO OFFERS

                    Log.e(TAG, "size is " + response.products.size());

                    if (isOffers) {

                        listProducts.clear();

                        Log.e(TAG, "has offers");

                        for (Products product : response.products) {
                            if (productHasOffer(product)) {
                                Log.e(TAG, "add in offers");
                                listProducts.add(product);
                            }
                        }

                        if (listProducts.size() > 0) {

                            adapter = new CompanyProductsAdapter(ProductsForStoreActivity.this, listProducts);

                            list.setAdapter(adapter);
                            list.setSelection(position);

                        } else {
                            Log.e(TAG, "no offers");
                            findViewById(R.id.textView1).setVisibility(View.VISIBLE);
                        }

                    } else {

                        Log.e(TAG, "no offers");

                        listProducts.clear();
                        for (Products prod : response.products) {
                            listProducts.add(prod);
                        }

                        adapter = new CompanyProductsAdapter(ProductsForStoreActivity.this, listProducts);

                        list.setAdapter(adapter);
                        list.setSelection(position);

                        if (getIntent().getExtras() != null) {
                            if (getIntent().getBooleanExtra("goToPosition",
                                    false)) {

                                String productId = getIntent().getExtras()
                                        .getString("position");
                                int i = 0;
                                for (Products product : response.products) {
                                    if (product.id.equals(productId)) {
                                        list.setSelection(i);
                                        getIntent().removeExtra("goToPosition");
                                        break;
                                    }
                                    i = i + 1;
                                }

                            }
                        }
                    }

                } else {
                    findViewById(R.id.textView1).setVisibility(View.VISIBLE);

                }

            } catch (Exception e) {
                Log.e(TAG, e);
            }

            findViewById(R.id.progressBar1).setVisibility(View.GONE);

        }
    }
}
