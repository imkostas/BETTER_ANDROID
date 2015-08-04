package com.astapley.thememe.better;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Filter extends Fragment implements View.OnClickListener {
    private Boolean searching = false;
    private static int width = 0, minWidth = 0, limit = 10;
    private Hashtags hashtags;
    private Users users;
    private InputMethodManager imm;
    private ScheduledFuture scheduledFuture;
    private ScheduledExecutorService scheduler;
    private Runnable runnable;

    private static View searchView;
    private TextView hashtagsTextView, usersTextView;
    private static EditText searchEditText;
    private ImageView indicatorImageView;
    public static LinearLayout searchLinearLayout, searchTopBarLinearLayout;
    private ViewPager viewPager;

    public Filter() {}  //

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.filter, container, false);

        imm = (InputMethodManager)getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        final ImageView searchImageView = (ImageView)rootView.findViewById(R.id.searchImageView);
        searchImageView.setOnClickListener(this);

        final TextView everythingTextView = (TextView)rootView.findViewById(R.id.everythingTextView);
        everythingTextView.setOnClickListener(this);

        final TextView favoriteHashtagsTextView = (TextView)rootView.findViewById(R.id.favoriteHashtagsTextView);
        favoriteHashtagsTextView.setOnClickListener(this);

        final TextView followingTextView = (TextView)rootView.findViewById(R.id.followingTextView);
        followingTextView.setOnClickListener(this);

        final TextView trendingTextView = (TextView)rootView.findViewById(R.id.trendingTextView);
        trendingTextView.setOnClickListener(this);

        hashtagsTextView = (TextView)rootView.findViewById(R.id.hashtagsTextView);
        hashtagsTextView.setTypeface(User.RalewaySemiBold);
        hashtagsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
