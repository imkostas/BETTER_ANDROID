package com.astapley.thememe.better;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.regions.Regions;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Intro extends FragmentActivity implements View.OnClickListener {
    private static final int NUM_PAGES = 4, ANIM_SPEED = 400;
    private Boolean showIntro = false;

    private ViewPager pager;
    private ImageView indicatorOne, indicatorTwo, indicatorThree, indicatorFour;
    private ImageView backgroundImageActual, backgroundImageReplacement, logo;
    private Drawable drawableShoesDark, drawableShoesSpot, drawablePost, drawableCrown, drawableGreenDot, drawableGrayDot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);

        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(User.windowSize);

        drawableShoesDark = getResources().getDrawable(R.drawable.initial_tutorial_shoes_dark);
        drawableShoesSpot = getResources().getDrawable(R.drawable.initial_tutorial_shoes_spot);
        drawablePost = getResources().getDrawable(R.drawable.initial_tutorial_post);
        drawableCrown = getResources().getDrawable(R.drawable.initial_tutorial_crown);
        drawableGreenDot = getResources().getDrawable(R.drawable.green_dot);
        drawableGrayDot = getResources().getDrawable(R.drawable.gray_dot);

        logo = (ImageView)findViewById(R.id.logo);
        logo.setAlpha(0.0f);

        ValueAnimator animator = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f);
        animator.setDuration(ANIM_SPEED);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
            @Override
            public void onAnimationEnd(Animator animation) {
                if(User.facebookID != 0){
                    getSupportFragmentManager().beginTransaction().add(R.id.container, new FacebookFragment()).commit();
                } else if(User.password.length() > 0) {
                    Log.d(User.LOGTAG, "Authorize using Developer Authenticated identities");
                } else {
                    showIntro = true;
                    showIntro();
                }
            }
        });
        animator.start();
    }

    @Override
    public void onBackPressed() {
        if(showIntro){
            if(pager.getCurrentItem() == 0)super.onBackPressed();
            else pager.setCurrentItem(pager.getCurrentItem() - 1);
        } else super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.getStartedButton:
                showSignUp();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        validateSession();
    }

    private void validateSession(){
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            String accessToken = session.getAccessToken();
            if(accessToken != null){
                Log.d(User.LOGTAG, accessToken);
            }
        } else if(session != null && session.isClosed()) {
            Log.d(User.LOGTAG, "Session closed");
        } else {
            Log.d(User.LOGTAG, "Session null");
        }
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {
        public PagerAdapter(FragmentManager fm) { super(fm); }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            switch(position + 1){
                case 1:
                    NoTitleFragment welcomeFragment = new NoTitleFragment();
                    welcomeFragment.textOne = getResources().getString(R.string.intro_welcome_subtext1);
                    welcomeFragment.textTwo = getResources().getString(R.string.intro_welcome_subtext2);
                    welcomeFragment.textThree = getResources().getString(R.string.intro_welcome_subtext3);
                    fragment = welcomeFragment;
                    break;
                case 2:
                    TitleFragment castFragment = new TitleFragment();
                    castFragment.title = getResources().getString(R.string.intro_cast_your_vote_title);
                    castFragment.textOne = getResources().getString(R.string.intro_cast_your_vote_subtext1);
                    castFragment.textTwo = getResources().getString(R.string.intro_cast_your_vote_subtext2);
                    fragment = castFragment;
                    break;
                case 3:
                    TitleFragment decisionFragment = new TitleFragment();
                    decisionFragment.title = getResources().getString(R.string.intro_make_better_decisions_title);
                    decisionFragment.textOne = getResources().getString(R.string.intro_make_better_decisions_subtext1);
                    decisionFragment.textTwo = getResources().getString(R.string.intro_make_better_decisions_subtext2);
                    fragment = decisionFragment;
                    break;
                case 4:
                    TitleFragment riseFragment = new TitleFragment();
                    riseFragment.title = getResources().getString(R.string.intro_rise_in_the_ranks_title);
                    riseFragment.textOne = getResources().getString(R.string.intro_rise_in_the_ranks_subtext1);
                    riseFragment.textTwo = getResources().getString(R.string.intro_rise_in_the_ranks_subtext2);
                    fragment = riseFragment;
                    break;
                default:
                    NoTitleFragment defaultFragment = new NoTitleFragment();
                    defaultFragment.textOne = getResources().getString(R.string.intro_welcome_subtext1);
                    defaultFragment.textTwo = getResources().getString(R.string.intro_welcome_subtext2);
                    defaultFragment.textThree = getResources().getString(R.string.intro_welcome_subtext3);
                    fragment = defaultFragment;
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    public void updateIndicators(int position){
        crossFade(position);
        switch (position){
            case 0:
                indicatorOne.setImageDrawable(drawableGreenDot);
                indicatorTwo.setImageDrawable(drawableGrayDot);
                break;

            case 1:
                indicatorOne.setImageDrawable(drawableGrayDot);
                indicatorTwo.setImageDrawable(drawableGreenDot);
                indicatorThree.setImageDrawable(drawableGrayDot);
                break;

            case 2:
                indicatorTwo.setImageDrawable(drawableGrayDot);
                indicatorThree.setImageDrawable(drawableGreenDot);
                indicatorFour.setImageDrawable(drawableGrayDot);
                break;

            case 3:
                indicatorThree.setImageDrawable(drawableGrayDot);
                indicatorFour.setImageDrawable(drawableGreenDot);
                break;

            default:
                indicatorOne.setImageDrawable(drawableGrayDot);
                indicatorTwo.setImageDrawable(drawableGrayDot);
                indicatorThree.setImageDrawable(drawableGrayDot);
                indicatorFour.setImageDrawable(drawableGrayDot);
                break;
        }
    }

    public void showSignUp(){
        Intent intent = new Intent(this, SignUp.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_anim);
    }

    public void showIntro(){
        final Button getStartedButton = (Button)findViewById(R.id.getStartedButton);
        getStartedButton.setTypeface(User.RalewaySemiBold);
        getStartedButton.setOnClickListener(this);

        backgroundImageActual = (ImageView)findViewById(R.id.backgroundImageActual);
        backgroundImageReplacement = (ImageView)findViewById(R.id.backgroundImageReplacement);
        backgroundImageReplacement.setAlpha(0.0f);

        // Instantiate a ViewPager and a PagerAdapter.
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);
        pager.setPageTransformer(true, new ZoomOutPageTransformer());
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position){ updateIndicators(position); }
        });

        indicatorOne = (ImageView)findViewById(R.id.indicatorOne);
        indicatorTwo = (ImageView)findViewById(R.id.indicatorTwo);
        indicatorThree = (ImageView)findViewById(R.id.indicatorThree);
        indicatorFour = (ImageView)findViewById(R.id.indicatorFour);
        updateIndicators(0);

        ImageView splash = (ImageView)findViewById(R.id.splash);
        ObjectAnimator.ofFloat(splash, "alpha", 1f, 0f).setDuration(1000).start();
    }

    private void autoLogIn(){
        Map<String, String> map = new HashMap<>();
        map.put("api_key", User.api_key);
        if(User.facebookID != 0){
            map.put("facebook_id", String.valueOf(User.facebookID));
        } else {
            map.put("username", User.username);
            map.put("hash", User.password);
        }
        JSONObject params = new JSONObject(map);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(com.android.volley.Request.Method.POST,
                User.api_uri + "user/login", params, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d(User.LOGTAG, response.toString());

                    SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                    prefs.edit().clear().apply();

                    JSONObject obj = response.getJSONObject("response");
                    JSONObject user = obj.getJSONObject("user");
                    JSONObject country = user.getJSONObject("country");
                    JSONObject rank = user.getJSONObject("rank");
                    JSONObject notification = user.getJSONObject("notification");

                    User.userID = user.getInt("id");
                    User.username = user.getString("username");
                    User.email = user.getString("email");
                    User.gender = (user.getInt("gender") == 2) ? "male" : "female";
                    User.birthday = user.getString("birthday");
                    User.birthdayFormatted = User.formatBirthday(User.birthday);
                    User.country = country.getString("name");
                    User.rank = new User.Rank(rank.getInt("rank"), rank.getInt("total_points"), rank.getInt("weekly_points"), rank.getInt("daily_points"), rank.getInt("badge_tastemaker"), rank.getInt("badge_adventurer"), rank.getInt("badge_admirer"), rank.getInt("badge_role_model"), rank.getInt("badge_celebrity"), rank.getInt("badge_idol"));
                    User.notificationSettings = new User.NotificationSettings(notification.getInt("voted_post"), notification.getInt("favorited_post"), notification.getInt("new_follower"));

                    showFeed();
                } catch (Exception e) {
                    e.printStackTrace();
                    showIntro();
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
                showIntro();
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq, "User - Log in");
    }

    public void showFeed(){
        this.finish();
        Intent intent = new Intent(this, FeedController.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_left_half);
    }

    public void crossFade(final Integer position){
        switch (position){
            case 0:
                backgroundImageReplacement.setImageDrawable(drawableShoesDark);
                break;
            case 1:
                backgroundImageReplacement.setImageDrawable(drawableShoesSpot);
                break;
            case 2:
                backgroundImageReplacement.setImageDrawable(drawablePost);
                break;
            case 3:
                backgroundImageReplacement.setImageDrawable(drawableCrown);
                break;
            default:
                backgroundImageReplacement.setImageDrawable(drawableShoesDark);
                break;
        }

        if(position == 0 && logo.getAlpha() != 1.0f)ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f).setDuration(ANIM_SPEED).start();
        else if(logo.getAlpha() == 1.0f && position > 0) ObjectAnimator.ofFloat(logo, "alpha", 1f, 0f).setDuration(ANIM_SPEED).start();

        ObjectAnimator.ofFloat(backgroundImageReplacement, "alpha", 0f, 1f).setDuration(ANIM_SPEED).start();
        ValueAnimator animator = ObjectAnimator.ofFloat(backgroundImageActual, "alpha", 1f, 0f);
        animator.setDuration(ANIM_SPEED);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
            @Override
            public void onAnimationEnd(Animator animation) {
                swapActual(position);
            }
        });
        animator.start();
    }

    public void swapActual(Integer position){
        switch (position){
            case 0:
                backgroundImageActual.setImageDrawable(drawableShoesDark);
                break;
            case 1:
                backgroundImageActual.setImageDrawable(drawableShoesSpot);
                break;
            case 2:
                backgroundImageActual.setImageDrawable(drawablePost);
                break;
            case 3:
                backgroundImageActual.setImageDrawable(drawableCrown);
                break;
            default:
                backgroundImageActual.setImageDrawable(drawableShoesDark);
                break;
        }
        backgroundImageActual.setAlpha(1.0f);
    }

    public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.6f;
        private static final float MIN_ALPHA = 0.4f;

        public void transformPage(View view, float position) {

            if (position < -1) {
                view.setAlpha(0);
            } else if (position <= 1) {
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
            } else {
                view.setAlpha(0);
            }
        }
    }

    public static class TitleFragment extends Fragment {
        public String title = "", textOne = "", textTwo = "";

        public TitleFragment() {}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.intro_title, container, false);

            if (!title.equals("") && !textOne.equals("") && !textTwo.equals("")) {
                final TextView textViewTitle = (TextView)rootView.findViewById(R.id.intro_title);
                textViewTitle.setText(title);
                textViewTitle.setTypeface(User.RalewayMedium);

                final TextView textViewSubtext1 = (TextView)rootView.findViewById(R.id.intro_subtext1);
                textViewSubtext1.setText(textOne);
                textViewSubtext1.setTypeface(User.RalewayRegular);

                final TextView textViewSubtext2 = (TextView)rootView.findViewById(R.id.intro_subtext2);
                textViewSubtext2.setText(textTwo);
                textViewSubtext2.setTypeface(User.RalewayRegular);
            } else Log.i(User.LOGTAG, "Arguments not initialized");

            return rootView;
        }
    }

    public static class NoTitleFragment extends Fragment {
        public String textOne = "", textTwo = "", textThree = "";

        public NoTitleFragment() {}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.intro_no_title, container, false);

            if (!textOne.equals("") && !textTwo.equals("") && !textThree.equals("")) {
                final TextView textViewSubtext1 = (TextView)rootView.findViewById(R.id.intro_subtext1);
                textViewSubtext1.setText(textOne);
                textViewSubtext1.setTypeface(User.RalewayRegular);

                final TextView textViewSubtext2 = (TextView)rootView.findViewById(R.id.intro_subtext2);
                textViewSubtext2.setText(textTwo);
                textViewSubtext2.setTypeface(User.RalewayRegular);

                final TextView textViewSubtext3 = (TextView)rootView.findViewById(R.id.intro_subtext3);
                textViewSubtext3.setText(textThree);
                textViewSubtext3.setTypeface(User.RalewayRegular);
            } else Log.i(User.LOGTAG, "Arguments not initialized");

            return rootView;
        }
    }

    public static class FacebookFragment extends Fragment {
        private ImageView logo;
        private UiLifecycleHelper uiHelper;
        private Session mSession;
        private Session.StatusCallback callback = new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                Log.d(User.LOGTAG, "Running status callback");

                onSessionStateChange(session, state, exception);
            }
        };

        public FacebookFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            uiHelper = new UiLifecycleHelper(getActivity(), callback);
            uiHelper.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.facebook, container, false);

            FrameLayout fbFrameLayout = (FrameLayout)rootView.findViewById(R.id.fbFrameLayout);

            logo = (ImageView)rootView.findViewById(R.id.logo);
            logo.setAlpha(0.0f);
            ObjectAnimator.ofFloat(logo, "alpha", 0.0f, 1.0f).setDuration(User.ANIM_SPEED).start();

