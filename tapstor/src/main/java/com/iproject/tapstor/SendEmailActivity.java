package com.iproject.tapstor;


import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iproject.tapstor.helper.TapstorActivity;
import com.iproject.tapstor.helper.TapstorData;
import com.iproject.tapstor.library.Log;
import com.iproject.tapstor.rest.RestResponse;
import com.iproject.tapstor.rest.RestServices;
import com.iproject.tapstor.rest.SendValueEmail;
import com.iproject.tapstor.rest.Send_email_result;

public class SendEmailActivity extends TapstorActivity {

    private static final String TAG = "SendEmailActivity";

    private TextView companyName, companyTel, companyAddress, companyEmail,
            representativeName, representativeTel, applicantEmail,
            multilineText;

    private boolean suggestion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_email);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        ActionBar actionBar = getSupportActionBar();

        ((TextView) findViewById(R.id.header_title)).setText(R.string.suggest_business);

        actionBar.setTitle("");
        actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.white));
        actionBar.setLogo(null);

        actionBar.setHomeButtonEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.arrow_left);
        actionBar.setDisplayHomeAsUpEnabled(true);


        companyName = (TextView) findViewById(R.id.editText1);

        companyTel = (TextView) findViewById(R.id.editText2);

        companyAddress = (TextView) findViewById(R.id.editText3);

        companyEmail = (TextView) findViewById(R.id.editText4);

        multilineText = (TextView) findViewById(R.id.editText8);

        suggestion = getIntent().getExtras().getBoolean("SUGGESTION", false);

        if (suggestion) {

            applicantEmail = (TextView) findViewById(R.id.editText5);

            applicantEmail.setVisibility(View.VISIBLE);
        } else {

            representativeName = (TextView) findViewById(R.id.editText6);
            representativeTel = (TextView) findViewById(R.id.editText7);

            representativeName.setVisibility(View.VISIBLE);
            representativeTel.setVisibility(View.VISIBLE);

        }

        Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (checkValidFields()) {
                    new SendEmailService().execute();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.fill_in_all_fields, Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    private boolean checkValidFields() {

        if (companyName.getText().toString().length() == 0) {
            return false;
        } else if (companyEmail.getText().toString().length() == 0) {
            return false;
        }

        if (suggestion) {

            if (applicantEmail.getText().toString().length() == 0) {
                return false;
            }

        } else {

            if (companyTel.getText().toString().length() == 0) {
                return false;
            } else if (companyAddress.getText().toString().length() == 0) {
                return false;
            } else if (representativeName.getText().toString().length() == 0) {
                return false;
            } else if (representativeTel.getText().toString().length() == 0) {
                return false;
            }

        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {

            // go back
            case android.R.id.home:
                onBackPressed();
                break;

        }

        return true;
    }

    private class SendEmailService extends AsyncTask<String, Void, String> {

        Send_email_result email_result;

        @Override
        protected String doInBackground(String... params) {
            try {

                Gson gson = new Gson();
                SendValueEmail sendValue = new SendValueEmail();

                String token = TapstorData.getInstance().getUserToken();
                String company = companyName.getText().toString();
                String tel = companyTel.getText().toString();
                String address = companyAddress.getText().toString();
                String email = companyEmail.getText().toString();
                String message = multilineText.getText().toString();

                if (suggestion) {

                    String type = "2";

                    String email2 = applicantEmail.getText().toString();

                    sendValue.SendValueEmailSuggestion(token, type, company,
                            tel, address, email, email2, message);
                } else {

                    String type = "1";

                    String name = representativeName.getText().toString();
                    String tel2 = representativeTel.getText().toString();

                    sendValue.SendValueEmailAdding(token, type, company, tel,
                            address, email, name, tel2, message);
                }

                String reader = RestServices.getInstance().postOperation(
                        sendValue, RestServices.getInstance().SEND_EMAIL);

                RestResponse response = gson.fromJson(reader,
                        RestResponse.class);
                email_result = response.send_email_result;

            } catch (Exception e) {
                Log.e(TAG, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (!(email_result == null || email_result.error == null)) {
                if (email_result.error.equals("")) {

                    Log.e(TAG, "Εστάλη με επιτυχία");
                    onBackPressed();

                } else {
                    Log.e(TAG, "Προέκυψε σφάλμα");
                }
            }
        }

    }

}
