package com.astapley.thememe.better;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class SettingsActivity extends FragmentActivity implements View.OnClickListener {
    private ArrayList<SettingsItem> settingsItem;
    private ListView settingsListView;
    private SettingsListAdapter settingsListViewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int touchPositionX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //top bar initialization
        final TextView topBarTitle = (TextView)findViewById(R.id.topBarTitle);
        topBarTitle.setTypeface(User.RalewaySemiBold);
        topBarTitle.setText(getString(R.string.settings_title));
        topBarTitle.setOnClickListener(this);

        settingsItem = new ArrayList<>();
        settingsListViewAdapter = new SettingsListAdapter(this, new ArrayList<SettingsItem>());
        settingsListView = (ListView) findViewById(R.id.settingsListView);
        populateSettingsItems();  ///tobe replaced with getSettings()
        settingsListView.setAdapter(settingsListViewAdapter);
        settingsListView.setDivider(null);
        settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:
                        startMyAccount();
                        break;
                    case 1:  //Notifications
                        startNotifications();
                        break;
                    case 2:  //Support
                        startSupport();
                        break;
                    case 3:  //Logout
                        alertMultiAction(getString(R.string.alert_logout),
                                getString(R.string.alert_no), getString(R.string.alert_yes), R.color.color_better);
                        break;
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        this.finish();
        overridePendingTransition(R.anim.no_anim, R.anim.slide_out_from_top);
    }

    private void startMyAccount(){
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_left_half);
    }

    private void startNotifications(){
        Intent intent = new Intent(this, NotificationsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_left_half);
    }

    private void startSupport(){
        Intent intent = new Intent(this, SupportActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_left_half);
    }

    private void logOut(){
        User.clear();
        this.finish();
        Intent intent = new Intent(getBaseContext(), Intro.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_out_from_top);
    }

    private void populateSettingsItems(){
        settingsItem.clear();
        settingsListViewAdapter.clear();
        settingsItem.add(new SettingsItem("", "My Account"));
        settingsItem.add(new SettingsItem("ic_notifications_grey600_24dp","Notifications"));
        settingsItem.add(new SettingsItem("ic_help_grey600_24dp","Support"));
        settingsItem.add(new SettingsItem("ic_logout_grey_24dp","Logout"));
        settingsListViewAdapter.notifyDataSetChanged();
        settingsListViewAdapter.addAll(settingsItem);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.topBarTitle:
                onBackPressed();
                break;
        }
    }

    private static class SettingsItem {
        public String icon;
        public String title;

        public SettingsItem(String icon, String title){
            this.icon = icon;
            this.title = title;

        }
    }

    public class SettingsListAdapter extends ArrayAdapter<SettingsItem> {
        public SettingsListAdapter(Context context, ArrayList<SettingsItem> itemList){ super(context, 0, itemList); }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SettingsItemHolder settingsItemHolder;

            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_settings, parent, false);

                settingsItemHolder = new SettingsItemHolder();
                settingsItemHolder.icon = (ImageView)convertView.findViewById(R.id.iconImageView);
                settingsItemHolder.title = (TextView)convertView.findViewById(R.id.titleTextView);
                settingsItemHolder.title.setTypeface(User.RalewaySemiBold);

                convertView.setTag(settingsItemHolder);
            } else {
                settingsItemHolder = (SettingsItemHolder)convertView.getTag();
            }

            SettingsItem settingsItem = getItem(position);
            settingsItemHolder.title.setText(settingsItem.title);

            if(settingsItem.icon.equals("")){
                settingsItemHolder.icon.setImageBitmap(ImageUtils.scaleBitmap(24, User.smallProfileBitmap));
            } else {
                settingsItemHolder.icon.setImageResource(getResources().getIdentifier(settingsItem.icon , "drawable", getPackageName()));
            }

            return convertView;
        }

        private class SettingsItemHolder {
            ImageView icon;
            TextView title;
        }
    }

    private void alertDismiss(String messageText, String dismissText){
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

    public void alertMultiAction(String messageText, String dismissText, String actionText, final int color) {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View alertView = layoutInflater.inflate(R.layout.alert_two_action_no_title, null);

        TextView message = (TextView) alertView.findViewById(R.id.message);
        message.setText(messageText);
        message.setTypeface(User.RalewayMedium);

        Button dismissBtn = (Button) alertView.findViewById(R.id.dismissBtn);
        dismissBtn.setText(dismissText);
        dismissBtn.setTextColor(getResources().getColor(color));
        dismissBtn.setTypeface(User.RalewayMedium);
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        Button actionBtn = (Button) alertView.findViewById(R.id.actionBtn);
        actionBtn.setText(actionText);
        actionBtn.setTextColor(getResources().getColor(color));
        actionBtn.setTypeface(User.RalewayMedium);
        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                logOut();
            }
        });

        alertDialog.setView(alertView);
        alertDialog.show();
    }
}
