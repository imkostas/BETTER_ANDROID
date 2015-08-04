package com.astapley.thememe.better;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.s3.transfermanager.Download;
import com.amazonaws.mobileconnectors.s3.transfermanager.Transfer;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferProgress;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import java.io.File;
import java.io.IOException;

public class Menu extends Fragment implements View.OnClickListener {
    private Rect rect;
    private TextView usernameTextView, rankingTextView, settingTextView;
    private ImageView profileImageView, backgroundProfileImageView;
    private FrameLayout profileFrameLayout;

    public Menu() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.menu, container, false);

        profileImageView = (ImageView)rootView.findViewById(R.id.profileImageView);
        backgroundProfileImageView = (ImageView)rootView.findViewById(R.id.backgroundProfileImageView);
        if(User.smallProfileBitmap == null) new DownloadProfileImage().execute(ImageUtils.PROFILE_SMALL);
        if(User.mediumProfileBitmap != null) updateProfileImageView(User.mediumProfileBitmap);
        else new DownloadProfileImage().execute(ImageUtils.PROFILE_MEDIUM);
        if(User.largeProfileBitmap != null) updateBackgroundProfileImageView(User.largeProfileBitmap);
        else new DownloadProfileImage().execute(ImageUtils.PROFILE_LARGE);

        usernameTextView = (TextView)rootView.findViewById(R.id.usernameTextView);
        usernameTextView.setText(User.username);

        profileFrameLayout = (FrameLayout)rootView.findViewById(R.id.profileFrameLayout);
        profileFrameLayout.setOnClickListener(this);

        rankingTextView = (TextView)rootView.findViewById(R.id.rankingTextView);
        rankingTextView.setOnClickListener(this);
//        rankingTextView.setOnTouchListener(this);

        settingTextView = (TextView)rootView.findViewById(R.id.settingsTextView);
        settingTextView.setOnClickListener(this);
//        settingTextView.setOnTouchListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.profileFrameLayout:
                showProfile();
                break;
            case R.id.rankingTextView:
                showRanking();
                break;
            case R.id.settingsTextView:
                showSettings();
                break;
        }
    }

    private void showProfile(){
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_anim);
    }

    private void showRanking(){
        Intent intent = new Intent(getActivity(), Ranking.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_anim);
    }

    private void showSettings(){
        Intent intent = new Intent(getActivity(), SettingsActivity.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_anim);
    }

    private class DownloadProfileImage extends AsyncTask<String, Void, ProfileImage> {
        protected ProfileImage doInBackground(String... params) {
            String type = params[0];
            String imagePath = "profile_image_" + type + ".png";
            String cacheDirectory = getActivity().getCacheDir().getAbsolutePath();

            File file = new File(getActivity().getCacheDir(), imagePath);
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Download download = User.transferManager.download(User.s3BucketName, "user/" + User.userID + "_" + type + ".png", file);
            try { download.waitForCompletion(); }
            catch (InterruptedException e) { e.printStackTrace(); }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return new ProfileImage(type, BitmapFactory.decodeFile(cacheDirectory + "/" + imagePath, options));
        }

        protected void onPostExecute(ProfileImage profileImage) {
            switch(profileImage.type){
                case ImageUtils.PROFILE_SMALL:
                    User.smallProfileBitmap = profileImage.bitmap;
                    break;
                case ImageUtils.PROFILE_MEDIUM:
                    User.mediumProfileBitmap = profileImage.bitmap;
                    updateProfileImageView(User.mediumProfileBitmap);
                    break;
                case ImageUtils.PROFILE_LARGE:
                    User.largeProfileBitmap = profileImage.bitmap;
                    updateBackgroundProfileImageView(User.largeProfileBitmap);
                    break;
            }
        }
    }

    private class ProfileImage {
        String type;
        Bitmap bitmap;

        private ProfileImage(String type, Bitmap bitmap){
            this.type = type;
            this.bitmap = bitmap;
        }
    }

    public void updateProfileImageView(Bitmap profileImage){
        Bitmap grayScaled = ImageUtils.grayScaleLuminosity(profileImage);
        Drawable mask = getResources().getDrawable(R.drawable.ic_cam_circle_account_116dp);
        Bitmap profileBitmap = Bitmap.createBitmap(grayScaled.getWidth(), grayScaled.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas profileCanvas = new Canvas(profileBitmap);
        profileCanvas.drawBitmap(grayScaled, 0, 0, null);
        mask.setBounds(0, 0, profileCanvas.getWidth(), profileCanvas.getHeight());
        mask.draw(profileCanvas);
        profileImageView.setImageBitmap(profileBitmap);
        User.profileImage = profileBitmap;
    }

    public void updateBackgroundProfileImageView(Bitmap backgroundImage){
        Drawable tint = getResources().getDrawable(R.drawable.drawable_tint);
        Bitmap backgroundBitmap = Bitmap.createBitmap(backgroundImage.getWidth(), backgroundImage.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas backgroundCanvas = new Canvas(backgroundBitmap);
        backgroundCanvas.drawBitmap(backgroundImage, 0, 0, null);
        tint.setBounds(0, 0, backgroundCanvas.getWidth(), backgroundCanvas.getHeight());
        tint.draw(backgroundCanvas);
        backgroundProfileImageView.setImageBitmap(backgroundBitmap);
        User.profileBackground = backgroundBitmap;
    }

//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        switch(v.getId()){
//            case R.id.rankingTextView:
//                switch(event.getActionMasked()){
//                    case MotionEvent.ACTION_DOWN:
//                        rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
//                        rankingState(true);
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        if(rect.contains(v.getLeft(), v.getTop(), v.getRight(), v.getBottom())){
//                            rankingState(false);
//                            Log.d(User.LOGTAG, "Call ranking view here");
//                        }
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        if(!rect.contains(v.getLeft(), v.getTop(), v.getRight(), v.getBottom())) rankingState(false);
//                        break;
//                }
//                return true;
//            case R.id.settingsTextView:
//                return true;
//        }
//        return false;
//    }
//
//    private void rankingState(boolean selected){
//        if(selected){
//            rankingTextView.setBackgroundColor(getResources().getColor(R.color.color_better));
//        } else {
//            rankingTextView.setBackgroundColor(getResources().getColor(R.color.color_clear));
//        }
//    }
}
