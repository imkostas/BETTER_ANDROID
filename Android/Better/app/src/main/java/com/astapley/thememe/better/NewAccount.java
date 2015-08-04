package com.astapley.thememe.better;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.s3.transfermanager.TransferProgress;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NewAccount extends FragmentActivity {
    private boolean cameraActive = false;
    private CustomCamera customCamera;
    private NewAccountFragment newAccountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container);

        newAccountFragment = new NewAccountFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.container, newAccountFragment).commit();

        customCamera = new CustomCamera();
    }

    @Override
    public void onBackPressed() {
        if(cameraActive){
            releaseCamera();
        } else {
            User.clearFacebookInfo();
            this.finish();
            overridePendingTransition(R.anim.no_anim, R.anim.slide_out_from_top);
        }
    }

    public void takePhoto(){
        cameraActive = true;
        getSupportFragmentManager().beginTransaction().add(R.id.container, customCamera, "camera").commit();
    }

    public void acceptedImage(Bitmap image){
        newAccountFragment.formatProfileBitmaps(image, true);
        releaseCamera();
    }

    private void releaseCamera(){
        customCamera.releaseCamera();
        cameraActive = false;
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.no_anim, R.anim.fade_out).remove(customCamera).commit();
    }

    public static class NewAccountFragment extends Fragment implements View.OnClickListener {
        private int gender = 0, passwordVisibility = 0;
        private static String birthday = "";

        private static Context context;
        private ImageView profileImageView, backgroundProfileImageView, topBarBackground, femaleImageView, maleImageView, visibilityImageView;
        private EditText usernameEditText, passwordEditText, emailEditText;
        private static TextView dobTextView, countryTextView;
        private NotifyingScrollView notifyingScrollView;
        private FrameLayout profileFrameLayout;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            final View rootView = inflater.inflate(R.layout.profile, container, false);

            context = getActivity().getBaseContext();

            //top bar initialization
            final TextView topBarTitle = (TextView)rootView.findViewById(R.id.topBarTitle);
            topBarTitle.setTypeface(User.RalewaySemiBold);
            topBarTitle.setText(getString(R.string.new_account_title));
            topBarTitle.setOnClickListener(this);
            topBarBackground = (ImageView)rootView.findViewById(R.id.topBarBackground);
            topBarBackground.setAlpha(0.0f);

            //initialize scrollview object);
            notifyingScrollView = (NotifyingScrollView)rootView.findViewById(R.id.notifyingScrollView);
            notifyingScrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
            notifyingScrollView.setFocusable(true);
            notifyingScrollView.setFocusableInTouchMode(true);
            notifyingScrollView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard();
                    v.requestFocusFromTouch();
                    return false;
                }
            });
            notifyingScrollView.setOnScrollChangedListener(OnScrollChangedListener);

            profileFrameLayout = (FrameLayout)rootView.findViewById(R.id.profileFrameLayout);
            backgroundProfileImageView = (ImageView)rootView.findViewById(R.id.backgroundProfileImageView);
            profileImageView = (ImageView)rootView.findViewById(R.id.profileImageView);

            View takePhotoView = rootView.findViewById(R.id.takePhotoView);
            takePhotoView.setOnClickListener(this);

            final TextView rankTextView = (TextView)rootView.findViewById(R.id.rankTextView);
            rankTextView.setTypeface(User.RalewaySemiBold);

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
            passwordEditText.setTypeface(User.RalewaySemiBold);
            passwordEditText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_UP)updateScrollView(1);
                    return false;
                }
            });

            visibilityImageView = (ImageView)rootView.findViewById(R.id.visibilityImageView);
            visibilityImageView.setOnClickListener(this);

            emailEditText = (EditText)rootView.findViewById(R.id.emailEditText);
            emailEditText.setTypeface(User.RalewaySemiBold);
            emailEditText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_UP)updateScrollView(2);
                    return false;
                }
            });

            final TextView IAM = (TextView)rootView.findViewById(R.id.IAM);
            IAM.setTypeface(User.RalewaySemiBold);

            femaleImageView = (ImageView)rootView.findViewById(R.id.femaleImageView);
            femaleImageView.setOnClickListener(this);

            maleImageView = (ImageView)rootView.findViewById(R.id.maleImageView);
            maleImageView.setOnClickListener(this);

            dobTextView = (TextView)rootView.findViewById(R.id.dobTextView);
            dobTextView.setTypeface(User.RobotoMedium);
            dobTextView.setOnClickListener(this);

            final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.countries_array, R.layout.custom_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            final Spinner spinner = (Spinner)rootView.findViewById(R.id.spinner);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    countryTextView.setText(parent.getItemAtPosition(position).toString());
                    if(!countryTextView.getText().equals("COUNTRY")){
                        countryTextView.setTextColor(getResources().getColor(R.color.color_almost_black));
                    } else {
                        countryTextView.setTextColor(getResources().getColor(R.color.color_light_gray));
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            countryTextView = (TextView)rootView.findViewById(R.id.countryTextView);
            countryTextView.setTypeface(User.RalewaySemiBold);
            countryTextView.setTextColor(getResources().getColor(R.color.color_light_gray));
            countryTextView.setText(getResources().getString(R.string.hint_country));

            final Button createAccountButton = (Button)rootView.findViewById(R.id.createAccountButton);
            createAccountButton.setOnClickListener(this);

            if(User.facebookID != 0){
                fetchPhoto();
                if(!User.email.equals(""))emailEditText.setText(User.email);
                birthday = User.birthday;
                updateDOB(User.birthdayFormatted);
                if(User.gender.equals("female")){
                    gender = 1;
                    maleImageView.setImageResource(R.drawable.account_button_female_pressed_125dp);
                } else if(User.gender.equals("male")) {
                    gender = 2;
                    maleImageView.setImageResource(R.drawable.account_button_male_pressed_125dp);
                }

                LinearLayout profileContainer = (LinearLayout)rootView.findViewById(R.id.container);
                LinearLayout passwordLinearLayout = (LinearLayout)rootView.findViewById(R.id.passwordLinearLayout);
                profileContainer.removeView(passwordLinearLayout);
            }

            return rootView;
        }

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.topBarTitle:
                    getActivity().onBackPressed();
                    break;
                case R.id.takePhotoView:
                    ((NewAccount)getActivity()).takePhoto();
                    break;
                case R.id.visibilityImageView:
                    updateVisibility();
                    break;
                case R.id.femaleImageView:
                    updateFemaleState();
                    break;
                case R.id.maleImageView:
                    updateMaleState();
                    break;
                case R.id.dobTextView:
                    new DatePickerFragment().show(getActivity().getSupportFragmentManager(), "datePicker");
                    break;
                case R.id.createAccountButton:
                    createAccount();
                    break;
            }
        }

        private void updateScrollView(int position){
            notifyingScrollView.smoothScrollTo(0, profileFrameLayout.getBottom() - topBarBackground.getHeight());
            switch(position){
                case 0:
                    usernameEditText.requestFocus();
                    break;
                case 1:
                    passwordEditText.requestFocus();
                    break;
                case 2:
                    emailEditText.requestFocus();
                    break;
            }
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

        private void updateFemaleState(){
            switch (gender){
                case 0:
                case 2:
                    gender = 1;
                    femaleImageView.setImageResource(R.drawable.account_button_female_pressed_125dp);
                    maleImageView.setImageResource(R.drawable.account_button_male_125dp);
                    break;
                case 1:
                    gender = 0;
                    femaleImageView.setImageResource(R.drawable.account_button_female_125dp);
                    break;
                default:
                    break;
            }
        }

        private void updateMaleState(){
            switch (gender){
                case 0:
                case 1:
                    gender = 2;
                    femaleImageView.setImageResource(R.drawable.account_button_female_125dp);
                    maleImageView.setImageResource(R.drawable.account_button_male_pressed_125dp);
                    break;
                case 2:
                    gender = 0;
                    maleImageView.setImageResource(R.drawable.account_button_male_125dp);
                    break;
                default:
                    break;
            }
        }

        private void hideSoftKeyboard(){
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(usernameEditText.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(emailEditText.getWindowToken(), 0);
        }

        private NotifyingScrollView.OnScrollChangedListener OnScrollChangedListener = new NotifyingScrollView.OnScrollChangedListener() {
            public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
                final int headerHeight = backgroundProfileImageView.getHeight() - topBarBackground.getHeight();
                final float ratio = (float) Math.min(Math.max(t, 0), headerHeight) / headerHeight;
                topBarBackground.setAlpha(ratio);
                profileFrameLayout.setAlpha(1-ratio);
            }
        };

        private static void updateDOB(String dob){
            dobTextView.setTextColor(context.getResources().getColor(R.color.color_almost_black));
            dobTextView.setTypeface(User.RobotoMedium);
            dobTextView.setTextSize(context.getResources().getDimension(R.dimen.new_account_dob_text_size));
            dobTextView.setText(dob);
        }

        public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
            @Override
            public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setStyle(DialogFragment.STYLE_NORMAL, R.style.MyDialog);
            }

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                // Use the current date as the default date in the picker
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                // Create a new instance of DatePickerDialog and return it
                return new DatePickerDialog(getActivity(), this, year, month, day);
            }

            public void onDateSet(DatePicker view, int year, int month, int day) {
                DecimalFormat decimalFormat = new DecimalFormat("00");
                String formattedMonth = decimalFormat.format(month + 1);
                String formattedDay = decimalFormat.format(day);
                birthday = Integer.toString(year) + "-" + formattedMonth + "-" + formattedDay;
                updateDOB(formattedMonth + "/" + formattedDay + "/" + Integer.toString(year));
            }
        }

        private void createAccount(){
            if(!validParams()){
                ((NewAccount)getActivity()).alertDismiss(getResources().getString(R.string.alert_new_account_more_info), getResources().getString(R.string.alert_got_it));
                return;
            }

            Map<String, String> map = new HashMap<>();
            map.put("api_key", User.api_key);
            map.put("username", usernameEditText.getText().toString());
            map.put("email", emailEditText.getText().toString());
            map.put("gender", String.valueOf(gender));
            map.put("birthday", birthday);
            map.put("country", countryTextView.getText().toString());
            if(User.facebookID != 0){
                map.put("facebook_id", String.valueOf(User.facebookID));
            } else {
                map.put("password", passwordEditText.getText().toString());
            }
            JSONObject params = new JSONObject(map);
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, User.api_uri + "user", params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Log.d(User.LOGTAG, response.toString());

                        SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
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

                        uploadAllProfileBitmaps();

                        showFeed();
                    } catch (Exception e) {
                        e.printStackTrace();
                        ((NewAccount)getActivity()).alertDismiss(getResources().getString(R.string.alert_log_in_general), getResources().getString(R.string.alert_got_it));
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        JSONObject obj = new JSONObject(new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers)));
                        Log.d(User.LOGTAG, obj.toString());
                        if(obj.has("error")){
                            JSONArray errors = obj.getJSONArray("error");
                            ((NewAccount)getActivity()).alertDismiss(errors.get(0).toString(), getResources().getString(R.string.alert_got_it));
                        } else { throw new Exception(); }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ((NewAccount)getActivity()).alertDismiss(getResources().getString(R.string.alert_new_account_general), getResources().getString(R.string.alert_got_it));
                    }
                }
            });
            AppController.getInstance().addToRequestQueue(jsonObjReq, "User - Create");
        }

        private boolean validParams(){
            boolean valid = false;
            if(usernameEditText.getText().length() > 0 && emailEditText.getText().length() > 0 &&
                    (gender == 1 || gender == 2) && dobTextView.getText().length() > 0){
                valid = true;
            }
            if(User.facebookID == 0 && passwordEditText.getText().length() == 0){
                valid = false;
            }
            if(User.smallProfileBitmap == null || User.mediumProfileBitmap == null || User.largeProfileBitmap == null){
                valid = false;
            }

            return valid;
        }

        private void fetchPhoto() {
            ImageRequest imageRequest = new ImageRequest(getResources().getString(R.string.facebook_graph_uri) + User.facebookID + "/picture?height=350&width=350", new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap bitmap) {
                    formatProfileBitmaps(bitmap, false);
                }
            }, 0, 0, null, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.i(User.LOGTAG, "Failed to fetch facebook profile photo");
                }
            });
            AppController.getInstance().addToRequestQueue(imageRequest, "Facebook Profile Image");
        }

        public void updateProfileImage(Bitmap profileImage, Bitmap backgroundImage){
            Bitmap grayScaled = ImageUtils.grayScaleLuminosity(profileImage);
            Drawable mask = context.getResources().getDrawable(R.drawable.ic_cam_circle_account_116dp);
            Bitmap profileBitmap = Bitmap.createBitmap(grayScaled.getWidth(), grayScaled.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas profileCanvas = new Canvas(profileBitmap);
            profileCanvas.drawBitmap(grayScaled, 0, 0, null);
            mask.setBounds(0, 0, profileCanvas.getWidth(), profileCanvas.getHeight());
            mask.draw(profileCanvas);
            profileImageView.setImageBitmap(profileBitmap);

            Drawable tint = context.getResources().getDrawable(R.drawable.drawable_tint);
            Bitmap backgroundBitmap = Bitmap.createBitmap(backgroundImage.getWidth(), backgroundImage.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas backgroundCanvas = new Canvas(backgroundBitmap);
            backgroundCanvas.drawBitmap(backgroundImage, 0, 0, null);
            tint.setBounds(0, 0, backgroundCanvas.getWidth(), backgroundCanvas.getHeight());
            tint.draw(backgroundCanvas);
            backgroundProfileImageView.setImageBitmap(backgroundBitmap);
        }

        private void formatProfileBitmaps(Bitmap image, Boolean rotate){
            User.smallProfileBitmap = ImageUtils.formatImage(ImageUtils.SMALL_PROFILE_DP, image, (rotate)?90:0);
            User.mediumProfileBitmap = ImageUtils.formatImage(ImageUtils.MEDIUM_PROFILE_DP, image, (rotate)?90:0);
            User.largeProfileBitmap = ImageUtils.formatImage(ImageUtils.LARGE_PROFILE_DP, image, (rotate)?90:0);

            updateProfileImage(User.mediumProfileBitmap, User.largeProfileBitmap);
        }

        private void uploadAllProfileBitmaps(){
            uploadImage(User.smallProfileBitmap, ImageUtils.PROFILE_SMALL, User.userID);
            uploadImage(User.mediumProfileBitmap, ImageUtils.PROFILE_MEDIUM, User.userID);
            uploadImage(User.largeProfileBitmap, ImageUtils.PROFILE_LARGE, User.userID);
        }

        private void uploadImage(Bitmap image, String type, int userID){
            if(User.transferManager != null){
                File file = new File(context.getCacheDir(), "temp.png");
                try {
                    file.createNewFile();
                    OutputStream stream = new FileOutputStream(file);
                    image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    stream.flush();
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Upload upload = User.transferManager.upload(User.s3BucketName, "user/" + userID + "_" + type + ".png", file);
                try { upload.waitForUploadResult(); }
                catch (InterruptedException e) { e.printStackTrace(); }

                Log.d(User.LOGTAG, "Successfully uploaded " + type + " user profile image");
            } else {
                Log.d(User.LOGTAG, "Unauthorized user - unable to upload image");
            }
        }

        private void showFeed(){
            getActivity().finish();
            Intent intent = new Intent(getActivity(), FeedController.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_left_half);
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
