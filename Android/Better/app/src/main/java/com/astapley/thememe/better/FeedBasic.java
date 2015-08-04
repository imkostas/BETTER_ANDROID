package com.astapley.thememe.better;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class FeedBasic extends FragmentActivity implements View.OnClickListener {
    private boolean threeDotVisible = false;
    private FrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_basic);

        getSupportFragmentManager().beginTransaction().add(R.id.feed, new Feed()).commit();

        //top bar initialization
        final TextView topBarTitle = (TextView)findViewById(R.id.topBarTitle);
        topBarTitle.setTypeface(User.RalewaySemiBold);
        topBarTitle.setText(User.filter.title);
        topBarTitle.setOnClickListener(this);

        container = (FrameLayout)findViewById(R.id.container);
        container.setAlpha(0.0f);
    }

    @Override
    public void onBackPressed() {
        if(threeDotVisible){
            hideThreeDotMenu();
        } else {
            this.finish();
            overridePendingTransition(R.anim.no_anim, R.anim.slide_out_from_top);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.topBarTitle:
                onBackPressed();
                break;
        }
    }

    public void showThreeDotMenu(int voteState){
        if(!threeDotVisible){
            threeDotVisible = true;
            ObjectAnimator.ofFloat(container, "alpha", 0f, 1f).setDuration(User.ANIM_SPEED).start();
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
            ValueAnimator animator = ObjectAnimator.ofFloat(container, "alpha", 1f, 0f);
            animator.setDuration(User.ANIM_SPEED);
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
