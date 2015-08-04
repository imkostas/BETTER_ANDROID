package com.astapley.thememe.better;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class ProfileActivity extends Activity implements View.OnClickListener {
    private ArrayList<ProfileItem> profileItem;
    private ImageView profileImageView, backgroundProfileImageView;
    private ListView profileListView;
    private ProfileListAdapter profileListViewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        //top bar initialization
        ImageView topBarBackground = (ImageView)findViewById(R.id.topBarBackground);
        topBarBackground.setAlpha(0.0f);
        TextView topBarTitle = (TextView)findViewById(R.id.leftTextView);
        topBarTitle.setText(User.username);
        topBarTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_back_white_24dp, 0, 0, 0);
        topBarTitle.setOnClickListener(this);
        ImageView settingsImageView = (ImageView)findViewById(R.id.rightImageView);
        settingsImageView.setImageResource(R.drawable.ic_settings_white_24dp);
        settingsImageView.setOnClickListener(this);

        TextView rankTextView = (TextView)findViewById(R.id.rankTextView);
        rankTextView.setTypeface(User.RalewaySemiBold);
        switch(User.rank.rank){
            case 0:
                rankTextView.setText("");
                rankTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.clear_icon, 0);
                break;
            case 1:
                rankTextView.setText("Newbie");
                rankTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_rank_newbie_grey_24dp, 0);
                break;
            case 2:
                rankTextView.setText("Mainstream");
                rankTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_rank_mainstream_grey_24dp, 0);
                break;
            case 3:
                rankTextView.setText("Trailblazer");
                rankTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_rank_trailblazer_grey_24dp, 0);
                break;
            case 4:
                rankTextView.setText("Trendsetter");
                rankTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_rank_trendsetter_grey_24dp, 0);
                break;
            case 5:
                rankTextView.setText("Crowned");
                rankTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_rank_crowned_grey_24dp, 0);
                break;
        }

        TextView ageTextView = (TextView)findViewById(R.id.ageTextView);
        ageTextView.setText(User.getAge());
        ageTextView.setTypeface(User.RobotoMedium);

        TextView genderTextView = (TextView)findViewById(R.id.genderTextView);
        genderTextView.setText((User.gender.equals("male")?"M":"F"));
        genderTextView.setTypeface(User.RalewayMedium);

        TextView countryTextView = (TextView)findViewById(R.id.countryTextView);
        countryTextView.setText(User.getCountry());
        countryTextView.setTypeface(User.RalewayMedium);

        profileItem = new ArrayList<>();

        profileListViewAdapter = new ProfileListAdapter(this, new ArrayList<ProfileItem>());
        profileListView = (ListView) findViewById(R.id.profileListView);
        profileListView.setAdapter(profileListViewAdapter);
        profileListView.setDivider(null);
        profileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setItemNormal();
                setItemSelected(view);
                nextPage(position);
            }

            public void setItemSelected(View view) {
                TextView sectionTextView = (TextView) view.findViewById(R.id.sectionTextView);
                sectionTextView.setTextColor(getResources().getColor(R.color.color_better));
            }

        });
        initializeProfileItems();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCounts();
            }
        });

        profileImageView = (ImageView)findViewById(R.id.profileImageView);
        backgroundProfileImageView = (ImageView)findViewById(R.id.backgroundProfileImageView);

        if(User.profileImage != null && User.profileBackground != null){
            profileImageView.setImageBitmap(User.profileImage);
            backgroundProfileImageView.setImageBitmap(User.profileBackground);
        } else {
            Bitmap bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Drawable drawable = getResources().getDrawable(R.drawable.feed_button_profile_default_f_56dp);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            Drawable mask = getResources().getDrawable(R.drawable.ic_cam_circle_account_116dp);
            mask.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            mask.draw(canvas);

            profileImageView.setImageBitmap(bitmap);
            backgroundProfileImageView.setImageResource(R.drawable.account_profile_panel_empty);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setItemNormal();
        getCounts();
    }

    @Override
    public void onBackPressed() {
        this.finish();
        overridePendingTransition(R.anim.no_anim, R.anim.slide_out_from_top);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.leftTextView:
                onBackPressed();
                break;
            case R.id.rightImageView:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_anim);
                break;
        }
    }

    private void setItemNormal(){
        for (int i=0; i< profileListView.getChildCount(); i++){
            View v = profileListView.getChildAt(i);
            TextView sectionTextView = ((TextView)v.findViewById(R.id.sectionTextView));
            sectionTextView.setTextColor(Color.BLACK);
        }
    }

    private void initializeProfileItems(){
        profileItem.clear();
        profileListViewAdapter.clear();
        profileItem.add(new ProfileItem("check", "My Votes", (User.counts == null)?-1:User.counts.myVotes, true));
        profileItem.add(new ProfileItem("plus", "My Posts", (User.counts == null)?-1:User.counts.myPosts, true));
        profileItem.add(new ProfileItem("heart", "Favorite Posts", (User.counts == null)?-1:User.counts.favoritePosts, false));
        profileItem.add(new ProfileItem("", "Favorite Tags", (User.counts == null)?-1:User.counts.favoriteTags, true));
        profileItem.add(new ProfileItem("follow", "Following", (User.counts == null)?-1:User.counts.following, false));
        profileItem.add(new ProfileItem("", "Followers", (User.counts == null) ? -1 : User.counts.followers, false));
        profileListViewAdapter.addAll(profileItem);
        profileListViewAdapter.notifyDataSetChanged();
    }

    private void nextPage(int position){
        Intent intent;

        switch(position){
            case 0:
                intent = new Intent(this, FeedBasic.class);
                User.filter.title = "My Votes";
                User.filter.type = "my_votes";
                break;
            case 1:
                intent = new Intent(this, FeedBasic.class);
                User.filter.title = "My Posts";
                User.filter.type = "my_posts";
                break;
            case 2:
                intent = new Intent(this, FeedBasic.class);
                User.filter.title = "Favorite Posts";
                User.filter.type = "favorite_posts";
                break;
            case 3:
                intent = new Intent(this, FavoriteTagsActivity.class);
                break;
            case 4:
                Bundle bundleFollowing = new Bundle();
                bundleFollowing.putInt("follow_state", 1);
                intent = new Intent(this, Follow.class);
                intent.putExtras(bundleFollowing);
                break;
            case 5:
                Bundle bundleFollowers = new Bundle();
                bundleFollowers.putInt("follow_state", 0);
                intent = new Intent(this, Follow.class);
                intent.putExtras(bundleFollowers);
                break;
            default:
                intent = new Intent(this, FavoriteTagsActivity.class);
        }

        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_anim);
    }

    private static class ProfileItem {
        public String icon;
        public String title;
        public int number;
        public boolean isUnderline;

        public ProfileItem(String icon, String title, int number, boolean isUnderline){
            this.icon = icon;
            this.title = title;
            this.number = number;
            this.isUnderline = isUnderline;
        }
    }

    private class ProfileListAdapter extends ArrayAdapter<ProfileItem> {
        public ProfileListAdapter(Context context, ArrayList<ProfileItem> itemList){
            super(context, 0, itemList);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ProfileItemHolder profileItemHolder;

            // Initialize view or get from tag
            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.profile_activity_item, parent, false);

                profileItemHolder = new ProfileItemHolder();
                profileItemHolder.icon = (ImageView)convertView.findViewById(R.id.itemImageView);
                profileItemHolder.title = (TextView)convertView.findViewById(R.id.sectionTextView);
                profileItemHolder.number = (TextView)convertView.findViewById(R.id.countTextView);
                profileItemHolder.line = convertView.findViewById(R.id.dividerView);

                convertView.setTag(profileItemHolder);
            } else profileItemHolder = (ProfileItemHolder)convertView.getTag();

            // Spacing formatting
            ProfileItem profileItem = getItem(position);
            ProfileItem previousProfileItem = getItem(Math.max(0, position - 1));
            if(!profileItem.isUnderline){
                LinearLayout container = (LinearLayout)convertView.findViewById(R.id.container);
                View bottomSpace = convertView.findViewById(R.id.bottomSpace);
                container.removeView(bottomSpace);

                profileItemHolder.line.setVisibility(View.GONE);
            }

            if(!previousProfileItem.isUnderline){
                LinearLayout container = (LinearLayout)convertView.findViewById(R.id.container);
                View topSpace = convertView.findViewById(R.id.topSpace);
                container.removeView(topSpace);
            }

            // Initialize with proper icon
            switch(profileItem.icon){
                case "check":
                    profileItemHolder.icon.setImageResource(R.drawable.ic_myvote_check_grey_24dp);
                    break;
                case "heart":
                    profileItemHolder.icon.setImageResource(R.drawable.ic_favorite_outline_grey600_24dp);
                    break;
                case "plus":
                    profileItemHolder.icon.setImageResource(R.drawable.ic_portrait_grey600_24dp);
                    break;
                case "follow":
                    profileItemHolder.icon.setImageResource(R.drawable.ic_person_add_grey_24dp);
                    break;
                default:
                    profileItemHolder.icon.setImageResource(R.drawable.clear_icon);
            }

            // Set text and typeface
            profileItemHolder.title.setText(profileItem.title);
            profileItemHolder.title.setTypeface(User.RalewaySemiBold);

            if(profileItem.number == -1){
                profileItemHolder.number.setText("");
            } else {
                profileItemHolder.number.setText(Integer.toString(profileItem.number));
            }
            profileItemHolder.number.setTypeface(User.RobotoMedium);

            return convertView;
        }

        private class ProfileItemHolder {
            ImageView icon;
            TextView title;
            TextView number;
            View line;
        }
    }

    private void getCounts(){
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                User.api_uri + "user/count/" + User.userID, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(User.LOGTAG, response.toString());

                try {
                    JSONObject json = response.getJSONObject("response");

                    if(User.counts == null){
                        User.counts = new User.UserCounts(json.getInt("vote_count"), json.getInt("post_count"), json.getInt("favorite_post_count"), json.getInt("favorite_hashtag_count"), json.getInt("following_count"), json.getInt("follower_count"));
                    } else {
                        User.updateCounts(json.getInt("vote_count"), json.getInt("post_count"), json.getInt("favorite_post_count"), json.getInt("favorite_hashtag_count"), json.getInt("following_count"), json.getInt("follower_count"));
                    }

                    for(ProfileItem item : profileItem){
                        switch(item.title){
                            case "My Votes":
                                item.number = User.counts.myVotes;
                                break;
                            case "My Posts":
                                item.number = User.counts.myPosts;
                                break;
                            case "Favorite Posts":
                                item.number = User.counts.favoritePosts;
                                break;
                            case "Favorite Tags":
                                item.number = User.counts.favoriteTags;
                                break;
                            case "Following":
                                item.number = User.counts.following;
                                break;
                            case "Followers":
                                item.number = User.counts.followers;
                                break;
                        }
                    }
                    profileListViewAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        AppController.getInstance().addToRequestQueue(jsonObjReq, "User - Count");
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