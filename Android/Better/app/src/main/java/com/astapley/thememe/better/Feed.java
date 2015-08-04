package com.astapley.thememe.better;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Feed extends Fragment {
    private int last = 0, limit = 5, major = 0;
    private String filter = "everything";
    private List<Post> posts;
    private SwipeRefreshLayout swipeRefreshLayout;
    public RecyclerViewAdapter recyclerViewAdapter;

    public Feed(){}

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public void onResume(){
        super.onResume();
        if(User.filter.hasChanged){
            Activity activity = getActivity();
            if(activity instanceof FeedController)((FeedController)activity).updateActionBarTitle(User.filter.title);
            refreshFeed();
        } else User.filter.type = filter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.feed, container, false);

        posts = new ArrayList<>();
        final LinearLayoutManager llm = new LinearLayoutManager(getActivity().getBaseContext());
        recyclerViewAdapter = new RecyclerViewAdapter(posts);
        final RecyclerView recyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setOnScrollListener(new InfiniteRecyclerScrollListener(llm) {
            @Override
            public void loadMore(int current_page) {
                getPosts();
            }
        });
        refreshFeed();

        swipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerView.setOnScrollListener(new InfiniteRecyclerScrollListener(llm) {
                    @Override
                    public void loadMore(int current_page) {
                        getPosts();
                    }
                });
                refreshFeed();
            }
        });

        return rootView;
    }

    private void refreshFeed(){
        User.filter.hasChanged = false;
        filter = User.filter.type;
        last = 0;
        major = 0;
        posts.clear();
        recyclerViewAdapter.notifyDataSetChanged();
        getPosts();
    }

    private void getPosts(){
        Log.d(User.LOGTAG, "Filter: " + User.filter.title);
        String url = User.api_uri;
        switch(User.filter.type){
            case "my_votes":
                url += "post/voted/" + User.userID + "/" + last + "/" + limit;
                break;
            case "my_posts":
                url += "post/posted/" + User.userID + "/1/" + last + "/" + limit;
                break;
            case "favorite_posts":
                url += "favoritepost/" + User.userID + "/" + last + "/" + limit;
                break;
            case "favorite_tags":
                url += "post/favoritehashtag/" + User.userID + "/" + last + "/" + limit;
                break;
            case "following":
                url += "post/following/" + User.userID + "/" + last + "/" + limit;
                break;
            case "trending":
                url += "post/trending/" + User.userID + "/" + major + "/" + last + "/" + limit;
                break;
            case "hashtag":
                url += "post/hashtag/" + User.filter.id + "/" + User.userID + "/" + last + "/" + limit;
                break;
            case "user":
                url += "post/posted/" + User.filter.id + "/0/" + last + "/" + limit;
                break;
            case "everything":
            default:
                url += "post/" + User.userID + "/" + last + "/" + limit;
                break;
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(User.LOGTAG, response.toString());
                int origPostSize = posts.size();

                try {
                    JSONObject json = response.getJSONObject("response");
                    JSONArray feed = json.getJSONArray("feed");

                    for(int i = 0; i < feed.length(); i++){
                        JSONObject post = feed.getJSONObject(i);

                        int myVote = 0;
                        if(post.getInt("user_id") == User.userID){
                            myVote = 3;
                        } else if(post.get("vote") instanceof JSONObject){
                            JSONObject vote = post.getJSONObject("vote");
                            myVote = vote.getInt("vote");
                        }

                        String hashtagsString = "";
                        String[] hashtagArray = post.getString("hashtags").split(" ");
                        for(int j = 0; j < hashtagArray.length; j++){
                            hashtagsString += "#";
                            if(j == 0 || j == 1)hashtagsString += "<b><font color='#212121'>";
                            hashtagsString += hashtagArray[j];
                            if(j == 0 || j == 1)hashtagsString += "</font></b>";
                            hashtagsString += "&nbsp;&nbsp;&#8203;";
                        }

                        posts.add(new Post(post.getInt("id"), post.getInt("user_id"), post.getInt("votes"), post.getInt("layout"), myVote, post.getString("username"), hashtagsString, hashtagArray, new Hotspot(post.getInt("voted_zero"), new Point(ImageUtils.dpToPx(post.getInt("hotspot_one_x")), ImageUtils.dpToPx(post.getInt("hotspot_one_y")))), new Hotspot(post.getInt("votes") - post.getInt("voted_zero"), new Point(ImageUtils.dpToPx(post.getInt("hotspot_two_x")), ImageUtils.dpToPx(post.getInt("hotspot_two_y"))))));
                        last = (post.has("last"))?post.getInt("last"):post.getInt("id");
                        if(post.has("major"))major = post.getInt("major");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                recyclerViewAdapter.notifyItemRangeInserted(origPostSize, posts.size());
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
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq, "Post - " + User.filter.title);
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.PostHolder> {
        int hotspotOffset = ImageUtils.dpToPx(5);
        List<Post> posts;

        RecyclerViewAdapter(List<Post> posts){ this.posts = posts; }

        @Override
        public int getItemCount() { return posts.size(); }

        @Override
        public PostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new PostHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false));
        }

        @Override
        public void onBindViewHolder(final PostHolder holder, final int position) {
            final Post post = posts.get(position);

            ImageLoader imageLoader = AppController.getInstance().getImageLoader();
            AppController.getInstance().getImageLoader().get(User.s3_uri + "user/" + post.userID + "_small.png", ImageLoader.getImageListener(holder.profileImageView, User.gender.equals("female") ? R.drawable.feed_button_profile_default_f_56dp : R.drawable.feed_button_profile_default_m_56dp, User.gender.equals("female") ? R.drawable.feed_button_profile_default_f_56dp : R.drawable.feed_button_profile_default_m_56dp));

            // Initialize CardView background and position
            holder.customCardView.setCardBackgroundColor(getResources().getColor(R.color.color_clear));
            holder.customCardView.setPosition(position);

            // Initialize username
            holder.usernameTextView.setText(post.username.toUpperCase());
            holder.usernameTextView.setTypeface(User.RobotoMedium);

            // Initialize number of votes
            holder.votesTextView.setText(Integer.toString(post.votes));
            holder.votesTextView.setTypeface(User.RobotoMedium);

            // Initialize hashtags
            holder.tagsTextView.setText(Html.fromHtml(post.tagString));
            holder.tagsTextView.setTypeface(User.RalewaySemiBold);

            // Initialize hotspot typeface
            holder.hotspotOneTextView.setTypeface(User.RobotoBold);
            holder.hotspotOnePercentTextView.setTypeface(User.RobotoBold);
            holder.hotspotTwoTextView.setTypeface(User.RobotoBold);
            holder.hotspotTwoPercentTextView.setTypeface(User.RobotoBold);

            // Initialize post layout type and set respective sizing
            int cardViewSize = User.windowSize.x - ImageUtils.dpToPx(13);
            int imageSize = cardViewSize/2;
            int imageSizeAdjusted = cardViewSize/2 + ImageUtils.dpToPx(2);
            int imagePadding = cardViewSize/2 - ImageUtils.dpToPx(1);
            switch(post.layoutType){
                case 1:
                    holder.firstImageView.getLayoutParams().width = imageSize;
                    holder.firstImageView.getLayoutParams().height = cardViewSize;
                    holder.firstImageView.setDefaultImageResId(R.drawable.clear_icon);

                    holder.secondImageView.setVisibility(View.VISIBLE);
                    holder.secondImageView.getLayoutParams().width = imageSize;
                    holder.secondImageView.getLayoutParams().height = cardViewSize;
                    holder.secondImageFrameLayout.setPadding(imageSize, 0, 0, 0);
                    holder.secondImageView.setDefaultImageResId(R.drawable.clear_icon);

                    holder.firstImageView.setImageUrl(User.s3_uri + "post/" + post.postID + "_1.png", imageLoader);
                    holder.secondImageView.setImageUrl(User.s3_uri + "post/" + post.postID + "_2.png", imageLoader);
                    break;
                case 2:
                    holder.firstImageView.getLayoutParams().width = cardViewSize;
                    holder.firstImageView.getLayoutParams().height = imageSize;
                    holder.firstImageView.setDefaultImageResId(R.drawable.clear_icon);

                    holder.secondImageView.setVisibility(View.VISIBLE);
                    holder.secondImageView.getLayoutParams().width = cardViewSize;
                    holder.secondImageView.getLayoutParams().height = imageSizeAdjusted;
                    holder.secondImageFrameLayout.setPadding(0, imagePadding, 0, 0);
                    holder.secondImageView.setDefaultImageResId(R.drawable.clear_icon);

                    holder.firstImageView.setImageUrl(User.s3_uri + "post/" + post.postID + "_1.png", imageLoader);
                    holder.secondImageView.setImageUrl(User.s3_uri + "post/" + post.postID + "_2.png", imageLoader);
                    break;
                case 0:
                default:
                    holder.firstImageView.getLayoutParams().width = cardViewSize;
                    holder.firstImageView.getLayoutParams().height = cardViewSize;
                    holder.firstImageView.setDefaultImageResId(R.drawable.clear_icon);
                    holder.firstImageView.setImageUrl(User.s3_uri + "post/" + post.postID + "_1.png", imageLoader);

                    holder.secondImageView.setVisibility(View.GONE);
            }

            // Set three dot menu OnClick listener
            holder.menuImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { getDetails(post); }
            });

            // Set hot spot one OnClick listener
            holder.hotspotOneImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    post.votes++;
                    post.myVoteState = 1;
                    post.hotspotOne.votes++;
                    updatePostHolder(holder);
                    vote(post.postID, post.myVoteState);
                }
            });

            // Set hot spot two OnClick listener
            holder.hotspotTwoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    post.votes++;
                    post.myVoteState = 2;
                    post.hotspotTwo.votes++;
                    updatePostHolder(holder);
                    vote(post.postID, post.myVoteState);
                }
            });

            // Set hotspot image position
            holder.hotspotOneImageView.setX(post.hotspotOne.position.x);
            holder.hotspotOneImageView.setY(post.hotspotOne.position.y);
            holder.hotspotTwoImageView.setX(post.hotspotTwo.position.x);
            holder.hotspotTwoImageView.setY(post.hotspotTwo.position.y);

            // Set hotspot result layout position accounting offset
            holder.hotspotOneFrameLayout.setX(post.hotspotOne.position.x - hotspotOffset);
            holder.hotspotOneFrameLayout.setY(post.hotspotOne.position.y - hotspotOffset);
            holder.hotspotTwoFrameLayout.setX(post.hotspotTwo.position.x - hotspotOffset);
            holder.hotspotTwoFrameLayout.setY(post.hotspotTwo.position.y - hotspotOffset);
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) { super.onAttachedToRecyclerView(recyclerView); }

        public class PostHolder extends RecyclerView.ViewHolder implements CustomCardView.InViewListener {
            private ImageView profileImageView, menuImageView, hotspotOneImageView, hotspotTwoImageView, hotspotOneCheckImageView, hotspotTwoCheckImageView;
            private TextView tagsTextView, usernameTextView, votesTextView, hotspotOneTextView, hotspotTwoTextView, hotspotOnePercentTextView, hotspotTwoPercentTextView;
            private FadeInNetworkImageView firstImageView, secondImageView;
            private ProgressWheel hotspotOneCustomProgressWheel, hotspotTwoCustomProgressWheel;
            private FrameLayout secondImageFrameLayout, hotspotOneFrameLayout, hotspotTwoFrameLayout;
            private CustomCardView customCardView;

            PostHolder(View itemView) {
                super(itemView);
                customCardView = (CustomCardView)itemView.findViewById(R.id.customCardView);
                customCardView.setInViewListener(this);

                profileImageView = (ImageView)itemView.findViewById(R.id.profileImageView);
                menuImageView = (ImageView)itemView.findViewById(R.id.menuImageView);
                tagsTextView = (TextView)itemView.findViewById(R.id.tagsTextView);
                usernameTextView = (TextView)itemView.findViewById(R.id.usernameTextView);
                votesTextView = (TextView)itemView.findViewById(R.id.votesTextView);

                firstImageView = (FadeInNetworkImageView)itemView.findViewById(R.id.firstImageView);
                hotspotOneFrameLayout = (FrameLayout)itemView.findViewById(R.id.hotspotOneFrameLayout);
                hotspotOneFrameLayout.setAlpha(0.0f);
                hotspotOneFrameLayout.setVisibility(View.GONE);
                hotspotOneImageView = (ImageView)itemView.findViewById(R.id.hotspotOneImageView);
                hotspotOneImageView.setAlpha(0.0f);
                hotspotOneCheckImageView = (ImageView)itemView.findViewById(R.id.hotspotOneCheckImageView);
                hotspotOneTextView = (TextView)itemView.findViewById(R.id.hotspotOneTextView);
                hotspotOnePercentTextView = (TextView)itemView.findViewById(R.id.hotspotOnePercentTextView);
                hotspotOneCustomProgressWheel = (ProgressWheel)itemView.findViewById(R.id.hotspotOneCustomProgressWheel);
                hotspotOneCustomProgressWheel.setReversed(true);
                hotspotOneCustomProgressWheel.setCallback(new ProgressWheel.ProgressCallback() {
                    @Override
                    public void onProgressUpdate(float progress) {
                        if(hotspotOneCustomProgressWheel.isLinearProgress()){ hotspotOneTextView.setText(Integer.toString(Math.round(progress*100))); }
                    }
                });

                secondImageFrameLayout = (FrameLayout)itemView.findViewById(R.id.secondImageFrameLayout);
                secondImageView = (FadeInNetworkImageView)itemView.findViewById(R.id.secondImageView);
                hotspotTwoFrameLayout = (FrameLayout)itemView.findViewById(R.id.hotspotTwoFrameLayout);
                hotspotTwoFrameLayout.setAlpha(0.0f);
                hotspotTwoFrameLayout.setVisibility(View.GONE);
                hotspotTwoImageView = (ImageView)itemView.findViewById(R.id.hotspotTwoImageView);
                hotspotTwoImageView.setAlpha(0.0f);
                hotspotTwoCheckImageView = (ImageView)itemView.findViewById(R.id.hotspotTwoCheckImageView);
                hotspotTwoTextView = (TextView)itemView.findViewById(R.id.hotspotTwoTextView);
                hotspotTwoPercentTextView = (TextView)itemView.findViewById(R.id.hotspotTwoPercentTextView);
                hotspotTwoCustomProgressWheel = (ProgressWheel)itemView.findViewById(R.id.hotspotTwoCustomProgressWheel);
                hotspotTwoCustomProgressWheel.setReversed(false);
                hotspotTwoCustomProgressWheel.setCallback(new ProgressWheel.ProgressCallback() {
                    @Override
                    public void onProgressUpdate(float progress) {
                        if(hotspotTwoCustomProgressWheel.isLinearProgress()){ hotspotTwoTextView.setText(Integer.toString((int)(progress*100))); }
                    }
                });
            }

            @Override
            public void onViewEnter(int position) {
                AnimatorSet animatorSet = new AnimatorSet();
                ValueAnimator firstHotspotAnimation = ObjectAnimator.ofFloat(hotspotOneImageView, "alpha", 0.0f, 1.0f);
                ValueAnimator secondHotspotAnimation = ObjectAnimator.ofFloat(hotspotTwoImageView, "alpha", 0.0f, 1.0f);
                animatorSet.play(firstHotspotAnimation).with(secondHotspotAnimation);

                Post post = posts.get(position);
                if(post.myVoteState > 0){
                    hotspotOneImageView.setEnabled(false);
                    hotspotTwoImageView.setEnabled(false);
                    hotspotOneImageView.setImageResource(R.drawable.hotspot_darkened);
                    hotspotTwoImageView.setImageResource(R.drawable.hotspot_darkened);

                    hotspotOneFrameLayout.setVisibility(View.VISIBLE);
                    hotspotTwoFrameLayout.setVisibility(View.VISIBLE);
                    ValueAnimator firstHotspotResultAnimation = ObjectAnimator.ofFloat(hotspotOneFrameLayout, "alpha", 0.0f, 1.0f);
                    ValueAnimator secondHotspotResultAnimation = ObjectAnimator.ofFloat(hotspotTwoFrameLayout, "alpha", 0.0f, 1.0f);
                    animatorSet.play(firstHotspotAnimation).with(firstHotspotResultAnimation);
                    animatorSet.play(firstHotspotAnimation).with(secondHotspotResultAnimation);

                    hotspotOneTextView.setText("0");
                    hotspotTwoTextView.setText("0");
                    hotspotOneCustomProgressWheel.setInstantProgress(0.0f);
                    hotspotTwoCustomProgressWheel.setInstantProgress(0.0f);

                    float hotspotOneVoteCount = (float)Math.round(((float)post.hotspotOne.votes/post.votes)*100)/100;
                    float hotspotTwoVoteCount = (float)Math.round(((float)post.hotspotTwo.votes/post.votes)*100)/100;

                    hotspotOneCustomProgressWheel.setBarColor((hotspotOneVoteCount >= 0.5f) ? 0xFF1DE9B6 : 0xFFD0D2D3);
                    hotspotTwoCustomProgressWheel.setBarColor((hotspotTwoVoteCount >= 0.5f) ? 0xFF1DE9B6 : 0xFFD0D2D3);
                    hotspotOneCustomProgressWheel.setProgress(hotspotOneVoteCount);
                    hotspotTwoCustomProgressWheel.setProgress(hotspotTwoVoteCount);

                    hotspotOneTextView.setTextColor((hotspotOneVoteCount >= 0.5f) ? getResources().getColor(R.color.color_better) : getResources().getColor(R.color.color_gray_default));
                    hotspotOnePercentTextView.setTextColor((hotspotOneVoteCount >= 0.5f) ? getResources().getColor(R.color.color_better) : getResources().getColor(R.color.color_gray_default));
                    hotspotTwoTextView.setTextColor((hotspotTwoVoteCount >= 0.5f) ? getResources().getColor(R.color.color_better) : getResources().getColor(R.color.color_gray_default));
                    hotspotTwoPercentTextView.setTextColor((hotspotTwoVoteCount >= 0.5f) ? getResources().getColor(R.color.color_better) : getResources().getColor(R.color.color_gray_default));

                    switch(post.myVoteState){
                        case 1:
                            hotspotOneCheckImageView.setVisibility(View.VISIBLE);
                            hotspotTwoCheckImageView.setVisibility(View.GONE);
                            if(hotspotOneVoteCount >= 0.5f) hotspotOneCheckImageView.setImageResource(R.drawable.ic_myvote_check_green_24dp);
                            else hotspotOneCheckImageView.setImageResource(R.drawable.ic_myvote_check_grey_24dp);
                            break;
                        case 2:
                            hotspotOneCheckImageView.setVisibility(View.GONE);
                            hotspotTwoCheckImageView.setVisibility(View.VISIBLE);
                            if(hotspotTwoVoteCount >= 0.5f) hotspotTwoCheckImageView.setImageResource(R.drawable.ic_myvote_check_green_24dp);
                            else hotspotTwoCheckImageView.setImageResource(R.drawable.ic_myvote_check_grey_24dp);
                            break;
                        case 3:
                            hotspotOneCheckImageView.setVisibility(View.GONE);
                            hotspotTwoCheckImageView.setVisibility(View.GONE);
                            break;
                    }
                } else {
                    hotspotOneImageView.setEnabled(true);
                    hotspotTwoImageView.setEnabled(true);
                    hotspotOneImageView.setImageResource(R.drawable.global_hotspot_blank_96dp);
                    hotspotTwoImageView.setImageResource(R.drawable.global_hotspot_blank_96dp);

                    hotspotOneFrameLayout.setVisibility(View.GONE);
                    hotspotTwoFrameLayout.setVisibility(View.GONE);
                    hotspotOneFrameLayout.setAlpha(0.0f);
                    hotspotTwoFrameLayout.setAlpha(0.0f);
                }

                animatorSet.setInterpolator(new DecelerateInterpolator());
                animatorSet.setDuration(500).start();
            }
        }

        private void updatePostHolder(PostHolder holder){
            Post post = posts.get(holder.getPosition());

            holder.hotspotOneImageView.setEnabled(false);
            holder.hotspotTwoImageView.setEnabled(false);
            holder.hotspotOneImageView.setImageResource(R.drawable.hotspot_darkened);
            holder.hotspotTwoImageView.setImageResource(R.drawable.hotspot_darkened);

            AnimatorSet animatorSet = new AnimatorSet();
            ValueAnimator firstHotspotResultAnimation = ObjectAnimator.ofFloat(holder.hotspotOneFrameLayout, "alpha", 0.0f, 1.0f);
            ValueAnimator secondHotspotResultAnimation = ObjectAnimator.ofFloat(holder.hotspotTwoFrameLayout, "alpha", 0.0f, 1.0f);
            animatorSet.play(firstHotspotResultAnimation).with(secondHotspotResultAnimation);
            holder.hotspotOneFrameLayout.setVisibility(View.VISIBLE);
            holder.hotspotTwoFrameLayout.setVisibility(View.VISIBLE);

            holder.hotspotOneTextView.setText("0");
            holder.hotspotTwoTextView.setText("0");
            holder.hotspotOneCustomProgressWheel.setInstantProgress(0.0f);
            holder.hotspotTwoCustomProgressWheel.setInstantProgress(0.0f);

            float hotspotOneVoteCount = (float)Math.round(((float)post.hotspotOne.votes/post.votes)*100)/100;
            float hotspotTwoVoteCount = (float)Math.round(((float)post.hotspotTwo.votes/post.votes)*100)/100;

            holder.hotspotOneCustomProgressWheel.setBarColor((hotspotOneVoteCount >= 0.5f) ? 0xFF1DE9B6 : 0xFFD0D2D3);
            holder.hotspotTwoCustomProgressWheel.setBarColor((hotspotTwoVoteCount >= 0.5f) ? 0xFF1DE9B6 : 0xFFD0D2D3);
            holder.hotspotOneCustomProgressWheel.setProgress(hotspotOneVoteCount);
            holder.hotspotTwoCustomProgressWheel.setProgress(hotspotTwoVoteCount);

            holder.hotspotOneTextView.setTextColor((hotspotOneVoteCount >= 0.5f) ? getResources().getColor(R.color.color_better) : getResources().getColor(R.color.color_gray_default));
            holder.hotspotOnePercentTextView.setTextColor((hotspotOneVoteCount >= 0.5f) ? getResources().getColor(R.color.color_better) : getResources().getColor(R.color.color_gray_default));
            holder.hotspotTwoTextView.setTextColor((hotspotTwoVoteCount >= 0.5f) ? getResources().getColor(R.color.color_better) : getResources().getColor(R.color.color_gray_default));
            holder.hotspotTwoPercentTextView.setTextColor((hotspotTwoVoteCount >= 0.5f) ? getResources().getColor(R.color.color_better) : getResources().getColor(R.color.color_gray_default));

            switch(post.myVoteState){
                case 1:
                    holder.hotspotOneCheckImageView.setVisibility(View.VISIBLE);
                    holder.hotspotTwoCheckImageView.setVisibility(View.GONE);
                    if(hotspotOneVoteCount >= 0.5f) holder.hotspotOneCheckImageView.setImageResource(R.drawable.ic_myvote_check_green_24dp);
                    else holder.hotspotOneCheckImageView.setImageResource(R.drawable.ic_myvote_check_grey_24dp);
                    break;
                case 2:
                    holder.hotspotOneCheckImageView.setVisibility(View.GONE);
                    holder.hotspotTwoCheckImageView.setVisibility(View.VISIBLE);
                    if(hotspotTwoVoteCount >= 0.5f) holder.hotspotTwoCheckImageView.setImageResource(R.drawable.ic_myvote_check_green_24dp);
                    else holder.hotspotTwoCheckImageView.setImageResource(R.drawable.ic_myvote_check_grey_24dp);
                    break;
            }

            animatorSet.setInterpolator(new DecelerateInterpolator());
            animatorSet.setDuration(500).start();
            recyclerViewAdapter.notifyItemChanged(holder.getPosition());
        }

        private void vote(int postID, int vote){
            Map<String, String> map = new HashMap<>();
            map.put("api_key", User.api_key);
            map.put("post_id", Integer.toString(postID));
            map.put("user_id", Integer.toString(User.userID));
            map.put("vote", Integer.toString(vote));

            JSONObject params = new JSONObject(map);
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, User.api_uri + "vote", params, new Response.Listener<JSONObject>() {
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
            AppController.getInstance().addToRequestQueue(jsonObjReq, "Vote - Create");
        }

        private void getDetails(final Post post){
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, User.api_uri + "post/detail/" + post.postID + "/" + User.userID, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(User.LOGTAG, response.toString());

                    try {
                        JSONObject json = response.getJSONObject("response");

                        // Lazy initialize
                        if (User.threeDotItems == null) User.threeDotItems = new ArrayList<>();
                        else User.threeDotItems.clear();

                        // Add "Voters"
                        if(post.votes > 0) User.threeDotItems.add(ThreeDotMenu.buildThreeDotItem(post.postID, 0, "Voters", false));

                        // Add "Favorite Post"
                        if(!User.username.toUpperCase().equals(post.username.toUpperCase())) User.threeDotItems.add(ThreeDotMenu.buildThreeDotItem(post.postID, 1, "Favorite Post", (json.getInt("favorited_post") == 1)));

                        // Add "Username"
                        if(!User.username.equals(post.username)) User.threeDotItems.add(ThreeDotMenu.buildThreeDotItem(post.userID, 2, post.username, (json.getInt("following") == 1)));

                        // Add "Hashtags"
                        JSONArray favoriteHashtags = json.getJSONArray("favorite_hashtags");
                        for(int i = 0; i < post.tags.length; i++) {
                            JSONObject hashtag = favoriteHashtags.getJSONObject(i);
                            User.threeDotItems.add(ThreeDotMenu.buildThreeDotItem(hashtag.getInt("hashtag_id"), 3, post.tags[i], (hashtag.getInt("favorited") == 1)));
                        }

                        // Add "Report Misuse"
                        if(!User.username.toUpperCase().equals(post.username.toUpperCase())) User.threeDotItems.add(ThreeDotMenu.buildThreeDotItem(0, 4, "Report Misuse", false));

                        Activity activity = getActivity();
                        if (activity instanceof FeedBasic) {
                            FeedBasic feedBasic = (FeedBasic) activity;
                            feedBasic.showThreeDotMenu(post.myVoteState);
                        } else if (activity instanceof FeedController) {
                            FeedController feedController = (FeedController) activity;
                            feedController.showThreeDotMenu(post.myVoteState);
                        }
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
            AppController.getInstance().addToRequestQueue(jsonObjReq, "Post - Detail");
        }
    }

    private class Post {
        int postID, userID, votes, layoutType, myVoteState;
        String username, tagString;
        String[] tags;
        Hotspot hotspotOne, hotspotTwo;

        Post(int postID, int userID, int votes, int layoutType, int myVoteState, String username, String tagString,  String[] tags, Hotspot hotspotOne, Hotspot hotspotTwo) {
            this.postID = postID;
            this.userID = userID;
            this.votes = votes;
            this.layoutType = layoutType;
            this.myVoteState = myVoteState;
            this.username = username;
            this.tagString = tagString;
            this.tags = tags;
            this.hotspotOne = hotspotOne;
            this.hotspotTwo = hotspotTwo;
        }
    }

    private class Hotspot {
        int votes;
        Point position;

        Hotspot(int votes, Point position){
            this.votes = votes;
            this.position = position;
        }
    }
}
