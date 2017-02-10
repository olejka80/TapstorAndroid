package com.iproject.tapstor;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iproject.tapstor.adapters.NotificationsAdapter;
import com.iproject.tapstor.helper.Helper;
import com.iproject.tapstor.helper.TapstorActivity;
import com.iproject.tapstor.helper.TapstorData;
import com.iproject.tapstor.library.Log;
import com.iproject.tapstor.rest.Messages;
import com.iproject.tapstor.rest.RestResponse;
import com.iproject.tapstor.rest.RestServices;
import com.iproject.tapstor.rest.Result_delete_message;
import com.iproject.tapstor.rest.Result_user_messages;

import java.util.ArrayList;
import java.util.Collections;

public class NotificationsActivity extends TapstorActivity {
    private final static String TAG = "NotificationsActivity";
    private ActionBar actionBar;
    private Context context;
    private ListView listView;
    // private Menu menu;
    private SubMenu subMenu;
    private MenuItem itemMenu;
    // private ActionMode myMode;
    private ActionMode.Callback callback;
    private NotificationsAdapter adapter;

    @Override
    protected void onResume() {

        refreshMessagesPopUp();
        super.onResume();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        actionBar = getSupportActionBar();


        context = NotificationsActivity.this;

        actionBar.setIcon(android.R.color.transparent);
        actionBar.setTitle("");
        new FetchNotifications().execute();

        findViewById(R.id.tab1).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, MainViewPagerActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                i.putExtra("TAB", 1);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                // onBackPressed();
                // overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        });

        findViewById(R.id.tab2).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(context, MainViewPagerActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                i.putExtra("TAB", 2);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);


            }
        });

        findViewById(R.id.tab3).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, MainViewPagerActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                i.putExtra("TAB", 3);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        });

        findViewById(R.id.tab4).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });

        findViewById(R.id.tab5).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Helper.checkForSensors(context)) {
                    Helper.checkCameraAugmented(NotificationsActivity.this, false);
                } else {
                    Toast.makeText(context, R.string.not_supported, Toast.LENGTH_SHORT).show();
                }
            }
        });


        findViewById(R.id.editTextView).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    listView.setItemChecked(0, true);
                } catch (Exception e) {
                    Log.e(TAG, e);
                }
            }
        });

    }

    private void showMessageDialog(String message) {

        final Dialog dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.inflate_dialog_message);
        dialog.setCanceledOnTouchOutside(true);

        // Fill up info and avatar
        TextView messageTextView = (TextView) dialog.findViewById(R.id.textView1);

        messageTextView.setText(message);

        dialog.show();

    }

    public void refreshMessagesPopUp() {
        TextView redBall = (TextView) findViewById(R.id.notification_num);

        if (TapstorData.getInstance().getUnreadMessages() != 0) {

            if (TapstorData.getInstance().getUnreadMessages() > 99) {
                redBall.setText(String.format(getResources().getString(R.string.placeholder_plus), 99));
            } else {
                redBall.setText(String.format(getResources().getString(R.string.placeholder_string), TapstorData.getInstance().getUnreadMessages()));
            }
            redBall.setVisibility(View.VISIBLE);
        } else {
            redBall.setVisibility(View.GONE);
        }

    }

    /**
     * @author Grassos Konstantinos <grassos.konstantinos@gmail.com>
     */
    private class FetchNotifications extends AsyncTask<String, Void, String> {

        private Result_user_messages result_user_messages;

        @Override
        protected void onPreExecute() {

            try {
                findViewById(R.id.progressBar1).setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }

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

                findViewById(R.id.progressBar1).setVisibility(View.GONE);

                if (result_user_messages.error.equals("")) {

                    // load notifications list
                    TapstorData.getInstance().notificationIdsofReadList = Helper.readNotifications(context);

                    if (TapstorData.getInstance().notificationIdsofReadList == null) {
                        TapstorData.getInstance().notificationIdsofReadList = new ArrayList<>();
                    }

                    listView = (ListView) findViewById(R.id.listView1);

                    Collections.reverse(result_user_messages.messages);

                    adapter = new NotificationsAdapter(context,
                            result_user_messages.messages);

                    if (result_user_messages.messages.size() == 0) {
                        findViewById(R.id.not_found).setVisibility(View.VISIBLE);

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

                    listView.setAdapter(adapter);

                    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

                    callback = new MultiChoiceModeListener() {

                        @Override
                        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                            // Capture total checked items
                            final int checkedCount = listView.getCheckedItemCount();
                            // Set the CAB title according to total checked items
                            mode.setTitle(String.format(getResources().getString(R.string.placeholder_selected), checkedCount));

                            // Calls toggleSelection method from ListViewAdapter Class

                            adapter.toggleSelection(position);
                        }

                        @Override
                        public boolean onActionItemClicked(ActionMode mode,
                                                           MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.delete:
                                    // Calls getSelectedIds method from
                                    // ListViewAdapter Class
                                    SparseBooleanArray selected = adapter
                                            .getSelectedIds();
                                    // Captures all selected ids with a loop
                                    for (int i = (selected.size() - 1); i >= 0; i--) {
                                        if (selected.valueAt(i)) {
                                            Messages selecteditem = adapter
                                                    .getItem(selected.keyAt(i));
                                            // Remove selected items following the
                                            // ids
                                            adapter.remove(selecteditem);

                                            new DeleteNotification(""
                                                    + selecteditem.id).execute();
                                        }
                                    }
                                    // Close CAB
                                    mode.finish();
                                    return true;
                                default:
                                    return false;
                            }
                        }

                        @Override
                        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                            mode.getMenuInflater().inflate(R.menu.notifications, menu);
                            // myMode = mode;
                            return true;
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode mode) {
                            adapter.removeSelection();
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                            return false;
                        }

                    };

                    listView.setMultiChoiceModeListener(((MultiChoiceModeListener) callback));

                    listView.setOnItemClickListener(new OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent,
                                                View view, int position, long id) {

                            if (!TapstorData.getInstance().notificationIdsofReadList.contains(((Messages) listView.getItemAtPosition(position)).id)) {
                                TapstorData.getInstance().notificationIdsofReadList.add(((Messages) listView.getItemAtPosition(position)).id);
                                TapstorData.getInstance().setUnreadMessages(TapstorData.getInstance().getUnreadMessages() - 1);
                                refreshMessagesPopUp();
                            }

                            Log.e(TAG, "READ");
                            Helper.storeNotifications(context, TapstorData.getInstance().notificationIdsofReadList);
                            adapter.notifyDataSetChanged();

                            Messages message = (Messages) listView.getItemAtPosition(position);


                            // send tracker.
                            Helper.sendGoogleAnalyticsAction("Notification Click", "" + message.id, null, context);


                            if (message.type == 1) {
                                // company

                                Intent intent = new Intent(new Intent(context, DetailedListingActivity.class));
                                intent.putExtra("fromNotifications", true);
                                intent.putExtra("openProduct", false);
                                intent.putExtra("CompanyId", message.company_id);

                                startActivity(intent);

                            } else if (message.type == 2) {
                                // product

                                Intent intent = new Intent(new Intent(context, DetailedListingActivity.class));
                                intent.putExtra("fromNotifications", true);
                                intent.putExtra("openProduct", true);
                                intent.putExtra("CompanyId", message.company_id);
                                intent.putExtra("ProductId", message.product_id);

                                startActivity(intent);

                            } else {
                                // message
                                showMessageDialog(message.message);
                                // TODO:
                                // open dialog

                            }
                        }

                    });

                } else if (result_user_messages.error.equals("E12")) {


                    findViewById(R.id.not_found).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.not_found).setVisibility(View.VISIBLE);
                }


            } catch (Exception e) {
                findViewById(R.id.not_found).setVisibility(View.VISIBLE);
            }

        }
    }

    /**
     * @author Grassos Konstantinos <grassos.konstantinos@gmail.com>
     */
    private class DeleteNotification extends AsyncTask<String, Void, String> {

        String id = "";
        private Result_delete_message result_delete_message;

        public DeleteNotification(String id) {
            this.id = id;
        }

        @Override
        protected void onPreExecute() {

            try {
                findViewById(R.id.progressBar1).setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }

        @Override
        protected String doInBackground(final String... args) {

            try {

                Gson gson = new Gson();

                String reader = RestServices.getInstance().getOperation(
                        RestServices.getInstance().DELETE_NOTIFICATION + id
                                + "/"
                                + TapstorData.getInstance().getUserToken());

                RestResponse response = gson.fromJson(reader,
                        RestResponse.class);

                result_delete_message = response.result_delete_message;

            } catch (Exception e) {
                Log.e(TAG, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final String result) {

            try {

                Log.e(TAG, "inside post execute");

                findViewById(R.id.progressBar1).setVisibility(View.GONE);

                if (result_delete_message.error.equals("")) {
                    // OK

                    boolean found = false;
                    for (int i = 0; i < TapstorData.getInstance().notificationIdsofReadList
                            .size(); i++) {
                        if (TapstorData.getInstance().notificationIdsofReadList
                                .get(i) == Integer.parseInt(id)) {
                            TapstorData.getInstance().notificationIdsofReadList
                                    .remove(i);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        TapstorData.getInstance()
                                .setUnreadMessages(
                                        TapstorData.getInstance()
                                                .getUnreadMessages() - 1);

                        refreshMessagesPopUp();
                    }

                    Helper.storeNotifications(context, TapstorData.getInstance().notificationIdsofReadList);

                } else if (result_delete_message.error.equals("E12")) {
                    // TODO: show no Ιnbox
                }
                if (result_delete_message.error.equals("E47")) {
                    // TODO: already deleted
                } else {
                    //
                }

            } catch (Exception e) {

            }

        }
    }
}
