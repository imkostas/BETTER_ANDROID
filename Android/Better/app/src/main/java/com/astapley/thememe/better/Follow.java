package com.astapley.thememe.better;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Follow  extends Activity implements View.OnClickListener {
    private FollowersListAdapter followListViewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int followState, last = 0, limit = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);

        followState = getIntent().getExtras().getInt("follow_state");

        //top bar initialization
        final TextView topBarTitle = (TextView)findViewById(R.id.topBarTitle);
        topBarTitle.setTypeface(User.RalewaySemiBold);
        topBarTitle.setText((followState == 1) ? getString(R.string.following_title) : getString(R.string.followers_title));
        topBarTitle.setOnClickListener(this);

        followListViewAdapter = new FollowersListAdapter(this, new ArrayList<FollowItem>());
        ListView followListView = (ListView) findViewById(R.id.listView);
        followListView.setAdapter(followListViewAdapter);
        followListView.setDivider(null);
        followListView.setOnScrollListener(new InfiniteListViewScrollListener() {
            @Override
            public void loadMore(int page, int totalItemsCount) { getFollow(); }
        });
        getFollow();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                last = 0;
                followListViewAdapter.clear();
                getFollow();
            }
        });
    }

    @Override
    public void onBackPressed() {
        this.finish();
        overridePendingTransition(R.anim.no_anim, R.anim.slide_out_from_top);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.topBarTitle:
                onBackPressed();
                break;
        }
    }

    private static class FollowItem {
        private int userID;
        private String username;
        private Boolean following;

        public FollowItem(int userID, String username, Boolean following){
            this.userID = userID;
            this.username = username;
            this.following = following;
        }
    }

    public class FollowersListAdapter extends ArrayAdapter<FollowItem> {
        public FollowersListAdapter(Context context, ArrayList<FollowItem> itemList){ super(context, 0, itemList); }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final FollowHolder holder;
            final FollowItem item = getItem(position);

            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.follow_item, parent, false);

                holder = new FollowHolder();
                holder.profileImageView = (ImageView)convertView.findViewById(R.id.profileImageView);
                holder.usernameTextView = (TextView)convertView.findViewById(R.id.usernameTextView);
                holder.actionImageView = (ImageView)convertView.findViewById(R.id.actionImageView);
                holder.actionImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(item.following)unfollowUser(item);
                        else followUser(item);
                        item.following = !item.following;
                    }
                });

                convertView.setTag(holder);
            } else { holder = (FollowHolder)convertView.getTag(); }

            holder.usernameTextView.setText(item.username);
            if(item.following)holder.actionImageView.setImageResource(R.drawable.ic_person_followed_green_24dp);
            else holder.actionImageView.setImageResource(R.drawable.ic_person_add_grey_24dp);

            return convertView;
        }

        private class FollowHolder {
            ImageView profileImageView, actionImageView;
            TextView usernameTextView;
        }
    }

    private void getFollow(){
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, User.api_uri + "follow/" + followState + "/" + User.userID + "/" + last + "/" + limit, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(User.LOGTAG, response.toString());

                try {
                    JSONObject json = response.getJSONObject("response");
                    JSONArray follow = json.getJSONArray((followState == 1)?"following":"followers");

                    for(int i = 0; i < follow.length(); i++){
                        JSONObject obj = follow.getJSONObject(i);
                        followListViewAdapter.add(new FollowItem(obj.getInt("user_id"), obj.getString("username"), ((followState == 1) || (obj.getInt("following") == 1))));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                last++;
                followListViewAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
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
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq, "Follow - Following/Follower");
    }

    private void followUser(FollowItem item){
        Map<String, String> map = new HashMap<>();
        map.put("api_key", User.api_key);
        map.put("user_id", Integer.toString(item.userID));
        map.put("follower_id", Integer.toString(User.userID));

        JSONObject params = new JSONObject(map);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, User.api_uri + "follow", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(User.LOGTAG, response.toString());
                followListViewAdapter.notifyDataSetChanged();
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
        AppController.getInstance().addToRequestQueue(jsonObjReq, "Follow - Create");
    }

    private void unfollowUser(FollowItem item){
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.DELETE, User.api_uri + "follow/" + item.userID + "/" + User.userID, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(User.LOGTAG, response.toString());
                followListViewAdapter.notifyDataSetChanged();
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
        AppController.getInstance().addToRequestQueue(jsonObjReq, "Follow - Delete");
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
