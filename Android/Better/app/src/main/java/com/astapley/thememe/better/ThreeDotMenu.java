package com.astapley.thememe.better;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

public class ThreeDotMenu extends Fragment implements View.OnClickListener {
    int voteState;
    ThreeDotListAdapter threeDotListAdapter;
    ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.three_dot_menu, container, false);

        View spacer = rootView.findViewById(R.id.spacer);
        spacer.setOnClickListener(this);

        threeDotListAdapter = new ThreeDotListAdapter(getActivity().getBaseContext(), User.threeDotItems);
        listView = (ListView)rootView.findViewById(R.id.listView);
        listView.setAdapter(threeDotListAdapter);

        Bundle bundle = this.getArguments();
        if(bundle != null) voteState = bundle.getInt("voteState");

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.spacer:
                hideThreeDotMenu();
                break;
        }
    }

    public static class ThreeDotItem {
        private int id, type;
        private String title;
        private boolean isActive;

        public ThreeDotItem(int id, int type, String title, boolean isActive){
            this.id = id;
            this.type = type;
            this.title = title;
            this.isActive = isActive;
        }
    }

    public static ThreeDotItem buildThreeDotItem(int id, int type, String title, boolean active){
        return new ThreeDotItem(id, type, title, active);
    }

    private void hideThreeDotMenu(){
        Activity activity = getActivity();
        if(activity instanceof FeedBasic){
            FeedBasic feedBasic = (FeedBasic)activity;
            feedBasic.hideThreeDotMenu();
        } else if(activity instanceof FeedController) {
            FeedController feedController = (FeedController)activity;
            feedController.hideThreeDotMenu();
        }
    }

    private class ThreeDotListAdapter extends ArrayAdapter<ThreeDotItem> {
        public ThreeDotListAdapter(Context context, ArrayList<ThreeDotItem> itemList){ super(context, 0, itemList); }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ThreeDotItemHolder holder;
            final ThreeDotItem item = User.threeDotItems.get(position);

            if(convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.favorite_item, parent, false);

                holder = new ThreeDotItemHolder();
                holder.hashTextView = (TextView)convertView.findViewById(R.id.hashTextView);
                holder.hashTextView.setTypeface(User.RalewayMedium);

                holder.titleTextView = (TextView)convertView.findViewById(R.id.tagTextView);
                holder.titleTextView.setTypeface(User.RalewayMedium);

                holder.actionImageView = (ImageView)convertView.findViewById(R.id.actionImageView);

                convertView.setTag(holder);
            } else holder = (ThreeDotItemHolder)convertView.getTag();

            // Add hashtag if needed
            if(item.type != 3)holder.hashTextView.setText("");
            else holder.hashTextView.setText("#");

            // Set title
            holder.titleTextView.setText(item.title);

            // Initialize icon
            switch(item.type){
                case 0:
                    holder.actionImageView.setImageResource(R.drawable.ic_chevron_right_grey600_24dp);
                    break;
                case 1:
                case 3:
                    if(item.isActive)holder.actionImageView.setImageResource(R.drawable.ic_favorite_green500_24dp);
                    else holder.actionImageView.setImageResource(R.drawable.ic_favorite_outline_grey600_24dp);
                    break;
                case 2:
                    if(item.isActive) holder.actionImageView.setImageResource(R.drawable.ic_person_followed_green_24dp);
                    else holder.actionImageView.setImageResource(R.drawable.ic_person_add_grey_24dp);
                    break;
                case 4:
                    holder.actionImageView.setImageResource(R.drawable.ic_warning_grey_24dp);
                    break;
            }

            // OnClickListener for item action
            holder.actionImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch(item.type){
                        case 0:
                            Intent intent = new Intent(getActivity(), Voters.class);
                            Bundle bundle = new Bundle();
                            bundle.putInt("post_id", item.id);
                            bundle.putInt("vote_state", voteState);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_anim);
                            break;
                        case 1:
                            if(item.isActive) unfavoritePost(item);
                            else favoritePost(item);
                            item.isActive = !item.isActive;
                            break;
                        case 2:
                            if(item.isActive) unfollowUser(item);
                            else followUser(item);
                            item.isActive = !item.isActive;
                            break;
                        case 3:
                            if(item.isActive) unfavoriteHashtag(item);
                            else favoriteHashtag(item);
                            item.isActive = !item.isActive;
                            break;
                        case 4:
                            User.makeToast("Repost Misuse");
                            break;
                    }
                }
            });

            return convertView;
        }

        private class ThreeDotItemHolder {
            TextView hashTextView, titleTextView;
            ImageView actionImageView;
        }
    }

    private void favoritePost(ThreeDotItem item){
        Map<String, String> map = new HashMap<>();
        map.put("api_key", User.api_key);
        map.put("post_id", Integer.toString(item.id));
        map.put("user_id", Integer.toString(User.userID));

        JSONObject params = new JSONObject(map);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, User.api_uri + "favoritepost", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(User.LOGTAG, response.toString());
                threeDotListAdapter.notifyDataSetChanged();
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    JSONObject obj = new JSONObject(new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers)));
                    Log.d(User.LOGTAG, obj.toString());
                    if(obj.has("error")){
                        JSONArray errors = obj.getJSONArray("error");
                        Activity activity = getActivity();
                        if(activity instanceof FeedBasic){
                            FeedBasic feedBasic = (FeedBasic)activity;
                            feedBasic.alertDismiss(errors.get(0).toString(), getResources().getString(R.string.alert_got_it));
                        } else if(activity instanceof FeedController) {
                            FeedController feedController = (FeedController)activity;
                            feedController.alertDismiss(errors.get(0).toString(), getResources().getString(R.string.alert_got_it));
                        }
                    } else { throw new Exception(); }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq, "FavoritePost - Create");
    }

    private void unfavoritePost(ThreeDotItem item){
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.DELETE, User.api_uri + "favoritepost/" + item.id + "/" + User.userID, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(User.LOGTAG, response.toString());
                threeDotListAdapter.notifyDataSetChanged();
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    JSONObject obj = new JSONObject(new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers)));
                    Log.d(User.LOGTAG, obj.toString());
                    if(obj.has("error")){
                        JSONArray errors = obj.getJSONArray("error");
                        Activity activity = getActivity();
                        if(activity instanceof FeedBasic){
                            FeedBasic feedBasic = (FeedBasic)activity;
                            feedBasic.alertDismiss(errors.get(0).toString(), getResources().getString(R.string.alert_got_it));
                        } else if(activity instanceof FeedController) {
                            FeedController feedController = (FeedController)activity;
                            feedController.alertDismiss(errors.get(0).toString(), getResources().getString(R.string.alert_got_it));
                        }
                    } else { throw new Exception(); }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq, "FavoritePost - Delete");
    }

    private void followUser(ThreeDotItem item){
        Map<String, String> map = new HashMap<>();
        map.put("api_key", User.api_key);
        map.put("user_id", Integer.toString(item.id));
        map.put("follower_id", Integer.toString(User.userID));

        JSONObject params = new JSONObject(map);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, User.api_uri + "follow", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(User.LOGTAG, response.toString());
                threeDotListAdapter.notifyDataSetChanged();
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    JSONObject obj = new JSONObject(new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers)));
                    Log.d(User.LOGTAG, obj.toString());
                    if(obj.has("error")){
                        JSONArray errors = obj.getJSONArray("error");
                        Activity activity = getActivity();
                        if(activity instanceof FeedBasic){
                            FeedBasic feedBasic = (FeedBasic)activity;
                            feedBasic.alertDismiss(errors.get(0).toString(), getResources().getString(R.string.alert_got_it));
                        } else if(activity instanceof FeedController) {
                            FeedController feedController = (FeedController)activity;
                            feedController.alertDismiss(errors.get(0).toString(), getResources().getString(R.string.alert_got_it));
                        }
                    } else { throw new Exception(); }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq, "Follow - Create");
    }

    private void unfollowUser(ThreeDotItem item){
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.DELETE, User.api_uri + "follow/" + item.id + "/" + User.userID, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(User.LOGTAG, response.toString());
                threeDotListAdapter.notifyDataSetChanged();
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    JSONObject obj = new JSONObject(new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers)));
                    Log.d(User.LOGTAG, obj.toString());
                    if(obj.has("error")){
                        JSONArray errors = obj.getJSONArray("error");
                        Activity activity = getActivity();
                        if(activity instanceof FeedBasic){
                            FeedBasic feedBasic = (FeedBasic)activity;
                            feedBasic.alertDismiss(errors.get(0).toString(), getResources().getString(R.string.alert_got_it));
                        } else if(activity instanceof FeedController) {
                            FeedController feedController = (FeedController)activity;
                            feedController.alertDismiss(errors.get(0).toString(), getResources().getString(R.string.alert_got_it));
                        }
                    } else { throw new Exception(); }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq, "Follow - Delete");
    }

    private void favoriteHashtag(ThreeDotItem item){
        Map<String, String> map = new HashMap<>();
        map.put("api_key", User.api_key);
        map.put("hashtag_id", Integer.toString(item.id));
        map.put("user_id", Integer.toString(User.userID));

        JSONObject params = new JSONObject(map);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, User.api_uri + "favoritehashtag", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(User.LOGTAG, response.toString());
                threeDotListAdapter.notifyDataSetChanged();
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    JSONObject obj = new JSONObject(new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers)));
                    Log.d(User.LOGTAG, obj.toString());
                    if(obj.has("error")){
                        JSONArray errors = obj.getJSONArray("error");
                        Activity activity = getActivity();
                        if(activity instanceof FeedBasic){
                            FeedBasic feedBasic = (FeedBasic)activity;
                            feedBasic.alertDismiss(errors.get(0).toString(), getResources().getString(R.string.alert_got_it));
                        } else if(activity instanceof FeedController) {
                            FeedController feedController = (FeedController)activity;
                            feedController.alertDismiss(errors.get(0).toString(), getResources().getString(R.string.alert_got_it));
                        }
                    } else { throw new Exception(); }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq, "FavoriteHashtag - Create");
    }

    private void unfavoriteHashtag(ThreeDotItem item){
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.DELETE, User.api_uri + "favoritehashtag/" + item.id + "/" + User.userID, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(User.LOGTAG, response.toString());
                threeDotListAdapter.notifyDataSetChanged();
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    JSONObject obj = new JSONObject(new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers)));
                    Log.d(User.LOGTAG, obj.toString());
                    if(obj.has("error")){
                        JSONArray errors = obj.getJSONArray("error");
                        Activity activity = getActivity();
                        if(activity instanceof FeedBasic){
                            FeedBasic feedBasic = (FeedBasic)activity;
                            feedBasic.alertDismiss(errors.get(0).toString(), getResources().getString(R.string.alert_got_it));
                        } else if(activity instanceof FeedController) {
                            FeedController feedController = (FeedController)activity;
                            feedController.alertDismiss(errors.get(0).toString(), getResources().getString(R.string.alert_got_it));
                        }
                    } else { throw new Exception(); }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq, "FavoriteHashtag - Delete");
    }
}
