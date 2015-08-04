package com.astapley.thememe.better;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.s3.transfermanager.TransferProgress;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.apmem.tools.layouts.FlowLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Post extends FragmentActivity implements View.OnClickListener {
    private static boolean cameraActive = false, editingTag = false;
    private static int activeLayout, hotspottedLayout, viewPagerState, activeImage, activeHotspot;
    private String hotspotOneText = "", hotspotTwoText = "";
    private Bitmap bitmapOne = null, bitmapTwo = null;

    private InputMethodManager imm;
    public static EditText hashtagEditText;
    private Button postBtn;
    private LinearLayout addTagLinearLayout;
    private FrameLayout container;
    private ScrollView scrollView;
    private CustomCamera customCamera;
    private CustomViewPager customViewPager;
    private PostTag postTag;
    private LayoutFragment layoutFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post);

        final View rootView = findViewById(R.id.rootView);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int heightDiff = rootView.getRootView().getHeight() - (r.bottom - r.top);
                if (heightDiff > 100)editingTag = true;
                else if(heightDiff <= 100)if(editingTag)hideTagEditor(false);
            }
        });

        imm = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);

        customCamera = new CustomCamera();
        if(savedInstanceState == null)showCamera();

        //top bar initialization
        final TextView topBarTitle = (TextView)findViewById(R.id.topBarTitle);
        topBarTitle.setTypeface(User.RalewaySemiBold);
        topBarTitle.setText(getString(R.string.post_title));
        topBarTitle.setOnClickListener(this);

        scrollView = (ScrollView)findViewById(R.id.scrollView);

        postBtn = (Button)findViewById(R.id.postBtn);
        postBtn.setTypeface(User.RalewaySemiBold);
        postBtn.setOnClickListener(this);

        final PagerAdapter viewPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        customViewPager = (CustomViewPager)findViewById(R.id.pager);
        customViewPager.setAdapter(viewPagerAdapter);
        customViewPager.setPagingEnabled(false);

        addTagLinearLayout = (LinearLayout)findViewById(R.id.addTagLinearLayout);
        addTagLinearLayout.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        addTagLinearLayout.setFocusable(true);
        addTagLinearLayout.setFocusableInTouchMode(true);
        addTagLinearLayout.setVisibility(View.GONE);
        addTagLinearLayout.setAlpha(0.0f);

        hashtagEditText = (EditText)findViewById(R.id.hashtagEditText);
        hashtagEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) { if(!hasFocus){
                if(viewPagerState == 2) hideAddTagEditor(false);
                else hideTagEditor(false);
            } }
        });

        final ImageView setTagImageView = (ImageView)findViewById(R.id.setTagImageView);
        setTagImageView.setOnClickListener(this);

        final View addTagView = findViewById(R.id.addTagView);
        addTagView.setOnClickListener(this);

        container = (FrameLayout)findViewById(R.id.container);
        container.post(new Runnable() {
            @Override
            public void run() {
                layoutUISubviews();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(cameraActive){
            releaseCamera();
        } else {
            switch(viewPagerState){
                case 0:
                    alertAction(getResources().getString(R.string.post_quit_alert), getResources().getString(R.string.alert_cancel), getResources().getString(R.string.alert_quit));
                    break;
                case 1:
                    layoutFragment.showLayoutTemplate();
                    break;
                case 2:
                    postBtn.setText(getResources().getString(R.string.post_next));
                    customViewPager.setCurrentItem(0);
                    break;
            }
            if(viewPagerState > 0)viewPagerState--;
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.topBarTitle:
                onBackPressed();
                break;
            case R.id.postBtn:
                updatePostState();
                break;
            case R.id.setTagImageView:
                if(viewPagerState==2) { if (hashtagEditText.getText().length() > 0) hideAddTagEditor(true); }
                else { if(hashtagEditText.getText().length() > 0) hideTagEditor(true); }
                break;
            case R.id.addTagView:
                hideTagEditor(false);
                break;
        }
    }

    private void quitPosting(){
        this.finish();
        overridePendingTransition(R.anim.no_anim, R.anim.slide_out_from_top);
    }

    private void layoutUISubviews(){
        viewPagerState = 0;
        activeLayout = 0;
        hotspottedLayout = -1;
        activeImage = 0;
        activeHotspot = -1;
        hotspotOneText = "";
        hotspotTwoText = "";

        customViewPager.getLayoutParams().height = container.getHeight() - ImageUtils.dpToPx(112);
    }

    private void showTagEditor(int selectedHotspot, final int hotspotY){
        activeHotspot = selectedHotspot;
        switch(activeHotspot){
            case 0:
                if(!hotspotOneText.equals(""))hashtagEditText.setText(hotspotOneText);
                break;
            case 1:
                if(!hotspotTwoText.equals(""))hashtagEditText.setText(hotspotTwoText);
                break;
        }

        hashtagEditText.requestFocus();
        imm.toggleSoftInputFromWindow(hashtagEditText.getWindowToken(), InputMethodManager.SHOW_FORCED, 0);

        addTagLinearLayout.setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(addTagLinearLayout, "alpha", 0.0f, 1.0f).setDuration(User.ANIM_SPEED).start();

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.smoothScrollTo(0, hotspotY);
            }
        });
    }

    private void hideTagEditor(boolean setTag){
        editingTag = false;
        imm.hideSoftInputFromWindow(hashtagEditText.getWindowToken(), 0);

        if(setTag){
            switch(activeHotspot){
                case 0:
                    if(hashtagEditText.getText().length() > 0){
                        hotspotOneText = hashtagEditText.getText().toString();
                        layoutFragment.updateHotspotImageViewOne(getResources().getDrawable(R.drawable.global_hotspot_blank_tag_96dp));
                    }
                    break;
                case 1:
                    if(hashtagEditText.getText().length() > 0){
                        hotspotTwoText = hashtagEditText.getText().toString();
                        layoutFragment.updateHotspotImageViewTwo(getResources().getDrawable(R.drawable.global_hotspot_blank_tag_96dp));
                    }
                    break;
            }
        }

        addTagLinearLayout.setAlpha(0.0f);
        hashtagEditText.setText("");
        activeHotspot = -1;
        addTagLinearLayout.setVisibility(View.GONE);
    }

    private void showAddTagEditor() {
        hashtagEditText.requestFocus();
        imm.toggleSoftInputFromWindow(hashtagEditText.getWindowToken(), InputMethodManager.SHOW_FORCED, 0);
        addTagLinearLayout.setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(addTagLinearLayout, "alpha", 0.0f, 1.0f).setDuration(User.ANIM_SPEED).start();
    }

    private void hideAddTagEditor(boolean addTag) {
        if(addTag){
            String tag = hashtagEditText.getText().toString();
            postTag.addTag(tag, true);
        }

        imm.hideSoftInputFromWindow(hashtagEditText.getWindowToken(), 0);
        addTagLinearLayout.setAlpha(0.0f);
        hashtagEditText.setText("");
        addTagLinearLayout.setVisibility(View.GONE);
    }

    private void updatePostState(){
        switch(viewPagerState){
            case 0:
                if(activeLayout == 0 && bitmapOne == null){
                    alertDismiss(getResources().getString(R.string.post_missing_image_single_layout), getResources().getString(R.string.alert_got_it));
                } else if(activeLayout > 0 && (bitmapOne == null || bitmapTwo == null)) {
                    alertDismiss(getResources().getString(R.string.post_missing_image_multi_layout), getResources().getString(R.string.alert_got_it));
                } else {
                    viewPagerState++;
                    layoutFragment.hideLayoutTemplate();
                }
                break;
            case 1:
                if(activeLayout == 0 && layoutFragment.checkHotspotCollision()){
                    alertDismiss(getResources().getString(R.string.post_hotspots_overlap), getResources().getString(R.string.alert_got_it));
                } else if(hotspotOneText.equals("") || hotspotTwoText.equals("")) {
                    alertDismiss(getResources().getString(R.string.post_hotspot_missing_tag), getResources().getString(R.string.alert_got_it));
                } else if(hotspotOneText.equals(hotspotTwoText)){
                    alertDismiss(getResources().getString(R.string.post_hotspots_same_tag), getResources().getString(R.string.alert_got_it));
                } else {
                    if(postTag.postTags.size() > 2){
                        postTag.postTags.get(0).tag = hotspotOneText;
                        postTag.postTags.get(1).tag = hotspotTwoText;
                        postTag.reAddTags();
                    } else {
                        postTag.clearTags();
                        postTag.addTag(hotspotOneText, false);
                        postTag.addTag(hotspotTwoText, false);
                    }

                    postBtn.setText(getResources().getString(R.string.post_text));
                    customViewPager.setCurrentItem(1);
                    viewPagerState++;
                }
                break;
            case 2:
                post(activeLayout, ImageUtils.pxToDP(layoutFragment.hotspotOneImageView.getX()), ImageUtils.pxToDP(layoutFragment.hotspotOneImageView.getY()), ImageUtils.pxToDP(layoutFragment.hotspotTwoImageView.getX()), ImageUtils.pxToDP(layoutFragment.hotspotTwoImageView.getY()), stringifyHashtags());
                break;
        }
    }

    private String stringifyHashtags(){
        String hashtags = "";
        for(PostTag.Tag tag : postTag.postTags) hashtags += tag.tag + " ";
        return hashtags.trim();
    }

    private void post(final int layout, int hotspotOneX, int hotspotOneY, int hotspotTwoX, int hotspotTwoY, String hashtags){
        Map<String, String> map = new HashMap<>();
        map.put("api_key", User.api_key);
        map.put("user_id", Integer.toString(User.userID));
        map.put("layout", Integer.toString(layout));
        map.put("hotspot_one_x", Integer.toString(hotspotOneX));
        map.put("hotspot_one_y", Integer.toString(hotspotOneY));
        map.put("hotspot_two_x", Integer.toString(hotspotTwoX));
        map.put("hotspot_two_y", Integer.toString(hotspotTwoY));
        map.put("hashtags", hashtags);

        Log.d(User.LOGTAG, map.toString());

        JSONObject params = new JSONObject(map);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, User.api_uri + "post", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(User.LOGTAG, response.toString());

                try {
                    int postID = response.getJSONObject("response").getJSONObject("post").getInt("id");
                    uploadImage(bitmapOne, postID, 1);
                    if(layout > 0)uploadImage(bitmapTwo, postID, 2);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(bitmapOne != null) bitmapOne.recycle();
                if(bitmapTwo != null) bitmapTwo.recycle();

                User.filter.hasChanged = true;
                quitPosting();
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
            }
        });
        AppController.getInstance().addToRequestQueue(jsonObjReq, "Post - Create");
    }

    public void acceptedImage(Bitmap bitmap){
        switch(activeImage){
            case 0:
                bitmapOne = ImageUtils.rotateImage(bitmap, 90);
                layoutFragment.updateImageViewOne(bitmapOne);
                break;
            case 1:
                bitmapTwo = ImageUtils.rotateImage(bitmap, 90);
                layoutFragment.updateImageViewTwo(bitmapTwo);
                break;
        }
        releaseCamera();
    }

    private void uploadImage(Bitmap image, int postID, int position){
        if(User.transferManager != null){
            File file = new File(getCacheDir(), "temp.png");
            try {
                file.createNewFile();
                OutputStream stream = new FileOutputStream(file);
                image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.flush();
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Upload upload = User.transferManager.upload(User.s3BucketName, "post/" + postID + "_" + position + ".png", file);
            try { upload.waitForUploadResult(); }
            catch (InterruptedException e) { e.printStackTrace(); }

            Log.d(User.LOGTAG, "Successfully uploaded post image " + position);
        } else {
            Log.d(User.LOGTAG, "Unauthorized user - unable to upload image");
        }
    }

    private void setBitmapOne(Bitmap bitmap){
        bitmapOne = bitmap;
    }

    private void setBitmapTwo(Bitmap bitmap){
        bitmapTwo = bitmap;
    }

    private void releaseCamera(){
        customCamera.releaseCamera();
        cameraActive = false;
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.no_anim, R.anim.fade_out).remove(customCamera).commit();
    }

    public void showCamera(){
        cameraActive = true;
        getSupportFragmentManager().beginTransaction().add(R.id.container, customCamera, "camera").commit();
    }

    private class PagerAdapter extends FragmentPagerAdapter {
        public PagerAdapter(FragmentManager fm) { super(fm); }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    layoutFragment = new LayoutFragment();
                    return layoutFragment;
                case 1:
                default:
                    postTag = new PostTag();
                    return postTag;

            }
        }

        @Override
        public int getCount() { return 2; }
    }

    public static class LayoutFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {
        private final int imageSpacing = 1;

        private boolean moved = false;
        private int activePointerId = -1;
        private float initialTouchX, initialTouchY, initialHotspotX, initialHotspotY;
        private float originalOneZoom = -1.0f, originalTwoZoom = -1.0f;
        private PointF originalOneScrollPosition, originalTwoScrollPosition;

        private TextView hotSpotTextView1, hotSpotTextView2;
        private ImageView singleLayoutImageView, verticalLayoutImageView, horizontalLayoutImageView;
        private ImageView addImageViewOne, addImageViewTwo, hotspotOneImageView, hotspotTwoImageView;
        private TouchImageView imageViewOne, imageViewTwo;
        private FrameLayout imageViewOneLayout, imageViewTwoLayout;
        private FrameLayout imageLayoutContainer;
        private LinearLayout layoutTypeContainer, hotSpotContainer;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.post_layout, container, false);

            imageViewOneLayout = (FrameLayout)rootView.findViewById(R.id.imageViewOneLayout);
            imageViewTwoLayout = (FrameLayout)rootView.findViewById(R.id.imageViewTwoLayout);

            addImageViewOne = (ImageView)rootView.findViewById(R.id.addImageViewOne);
            addImageViewTwo = (ImageView)rootView.findViewById(R.id.addImageViewTwo);

            imageViewOne = (TouchImageView)rootView.findViewById(R.id.imageViewOne);
            imageViewOne.setDrawingCacheEnabled(true);
            imageViewOne.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            imageViewOne.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageViewOne.setOnClickListener(this);

            imageViewTwo = (TouchImageView)rootView.findViewById(R.id.imageViewTwo);
            imageViewTwo.setDrawingCacheEnabled(true);
            imageViewTwo.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            imageViewTwo.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageViewTwo.setOnClickListener(this);

            hotspotOneImageView = (ImageView)rootView.findViewById(R.id.hotspotOneImageView);
            hotspotOneImageView.setAlpha(0.0f);
            hotspotOneImageView.setOnTouchListener(this);
            hotspotOneImageView.setOnClickListener(this);

            hotspotTwoImageView = (ImageView)rootView.findViewById(R.id.hotspotTwoImageView);
            hotspotTwoImageView.setAlpha(0.0f);
            hotspotTwoImageView.setOnTouchListener(this);
            hotspotTwoImageView.setOnClickListener(this);

            layoutTypeContainer = (LinearLayout)rootView.findViewById(R.id.layoutTypeContainer);
            layoutTypeContainer.setAlpha(1.0f);

            hotSpotContainer = (LinearLayout)rootView.findViewById(R.id.hotSpotContainer);
            hotSpotContainer.setAlpha(0.0f);

            hotSpotTextView1 = (TextView)rootView.findViewById(R.id.hotSpotTextView1);
            hotSpotTextView1.setTypeface(User.RalewayMedium);

            hotSpotTextView2 = (TextView)rootView.findViewById(R.id.hotSpotTextView2);
            hotSpotTextView2.setTypeface(User.RalewayMedium);

            singleLayoutImageView = (ImageView)rootView.findViewById(R.id.singleLayoutImageView);
            singleLayoutImageView.setOnClickListener(this);

            verticalLayoutImageView = (ImageView)rootView.findViewById(R.id.verticalLayoutImageView);
            verticalLayoutImageView.setOnClickListener(this);

            horizontalLayoutImageView = (ImageView)rootView.findViewById(R.id.horizontalLayoutImageView);
            horizontalLayoutImageView.setOnClickListener(this);

            imageLayoutContainer = (FrameLayout)rootView.findViewById(R.id.imageLayoutContainer);
            imageLayoutContainer.post(new Runnable() {
                @Override
                public void run() {
                    updateUISubviews();
                }
            });

            return rootView;
        }

        private void updateUISubviews(){
            // Initialize layout frame square
            imageLayoutContainer.getLayoutParams().width = User.windowSize.x;
            imageLayoutContainer.getLayoutParams().height = imageLayoutContainer.getLayoutParams().width;

            // Initialize with single image layout
            imageViewOneLayout.getLayoutParams().width = User.windowSize.x;
            imageViewOneLayout.getLayoutParams().height = User.windowSize.x;
            imageViewTwoLayout.setPadding(User.windowSize.x, 0, 0, 0);

            enableHotspots(false);
        }

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.singleLayoutImageView:
                    setSingleLayout();
                    break;
                case R.id.verticalLayoutImageView:
                    setVerticalLayout();
                    break;
                case R.id.horizontalLayoutImageView:
                    setHorizontalLayout();
                    break;
                case R.id.imageViewOne:
                    if(viewPagerState == 0){
                        activeImage = 0;
                        ((Post)getActivity()).showCamera();
                    }
                    break;
                case R.id.imageViewTwo:
                    if(viewPagerState == 0){
                        activeImage = 1;
                        ((Post)getActivity()).showCamera();
                    }
                    break;
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final int action = MotionEventCompat.getActionMasked(event);

            switch(action){
                case MotionEvent.ACTION_DOWN: {
                    moved = false;
                    final int pointerIndex = MotionEventCompat.getActionIndex(event);
                    initialTouchX = MotionEventCompat.getX(event, pointerIndex);
                    initialTouchY = MotionEventCompat.getY(event, pointerIndex);
                    activePointerId = MotionEventCompat.getPointerId(event, 0);
                    switch(v.getId()){
                        case R.id.hotspotOneImageView:
                            initialHotspotX = hotspotOneImageView.getX();
                            initialHotspotY = hotspotOneImageView.getY();
                            break;
                        case R.id.hotspotTwoImageView:
                            initialHotspotX = hotspotTwoImageView.getX();
                            initialHotspotY = hotspotTwoImageView.getY();
                            break;
                    }
                    break;
                }

                case MotionEvent.ACTION_UP: {
                    switch(v.getId()){
                        case R.id.hotspotOneImageView:
                            if(!moved && clickedHotspot(initialHotspotX, hotspotOneImageView.getX(), initialHotspotY, hotspotOneImageView.getY())){
                                ((Post)getActivity()).showTagEditor(0, (int)hotspotOneImageView.getY());
                            }
                            break;
                        case R.id.hotspotTwoImageView:
                            if(!moved && clickedHotspot(initialHotspotX, hotspotTwoImageView.getX(), initialHotspotY, hotspotTwoImageView.getY())){
                                ((Post)getActivity()).showTagEditor(1, (int)hotspotTwoImageView.getY());
                            }
                            break;
                    }
                    activePointerId = -1;
                    break;
                }

                case MotionEvent.ACTION_MOVE: {
                    final int pointerIndex = MotionEventCompat.findPointerIndex(event, activePointerId);
                    final float dx = MotionEventCompat.getX(event, pointerIndex) - initialTouchX;
                    final float dy = MotionEventCompat.getY(event, pointerIndex) - initialTouchY;

                    switch(v.getId()){
                        case R.id.hotspotOneImageView:
                            if(!moved && (Math.abs(initialHotspotX - hotspotOneImageView.getX()) > 5 || Math.abs(initialHotspotY - hotspotOneImageView.getY()) > 5))moved = true;

                            switch(activeLayout){
                                case 0:
                                    hotspotOneImageView.setX(clamp(hotspotOneImageView.getX() + dx, 0, imageLayoutContainer.getWidth() - hotspotOneImageView.getWidth()));
                                    hotspotOneImageView.setY(clamp(hotspotOneImageView.getY() + dy, 0, imageLayoutContainer.getHeight() - hotspotOneImageView.getHeight()));
                                    break;
                                case 1:
                                    hotspotOneImageView.setX(clamp(hotspotOneImageView.getX() + dx, 0, imageLayoutContainer.getWidth() / 2 - hotspotOneImageView.getWidth()));
                                    hotspotOneImageView.setY(clamp(hotspotOneImageView.getY() + dy, 0, imageLayoutContainer.getHeight() - hotspotOneImageView.getHeight()));
                                    break;
                                case 2:
                                    hotspotOneImageView.setX(clamp(hotspotOneImageView.getX() + dx, 0, imageLayoutContainer.getWidth() - hotspotOneImageView.getWidth()));
                                    hotspotOneImageView.setY(clamp(hotspotOneImageView.getY() + dy, 0, imageLayoutContainer.getHeight() / 2 - hotspotOneImageView.getHeight()));
                                    break;
                            }
                            break;
                        case R.id.hotspotTwoImageView:
                            if(!moved && (Math.abs(initialHotspotX - hotspotTwoImageView.getX()) > 5 || Math.abs(initialHotspotY - hotspotTwoImageView.getY()) > 5))moved = true;

                            switch(activeLayout){
                                case 0:
                                    hotspotTwoImageView.setX(clamp(hotspotTwoImageView.getX() + dx, 0, imageLayoutContainer.getWidth() - hotspotTwoImageView.getWidth()));
                                    hotspotTwoImageView.setY(clamp(hotspotTwoImageView.getY() + dy, 0, imageLayoutContainer.getHeight() - hotspotTwoImageView.getHeight()));
                                    break;
                                case 1:
                                    hotspotTwoImageView.setX(clamp(hotspotTwoImageView.getX() + dx, imageLayoutContainer.getWidth() / 2, imageLayoutContainer.getWidth() - hotspotTwoImageView.getWidth()));
                                    hotspotTwoImageView.setY(clamp(hotspotTwoImageView.getY() + dy, 0, imageLayoutContainer.getHeight() - hotspotTwoImageView.getHeight()));
                                    break;
                                case 2:
                                    hotspotTwoImageView.setX(clamp(hotspotTwoImageView.getX() + dx, 0, imageLayoutContainer.getWidth() - hotspotTwoImageView.getWidth()));
                                    hotspotTwoImageView.setY(clamp(hotspotTwoImageView.getY() + dy, imageLayoutContainer.getHeight() / 2, imageLayoutContainer.getHeight() - hotspotTwoImageView.getHeight()));
                                    break;
                            }
                            break;
                    }
                    break;
                }

                case MotionEvent.ACTION_CANCEL: {
                    activePointerId = -1;
                    break;
                }

                case MotionEvent.ACTION_POINTER_UP: {
                    final int pointerIndex = MotionEventCompat.getActionIndex(event);
                    final int pointerId = MotionEventCompat.getPointerId(event, pointerIndex);

                    if(pointerId == activePointerId) {
                        // This was our active pointer going up. Choose a new active pointer and adjust accordingly.
                        final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                        initialTouchX = MotionEventCompat.getX(event, newPointerIndex);
                        initialTouchY = MotionEventCompat.getY(event, newPointerIndex);
                        activePointerId = MotionEventCompat.getPointerId(event, newPointerIndex);
                    }
                    break;
                }
            }
            return true;
        }

        private float clamp(float val, float min, float max) { return Math.max(min, Math.min(max, val)); }

        private boolean clickedHotspot(float startX, float endX, float startY, float endY) { return (Math.abs(startX - endX) < 5 && Math.abs(startY - endY) < 5); }

        private boolean checkHotspotCollision(){
            int radius = hotspotOneImageView.getWidth()/2;
            float dx = (hotspotOneImageView.getX() + radius) - (hotspotTwoImageView.getX() + radius);
            float dy = (hotspotOneImageView.getY() + radius) - (hotspotTwoImageView.getY() + radius);
            float distance = (float)Math.sqrt(dx * dx + dy * dy);
            return (distance < radius * 2);
        }

        private void showLayoutTemplate(){
            enableHotspots(false);

            addImageViewOne.setVisibility(View.GONE);
            addImageViewOne.setImageDrawable(null);
            imageViewOne.destroyDrawingCache();
            if(originalOneZoom != -1.0f && originalOneScrollPosition != null){
                imageViewOne.setZoom(originalOneZoom);
                imageViewOne.setScrollPosition(originalOneScrollPosition.x, originalOneScrollPosition.y);
            }

            if(activeLayout > 0){
                addImageViewTwo.setVisibility(View.GONE);
                addImageViewTwo.setImageDrawable(null);
                imageViewTwo.destroyDrawingCache();
                if(originalTwoZoom != -1.0f && originalTwoScrollPosition != null){
                    imageViewTwo.setZoom(originalTwoZoom);
                    imageViewTwo.setScrollPosition(originalTwoScrollPosition.x, originalTwoScrollPosition.y);
                }
            }

            ObjectAnimator.ofFloat(layoutTypeContainer, "alpha", 0.0f, 1.0f).setDuration(User.ANIM_SPEED).start();
            ObjectAnimator.ofFloat(hotSpotContainer, "alpha", 1.0f, 0.0f).setDuration(User.ANIM_SPEED).start();
            ObjectAnimator.ofFloat(hotspotOneImageView, "alpha", 1.0f, 0.0f).setDuration(User.ANIM_SPEED).start();
            ObjectAnimator.ofFloat(hotspotTwoImageView, "alpha", 1.0f, 0.0f).setDuration(User.ANIM_SPEED).start();
        }

        private void hideLayoutTemplate(){
            originalOneZoom = imageViewOne.getCurrentZoom();
            originalOneScrollPosition = imageViewOne.getScrollPosition();
            imageViewOne.buildDrawingCache();
            Bitmap bitmapOne = imageViewOne.getDrawingCache();
            ((Post)getActivity()).setBitmapOne(bitmapOne);
            addImageViewOne.setImageBitmap(bitmapOne);
            addImageViewOne.setVisibility(View.VISIBLE);

            if(activeLayout > 0){
                originalTwoZoom = imageViewTwo.getCurrentZoom();
                originalTwoScrollPosition = imageViewTwo.getScrollPosition();
                imageViewTwo.buildDrawingCache();
                Bitmap bitmapTwo = imageViewTwo.getDrawingCache();
                ((Post)getActivity()).setBitmapTwo(bitmapTwo);
                addImageViewTwo.setImageBitmap(bitmapTwo);
                addImageViewTwo.setVisibility(View.VISIBLE);
            }

            if(activeLayout != hotspottedLayout)updateHotspotPosition(activeLayout < 2);
            enableHotspots(true);
            ObjectAnimator.ofFloat(layoutTypeContainer, "alpha", 1.0f, 0.0f).setDuration(User.ANIM_SPEED).start();
            ObjectAnimator.ofFloat(hotSpotContainer, "alpha", 0.0f, 1.0f).setDuration(User.ANIM_SPEED).start();
            ObjectAnimator.ofFloat(hotspotOneImageView, "alpha", 0.0f, 1.0f).setDuration(User.ANIM_SPEED).start();
            ObjectAnimator.ofFloat(hotspotTwoImageView, "alpha", 0.0f, 1.0f).setDuration(User.ANIM_SPEED).start();
        }

        private void updateHotspotImageViewOne(Drawable drawable){ hotspotOneImageView.setImageDrawable(drawable); }

        private void updateHotspotImageViewTwo(Drawable drawable){ hotspotTwoImageView.setImageDrawable(drawable); }

        private void updateImageViewOne(Bitmap bitmap){
            addImageViewOne.setVisibility(View.GONE);
            imageViewOne.setImageBitmap(bitmap);
        }

        private void updateImageViewTwo(Bitmap bitmap){
            addImageViewTwo.setVisibility(View.GONE);
            imageViewTwo.setImageBitmap(bitmap);
        }

        private void enableLayoutImageViews(boolean enable){
            singleLayoutImageView.setEnabled(enable);
            verticalLayoutImageView.setEnabled(enable);
            horizontalLayoutImageView.setEnabled(enable);
        }

        private void enableHotspots(boolean enabled){
            imageViewOne.setEnabled(!enabled);
            imageViewTwo.setEnabled(!enabled);

            hotspotOneImageView.setEnabled(enabled);
            hotspotTwoImageView.setEnabled(enabled);
            if(enabled){
                hotspotOneImageView.setVisibility(View.VISIBLE);
                hotspotTwoImageView.setVisibility(View.VISIBLE);
            } else {
                hotspotOneImageView.setVisibility(View.GONE);
                hotspotTwoImageView.setVisibility(View.GONE);
            }
        }

        private void updateHotspotPosition(boolean isVertical){
            hotspottedLayout = activeLayout;
            if(isVertical){
                hotspotOneImageView.setX((imageLayoutContainer.getWidth()/4) - (hotspotOneImageView.getWidth()/2));
                hotspotOneImageView.setY((imageLayoutContainer.getHeight()/2) - (hotspotOneImageView.getHeight()/2));
                hotspotTwoImageView.setX((imageLayoutContainer.getWidth() - (imageLayoutContainer.getWidth()/4)) - (hotspotOneImageView.getWidth()/2));
                hotspotTwoImageView.setY((imageLayoutContainer.getHeight()/2) - (hotspotOneImageView.getHeight()/2));
            } else {
                hotspotOneImageView.setX((imageLayoutContainer.getWidth()/2) - (hotspotOneImageView.getWidth()/2));
                hotspotOneImageView.setY((imageLayoutContainer.getHeight()/4) - (hotspotOneImageView.getHeight()/2));
                hotspotTwoImageView.setX((imageLayoutContainer.getWidth()/2) - (hotspotOneImageView.getWidth()/2));
                hotspotTwoImageView.setY((imageLayoutContainer.getHeight() - (imageLayoutContainer.getHeight()/4)) - (hotspotOneImageView.getHeight()/2));
            }
        }

        private void updateLayoutDrawables(int activeLayout){
            switch(activeLayout){
                case 0:
                    singleLayoutImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_post1_white_72px));
                    verticalLayoutImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_post2v_gray_24dp));
                    horizontalLayoutImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_post2h_gray_24dp));
                    break;
                case 1:
                    singleLayoutImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_post1_gray_24dp));
                    verticalLayoutImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_post2v_white_24dp));
                    horizontalLayoutImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_post2h_gray_24dp));
                    break;
                case 2:
                    singleLayoutImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_post1_gray_24dp));
                    verticalLayoutImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_post2v_gray_24dp));
                    horizontalLayoutImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_post2h_white_24dp));
                    break;
            }
        }

        private void setSingleLayout(){
            if(activeLayout != 0){
                // Disable onclick while animating
                enableLayoutImageViews(false);

                // Update drawables
                updateLayoutDrawables(0);

                // Animate to single layout
                ValueAnimator animator = ValueAnimator.ofInt(User.windowSize.x/2, User.windowSize.x);
                animator.setDuration(User.ANIM_SPEED);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        if(activeLayout == 1){
                            imageViewOneLayout.getLayoutParams().width = (int)valueAnimator.getAnimatedValue();
                            imageViewTwoLayout.setPadding(Integer.parseInt(valueAnimator.getAnimatedValue().toString()), 0, 0, 0);
                        } else {
                            imageViewOneLayout.getLayoutParams().height = (int) valueAnimator.getAnimatedValue();
                            imageViewTwoLayout.setPadding(0, (int)valueAnimator.getAnimatedValue(), 0, 0);
                        }
                    }
                });
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        activeLayout = 0;
                        enableLayoutImageViews(true);
                    }
                });
                animator.start();
            }
        }

        private void setVerticalLayout(){
            if(activeLayout != 1){
                // Disable onclick while animating
                enableLayoutImageViews(false);

                // Update drawables
                updateLayoutDrawables(1);

                // Animate to vertical layout
                ValueAnimator animator = ValueAnimator.ofInt(User.windowSize.x, User.windowSize.x/2);
                animator.setDuration(User.ANIM_SPEED);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        if (activeLayout == 0) {
                            imageViewOneLayout.getLayoutParams().width = (int)valueAnimator.getAnimatedValue() - ImageUtils.dpToPx(imageSpacing);
                            imageViewTwoLayout.setPadding(Integer.parseInt(valueAnimator.getAnimatedValue().toString()) + ImageUtils.dpToPx(imageSpacing), 0, 0, 0);
                        } else {
                            imageViewOneLayout.getLayoutParams().width = (int)valueAnimator.getAnimatedValue() - ImageUtils.dpToPx(imageSpacing);
                            imageViewOneLayout.getLayoutParams().height = User.windowSize.x/2 + (User.windowSize.x - (int)valueAnimator.getAnimatedValue());
                            imageViewTwoLayout.setPadding(User.windowSize.x - (int)valueAnimator.getAnimatedValue() + ImageUtils.dpToPx(imageSpacing), User.windowSize.x/2 - (User.windowSize.x - (int)valueAnimator.getAnimatedValue()), 0, 0);
                        }
                    }
                });
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        activeLayout = 1;
                        enableLayoutImageViews(true);
                    }
                });
                animator.start();
            }
        }

        private void setHorizontalLayout(){
            if(activeLayout != 2){
                // Disable onclick while animating
                enableLayoutImageViews(false);

                // Update drawables
                updateLayoutDrawables(2);

                // Animate to horizontal layout
                ValueAnimator animator = ValueAnimator.ofInt(User.windowSize.x, User.windowSize.x/2);
                animator.setDuration(User.ANIM_SPEED);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        if(activeLayout == 0){
                            imageViewOneLayout.getLayoutParams().height = (int)valueAnimator.getAnimatedValue() - ImageUtils.dpToPx(imageSpacing);
                            imageViewTwoLayout.setPadding(0, (int)valueAnimator.getAnimatedValue() + ImageUtils.dpToPx(imageSpacing), 0, 0);
                        } else {
                            imageViewOneLayout.getLayoutParams().width = User.windowSize.x/2 + (User.windowSize.x - (int)valueAnimator.getAnimatedValue());
                            imageViewOneLayout.getLayoutParams().height = (int)valueAnimator.getAnimatedValue() - ImageUtils.dpToPx(imageSpacing);
                            imageViewTwoLayout.setPadding(User.windowSize.x/2 - (User.windowSize.x - (int)valueAnimator.getAnimatedValue()), User.windowSize.x - (int)valueAnimator.getAnimatedValue() + ImageUtils.dpToPx(imageSpacing), 0, 0);
                        }
                    }
                });
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        activeLayout = 2;
                        enableLayoutImageViews(true);
                    }
                });
                animator.start();
            }
        }
    }

    public static class PostTag extends Fragment  {
        private ArrayList<Tag> postTags, suggestedTags;
        private TextView titleTextView, subTitleTextView, tapToAddTextView;
        private GridViewAdapter gridViewAdapter;
        private FlowLayout flowLayout;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.post_tag, container, false);

            postTags = new ArrayList<>();

            titleTextView = (TextView)rootView.findViewById(R.id.titleTextView);
            titleTextView.setTypeface(User.RalewaySemiBold);

            subTitleTextView = (TextView)rootView.findViewById(R.id.subTitleTextView);
            subTitleTextView.setTypeface(User.RalewayMedium);

            tapToAddTextView = (TextView)rootView.findViewById(R.id.tapToAddTextView);
            tapToAddTextView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_UP) {
                        ((Post)getActivity()).showAddTagEditor();
                    }
                    return false;
                }
            });

            flowLayout = (FlowLayout)rootView.findViewById(R.id.flowLayout);

            suggestedTags = new ArrayList<>();
            List<String> tagList = Arrays.asList(getResources().getStringArray(R.array.tag_array));
            for(int i = 0; i < tagList.size(); i++)suggestedTags.add(new Tag(tagList.get(i)));

            gridViewAdapter = new GridViewAdapter();
            GridView gridView = (GridView)rootView.findViewById(R.id.gridView);
            gridView.setAdapter(gridViewAdapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(!suggestedTags.get(position).isSelected) {
                        suggestedTags.get(position).isSelected = true;
                        gridViewAdapter.notifyDataSetChanged();
                        flowLayout.addView(createTagView(suggestedTags.get(position).tag, true));
                        postTags.add(new Tag(suggestedTags.get(position).tag, position, true));
                    }
                }
            });

            return rootView;
        }

        public void clearTags(){
            postTags.clear();
            flowLayout.removeAllViews();
        }

        public void reAddTags(){
            flowLayout.removeAllViews();
            for(int i = 0; i < postTags.size(); i++) flowLayout.addView(createTagView(postTags.get(i).tag, postTags.get(i).isSelected));
        }

        public void addTag(String tag, boolean editable){
            Log.d(User.LOGTAG, "" + postTags.size());
            flowLayout.addView(createTagView(tag, editable));
            postTags.add(new Tag(tag));
        }

        private View createTagView(String tag, boolean editable) {
            final View tagView = getActivity().getLayoutInflater().inflate(R.layout.post_tag_item, flowLayout, false);

            TextView hashTextView = (TextView)tagView.findViewById(R.id.hashTextView);
            hashTextView.setTypeface(User.RalewayMedium);

            TextView tagTextView = (TextView)tagView.findViewById(R.id.tagTextView);
            tagTextView.setTypeface(User.RalewayMedium);
            tagTextView.setText(tag);

            ImageView deleteImageView = (ImageView)tagView.findViewById(R.id.deleteImageView);
            deleteImageView.setTag(tag);
            if(editable){
                deleteImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String tag = (String)v.getTag();
                        Log.d(User.LOGTAG, "Tag " + tag);
                        for(int i = 0; i < postTags.size(); i++){
                            if(postTags.get(i).tag.equals(tag)){
                                suggestedTags.get(postTags.get(i).position).isSelected = false;
                                gridViewAdapter.notifyDataSetChanged();
                                postTags.remove(i);
                                flowLayout.removeView(tagView);
                                break;
                            }
                        }
                    }
                });
            } else {
                deleteImageView.setVisibility(View.GONE);

                ImageView backgroundImageView = (ImageView)tagView.findViewById(R.id.backgroundImageView);
                backgroundImageView.setVisibility(View.GONE);

                tagTextView.setTextColor(getResources().getColor(R.color.color_almost_black));
            }

            return tagView;
        }

        private class GridViewAdapter extends BaseAdapter {
            public GridViewAdapter(){}

            @Override
            public int getCount() { return suggestedTags.size(); }

            @Override
            public Object getItem(int position) { return position; }

            @Override
            public long getItemId(int position) { return position; }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TagHolder holder;

                if(convertView == null){
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.item_tag, parent, false);

                    holder = new TagHolder();
                    holder.hashTextView = (TextView)convertView.findViewById(R.id.hashTextView);
                    holder.tagTextView = (TextView)convertView.findViewById(R.id.tagTextView);

                    convertView.setTag(holder);
                } else { holder = (TagHolder)convertView.getTag(); }

                Tag tag = suggestedTags.get(position);
                holder.tagTextView.setText(tag.tag);
                if(tag.isSelected)holder.tagTextView.setTextColor(getResources().getColor(R.color.color_gray_default));
                else holder.tagTextView.setTextColor(getResources().getColor(R.color.color_almost_black));

                holder.hashTextView.setTypeface(User.RalewayMedium);
                holder.tagTextView.setTypeface(User.RalewayMedium);

                return convertView;
            }

            private class TagHolder {
                TextView hashTextView, tagTextView;
            }
        }

        private class Tag {
            private String tag;
            private int position;
            private boolean isSelected = false;

            private Tag(String tag){ this.tag = tag; }

            private Tag(String tag, int position, boolean isSelected){
                this.tag = tag;
                this.position = position;
                this.isSelected = isSelected;
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
                alertDialog.dismiss();
                quitPosting();
            }
        });

        alertDialog.setView(alertView);
        alertDialog.show();
    }
}
