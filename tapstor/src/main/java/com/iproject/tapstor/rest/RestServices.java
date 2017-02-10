package com.iproject.tapstor.rest;

import com.google.gson.Gson;
import com.iproject.tapstor.library.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RestServices {

    private static RestServices mRestServices;

    private static OkHttpClient mOkHttpClient;
    // GET
    public final String GET_USER_NOTIFICATIONS = "http://www.tapstorbusiness.com/rest/user_messages/";// :token;
    // GET ::
    public final String DELETE_NOTIFICATION = "http://www.tapstorbusiness.com/rest/delete_message/";// :id/:token;
    // GET
    public final String DO_LOGIN123 = "http://www.tapstorbusiness.com/rest/do_login/";// :plaform/:device_id

    // TAPSTOR SERVICES URLs
    // platform (0:iOS,1:android)
    // Categories
    public final String GET_CATS = "http://www.tapstorbusiness.com/rest/v2/get_categories";

    // Normal Response
    //
    // result_user_messages
    //
    // id,
    // company_id,
    // product_id
    // message

    // Failure Responses
    // E12 if no messages in database
    // Get Company details or product details
    public final String GET_COMPANY_DETAILS = "http://www.tapstorbusiness.com/rest/v2/get_element/";// :type/:id/:token/:language

    // Normal Response
    //
    // result_delete_message
    //
    // Failure Responses
    // E12 if no messages in database
    // E47 if message already deleted
    //
    // GET PRODUCTS
    public final String GET_COMPANY_PRODUCTS = "http://www.tapstorbusiness.com/rest/v2/get_company_products/";
    // Favorite
    public final String DO_FAVORITE = "http://www.tapstorbusiness.com/rest/do_favorite/";// :token/:type/:id
    // http://www.tapstorbusiness.com/rest/do_favorite/21cde613f445fe4ab8ed05427ebbfb2d8006ea70/1/450
    // POST
    public final String GET_RESULTS = "http://www.tapstorbusiness.com/rest/v2/get_results";
    // UPDATE PROFILE
    public final String UPDATE_PROFILE = "http://www.tapstorbusiness.com/rest/update_profile";
    // {"token":"38025071b295df68d4fdb705625223724a0fb2b4","l_name":"eponimo","f_name":"onoma"}
    // POST A COMMENT
    public final String DO_RATING = "http://www.tapstorbusiness.com/rest/do_rating";
    // {"type":"1","id":"453","rating":"4",
    // "comment":"to sxolio mou einai auto vazo 4 asteria gia afto ton logo",
    // "token":"76e4320862d0b301c806eaa5cdf651c3f566b3f9"}
    // GET AR NEAR STORES
    public final String AR_STORES = "http://www.tapstorbusiness.com/rest/a_reality";// {"lat":"22.222222","lng":"33.333333"}
    // Add Suggest company
    public final String SEND_EMAIL = "http://www.tapstorbusiness.com/rest/send_email";
    public final String NEW_LOGIN_ANDROID = "http://www.tapstorbusiness.com/rest/do_login_android";
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private String TAG = "RestServices";

    public RestServices() {

    }

    public static RestServices getInstance() {
        if (mRestServices == null) {
            mRestServices = new RestServices();
            mOkHttpClient = new OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build();
        }
        return mRestServices;
    }

    // Method: POST
    // Data :
    // {"unique":"unique device id","udid":"device id for push notifications"}

    // Θέλω να προσθέσω την επιχείρηση μου
    // Type=1
    // body : [token,type,company,tel,address,email,name,tel2,message]
    //
    // Θέλω να προτείνω μια επιχείρηση
    // Type=2
    // body=[token,type,company,tel,address,email,email2,message]

    /**
     * @param url  The URL
     * @param data the object entity to send
     * @return JSON String response
     */
    public String postOperation(Object data, String url) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        Log.i(TAG, url);
        Log.i(TAG, json);
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder().url(url).post(body).header("Content-Encoding", "gzip").build();
        Response response;
        try {
            Log.i(TAG, url);
            response = mOkHttpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            Log.e(TAG, e);
        }
        return "";
    }

    /**
     * @param url The URL
     * @return JSON String response
     */
    public String getOperation(String url) {
        Request request = new Request.Builder().url(url).build();
        Response response;
        try {
            Log.i(TAG, url);
            response = mOkHttpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            Log.e(TAG, e);
            return "";
        } catch (Exception e) {
            Log.e(TAG, e);
            return "";
        }
    }

}