viewPager.setCurrentItem(0);
            }
        });

        usersTextView = (TextView)rootView.findViewById(R.id.usersTextView);
        usersTextView.setTypeface(User.RalewaySemiBold);
        usersTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
            }
        });

        indicatorImageView = (ImageView)rootView.findViewById(R.id.indicatorImageView);

        searchLinearLayout = (LinearLayout)rootView.findViewById(R.id.searchLinearLayout);
        searchLinearLayout.setVisibility(View.GONE);
        searchLinearLayout.setAlpha(0.0f);

        PagerAdapter viewPagerAdapter = new PagerAdapter(getActivity().getSupportFragmentManager());
        viewPager = (ViewPager)rootView.findViewById(R.id.viewPager);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) { updateActiveTab(position); }
        });
        updateActiveTab(0);

        searchTopBarLinearLayout = (LinearLayout)rootView.findViewById(R.id.searchTopBarLinearLayout);
        searchTopBarLinearLayout.setVisibility(View.GONE);
        searchTopBarLinearLayout.setAlpha(0.0f);

        final ImageView backImageView = (ImageView)rootView.findViewById(R.id.backImageView);
        backImageView.setOnClickListener(this);

        searchEditText = (EditText)rootView.findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(scheduledFuture != null)scheduledFuture.cancel(true);
            }
            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                if(count == 0){
                    hashtags.clearHashtags();
                    users.clearUsers();
                } else {
                    scheduler = Executors.newScheduledThreadPool(1);
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.d(User.LOGTAG, "Runnable executed");
                            getHashtags(s.toString());
                            getUsers(s.toString());
                        }
                    };
                    scheduledFuture = scheduler.schedule(runnable, 500, TimeUnit.MILLISECONDS);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        width = User.windowSize.x - ImageUtils.dpToPx(120);
        minWidth = ImageUtils.dpToPx(36);
        searchView = rootView.findViewById(R.id.searchView);
        searchView.getLayoutParams().width = width;

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.backImageView:
            case R.id.searchImageView:
                toggleFilter();
                break;
            case R.id.everythingTextView:
                if(!User.filter.title.equals("Everything")){
                    User.filter.hasChanged = true;
                    User.filter.title = "Everything";
                    User.filter.type = "everything";
                    ((FeedController)getActivity()).feed.onResume();
                } else {
                    ((FeedController)getActivity()).closeRightDrawer();
                }
                break;
            case R.id.favoriteHashtagsTextView:
                if(!User.filter.title.equals("Favorite Tags")){
                    User.filter.hasChanged = true;
                    User.filter.title = "Favorite Tags";
                    User.filter.type = "favorite_tags";
                    ((FeedController)getActivity()).feed.onResume();
                } else {
                    ((FeedController)getActivity()).closeRightDrawer();
                }
                break;
            case R.id.followingTextView:
                if(!User.filter.title.equals("Following")){
                    User.filter.hasChanged = true;
                    User.filter.title = "Following";
                    User.filter.type = "following";
                    ((FeedController)getActivity()).feed.onResume();
                } else {
                    ((FeedController)getActivity()).closeRightDrawer();
                }
                break;
            case R.id.trendingTextView:
                if(!User.filter.title.equals("Trending")){
                    User.filter.hasChanged = true;
                    User.filter.title = "Trending";
                    User.filter.type = "trending";
                    ((FeedController)getActivity()).feed.onResume();
                } else {
                    ((FeedController)getActivity()).closeRightDrawer();
                }
                break;
        }
    }

    private class PagerAdapter extends FragmentPagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    hashtags = new Hashtags();
                    return hashtags;
                default:
                    users = new Users();
                    return users;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public static void updateSearchLinearLayout(final InputMethodManager imm, final Boolean searching){
        ValueAnimator valueAnimator = ValueAnimator.ofInt((searching)?width:minWidth, (searching)?minWidth:width);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                searchView.getLayoutParams().width = (int)animation.getAnimatedValue();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

                if(!searching) {
                    searchTopBarLinearLayout.setAlpha(0.0f);
                    searchTopBarLinearLayout.setVisibility(View.GONE);
                    imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                    searchEditText.clearFocus();
                    searchEditText.setText("");
                }
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if(searching){
                    searchTopBarLinearLayout.setVisibility(View.VISIBLE);
                    searchTopBarLinearLayout.animate().alpha(1.0f).setDuration(User.ANIM_SPEED).start();
                    searchEditText.requestFocus();
                    imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        ValueAnimator animator = ObjectAnimator.ofFloat(searchLinearLayout, "alpha", searching ? 0.0f : 1.0f, searching ? 1.0f : 0.0f);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

                if(searching) searchLinearLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if(!searching) searchLinearLayout.setVisibility(View.GONE);
            }
        });

        animator.setDuration(User.ANIM_SPEED);
        valueAnimator.setDuration(User.ANIM_SPEED);
        animator.start();
        valueAnimator.start();
    }

    private void updateActiveTab(int position){
        switch (position) {
            case 0:
                hashtagsTextView.setTextColor(getResources().getColor(R.color.color_white));
                usersTextView.setTextColor(getResources().getColor(R.color.color_light_gray_green));
                indicatorImageView.setPadding(0, 0, User.windowSize.x / 2, 0);
                break;
            case 1:
                hashtagsTextView.setTextColor(getResources().getColor(R.color.color_light_gray_green));
                usersTextView.setTextColor(getResources().getColor(R.color.color_white));
                indicatorImageView.setPadding(User.windowSize.x / 2, 0, 0, 0);
                break;
        }
    }

    private void toggleFilter(){
        searching = !searching;
        if(!searching) {
            ((FeedController)getActivity()).notifyRecyclerView();
            hashtags.clearHashtags();
            users.clearUsers();
        }
        updateSearchLinearLayout(imm, searching);
        ((FeedController)getActivity()).updateFilterDrawer(searching);
    }

    private void getHashtags(final String hashtag){
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                User.api_uri + "hashtag/" + hashtag + "/" + limit, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(User.LOGTAG, response.toString());

                try {
                    JSONObject json = response.getJSONObject("response");
                    JSONArray hashtagss = json.getJSONArray("hashtags");
                    ArrayList<Hashtag> h = new ArrayList<>();

                    for(int i = 0; i < hashtagss.length(); i++){
                        JSONObject hashtag = hashtagss.getJSONObject(i);
                        h.add(new Hashtag(hashtag.getInt("id"), hashtag.getString("name")));
                    }

                    hashtags.updateHashtags(h);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    JSONObject obj = new JSONObject(new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers)));
                    Log.d(User.LOGTAG, obj.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq, "Hashtag - index");
    }

    private void getUsers(final String username){
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                User.api_uri + "user/" + username + "/" + limit, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(User.LOGTAG, response.toString());

                try {
                    JSONObject json = response.getJSONObject("response");
                    JSONArray usernames = json.getJSONArray("usernames");
                    ArrayList<SimpleUser> userss = new ArrayList<>();

                    for(int i = 0; i < usernames.length(); i++){
                        JSONObject user = usernames.getJSONObject(i);
                        userss.add(new SimpleUser(user.getInt("id"), user.getString("username")));
                    }

                    users.updateUsers(userss);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    JSONObject obj = new JSONObject(new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers)));
                    Log.d(User.LOGTAG, obj.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq, "Hashtag - index");
    }

    public static class Hashtags extends Fragment {
        private HashtagsListAdapter hashtagsListAdapter;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.list_view_simple, container, false);

            hashtagsListAdapter = new HashtagsListAdapter(getActivity(), new ArrayList<Hashtag>());
            ListView listView = (ListView)rootView.findViewById(R.id.listView);
            listView.setAdapter(hashtagsListAdapter);
            listView.setDivider(null);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Hashtag hashtag = hashtagsListAdapter.getItem(position);
                    User.filter.hasChanged = true;
                    User.filter.id = hashtag.id;
                    User.filter.title = "#" + hashtag.name;
                    User.filter.type = "hashtag";
                    ((FeedController)getActivity()).filter.toggleFilter();
                    ((FeedController)getActivity()).feed.onResume();
                }
            });

            return rootView;
        }

        private void clearHashtags(){
            hashtagsListAdapter.clear();
            hashtagsListAdapter.notifyDataSetChanged();
        }

        private void updateHashtags(ArrayList<Hashtag> hashtags){
            hashtagsListAdapter.clear();
            hashtagsListAdapter.addAll(hashtags);
            hashtagsListAdapter.notifyDataSetChanged();
        }

        private class HashtagsListAdapter extends ArrayAdapter<Hashtag> {

            public HashtagsListAdapter(Context context, ArrayList<Hashtag> itemList){ super(context, 0, itemList); }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final HashtagHolder holder;
                final Hashtag item = getItem(position);

                if(convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.favorite_item, parent, false);

                    holder = new HashtagHolder();
                    holder.hash = (TextView)convertView.findViewById(R.id.hashTextView);
                    holder.hashTag = (TextView)convertView.findViewById(R.id.tagTextView);
                    holder.icon = (ImageView)convertView.findViewById(R.id.actionImageView);

                    convertView.setTag(holder);
                } else { holder = (HashtagHolder)convertView.getTag(); }

                holder.hash.setTypeface(User.RalewayMedium);
                holder.hashTag.setText(item.name);
                holder.hashTag.setTypeface(User.RalewayMedium);
                holder.icon.setImageResource(R.drawable.clear_icon);

                return convertView;
            }

            private class HashtagHolder {
                TextView hash, hashTag;
                ImageView icon;
            }
        }
    }

    public static class Users extends Fragment {
        private UsersListAdapter usersListAdapter;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.list_view_simple, container, false);

            usersListAdapter = new UsersListAdapter(getActivity(), new ArrayList<SimpleUser>());
            ListView listView = (ListView)rootView.findViewById(R.id.listView);
            listView.setAdapter(usersListAdapter);
            listView.setDivider(null);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    SimpleUser user = usersListAdapter.getItem(position);
                    User.filter.hasChanged = true;
                    User.filter.id = user.id;
                    User.filter.title = user.username;
                    User.filter.type = "user";
                    ((FeedController)getActivity()).filter.toggleFilter();
                    ((FeedController)getActivity()).feed.onResume();
                }
            });

            return rootView;
        }

        private void clearUsers(){
            usersListAdapter.clear();
            usersListAdapter.notifyDataSetChanged();
        }

        private void updateUsers(ArrayList<SimpleUser> users){
            usersListAdapter.clear();
            usersListAdapter.addAll(users);
            usersListAdapter.notifyDataSetChanged();
        }

        private class UsersListAdapter extends ArrayAdapter<SimpleUser> {

            public UsersListAdapter(Context context, ArrayList<SimpleUser> itemList){ super(context, 0, itemList); }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final UserHolder holder;
                final SimpleUser item = getItem(position);

                if(convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.follow_item, parent, false);

                    holder = new UserHolder();
                    holder.profileImageView = (ImageView)convertView.findViewById(R.id.profileImageView);
                    holder.usernameTextView = (TextView)convertView.findViewById(R.id.usernameTextView);
                    holder.actionImageView = (ImageView)convertView.findViewById(R.id.actionImageView);

                    convertView.setTag(holder);
                } else { holder = (UserHolder)convertView.getTag(); }

                holder.profileImageView.setImageResource(R.drawable.feed_button_profile_default_m_56dp);
                holder.usernameTextView.setTypeface(User.RalewayMedium);
                holder.usernameTextView.setText(item.username);
                holder.actionImageView.setImageResource(R.drawable.clear_icon);

                return convertView;
            }

            private class UserHolder {
                TextView usernameTextView;
                ImageView profileImageView, actionImageView;
            }
        }
    }

    private class Hashtag {
        private int id;
        private String name;

        public Hashtag(int id, String name){
            this.id = id;
            this.name = name;
        }
    }

    private class SimpleUser {
        private int id;
        private String username;

        public SimpleUser(int id, String username){
            this.id = id;
            this.username = username;
        }
    }
}
