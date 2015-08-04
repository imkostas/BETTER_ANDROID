package com.astapley.thememe.better;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SignUp extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_activity);

        getSupportFragmentManager().beginTransaction().add(android.R.id.content, new SignUpFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        this.finish();
        overridePendingTransition(R.anim.no_anim, R.anim.slide_out_from_top);
    }

    public static class SignUpFragment extends Fragment implements View.OnClickListener {
        private UiLifecycleHelper uiHelper;
        private Session mSession;
        private Session.StatusCallback callback = new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                Log.d(User.LOGTAG, "Running status callback");

                onSessionStateChange(session, state, exception);
            }
        };

        public SignUpFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            uiHelper = new UiLifecycleHelper(getActivity(), callback);
            uiHelper.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.sign_up_fragment, container, false);

            Button emailBtn = (Button)rootView.findViewById(R.id.emailBtn);
            emailBtn.setTypeface(User.RalewaySemiBold);
            emailBtn.setOnClickListener(this);

            LinearLayout logInLayout = (LinearLayout)rootView.findViewById(R.id.logInLayout);
            logInLayout.setOnClickListener(this);

            TextView haveAccountTextView = (TextView)rootView.findViewById(R.id.haveAccountTextView);
            haveAccountTextView.setTypeface(User.RalewayRegular);

            TextView logInTextView = (TextView)rootView.findViewById(R.id.logInTextView);
            logInTextView.setTypeface(User.RalewayRegular);

            LoginButton facebookBtn = (LoginButton) rootView.findViewById(R.id.authButton);
            facebookBtn.setTypeface(User.RalewaySemiBold);
            facebookBtn.setFragment(this);
            facebookBtn.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday")); //"user_likes", "user_status"

            return rootView;
        }

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.emailBtn:
                    showSignUp();
                    break;
                case R.id.logInLayout:
                    showLogIn();
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

            Log.d(User.LOGTAG, "Got to onResume");

            Session session = Session.getActiveSession();
            if (session != null && (session.isOpened() || session.isClosed()) ) {
                onSessionStateChange(session, session.getState(), null);
            } else {
                Log.d(User.LOGTAG, "Session null");
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

                                showSignUp();
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
                if (!mSession.getAccessToken().equals(session.getAccessToken())){
                    return true;
                }
            } else if (session.getAccessToken() != null) return true;
            return false;
        }

        private class GetIdentityID extends AsyncTask<Void, Void, Void> {
            protected Void doInBackground(Void... params) {
                User.credentialsProvider.getIdentityId();
                User.transferManager = new TransferManager(User.credentialsProvider);
                Log.d(User.LOGTAG, "Got Identity ID and created transfer manager");
                return null;
            }
        }

        private void showSignUp(){
            Intent intent = new Intent(getActivity(), NewAccount.class);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_anim);
        }

        private void showLogIn(){
            Intent intent = new Intent(getActivity(), LogIn.class);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_anim);
        }
    }
}
