package com.astapley.thememe.better;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LogIn extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in_activity);
        if(savedInstanceState == null)getSupportFragmentManager().beginTransaction().add(R.id.container, new LogInFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        User.clearFacebookInfo();
        this.finish();
        overridePendingTransition(R.anim.no_anim, R.anim.slide_out_from_top);
    }

    public static class LogInFragment extends Fragment implements View.OnClickListener {
        private int passwordVisibility = 0;
        private ImageView visibilityImageView;
        private EditText usernameEditText, passwordEditText;
        private LoginButton facebookBtn;
        private LinearLayout layoutContainer;
        private NotifyingScrollView notifyingScrollView;
        private UiLifecycleHelper uiHelper;
        private Session mSession;

        private Session.StatusCallback callback = new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) { onSessionStateChange(session, state, exception); }
        };

        public LogInFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            uiHelper = new UiLifecycleHelper(getActivity(), callback);
            uiHelper.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.log_in_fragment, container, false);

            layoutContainer = (LinearLayout)rootView.findViewById(R.id.container);
            layoutContainer.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
            layoutContainer.setFocusableInTouchMode(true);

            notifyingScrollView = (NotifyingScrollView)rootView.findViewById(R.id.notifyingScrollView);
            notifyingScrollView.setSmoothScrollingEnabled(true);
//            notifyingScrollView.setOnScrollChangedListener(OnScrollChangedListener);

            usernameEditText = (EditText)rootView.findViewById(R.id.usernameEditText);
            usernameEditText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            usernameEditText.setTypeface(User.RobotoMedium);
            usernameEditText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_UP)updateScrollView(0);
                    return false;
                }
            });

            passwordEditText = (EditText)rootView.findViewById(R.id.passwordEditText);
            passwordEditText.setTypeface(User.RobotoMedium);
            passwordEditText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_UP)updateScrollView(1);
                    return false;
                }
            });

            final TextView forgotTextView = (TextView)rootView.findViewById(R.id.forgotTextView);
            forgotTextView.setTypeface(User.RalewayRegular);
            forgotTextView.setOnClickListener(this);

            final TextView orTextView = (TextView)rootView.findViewById(R.id.orTextView);
            orTextView.setTypeface(User.RalewayRegular);

            visibilityImageView = (ImageView)rootView.findViewById(R.id.visibilityImageView);
            visibilityImageView.setOnClickListener(this);

            final Button logInBtn = (Button)rootView.findViewById(R.id.logInBtn);
            logInBtn.setTypeface(User.RalewaySemiBold);
            logInBtn.setOnClickListener(this);

            facebookBtn = (LoginButton)rootView.findViewById(R.id.authButton);
            facebookBtn.setTypeface(User.RalewaySemiBold);
            facebookBtn.setFragment(this);
            facebookBtn.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday")); //"user_likes", "user_status"

            return rootView;
        }

        private void updateVisibility(){
            switch (passwordVisibility){
                case 0:
                    passwordVisibility = 1;
                    passwordEditText.setTransformationMethod(null);
                    visibilityImageView.setImageResource(R.drawable.ic_visibility_on_green_24dp);
                    break;
                case 1:
                default:
                    passwordVisibility = 0;
                    passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
                    visibilityImageView.setImageResource(R.drawable.ic_visibility_off_grey600_24dp);
                    break;
            }
        }

//        private NotifyingScrollView.OnScrollChangedListener OnScrollChangedListener = new NotifyingScrollView.OnScrollChangedListener() {
//            public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
//                notifyingScrollView.smoothScrollTo(0, 0);
//            }
//        };
//
        private void updateScrollView(int position){
            notifyingScrollView.smoothScrollTo(0, layoutContainer.getBottom());
//            switch(position){
//                case 0:
//                    usernameEditText.requestFocus();
//                    break;
//                case 1:
//                    passwordEditText.requestFocus();
//                    break;
//            }
        }

        private boolean validParams(){
            return (usernameEditText.length() > 0 && passwordEditText.length() > 0);
        }

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.visibilityImageView:
                    updateVisibility();
                    break;
                case R.id.logInBtn:
                    logIn();
