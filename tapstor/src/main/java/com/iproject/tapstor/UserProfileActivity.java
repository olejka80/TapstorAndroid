package com.iproject.tapstor;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iproject.tapstor.helper.Helper;
import com.iproject.tapstor.helper.TapstorActivity;
import com.iproject.tapstor.helper.TapstorData;
import com.iproject.tapstor.library.CameraAccess;
import com.iproject.tapstor.library.Log;
import com.iproject.tapstor.library.SharedPreferencesStorage;
import com.iproject.tapstor.library.StorageAccess;
import com.iproject.tapstor.rest.RestResponse;
import com.iproject.tapstor.rest.RestServices;
import com.iproject.tapstor.rest.Result_update_profile;
import com.iproject.tapstor.rest.SendPostValueProfileUpdate;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserProfileActivity extends TapstorActivity {

    // CODE FOR CAMERA INTENT
    private static final int GALLERY_PICTURE = 0303;
    // CODE FOR CAMERA INTENT FOR AVATAR
    private static final int REQUEST_IMAGE_CAPTURE = 9999;
    private static String response = "";
    /* CHANGES */
    private static HttpURLConnection connection = null;
    private static DataOutputStream outputStream = null;
    private static String urlServer = "http://www.tapstorbusiness.com/upload_handler.php";
    private static String lineEnd = "\r\n";
    private static String twoHyphens = "--";
    private static String boundary = "*****";
    private static int bytesRead, bytesAvailable, bufferSize;
    // private static DataInputStream inputStream = null;
    private static byte[] buffer;
    private static int maxBufferSize = 1 * 1024 * 1024;
    private static int serverResponseCode;
    private static String serverResponseMessage;
    /******************************************/

    String USER_PREFS = "USER_PREFS";
    String USER_NAME = "USER_NAME";
    String USER_SURNAME = "USER_SURNAME";
    String AVATAR_PATH = "AVATAR_PATH";
    String filePath = "";
    Uri selectedImageUri;
    private Context context;
    private String TAG = "UserProfileActivity";
    private EditText name, surname;
    private ImageView avatarPictureHolder;
    private AlertDialog avatarAlertDialog;
    private AlertDialog.Builder avatarBuilder;
    private Bitmap imageBitmap;
    private Button buttonSelectLanuage;

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        context = UserProfileActivity.this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");


        ((TextView) findViewById(R.id.header_title)).setText(R.string.profile);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle("");
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.white));
        actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setHomeButtonEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.arrow_left);
        actionBar.setDisplayHomeAsUpEnabled(true);

        name = (EditText) findViewById(R.id.editText1);
        surname = (EditText) findViewById(R.id.editText2);

        avatarPictureHolder = (ImageView) findViewById(R.id.imageView1);

        getUserInfoAndSetTheFields();

        avatarPictureHolder.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                showDialogToPromtUserToSelectImage();

            }
        });

        Button save = (Button) findViewById(R.id.button1);
        assert save != null;
        save.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (fieldsAreFilled()) {
                    new UpdateProfile().execute();
                } else {
                    Toast.makeText(context, R.string.fill_in_all_fields, Toast.LENGTH_SHORT).show();
                }

            }
        });

        buttonSelectLanuage = (Button) findViewById(R.id.buttonSelectLanuage);
        buttonSelectLanuage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(UserProfileActivity.this)
                        .setTitle(R.string.select_language_title)
                        .setItems(R.array.select_language, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setLanguage(which);
                                dialog.dismiss();

                            }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();

            }
        });
    }

    private void setLanguage(int languagePosition) {
        final String language;
        if (languagePosition == 0) language = "el";
        else if (languagePosition == 2) language = "ru";
        else language = "en";
        new AlertDialog.Builder(UserProfileActivity.this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.restart_app_to_change_language)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferencesStorage.getSharedPreferences(UserProfileActivity.this).set(LANGUAGE_KEY, language);

                        Intent intent = new Intent(getApplicationContext(), SplashTapstorActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);


                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        StorageAccess.getInstance(this).onRequestPermissionsResult(requestCode, permissions, grantResults);
        CameraAccess.getInstance(this).onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected boolean fieldsAreFilled() {
        if (name.getText().toString().trim().length() == 0) {
            return false;
        } else if (surname.getText().toString().trim().length() == 0) {
            return false;
        }
        return true;
    }

    protected void showDialogToPromtUserToSelectImage() {

        avatarBuilder = new AlertDialog.Builder(context);

        avatarBuilder
                .setTitle(R.string.choose_avatar)
                .setItems(
                        new String[]{
                                context.getResources().getString(
                                        R.string.from_gallery),
                                context.getResources().getString(
                                        R.string.from_camera)},
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (which == 0) {

                                    if (StorageAccess.getInstance(UserProfileActivity.this).isPermissionGranted()) {
                                        Intent intent = new Intent();
                                        intent.setType("image/*");
                                        intent.setAction(Intent.ACTION_GET_CONTENT);
                                        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), GALLERY_PICTURE);
                                        dialog.dismiss();

                                    } else {
                                        StorageAccess.getInstance(UserProfileActivity.this).checkPermission();
                                    }

                                } else {


                                    if (CameraAccess.getInstance(UserProfileActivity.this).isPermissionGranted()) {


                                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                                        }
                                    } else {
                                        CameraAccess.getInstance(UserProfileActivity.this).checkPermission();
                                    }

                                }
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        });

        // create alert dialog
        avatarAlertDialog = avatarBuilder.create();

        // show it
        avatarAlertDialog.show();

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

    private void getUserInfoAndSetTheFields() {

        if (Helper.checkIfUserHasProfile(context)) {

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
                    if (!avatarPath.equals("")) {
                        Log.e(TAG, "avatar path in disk " + avatarPath);

                        imageBitmap = Bitmap.createScaledBitmap(
                                BitmapFactory.decodeFile(avatarPath), 150, 150,
                                true);

                        imageBitmap = Helper.rotateMediaFile(avatarPath, imageBitmap);

                        avatarPictureHolder
                                .setImageResource(R.drawable.avatar_circle);
                        avatarPictureHolder.setImageBitmap(Helper
                                .getCroppedBitmap(imageBitmap));
                    } else {
                        Log.e(TAG, "avatar is empty");
                    }
                } else {
                    Log.e(TAG, "avatar is null");
                }

            } catch (Exception e) {
                Log.e(TAG, e);
            }

        }
    }

    /**
     * Sends the file to the server to be saved accordingly
     *
     * @param pathToOurFile the path to the file
     * @param name          the name of file
     * @return a string containing the path
     */
    public String sendFileToServer(String pathToOurFile, String name) {

        try {

            Log.i(TAG, "Upload File: " + pathToOurFile);
            FileInputStream fileInputStream = null;

            fileInputStream = new FileInputStream(new File(pathToOurFile));

            String urlAddress = "";

            urlAddress = urlServer;

            URL url = new URL(urlAddress);
            connection = (HttpURLConnection) url.openConnection();
            // Allow Inputs & Outputs
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            // Enable POST method
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Connection", "close");

            // connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);

            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);

            String extention = "";

            int i = pathToOurFile.lastIndexOf('.');
            if (i > 0) {
                extention = pathToOurFile.substring(i + 1);
            }

            outputStream
                    .writeBytes("Content-Disposition: form-data; name=\"upfile\";filename=\""
                            + name + "." + extention + "\"" + lineEnd);
            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // Read file
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                Log.i(TAG, "Bsize:" + bufferSize);
            }

            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens
                    + lineEnd);

            try {
                // Responses from the server (code and message)
                serverResponseCode = connection.getResponseCode();
                Log.i(TAG, "Code:" + serverResponseCode);

            } catch (Exception e) {

                Log.e(TAG, e);

            }

            try {

                serverResponseMessage = connection.getResponseMessage();
                Log.i(TAG, " MessageChat:" + serverResponseMessage);

            } catch (Exception exe) {
                response = "ERROR";
                Log.e(TAG, exe);

            }

            fileInputStream.close();
            outputStream.flush();
            outputStream.close();

            response = "OK";
            return response;

            // filePath = "";
            // postMessage = "";

        } catch (Exception ex) {
            Log.e(TAG, "error while uploading file");
            ex.printStackTrace();
            response = "ERROR";
            // filePath = "";
            // postMessage = "";
            // Exception handling

        }

        return response;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = Images.Media.insertImage(inContext.getContentResolver(),
                inImage, "Title", null);
        return Uri.parse(path);
    }

    /**
     * on activity result for camera or gallery picture pick to set user profile
     * image and path to stored image
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                Bundle extras = data.getExtras();

                imageBitmap = Bitmap.createScaledBitmap((Bitmap) extras.get("data"), 150, 150, true);

                avatarPictureHolder.setImageResource(R.drawable.avatar_circle);

                avatarPictureHolder.setImageBitmap(Helper.getCroppedBitmap(imageBitmap));

                selectedImageUri = getImageUri(context, imageBitmap);
                // selectedImageUri = (Uri) data.getExtras().get("data");

                // IO FILE Manager
                filePath = getPath(context, selectedImageUri);


            } catch (Exception e) {
                Log.e(TAG, e);
            }


        } else if (requestCode == GALLERY_PICTURE && resultCode == RESULT_OK) {

            Uri selectedImageUri = data.getData();

            // IO FILE Manager
            String filemanagerstring = selectedImageUri.getPath();

            // MEDIA GALLERY
            String selectedImagePath = getPath(context, data.getData());

            // NOW WE HAVE OUR WANTED STRING
            if (selectedImagePath != null) {
                filePath = selectedImagePath;
            } else {
                filePath = filemanagerstring;
            }

            Log.d("SET AVATAR", filePath);

            imageBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(filePath), 300, 300, true);
            avatarPictureHolder.setImageResource(R.drawable.avatar_circle);
            avatarPictureHolder.setImageBitmap(Helper.getCroppedBitmap(imageBitmap));

        }
    }

    /**
     * Implements touch behavior and detect touches outside the keyboard area so
     * as to close the keyboard.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent touch) {

        View v1 = getCurrentFocus();
        boolean coor = super.dispatchTouchEvent(touch);

        try {

            if (v1 instanceof EditText) {
                View v2 = getCurrentFocus();
                int scrcoords[] = new int[2];
                v2.getLocationOnScreen(scrcoords);
                float x = touch.getRawX() + v2.getLeft() - scrcoords[0];
                float y = touch.getRawY() + v2.getTop() - scrcoords[1];

                if (touch.getAction() == MotionEvent.ACTION_UP
                        && (x < v2.getLeft() || x >= v2.getRight()
                        || y < v2.getTop() || y > v2.getBottom())) {

                    InputMethodManager i = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    i.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
                }
            }
            return coor;

        } catch (Exception e) {
            return coor;
        }
    }

    private class SendFile extends AsyncTask<String, Void, String> {
        ProgressDialog dialog = new ProgressDialog(context);
        private String path;
        private String imageName;

        public SendFile(String path, String name) {

            this.path = path;
            this.imageName = name;

        }

        // private ProgressDialog Dialog = new
        // ProgressDialog(ProfileActivity.this);

        /**
         * before the execution of the main task display a dialog
         */
        protected void onPreExecute() {

            try {

                Log.d(null, "file is being uploaded");
                dialog.setMessage(getResources().getString(R.string.please_wait));
                dialog.show();

            } catch (Exception e) {
                Log.e(TAG, e);
            }

        }

        /**
         * try to find GPS update for as long as it takes for the device
         */
        protected String doInBackground(final String... args) {

            try {

                sendFileToServer(path, imageName);

            } catch (Exception e) {
                Log.e(TAG, e);
            }

            return null;
        }

        /**
         * after the device receives GPS
         */
        protected void onPostExecute(final String result) {

            try {
                dialog.dismiss();

                Toast.makeText(context, R.string.profile_update, Toast.LENGTH_LONG).show();

                SharedPreferences sharedPrefs = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);

                sharedPrefs.edit()
                        .putString(USER_NAME, name.getText().toString())
                        .putString(USER_SURNAME, surname.getText().toString())
                        .putString(AVATAR_PATH, path).commit();
                Log.d(null, "file uploaded successfully");

            } catch (Exception e) {
                Log.e(TAG, e);
                // onBackPressed();
            }

        }
    }

    /**
     * Update profile class makes an Asynchronous call to web service
     *
     * @author Grassos Konstantinos <grassos.konstantinos@gmail.com>
     */
    private class UpdateProfile extends AsyncTask<String, Void, String> {

        ProgressDialog dialog = new ProgressDialog(context);
        private RestResponse rest;
        private Result_update_profile resultUpdateProfile;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            dialog.setMessage(getResources().getString(R.string.please_wait));
            dialog.show();
            super.onPreExecute();
        }

        protected String doInBackground(final String... args) {

            try {

                Gson gson = new Gson();
                String token = TapstorData.getInstance().getUserToken();
                String f_name = name.getText().toString();
                String l_name = surname.getText().toString();
                SendPostValueProfileUpdate prof = new SendPostValueProfileUpdate(
                        token, l_name, f_name);

                // Call to web service
                String reader = RestServices.getInstance().postOperation(prof,
                        RestServices.getInstance().UPDATE_PROFILE);

                rest = gson.fromJson(reader, RestResponse.class);

                resultUpdateProfile = rest.result_update_profile;
            } catch (Exception e) {
                Log.e(TAG, e);
            }

            return null;
        }

        protected void onPostExecute(final String result) {

            try {
                dialog.dismiss();

                if (resultUpdateProfile.error.equals("")) {

                    Log.e(TAG, "no error");
                    String postID = resultUpdateProfile.user;

                    if (!filePath.equals("")) {
                        Log.e(TAG, "file path not empty");

                        Log.e(TAG, "path: " + filePath);

                        new SendFile(filePath, postID).execute();

                    } else {
                        Log.e(TAG, "file path empty");
                        Toast.makeText(context, R.string.profile_update, Toast.LENGTH_LONG).show();

                        SharedPreferences sharedPrefs = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);

                        sharedPrefs
                                .edit()
                                .putString(USER_NAME, name.getText().toString())
                                .putString(USER_SURNAME, surname.getText().toString())
                                .putString(AVATAR_PATH, "").commit();

                    }

                } else {
                    Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                Log.e(TAG, e);
            }

        }
    }

}
