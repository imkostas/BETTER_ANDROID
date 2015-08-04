package com.astapley.thememe.better;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class FeedController extends FragmentActivity implements View.OnClickListener {
    private boolean filterVisible = false;
    private boolean threeDotVisible = false;
    private static final int ANIM_SPEED = 150;

    private TextView leftTextView;
    private DrawerLayout drawerLayout;
    private FrameLayout leftDrawer, rightDrawer, container;
    public Feed feed;
    public Filter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_controller);

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        feed = new Feed();
        filter = new Filter();
        getSupportFragmentManager().beginTransaction().add(R.id.feed, feed).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.left_navigation_drawer, new Menu()).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.right_navigation_drawer, filter).commit();

        leftDrawer = (FrameLayout)findViewById(R.id.left_navigation_drawer);
        rightDrawer = (FrameLayout)findViewById(R.id.right_navigation_drawer);

        leftTextView = (TextView)findViewById(R.id.leftTextView);
        leftTextView.setTypeface(User.RalewayMedium);
        leftTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_green900_24dp, 0, 0, 0);
        leftTextView.setOnClickListener(this);

        ImageView rightImageView = (ImageView)findViewById(R.id.rightImageView);
        rightImageView.setImageResource(R.drawable.ic_filter_list_green900_24dp);
        rightImageView.setOnClickListener(this);

        ImageView cameraImageView = (ImageView)findViewById(R.id.cameraImageView);
        cameraImageView.setOnClickListener(this);

        container = (FrameLayout)findViewById(R.id.container);
        container.setAlpha(0.0f);
    }

    @Override
    public void onBackPressed() {
        if(threeDotVisible){
            hideThreeDotMenu();
        } else if(filterVisible) {
            hideFilterBySearch(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.leftTextView:
                if(drawerLayout.isDrawerOpen(rightDrawer))drawerLayout.closeDrawer(rightDrawer);
                drawerLayout.openDrawer(leftDrawer);
                break;
            case R.id.rightImageView:
                if(drawerLayout.isDrawerOpen(leftDrawer))drawerLayout.closeDrawer(leftDrawer);
                drawerLayout.openDrawer(rightDrawer);
                break;
            case R.id.cameraImageView:
                showPost();
                break;
        }
    }

    public void updateActionBarTitle(String title){
        leftTextView.setText(title);
        closeRightDrawer();
    }

    public void updateFilterDrawer(final Boolean searching){
        int margin = ImageUtils.dpToPx(64) * -1;
        ValueAnimator valueAnimator = ValueAnimator.ofInt((searching)?0:margin, (searching)?margin:0);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                DrawerLayout.LayoutParams layoutParams = (DrawerLayout.LayoutParams) rightDrawer.getLayoutParams();
                layoutParams.setMargins((int) animation.getAnimatedValue(), 0, 0, 0);
                rightDrawer.setLayoutParams(layoutParams);
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

                drawerLayout.setDrawerLockMode(searching ? DrawerLayout.LOCK_MODE_LOCKED_OPEN : DrawerLayout.LOCK_MODE_UNLOCKED, rightDrawer);
                drawerLayout.setDrawerLockMode(searching ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED, leftDrawer);
            }
        });
        valueAnimator.setDuration(User.ANIM_SPEED);
        valueAnimator.start();
    }

    private void showPost(){
        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            Intent intent = new Intent(this, Post.class);
            startActivity(intent);
        } else Log.d(User.LOGTAG, "User's device doesn't have camera...this shouldn't happen");
    }

    public void showThreeDotMenu(int voteState){
        if(!threeDotVisible){
            threeDotVisible = true;
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            ObjectAnimator.ofFloat(container, "alpha", 0f, 1f).setDuration(ANIM_SPEED).start();
            Bundle bundle = new Bundle();
            bundle.putInt("vote_state", voteState);
            ThreeDotMenu threeDotMenu = new ThreeDotMenu();
            threeDotMenu.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_from_bottom, R.anim.fade_out).add(R.id.container, threeDotMenu, "three_dot_menu").commit();
        }
    }

    public void hideThreeDotMenu(){
        if(threeDotVisible){
            threeDotVisible = false;
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            ValueAnimator animator = ObjectAnimator.ofFloat(container, "alpha", 1f, 0f);
            animator.setDuration(ANIM_SPEED);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    Fragment threeDotFragment = getSupportFragmentManager().findFragmentByTag("three_dot_menu");
                    getSupportFragmentManager().beginTransaction().remove(threeDotFragment).commit();
                }
            });
            animator.start();
        }
    }

    public void closeRightDrawer(){
        drawerLayout.closeDrawer(rightDrawer);
    }

    public void notifyRecyclerView(){
        feed.recyclerViewAdapter.notifyDataSetChanged();
    }

    public void showFilterBySearch(){
        if(!filterVisible){
            filterVisible = true;
            ObjectAnimator.ofFloat(container, "alpha", 0f, 1f).setDuration(ANIM_SPEED).start();
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_from_bottom, R.anim.fade_out).add(R.id.container, new FilterBySearch(), "filter_fragment").commit();
        }
    }

    public void hideFilterBySearch(boolean shouldCloseDrawer){
        if(filterVisible){
            filterVisible = false;
            if(shouldCloseDrawer)closeRightDrawer();
            ValueAnimator animator = ObjectAnimator.ofFloat(container, "alpha", 1f, 0f);
            animator.setDuration(ANIM_SPEED);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    Fragment filterFragment = getSupportFragmentManager().findFragmentByTag("filter_fragment");
                    getSupportFragmentManager().beginTransaction().remove(filterFragment).commit();
                }
            });
            animator.start();
        }
    }

    public void alertDismiss(String messageText, String dismissText){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View alertView = layoutInflater.inflate(R.layout.alert_one_action_no_title, null);

        TextView message = (TextView)alertView.findViewById(R.id.message);
        message.setText(messageText);
        message.setTypeface(User.RalewayMedium);

        Button dismissBtn = (Button)alertView.findViewById(R.id.dismissBtn);
        dismissBtn.setText(dismissText);
        dismissBtn.setTypeface(User.RalewayMedium);
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