//                    showFeed();
                    break;
                case R.id.forgotTextView:
                    showForgotPassword();
                    break;
            }
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
            } else if (state.isClosed()) {
                Log.i(User.LOGTAG, "Logged out...");
            }
        }

        private boolean isSessionChanged(Session session) {
            if (mSession.getState() != session.getState())
                return true;

            if (mSession.getAccessToken() != null) {
                if (!mSession.getAccessToken().equals(session.getAccessToken()))
                    return true;
            } else if (session.getAccessToken() != null) {
                return true;
            }
            return false;
        }

        private class GetIdentityID extends AsyncTask<Void, Void, Void> {
            protected Void doInBackground(Void... params) {
                User.credentialsProvider.getIdentityId();
                User.transferManager = new TransferManager(User.credentialsProvider);
                Log.d(User.LOGTAG, "Got Identity ID and created transfer manager");

                if (User.facebookID != 0) {
                    logIn();
                } else {
                    ((LogIn)getActivity()).alertDismiss(getActivity().getResources().getString(R.string.alert_log_in_facebook_failed), getActivity().getResources().getString(R.string.alert_try_again));
                }

                return null;
            }
        }

        private void logIn(){
            if(User.facebookID == 0 && !validParams()){
                ((LogIn)getActivity()).alertDismiss(getActivity().getResources().getString(R.string.alert_log_in_more_info), getActivity().getResources().getString(R.string.alert_got_it));
                return;
            }

            Map<String, String> map = new HashMap<>();
            map.put("api_key", User.api_key);
            if(User.facebookID != 0){
                map.put("facebook_id", String.valueOf(User.facebookID));
            } else {
                map.put("username", usernameEditText.getText().toString());
                map.put("password", passwordEditText.getText().toString());
            }
            JSONObject params = new JSONObject(map);
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(com.android.volley.Request.Method.POST,
                    User.api_uri + "user/login", params, new com.android.volley.Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Log.d(User.LOGTAG, response.toString());

                        SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
                        Log.d(User.LOGTAG, prefs.getAll().toString());
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

                        prefs.edit().putString(getActivity().getPackageName() + ".username", User.username).apply();
                        if(User.facebookID != 0){
                            prefs.edit().putLong(getActivity().getPackageName() + ".facebook_id", User.facebookID).apply();
                        } else {
                            prefs.edit().putString(getActivity().getPackageName() + ".password", user.getString("password")).apply();
                        }
                        showFeed();
                    } catch (Exception e) {
                        e.printStackTrace();
                        ((LogIn)getActivity()).alertDismiss(getActivity().getResources().getString(R.string.alert_log_in_general), getActivity().getResources().getString(R.string.alert_got_it));
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
                            ((LogIn)getActivity()).alertDismiss(errors.get(0).toString(), getResources().getString(R.string.alert_got_it));
                        } else { throw new Exception(); }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ((LogIn)getActivity()).alertDismiss(getResources().getString(R.string.alert_log_in_general), getResources().getString(R.string.alert_got_it));
                    }
                }
            });
            AppController.getInstance().addToRequestQueue(jsonObjReq, "User - Log in");
        }

        private void showFeed(){
            getActivity().finish();
            Intent intent = new Intent(getActivity(), FeedController.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_left_half);
        }

        private void showForgotPassword(){
            Intent intent = new Intent(getActivity(), ForgotPassword.class);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_anim);
        }
    }

    public void alertTitleDismiss(String titleText, String messageText, String dismissText){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View alertView = layoutInflater.inflate(R.layout.alert_one_action_with_title, null);

        TextView title = (TextView)alertView.findViewById(R.id.title);
        title.setText(titleText);
        title.setTypeface(User.RalewaySemiBold);

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

    public void alertAction(String messageText, String dismissText, String actionText){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View alertView = layoutInflater.inflate(R.layout.alert_two_action_no_title, null);

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

        Button actionBtn = (Button)alertView.findViewById(R.id.actionBtn);
        actionBtn.setText(actionText);
        actionBtn.setTypeface(User.RalewaySemiBold);
        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Delete tag here", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }
        });

        alertDialog.setView(alertView);
        alertDialog.show();
    }
}
