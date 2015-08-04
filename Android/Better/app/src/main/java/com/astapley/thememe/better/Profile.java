package com.astapley.thememe.better;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Profile extends FragmentActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PROFILE_DP = 116, BACKGROUND_DP = 300;
    private CustomCamera customCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container);
        getSupportFragmentManager().beginTransaction().add(R.id.container, new UpdateAccountFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        this.finish();
        overridePendingTransition(R.anim.slide_in_from_left_half, R.anim.slide_out_right);
    }

    public void takePhoto(){
        customCamera = new CustomCamera();
        getSupportFragmentManager().beginTransaction().add(R.id.container, customCamera, "camera").commit();

    }

    public void hideCamera(Bitmap image){
        Bitmap updateImage = ImageUtils.formatImage(PROFILE_DP, image, 90);
        Bitmap backgroundImage = ImageUtils.formatImage(BACKGROUND_DP, image, 90);
        UpdateAccountFragment.updateProfileImage(updateImage, backgroundImage);

        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.no_anim, R.anim.fade_out).remove(customCamera).commit();
    }

    public static class UpdateAccountFragment extends Fragment implements View.OnClickListener {
        private int gender = 0;
        private static String birthday = User.birthday, formattedBirthday = User.birthdayFormatted;

        private static Context context;
        private static ImageView updateImageView, backgroundProfileImageView;
        private ImageView topBarBackground, femaleImageView, maleImageView, visibilityImageView;
        private TextView usernameTextView;
        private static TextView birthdayTextView, countryTextView;
        private NotifyingScrollView notifyingScrollView;
        private FrameLayout updateFrameLayout;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
            final View rootView = inflater.inflate(R.layout.update, container, false);

            context = getActivity().getBaseContext();

            //top bar initialization
            final TextView topBarTitle = (TextView)rootView.findViewById(R.id.topBarTitle);
            topBarTitle.setTypeface(User.RalewaySemiBold);
            topBarTitle.setText(getString(R.string.profile_title));
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
                    v.requestFocusFromTouch();
                    return false;
                }
            });
            notifyingScrollView.setOnScrollChangedListener(OnScrollChangedListener);

            updateFrameLayout = (FrameLayout)rootView.findViewById(R.id.updateFrameLayout);
            backgroundProfileImageView = (ImageView)rootView.findViewById(R.id.backgroundProfileImageView);
            updateImageView = (ImageView)rootView.findViewById(R.id.updateImageView);

            View takePhotoView = rootView.findViewById(R.id.takePhotoView);
            takePhotoView.setOnClickListener(this);

            usernameTextView = (TextView)rootView.findViewById(R.id.usernameTextView);
            usernameTextView.setText(User.username.toUpperCase());
            usernameTextView.setTypeface(User.RobotoMedium);

            final TextView IAM = (TextView)rootView.findViewById(R.id.IAM);
            IAM.setTypeface(User.RalewaySemiBold);

            femaleImageView = (ImageView)rootView.findViewById(R.id.femaleImageView);
            femaleImageView.setOnClickListener(this);

            maleImageView = (ImageView)rootView.findViewById(R.id.maleImageView);
            maleImageView.setOnClickListener(this);

            birthdayTextView = (TextView)rootView.findViewById(R.id.birthdayTextView);
            birthdayTextView.setText(User.birthdayFormatted);
            birthdayTextView.setTypeface(User.RobotoMedium);
            birthdayTextView.setOnClickListener(this);

            final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.countries_array, R.layout.custom_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            final Spinner spinner = (Spinner)rootView.findViewById(R.id.spinner);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    countryTextView.setText(parent.getItemAtPosition(position).toString());
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            countryTextView = (TextView)rootView.findViewById(R.id.countryTextView);
            countryTextView.setText(User.country);
            countryTextView.setTypeface(User.RalewaySemiBold);

            final Button updateAccountButton = (Button)rootView.findViewById(R.id.updateAccountButton);
            updateAccountButton.setOnClickListener(this);

            if(User.profileImage != null && User.profileBackground != null){
                updateImageView.setImageBitmap(User.profileImage);
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

                updateImageView.setImageBitmap(bitmap);
                backgroundProfileImageView.setImageResource(R.drawable.account_profile_panel_empty);
            }

            if(User.mediumProfileBitmap != null) updateProfileImage(User.mediumProfileBitmap, User.largeProfileBitmap);

            if(User.gender.equals("female")){
                gender = 1;
                femaleImageView.setImageResource(R.drawable.account_button_female_pressed_125dp);
                maleImageView.setImageResource(R.drawable.account_button_male_125dp);
            } else if(User.gender.equals("male")) {
                gender = 2;
                femaleImageView.setImageResource(R.drawable.account_button_female_125dp);
                maleImageView.setImageResource(R.drawable.account_button_male_pressed_125dp);
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
//                    ((Profile)getActivity()).takePhoto();
                    break;
                case R.id.femaleImageView:
                    updateFemaleState();
                    break;
                case R.id.maleImageView:
                    updateMaleState();
                    break;
                case R.id.birthdayTextView:
                    new DatePickerFragment().show(getActivity().getSupportFragmentManager(), "datePicker");
                    break;
                case R.id.updateAccountButton:
                    updateUser();
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

        private NotifyingScrollView.OnScrollChangedListener OnScrollChangedListener = new NotifyingScrollView.OnScrollChangedListener() {
            public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
                final int headerHeight = backgroundProfileImageView.getHeight() - topBarBackground.getHeight();
                final float ratio = (float) Math.min(Math.max(t, 0), headerHeight) / headerHeight;
                topBarBackground.setAlpha(ratio);
                updateFrameLayout.setAlpha(1-ratio);
            }
        };

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
                formattedBirthday = formattedMonth + "/" + formattedDay + "/" + Integer.toString(year);
                birthdayTextView.setText(formattedBirthday);
            }
        }

        public static void updateProfileImage(Bitmap updateImage, Bitmap backgroundImage){
            Bitmap grayScaled = ImageUtils.grayScaleLuminosity(updateImage);
            Drawable mask = context.getResources().getDrawable(R.drawable.ic_cam_circle_account_116dp);
            Bitmap updateBitmap = Bitmap.createBitmap(grayScaled.getWidth(), grayScaled.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas updateCanvas = new Canvas(updateBitmap);
            updateCanvas.drawBitmap(grayScaled, 0, 0, null);
            mask.setBounds(0, 0, updateCanvas.getWidth(), updateCanvas.getHeight());
            mask.draw(updateCanvas);
            updateImageView.setImageBitmap(updateBitmap);

            Drawable tint = context.getResources().getDrawable(R.drawable.drawable_tint);
            Bitmap backgroundBitmap = Bitmap.createBitmap(backgroundImage.getWidth(), backgroundImage.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas backgroundCanvas = new Canvas(backgroundBitmap);
            backgroundCanvas.drawBitmap(backgroundImage, 0, 0, null);
            tint.setBounds(0, 0, backgroundCanvas.getWidth(), backgroundCanvas.getHeight());
            tint.draw(backgroundCanvas);
            backgroundProfileImageView.setImageBitmap(backgroundBitmap);
        }

        private void updateUser(){
            if(!needToUpdate()){
                ((Profile)getActivity()).alertDismiss(getResources().getString(R.string.profile_update_my_account_successful), getResources().getString(R.string.alert_got_it));
                return;
            }

            Map<String, String> map = new HashMap<>();
            map.put("api_key", User.api_key);
            map.put("user_id", Integer.toString(User.userID));
            map.put("birthday", birthday);
            map.put("gender", Integer.toString(gender));

            JSONObject params = new JSONObject(map);
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    User.api_uri + "user/update", params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(User.LOGTAG, response.toString());
                    ((Profile)getActivity()).alertDismiss(getResources().getString(R.string.profile_update_my_account_successful), getResources().getString(R.string.alert_got_it));

                    User.birthday = birthday;
                    User.birthdayFormatted = formattedBirthday;
                    User.gender = (gender == 1)?"female":"male";
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        JSONObject obj = new JSONObject(new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers)));
                        Log.d(User.LOGTAG, obj.toString());
                        if(obj.has("error")){
                            JSONArray errors = obj.getJSONArray("error");
                            ((Profile)getActivity()).alertDismiss(errors.get(0).toString(), getResources().getString(R.string.alert_got_it));
                        } else { throw new Exception(); }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            AppController.getInstance().addToRequestQueue(jsonObjReq, "User - Update");
        }

        private boolean needToUpdate(){
            boolean valid = false;

            int genderOrig = User.gender.equals("female")?1:2;
            if(gender != genderOrig || !User.birthday.equals(birthday)){
                valid = true;
            }
            return valid;
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