//            ValueAnimator animator = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f);
//            animator.setDuration(ANIM_SPEED);
//            animator.addListener(new Animator.AnimatorListener() {
//                @Override
//                public void onAnimationStart(Animator animation) {}
//                @Override
//                public void onAnimationCancel(Animator animation) {}
//                @Override
//                public void onAnimationRepeat(Animator animation) {}
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
//                    String username = prefs.getString(getActivity().getPackageName() + ".username", "");
//                    String password = prefs.getString(getActivity().getPackageName() + ".password", "");
//                    Long facebookID = prefs.getLong(getActivity().getPackageName() + ".facebook_id", 0);
//
////                Log.i(User.LOGTAG, username + " " + password + " " + Long.toString(facebookID));
////                if((username.length() > 0 && password.length() > 0) || facebookID != 0){
////                    autoLogIn(username, password, facebookID);
////                } else {
////                    showIntro = true;
////                    showIntro();
////                }
//
//                    showIntro = true;
//                    showIntro();
//                }
//            });
//            animator.start();

            return null;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        @Override
        public void onDetach() {
            super.onDetach();
        }

        @Override
        public void onResume() {
            super.onResume();

            Session session = Session.getActiveSession();
            if (session != null && (session.isOpened() || session.isClosed()) ) {
                onSessionStateChange(session, session.getState(), null);
            }

            uiHelper.onResume();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            uiHelper.onActivityResult(requestCode, resultCode, data);
        }

        @Override
        public void onPause() {
            super.onPause();
            uiHelper.onPause();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            uiHelper.onDestroy();
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            uiHelper.onSaveInstanceState(outState);
        }

        private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
            if (state.isOpened()) {
                if (mSession == null || isSessionChanged(session)) {
                    mSession = session;
                    Request.newMeRequest(session, new Request.GraphUserCallback() {
                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            if (user != null) {
                                User.storeFacebookInfo(user);

                                Map<String, String> logins = new HashMap<>();
                                logins.put("graph.facebook.com", session.getAccessToken());
                                User.credentialsProvider.setLogins(logins);
                                new GetIdentityID().execute();
                            }
                        }
                    }).executeAsync();
                }
            } else if (state.isClosed()) { Log.i(User.LOGTAG, "Logged out..."); }
        }

        private boolean isSessionChanged(Session session) {
            if (mSession.getState() != session.getState())
                return true;

            if (mSession.getAccessToken() != null) {
                if (!mSession.getAccessToken().equals(session.getAccessToken())) return true;
            } else if (session.getAccessToken() != null) return true;

            return false;
        }

        private class GetIdentityID extends AsyncTask<Void, Void, String> {
            protected String doInBackground(Void... params) {
                User.credentialsProvider.getIdentityId();
                User.transferManager = new TransferManager(User.credentialsProvider);
                Log.d(User.LOGTAG, "Got Identity ID and created transfer manager");
                return "";
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                ((Intro)getActivity()).autoLogIn();
            }
        }
    }

    public void alertDismiss(String messageText, String dismissText){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View alertView = layoutInflater.inflate(R.layout.alert_one_action_no_title, null);

        TextView message = (TextView)alertView.findViewById(R.id.message);
        message.setText(messageText);
        message.setTypeface(User.RalewaySemiBold);

        Button dismissBtn = (Button)alertView.findViewById(R.id.dismissBtn);
        dismissBtn.setText(dismissText);
        dismissBtn.setTypeface(User.RalewaySemiBold);
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
