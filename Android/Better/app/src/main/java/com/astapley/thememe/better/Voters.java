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

public class Voters  extends Activity implements View.OnClickListener {
    private VoterListAdapter votersListViewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int postID, voteState, last = 0, limit = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);

        //top bar initialization
        final TextView topBarTitle = (TextView)findViewById(R.id.topBarTitle);
        topBarTitle.setTypeface(User.RalewaySemiBold);
        topBarTitle.setText(getString(R.string.voters_title));
        topBarTitle.setOnClickListener(this);

        votersListViewAdapter = new VoterListAdapter(this, new ArrayList<Voter>());
        final ListView votersListView = (ListView) findViewById(R.id.listView);
        votersListView.setAdapter(votersListViewAdapter);
        votersListView.setDivider(null);
        votersListView.setOnScrollListener(new InfiniteListViewScrollListener() {
            @Override
            public void loadMore(int page, int totalItemsCount) { getVoters(); }
        });
        postID = getIntent().getExtras().getInt("post_id");
        voteState = getIntent().getExtras().getInt("vote_state");
        getVoters();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                last = 0;
                votersListViewAdapter.clear();
                getVoters();
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

    private class Voter {
        private int userID;
        private String username;
        private Boolean votedSame, following;

        public Voter(int userID, String username, Boolean votedSame, Boolean following){
            this.userID = userID;
            this.username = username;
            this.votedSame = votedSame;
            this.following = following;
        }
    }

    public class VoterListAdapter extends ArrayAdapter<Voter> {
        public VoterListAdapter(Context context, ArrayList<Voter> itemList){ super(context, 0, itemList); }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final VoterHolder holder;
            final Voter voter = getItem(position);

            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_voter, parent, false);

                holder = new VoterHolder();
                holder.profileImageView = (ImageView)convertView.findViewById(R.id.profileImageView);
                holder.usernameTextView = (TextView)convertView.findViewById(R.id.usernameTextView);
                holder.voteImageView = (ImageView)convertView.findViewById(R.id.voteImageView);
                holder.actionImageView = (ImageView)convertView.findViewById(R.id.actionImageView);
                holder.actionImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(voter.following) unfollowUser(voter);
                        else followUser(voter);
                        voter.following = !voter.following;
                    }
                });

                convertView.setTag(holder);
            } else { holder = (VoterHolder)convertView.getTag(); }

            holder.usernameTextView.setText(voter.username);

            if(voter.votedSame) holder.voteImageView.setImageResource(R.drawable.ic_thumb_up_16dp);
            else holder.voteImageView.setImageResource(R.drawable.clear_icon);

            holder.actionImageView.setEnabled(true);
            if(voter.following) holder.actionImageView.setImageResource(R.drawable.ic_person_followed_green_24dp);
            else {
                if(voter.userID == User.userID){
                    holder.actionImageView.setImageResource(R.drawable.clear_icon);
                    holder.actionImageView.setEnabled(false);
                } else {
                    holder.actionImageView.setImageResource(R.drawable.ic_person_add_grey_24dp);
                }
            }

            return convertView;
        }

        private class VoterHolder {
            ImageView profileImageView, voteImageView, actionImageView;
            TextView usernameTextView;
        }
    }

    private void getVoters(){
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, User.api_uri + "post/voters/" + postID + "/" + User.userID + "/" + last + "/" + limit, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(User.LOGTAG, response.toString());

                try {
                    JSONObject json = response.getJSONObject("response");
                    JSONArray voters = json.getJSONArray("voters");

                    for(int i = 0; i < voters.length(); i++){
                        JSONObject voter = voters.getJSONObject(i);
                        votersListViewAdapter.add(new Voter(voter.getInt("user_id"), voter.getString("username"), (voter.getInt("vote") == voteState), (voter.getInt("following") == 1)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                last++;
                votersListViewAdapter.notifyDataSetChanged();
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
        AppController.getInstance().addToRequestQueue(jsonObjReq, "Post - Voters");
    }

    private void followUser(Voter voter){
        Map<String, String> map = new HashMap<>();
        map.put("api_key", User.api_key);
        map.put("user_id", Integer.toString(voter.userID));
        map.put("follower_id", Integer.toString(User.userID));

        JSONObject params = new JSONObject(map);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, User.api_uri + "follow", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(User.LOGTAG, response.toString());
                votersListViewAdapter.notifyDataSetChanged();
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

    private void unfollowUser(Voter voter){
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.DELETE, User.api_uri + "follow/" + voter.userID + "/" + User.userID, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(User.LOGTAG, response.toString());
                votersListViewAdapter.notifyDataSetChanged();
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

