package com.iproject.tapstor.library;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.iproject.tapstor.R;

import java.io.File;

/**
 * @author Dimitris Touzloudis <dimtitris.touzloudis@gmail.com>
 *         A sigleton to cover the camera access with a simple api.
 */
public class CameraAccess {

    public static final int REQUEST_CAMERA_ACCESS_PERMISSION = 146;
    private static final String TAG = "CameraAccess";
    private static CameraAccess mData = null;
    private boolean mHasCheckedPermissions;


    private AppCompatActivity mAppCompatActivity;
    private boolean mShowCameraPermissionDirection = false;

    public CameraAccess(AppCompatActivity appCompatActivity) {
        this.mAppCompatActivity = appCompatActivity;
        this.mHasCheckedPermissions = Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1; //If lower than Android 22, then all permissions are granted. No need to check :D
    }


    public static synchronized CameraAccess getInstance(AppCompatActivity appCompatActivity) {
        if (mData == null) mData = new CameraAccess(appCompatActivity); // Create the instance
        return mData;
    }

    public File getFile(String path) {
        if (!mHasCheckedPermissions)
            throw new SecurityException("Use checkPermission method first.");
        return new File(path);
    }


    public boolean isPermissionGranted() {
        if (!mHasCheckedPermissions)
            return false;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) { //If higher than Android API 23.
            int hasLocationPermission = ContextCompat.checkSelfPermission(mAppCompatActivity, android.Manifest.permission.CAMERA);
            return hasLocationPermission == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public boolean hasCheckedPermissions() {
        return mHasCheckedPermissions;
    }

    public boolean isPermissionGranted(boolean noException) {
        if (!noException) return isPermissionGranted();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) { //If higher than Android API 23.
            int hasLocationPermission = ContextCompat.checkSelfPermission(mAppCompatActivity, android.Manifest.permission.CAMERA);
            return hasLocationPermission == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults, DialogInterface.OnClickListener onClickListener) {

        if (requestCode == REQUEST_CAMERA_ACCESS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                if (!mShowCameraPermissionDirection) {
                    mShowCameraPermissionDirection = true;
                    showMessage(R.string.access_camera_not_granted, onClickListener);
                }
            }

        }

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        this.onRequestPermissionsResult(requestCode, permissions, grantResults, null);
    }


    public void checkPermission() {
        mHasCheckedPermissions = true;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) return;
        if (this.isPermissionGranted()) return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(mAppCompatActivity, Manifest.permission.CAMERA)) {
            mShowCameraPermissionDirection = false;


            showMessageOKCancel(R.string.access_camera_description,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(mAppCompatActivity, new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA_ACCESS_PERMISSION);
                        }
                    });
            return;
        } else {
            if (mShowCameraPermissionDirection) {
                showMessage(R.string.access_camera_directions);
            }
        }
        ActivityCompat.requestPermissions(mAppCompatActivity, new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_ACCESS_PERMISSION);
    }


    private void showMessageOKCancel(int message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(mAppCompatActivity)
                .setTitle(R.string.permission_denied)
                .setMessage(message)
                .setPositiveButton(R.string.permission_retry, okListener)
                .setNegativeButton(R.string.permission_im_sure, null)
                .create()
                .show();
    }

    private void showMessage(int message) {
        new android.support.v7.app.AlertDialog.Builder(mAppCompatActivity)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .create()
                .show();
    }

    private void showMessage(int message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(mAppCompatActivity)
                .setMessage(message)
                .setPositiveButton(R.string.ok, okListener)
                .create()
                .show();
    }


}
