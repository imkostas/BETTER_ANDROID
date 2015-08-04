package com.astapley.thememe.better;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotificationsActivity extends Activity implements View.OnClickListener {
    private NotificationsListAdapter notificationsListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);

        //top bar initialization
        final TextView topBarTitle = (TextView)findViewById(R.id.topBarTitle);
        topBarTitle.setTypeface(User.RalewaySemiBold);
        topBarTitle.setText(getString(R.string.notifications_title));
        topBarTitle.setOnClickListener(this);

        notificationsListViewAdapter = new NotificationsListAdapter(this, new ArrayList<NotificationItem>());
        ListView notificationsListView = (ListView) findViewById(R.id.listView);
        notificationsListView.setAdapter(notificationsListViewAdapter);
        notificationsListView.setDivider(null);
        initializeNotificationSettings();
    }

    @Override
    public void onBackPressed() {
        this.finish();
        overridePendingTransition(R.anim.slide_in_from_left_half, R.anim.slide_out_right);
    }

    private void initializeNotificationSettings(){
        notificationsListViewAdapter.clear();
        notificationsListViewAdapter.add(new NotificationItem("Someone votes on my post", (User.notificationSettings.votedPost != 0)));
        notificationsListViewAdapter.add(new NotificationItem("Someone favorites  my post", (User.notificationSettings.favoritedPost != 0)));
        notificationsListViewAdapter.add(new NotificationItem("I gain a new follower", (User.notificationSettings.newFollower != 0)));
        notificationsListViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.topBarTitle:
                onBackPressed();
                break;
        }
    }

    private static class NotificationItem {
        private String title;
        private Boolean isActive;

        public NotificationItem(String title, Boolean isActive){
            this.title = title;
            this.isActive = isActive;
        }
    }

    public class NotificationsListAdapter extends ArrayAdapter<NotificationItem> {
        public NotificationsListAdapter(Context context, ArrayList<NotificationItem> itemList){ super(context, 0, itemList); }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final NotificationItemHolder holder;

            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_notifications, parent, false);

                holder = new NotificationItemHolder();
                holder.notificationTextView = (TextView)convertView.findViewById(R.id.notificationTextView);
                holder.actionImageView = (ImageView)convertView.findViewById(R.id.actionImageView);
                holder.actionImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NotificationItem notificationItem = getItem(position);
                        notificationItem.isActive = !notificationItem.isActive;
                        if(notificationItem.isActive)holder.actionImageView.setImageResource(R.drawable.ic_switch_on_38x32dp);
                        else holder.actionImageView.setImageResource(R.drawable.ic_switch_off_38x32dp);

                        updateNotificationItem(position, (notificationItem.isActive)?1:0);
                    }
                });

                convertView.setTag(holder);
            } else {
                holder = (NotificationItemHolder)convertView.getTag();
            }

            NotificationItem notificationItem = getItem(position);
            holder.notificationTextView.setText(notificationItem.title);
            holder.notificationTextView.setTypeface(User.RalewaySemiBold);
            if(notificationItem.isActive)holder.actionImageView.setImageResource(R.drawable.ic_switch_on_38x32dp);
            else holder.actionImageView.setImageResource(R.drawable.ic_switch_off_38x32dp);

            return convertView;
        }

        private class NotificationItemHolder {
            TextView notificationTextView;
            ImageView actionImageView;
        }
    }

    private void updateNotificationItem(final int position, final int value){
        Map<String, String> map = new HashMap<>();
        map.put("api_key", User.api_key);
        map.put("user_id", Integer.toString(User.userID));
        map.put("type", Integer.toString(position));

        Log.d(User.LOGTAG, "Position: " + position);

        JSONObject params = new JSONObject(map);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, User.api_uri + "notification", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(User.LOGTAG, response.toString());

                switch(position){
                    case 0:
                        User.notificationSettings.votedPost = value;
                        break;
                    case 1:
                        User.notificationSettings.favoritedPost = value;
                        break;
                    case 2:
                        User.notificationSettings.newFollower = value;
                        break;
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    JSONObject obj = new JSONObject(new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers)));
                    Log.d(User.LOGTAG, obj.toString());
                    if(obj.has("error")){
                        JSONArray errors = obj.getJSONArray("error");
                        alertDismiss(errors.get(0).toString(), getResources().getString(R.string.alert_got_it));
                    } else { throw new Exception(); }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq, "Notification - Update");
    }

    private void alertDismiss(String messageText, String dismissText){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View alertView = layoutInflater.inflate(R.layout.alert_one_action_no_title, null);

        TextView message = (TextView)alertView.findViewById(R.id.message);
        message.setText(messageText);
        message.setTypeface(User.RalewayMedium);

        Button dismissBtn = (Button)alertView.findViewById(R.id.dismissBtn);
        dismissBtn.setText(dismissText);
        dismissBtn.setTypeface(User.RalewayMedium);
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.setView(alertView);
        alertDialog.show();
    }
}
