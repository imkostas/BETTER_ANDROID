package com.astapley.thememe.better;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.regions.Regions;
import com.facebook.Session;
import com.facebook.model.GraphUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class User {
    public static final String LOGTAG = "Better";
//    public static final int DEVICE_TYPE = 2;
    public static final int ANIM_SPEED = 200;

    private static User user;
    private static Context context;

    public static String api_uri;
    public static String api_key;

    public static String s3_uri;
    public static String s3BucketName;
    public static CognitoCachingCredentialsProvider credentialsProvider;
    public static TransferManager transferManager;

    public static int userID;
    public static long facebookID;
    public static String username;
    public static String password;
    public static String email;
    public static String gender;
    public static String birthday;
    public static String birthdayFormatted;
    public static String country;

    public static Bitmap smallProfileBitmap, mediumProfileBitmap, largeProfileBitmap, profileImage, profileBackground;

    public static Rank rank;
    public static NotificationSettings notificationSettings;
    public static Filter filter;
    public static UserCounts counts;

    public static Point windowSize;
    public static float density;

    public static ArrayList<ThreeDotMenu.ThreeDotItem> threeDotItems;

    //fonts
    public static Typeface RalewaySemiBold;
    public static Typeface RalewayRegular;
    public static Typeface RalewayMedium;
    public static Typeface RobotoMedium;
    public static Typeface RobotoBold;
    public static Typeface RobotoBlack;

    private User() {
        api_uri = context.getString(R.string.api_uri);
        api_key = context.getString(R.string.api_key);

        s3_uri = context.getString(R.string.s3_uri);
        s3BucketName = context.getString(R.string.s3_bucket_name);
        credentialsProvider = createCredentialsProvider();
        transferManager = null;

        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        facebookID = prefs.getLong(context.getPackageName() + ".facebook_id", 0);
        username = prefs.getString(context.getPackageName() + ".username", "");
        password = prefs.getString(context.getPackageName() + ".password", "");

        userID = 0;
        email = "";
        gender = "";
        birthday = "";
        birthdayFormatted = "";
        country = "";

        smallProfileBitmap = null;
        mediumProfileBitmap = null;
        largeProfileBitmap = null;
        profileImage = null;
        profileBackground = null;

        rank = null;
        notificationSettings = null;
        filter = new Filter(0, "Everything", "everything", false);
        counts = null;

        windowSize = new Point(0, 0);
        density = context.getResources().getDisplayMetrics().density;

        RalewaySemiBold = Typeface.createFromAsset(context.getAssets(), "Raleway-SemiBold.ttf");
        RalewayRegular = Typeface.createFromAsset(context.getAssets(), "Raleway-Regular.ttf");
        RalewayMedium = Typeface.createFromAsset(context.getAssets(), "Raleway-Medium.ttf");
        RobotoMedium = Typeface.createFromAsset(context.getAssets(), "Roboto-Medium.ttf");
        RobotoBold = Typeface.createFromAsset(context.getAssets(), "Roboto-Bold.ttf");
        RobotoBlack = Typeface.createFromAsset(context.getAssets(), "Roboto-Black.ttf");
    }

    public static void initUser(Context ctx) {
        context = ctx;
        if(user == null)user = new User();
    }

    public static void storeFacebookInfo(GraphUser user) {
        try { facebookID = Long.parseLong(user.getId()); }
        catch (NullPointerException e) { Log.i(LOGTAG, "Failed to save user id"); }

        try { email = user.getProperty("email").toString(); }
        catch (NullPointerException e) { Log.i(LOGTAG, "Failed to save user email"); }

        try {
            birthdayFormatted = user.getProperty("birthday").toString();
            birthday = formatBirthdayMySQL(birthdayFormatted);
        } catch (NullPointerException e) { Log.i(LOGTAG, "Failed to save user birthday"); }

        try { gender = user.getProperty("gender").toString(); }
        catch (NullPointerException e) { Log.i(LOGTAG, "Failed to save user gender"); }
    }

    public static void clearFacebookInfo() {
        facebookID = 0;
        email = "";
        gender = "";
        birthday = "";
    }

    private CognitoCachingCredentialsProvider createCredentialsProvider(){
        return new CognitoCachingCredentialsProvider(context, "us-east-1:c50201ed-1858-4018-a129-385a0585161c", Regions.US_EAST_1);
    }

    public static void makeToast(String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void clear(){
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        user = new User();

        Session session = Session.getActiveSession();
        if(session != null){
            session.closeAndClearTokenInformation();
        } else {
            Log.d(User.LOGTAG, "No active session");
        }
    }

    public static class Rank {
        int rank;
        int totalPoints;
        int weeklyPoints;
        int dailyPoints;
        int badgeTastemaker;
        int badgeAdventurer;
        int badgeAdmirer;
        int badgeRoleModel;
        int badgeCelebrity;
        int badgeIdol;

        public Rank(int rank, int totalPoints, int weeklyPoints,
                    int dailyPoints, int badgeTastemaker, int badgeAdventurer,
                    int badgeAdmirer, int badgeRoleModel, int badgeCelebrity, int badgeIdol) {
            this.rank = rank;
            this.totalPoints = totalPoints;
            this.weeklyPoints = weeklyPoints;
            this.dailyPoints = dailyPoints;
            this.badgeTastemaker = badgeTastemaker;
            this.badgeAdventurer = badgeAdventurer;
            this.badgeAdmirer = badgeAdmirer;
            this.badgeRoleModel = badgeRoleModel;
            this.badgeCelebrity = badgeCelebrity;
            this.badgeIdol = badgeIdol;
        }
    }

    public static class NotificationSettings {
        int votedPost;
        int favoritedPost;
        int newFollower;

        public NotificationSettings(int votedPost, int favoritedPost, int newFollower){
            this.votedPost = votedPost;
            this.favoritedPost = favoritedPost;
            this.newFollower = newFollower;
        }
    }

    public class Filter {
        public int id;
        public String title;
        public String type;
        public Boolean hasChanged;

        private Filter(int id, String title, String type, Boolean hasChanged){
            this.id = id;
            this.title = title;
            this.type = type;
            this.hasChanged = hasChanged;
        }
    }

    public static class UserCounts {
        int myVotes, myPosts, favoritePosts, favoriteTags, following, followers;

        public UserCounts(int myVotes, int myPosts, int favoritePosts, int favoriteTags, int following, int followers){
            this.myVotes = myVotes;
            this.myPosts = myPosts;
            this.favoritePosts = favoritePosts;
            this.favoriteTags = favoriteTags;
            this.following = following;
            this.followers = followers;
        }
    }

    public static void updateCounts(int myVotes, int myPosts, int favoritePosts, int favoriteTags, int following, int followers){
        User.counts.myVotes = myVotes;
        User.counts.myPosts = myPosts;
        User.counts.favoritePosts = favoritePosts;
        User.counts.favoriteTags = favoriteTags;
        User.counts.following = following;
        User.counts.followers = followers;
    }

    public static String formatBirthday(String str){
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date birthDate = simpleDateFormat.parse(str);
            Calendar birthday = Calendar.getInstance();
            birthday.setTime(birthDate);
            String month = birthday.get(Calendar.MONTH) + 1 + "";
            if(month.length() < 2)month = "0" + month;
            return month + "/" + birthday.get(Calendar.DAY_OF_MONTH) + "/" + birthday.get(Calendar.YEAR);
        } catch (ParseException e) {
            e.printStackTrace();
            return str;
        }
    }

    public static String formatBirthdayMySQL(String str){
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            Date birthDate = simpleDateFormat.parse(str);
            Calendar birthday = Calendar.getInstance();
            birthday.setTime(birthDate);
            String month = birthday.get(Calendar.MONTH) + 1 + "";
            if(month.length() < 2)month = "0" + month;
            return birthday.get(Calendar.YEAR) + "-" + month + "-" + birthday.get(Calendar.DAY_OF_MONTH);
        } catch (ParseException e) {
            e.printStackTrace();
            return str;
        }
    }

    public static String getAge(){
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date birthDate = simpleDateFormat.parse(User.birthday);

            Calendar birthday = Calendar.getInstance();
            birthday.setTime(birthDate);
            Calendar currentDay = Calendar.getInstance();

            if(currentDay.get(Calendar.MONTH) < birthday.get(Calendar.MONTH) || (currentDay.get(Calendar.MONTH) == birthday.get(Calendar.MONTH) && currentDay.get(Calendar.DAY_OF_MONTH) < birthday.get(Calendar.DAY_OF_MONTH))) {
                return Integer.toString(currentDay.get(Calendar.YEAR) - birthday.get(Calendar.YEAR) - 1);
            } else {
                return Integer.toString(currentDay.get(Calendar.YEAR) - birthday.get(Calendar.YEAR));
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getCountry(){
        String country = "";

        switch(User.country){
            case "UNITED STATES OF AMERICA":
                country = "USA";
                break;
        }

        return country;
    }
}
