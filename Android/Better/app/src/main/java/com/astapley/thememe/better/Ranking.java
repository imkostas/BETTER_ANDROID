package com.astapley.thememe.better;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ranking extends FragmentActivity implements View.OnClickListener {
    private ViewPager viewPager;
    private TextView myRankingTextView, leaderBoardTextView;
    private ImageView indicatorImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ranking);

        //top bar initialization
        final TextView topBarTitle = (TextView)findViewById(R.id.topBarTitle);
        topBarTitle.setTypeface(User.RalewaySemiBold);
        topBarTitle.setText(getString(R.string.ranking_title));
        topBarTitle.setOnClickListener(this);

        myRankingTextView = (TextView)findViewById(R.id.myRankingTextView);
        myRankingTextView.setTextColor(getResources().getColor(R.color.color_white));
        myRankingTextView.setTypeface(User.RalewaySemiBold);
        myRankingTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { viewPager.setCurrentItem(0);
            }
        });

        leaderBoardTextView = (TextView)findViewById(R.id.leaderBoardTextView);
        leaderBoardTextView.setTextColor(getResources().getColor(R.color.color_light_gray_green));
        leaderBoardTextView.setTypeface(User.RalewaySemiBold);
        leaderBoardTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {viewPager.setCurrentItem(1);
            }
        });

        indicatorImageView = (ImageView)findViewById(R.id.indicatorImageView);
        indicatorImageView.setPadding(0, 0, User.windowSize.x/2, 0);

        PagerAdapter viewPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager)findViewById(R.id.viewPager);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                switch(position){
                    case 0:
                        myRankingTextView.setTextColor(getResources().getColor(R.color.color_white));
                        leaderBoardTextView.setTextColor(getResources().getColor(R.color.color_light_gray_green));
                        indicatorImageView.setPadding(0, 0, User.windowSize.x/2, 0);
                        break;
                    case 1:
                        myRankingTextView.setTextColor(getResources().getColor(R.color.color_light_gray_green));
                        leaderBoardTextView.setTextColor(getResources().getColor(R.color.color_white));
                        indicatorImageView.setPadding(User.windowSize.x/2, 0, 0, 0);
                        break;
                }
            }
        });
        viewPager.setCurrentItem(0);
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

    private class PagerAdapter extends FragmentPagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    return new MyRanking();
                default:
                    return new Leaderboard();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public static class MyRanking extends Fragment {
        private ArrayList<Badge> badges;
        private GridViewAdapter gridViewAdapter;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.my_ranking, container, false);

            TextView rankTextView = (TextView)rootView.findViewById(R.id.rankTextView);
            rankTextView.setTypeface(User.RalewayMedium);
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

            TextView pointsTextView = (TextView)rootView.findViewById(R.id.pointsTextView);
            pointsTextView.setTypeface(User.RobotoMedium);
            pointsTextView.setText(Integer.toString(User.rank.totalPoints));

            badges = new ArrayList<>();
            gridViewAdapter = new GridViewAdapter();
            GridView gridView = (GridView)rootView.findViewById(R.id.gridView);
            gridView.setAdapter(gridViewAdapter);
            loadBadges();

            ImageView backgroundProfileImageView = (ImageView)rootView.findViewById(R.id.backgroundProfileImageView);
            ImageView profileImageView = (ImageView)rootView.findViewById(R.id.profileImageView);

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

            View rankNewbie = rootView.findViewById(R.id.rankNewbie);
            rankNewbie.setBackgroundColor(getResources().getColor((User.rank.rank > 0)?R.color.color_better_dark:R.color.color_light_gray));

            View rankMainstream = rootView.findViewById(R.id.rankMainstream);
            rankMainstream.setBackgroundColor(getResources().getColor((User.rank.rank > 1)?R.color.color_better_dark:R.color.color_light_gray));

            View rankTrailblazer = rootView.findViewById(R.id.rankTrailblazer);
            rankTrailblazer.setBackgroundColor(getResources().getColor((User.rank.rank > 2)?R.color.color_better_dark:R.color.color_light_gray));

            View rankTrendsetter = rootView.findViewById(R.id.rankTrendsetter);
            rankTrendsetter.setBackgroundColor(getResources().getColor((User.rank.rank > 3)?R.color.color_better_dark:R.color.color_light_gray));

            View rankCrowned = rootView.findViewById(R.id.rankCrowned);
            rankCrowned.setBackgroundColor(getResources().getColor((User.rank.rank > 4)?R.color.color_better_dark:R.color.color_light_gray));

            ImageView crownedImageView = (ImageView)rootView.findViewById(R.id.crownedImageView);
            crownedImageView.setImageResource((User.rank.rank > 4)?R.drawable.ic_crown_solid_30dp:R.drawable.ic_better_crown_38dp);

            return rootView;
        }

        private void loadBadges(){
            int[] badgeStats = {User.rank.badgeTastemaker, User.rank.badgeAdventurer, User.rank.badgeAdmirer, User.rank.badgeRoleModel, User.rank.badgeCelebrity, User.rank.badgeIdol};
            List<String> badgeList = Arrays.asList(getResources().getStringArray(R.array.badge_array));
            for(int i = 0; i < badgeList.size(); i++)badges.add(new Badge(badgeList.get(i), badgeStats[i]));
            gridViewAdapter.notifyDataSetChanged();
        }

        private class GridViewAdapter extends BaseAdapter {
            public GridViewAdapter(){}

            @Override
            public int getCount() { return badges.size(); }

            @Override
            public Object getItem(int position) { return position; }

            @Override
            public long getItemId(int position) { return position; }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                BadgeHolder holder;

                // If convertView null initialize - otherwise leverage ViewHolder pattern by accessing tag
                if(convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.my_ranking_item, parent, false);

                    holder = new BadgeHolder();
                    holder.badgeImageView = (ImageView)convertView.findViewById(R.id.badgeImageView);
                    holder.titleTextView = (TextView)convertView.findViewById(R.id.titleTextView);
                    holder.statusTextView = (TextView)convertView.findViewById(R.id.statusTextView);

                    convertView.setTag(holder);
                } else holder = (BadgeHolder)convertView.getTag();

                // Initialize UI elements for product at indicated position
                Badge badge = badges.get(position);
                String status = "";
                switch(badge.status){
                    case 1:
                        status = "BRONZE";
                        break;
                    case 2:
                        status = "SILVER";
                        break;
                    case 3:
                        status = "GOLD";
                        break;
                    case 0:
                    default:
                        status = "";
                }

                if(!status.equals(""))holder.badgeImageView.setImageResource(getResources().getIdentifier("drawable/" + "badge_" + status.toLowerCase() + "_" + badge.title.replace(" ", "").toLowerCase() + "_80dp", "drawable", getActivity().getPackageName()));
                else holder.badgeImageView.setImageResource(getResources().getIdentifier("drawable/" + "badge_default_80dp", "drawable", getActivity().getPackageName()));
                holder.titleTextView.setText(badge.title);
                holder.titleTextView.setTypeface(User.RalewayMedium);
                holder.statusTextView.setText(status);
                holder.statusTextView.setTypeface(User.RalewayMedium);

                return convertView;
            }

            // ProductHolder class to make use of the ViewHolder pattern
            private class BadgeHolder {
                ImageView badgeImageView;
                TextView titleTextView;
                TextView statusTextView;
            }
        }

        private class Badge {
            private String title;
            private int status;

            private Badge(String title, int status){
                this.title = title;
                this.status = status;
            }
        }
    }

    public static class Leaderboard extends Fragment {
        private ArrayList<RankingItem> rankingDaily, rankingWeekly, rankingAllTime;
        private ListView rankingListView;
        private RankingListAdapter rankingListViewAdapter;
        private Button dailyBtn, weeklyBtn, allTimeBtn;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.leaderboard, container, false);

            rankingDaily = new ArrayList<>();
            rankingDaily.add(new RankingItem("imMRcool", 0, 1200));
            rankingDaily.add(new RankingItem("anthercoolperson", 1, 1100));
            rankingDaily.add(new RankingItem("ohyeah", 2, 1000));
            rankingDaily.add(new RankingItem("imMRcool", 3, 900));
            rankingDaily.add(new RankingItem("anthercoolperson", 4, 800));
            rankingDaily.add(new RankingItem("ohyeah", 1, 700));
            rankingDaily.add(new RankingItem("imMRcool", 1, 600));
            rankingDaily.add(new RankingItem("anthercoolperson", 1, 500));
            rankingDaily.add(new RankingItem("ohyeah", 1, 400));
            rankingDaily.add(new RankingItem("imMRcool", 1, 300));

            rankingWeekly = new ArrayList<>();
            rankingWeekly.add(new RankingItem("imMRcool", 1, 5700));
            rankingWeekly.add(new RankingItem("imMRcool", 1, 5600));
            rankingWeekly.add(new RankingItem("imMRcool", 1, 5500));
            rankingWeekly.add(new RankingItem("imMRcool", 1, 5400));
            rankingWeekly.add(new RankingItem("imMRcool", 1, 5300));
            rankingWeekly.add(new RankingItem("imMRcool", 1, 5200));
            rankingWeekly.add(new RankingItem("imMRcool", 1, 5100));
            rankingWeekly.add(new RankingItem("imMRcool", 1, 5000));
            rankingWeekly.add(new RankingItem("imMRcool", 1, 4900));
            rankingWeekly.add(new RankingItem("imMRcool", 1, 4800));

            rankingAllTime = new ArrayList<>();
            rankingAllTime.add(new RankingItem("imMRcool", 1, 46000));
            rankingAllTime.add(new RankingItem("anthercoolperson", 1, 45000));
            rankingAllTime.add(new RankingItem("ohyeah", 1, 44000));
            rankingAllTime.add(new RankingItem("ohyeahagain", 1, 43000));
            rankingAllTime.add(new RankingItem("imMRcool", 1, 42000));
            rankingAllTime.add(new RankingItem("anthercoolperson", 1, 41000));
            rankingAllTime.add(new RankingItem("ohyeah", 1, 40000));
            rankingAllTime.add(new RankingItem("ohyeahagain", 1, 39000));
            rankingAllTime.add(new RankingItem("imMRcool", 1, 38000));
            rankingAllTime.add(new RankingItem("anthercoolperson", 1, 37000));

            rankingListViewAdapter = new RankingListAdapter(getActivity(), new ArrayList<RankingItem>());
            rankingListView = (ListView)rootView.findViewById(R.id.rankingListView);
            rankingListView.setAdapter(rankingListViewAdapter);
            rankingListView.setDivider(null);

            dailyBtn = (Button)rootView.findViewById(R.id.dailyBtn);
            dailyBtn.setTypeface(User.RalewaySemiBold);
            dailyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateLeaderBoard(1);
                }
            });

            weeklyBtn = (Button)rootView.findViewById(R.id.weeklyBtn);
            weeklyBtn.setTypeface(User.RalewaySemiBold);
            weeklyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateLeaderBoard(2);
                }
            });

            allTimeBtn = (Button)rootView.findViewById(R.id.allTimeBtn);
            allTimeBtn.setTypeface(User.RalewaySemiBold);
            allTimeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateLeaderBoard(3);
                }
            });

            updateLeaderBoard(1);

            return rootView;
        }

        private void updateLeaderBoard(int activeLeaderBoard){
            updateLeaderBoardTabs(activeLeaderBoard);

            rankingListViewAdapter.clear();
            switch(activeLeaderBoard){
                case 1:
                    rankingListViewAdapter.addAll(rankingDaily);
                    break;
                case 2:
                    rankingListViewAdapter.addAll(rankingWeekly);
                    break;
                case 3:
                    rankingListViewAdapter.addAll(rankingAllTime);
                    break;
            }
            rankingListView.smoothScrollToPosition(0);
            rankingListViewAdapter.notifyDataSetChanged();
        }

        private void updateLeaderBoardTabs(int activeTab){
            switch(activeTab){
                case 1:
                    dailyBtn.setBackgroundColor(getResources().getColor(R.color.color_white));
                    weeklyBtn.setBackgroundColor(getResources().getColor(R.color.color_gray_tab));
                    allTimeBtn.setBackgroundColor(getResources().getColor(R.color.color_gray_tab));
                    break;
                case 2:
                    dailyBtn.setBackgroundColor(getResources().getColor(R.color.color_gray_tab));
                    weeklyBtn.setBackgroundColor(getResources().getColor(R.color.color_white));
                    allTimeBtn.setBackgroundColor(getResources().getColor(R.color.color_gray_tab));
                    break;
                case 3:
                    dailyBtn.setBackgroundColor(getResources().getColor(R.color.color_gray_tab));
                    weeklyBtn.setBackgroundColor(getResources().getColor(R.color.color_gray_tab));
                    allTimeBtn.setBackgroundColor(getResources().getColor(R.color.color_white));
                    break;
            }
        }

        private static class RankingItem {
            private String username;
            private int rank;
            private int points;

            public RankingItem(String username, int rank, int points){
                this.username = username;
                this.rank = rank;
                this.points = points;
            }
        }

        public class RankingListAdapter extends ArrayAdapter<RankingItem> {
            public RankingListAdapter(Context context, ArrayList<RankingItem> itemList){ super(context, 0, itemList); }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                RankingItemHolder holder;

                if(convertView == null) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    convertView = inflater.inflate(R.layout.leaderboard_item, parent, false);

                    holder = new RankingItemHolder();
                    holder.rankTextView = (TextView)convertView.findViewById(R.id.rankTextView);
                    holder.rankTextView.setTypeface(User.RalewayMedium);
                    holder.profileImageView = (ImageView)convertView.findViewById(R.id.profileImageView);
                    holder.usernameTextView = (TextView)convertView.findViewById(R.id.usernameTextView);
                    holder.usernameTextView.setTypeface(User.RalewayMedium);
                    holder.rankImageView = (ImageView)convertView.findViewById(R.id.rankImageView);
                    holder.pointsTextView = (TextView)convertView.findViewById(R.id.pointsTextView);
                    holder.pointsTextView.setTypeface(User.RalewayMedium);

                    convertView.setTag(holder);
                } else holder = (RankingItemHolder)convertView.getTag();

                RankingItem item = getItem(position);
                holder.rankTextView.setText(Integer.toString(position + 1));
                holder.rankTextView.setTypeface(User.RobotoMedium);
                holder.usernameTextView.setText(item.username.toUpperCase());
                holder.usernameTextView.setTypeface(User.RalewayMedium);
                holder.pointsTextView.setText(Integer.toString(item.points));
                holder.pointsTextView.setTypeface(User.RobotoMedium);

                AppController.getInstance().getImageLoader().get("", ImageLoader.getImageListener(holder.profileImageView, R.drawable.feed_button_profile_default_m_56dp, R.drawable.feed_button_profile_default_m_56dp));

                Drawable rankDrawable;
                switch(item.rank){
                    case 0:
                        rankDrawable = getResources().getDrawable(R.drawable.ic_rank_newbie_grey_24dp);
                        break;
                    case 1:
                        rankDrawable = getResources().getDrawable(R.drawable.ic_rank_trailblazer_grey_24dp);
                        break;
                    case 2:
                        rankDrawable = getResources().getDrawable(R.drawable.ic_rank_trendsetter_grey_24dp);
                        break;
                    case 3:
                        rankDrawable = getResources().getDrawable(R.drawable.ic_rank_mainstream_grey_24dp);
                        break;
                    case 4:
                        rankDrawable = getResources().getDrawable(R.drawable.ic_rank_crowned_grey_24dp);
                        break;
                    default:
                        rankDrawable = getResources().getDrawable(R.drawable.ic_rank_newbie_grey_24dp);
                }
                holder.rankImageView.setImageDrawable(rankDrawable);

                return convertView;
            }

            private class RankingItemHolder {
                TextView rankTextView;
                ImageView profileImageView;
                TextView usernameTextView;
                ImageView rankImageView;
                TextView pointsTextView;
            }
        }
    }
}