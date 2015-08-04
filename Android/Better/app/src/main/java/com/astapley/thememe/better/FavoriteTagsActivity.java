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

public class FavoriteTagsActivity extends Activity implements View.OnClickListener {
    private int last = 0, limit = 15;
    private FavoriteTagsListAdapter favoriteTagsListViewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);

        //top bar initialization
        final TextView topBarTitle = (TextView)findViewById(R.id.topBarTitle);
        topBarTitle.setTypeface(User.RalewaySemiBold);
        topBarTitle.setText(getString(R.string.favorites_title));
        topBarTitle.setOnClickListener(this);

        favoriteTagsListViewAdapter = new FavoriteTagsListAdapter(this, new ArrayList<FavoriteHashtag>());
        ListView favoriteTagsListView = (ListView) findViewById(R.id.listView);
        favoriteTagsListView.setAdapter(favoriteTagsListViewAdapter);
        favoriteTagsListView.setDivider(null);
        favoriteTagsListView.setOnScrollListener(new InfiniteListViewScrollListener() {
            @Override
            public void loadMore(int page, int totalItemsCount) { getFavoriteHashtags(); }
        });
        getFavoriteHashtags();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                last = 0;
                favoriteTagsListViewAdapter.clear();
                getFavoriteHashtags();
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

    private class FavoriteHashtag {
        private int id;
        private String text;
        private Boolean isFavorite;

        public FavoriteHashtag(int id, String text, Boolean isFavorite){
            this.id = id;
            this.text = text;
            this.isFavorite = isFavorite;
        }
    }

    public class FavoriteTagsListAdapter extends ArrayAdapter<FavoriteHashtag> {

        public FavoriteTagsListAdapter(Context context, ArrayList<FavoriteHashtag> itemList){
            super(context, 0, itemList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final HashtagHolder holder;
            final FavoriteHashtag item = getItem(position);

            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.favorite_item, parent, false);

                holder = new HashtagHolder();
                holder.hash = (TextView)convertView.findViewById(R.id.hashTextView);
                holder.hashTag = (TextView)convertView.findViewById(R.id.tagTextView);
                holder.icon = (ImageView)convertView.findViewById(R.id.actionImageView);
                holder.icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(item.isFavorite){
                            unfavoriteHashtag(item);
                            holder.icon.setImageResource(R.drawable.ic_add_circle_outline_grey600_24dp);
                        } else {
                            favoriteHashtag(item);
                            holder.icon.setImageResource(R.drawable.ic_remove_circle_outline_green_24dp);
                        }

                        item.isFavorite = !item.isFavorite;
                    }
                });

                convertView.setTag(holder);
            } else { holder = (HashtagHolder)convertView.getTag(); }

            holder.hash.setTypeface(User.RalewayMedium);
            holder.hashTag.setText(item.text);
            holder.hashTag.setTypeface(User.RalewayMedium);

            return convertView;
        }

        private class HashtagHolder {
            TextView hash, hashTag;
            ImageView icon;
        }
    }

    private void getFavoriteHashtags(){
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                User.api_uri + "favoritehashtag/" + User.userID + "/" + last + "/" + limit, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(User.LOGTAG, response.toString());

                try {
                    JSONObject json = response.getJSONObject("response");
                    JSONArray favoriteHashtags = json.getJSONArray("favorite_hashtags");

                    for(int i = 0; i < favoriteHashtags.length(); i++){
                        JSONObject favoriteHashtag = favoriteHashtags.getJSONObject(i);
                        favoriteTagsListViewAdapter.add(new FavoriteHashtag(favoriteHashtag.getInt("hashtag_id"), favoriteHashtag.getString("name"), true));
                        last = favoriteHashtag.getInt("id");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                favoriteTagsListViewAdapter.notifyDataSetChanged();
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
        AppController.getInstance().addToRequestQueue(jsonObjReq, "FavoriteHashtag - index");
    }

    private void favoriteHashtag(FavoriteHashtag item){
        Map<String, String> map = new HashMap<>();
        map.put("api_key", User.api_key);
        map.put("hashtag_id", Integer.toString(item.id));
        map.put("user_id", Integer.toString(User.userID));

        JSONObject params = new JSONObject(map);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, User.api_uri + "favoritehashtag", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(User.LOGTAG, response.toString());
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
        AppController.getInstance().addToRequestQueue(jsonObjReq, "FavoriteHashtag - Create");
    }

    private void unfavoriteHashtag(FavoriteHashtag item){
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.DELETE,
                User.api_uri + "favoritehashtag/" + item.id + "/" + User.userID, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(User.LOGTAG, response.toString());
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
        AppController.getInstance().addToRequestQueue(jsonObjReq, "FavoriteHashtag - Delete");
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
