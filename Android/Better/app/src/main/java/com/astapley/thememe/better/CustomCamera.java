package com.astapley.thememe.better;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.io.IOException;
import java.util.List;

public class CustomCamera extends Fragment {
    private static final int ANIM_SPEED = 150;
    private boolean cameraReleased = false;
    private boolean cameraFrontFacing = false;
    private int flashState = 0;

    private android.hardware.Camera mCamera;
    private CameraPreview mPreview;
    private FrameLayout preview;
    private Bitmap image;

    private ImageButton captureButton;
    private ImageButton retakeBtn;
    private ImageButton acceptBtn;
    private ImageButton flashBtn;
    private ImageButton galleryBtn;
    private ImageButton cameraFacingBtn;

    private android.hardware.Camera.PictureCallback tookPicture = new android.hardware.Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
            updateCameraUI(false);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            image = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        }
    };

    public CustomCamera() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.custom_camera, container, false);

        preview = (FrameLayout)rootView.findViewById(R.id.cameraPreview);
        preview.getLayoutParams().width = User.windowSize.x;
        preview.getLayoutParams().height = User.windowSize.y;
        loadCamera();

        captureButton = (ImageButton)rootView.findViewById(R.id.captureBtn);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCamera.takePicture(null, null, tookPicture);
                    }
                }
        );

        retakeBtn = (ImageButton)rootView.findViewById(R.id.retakeBtn);
        retakeBtn.setAlpha(0.0f);
        retakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.startPreview();
                updateCameraUI(true);
            }
        });

        acceptBtn = (ImageButton)rootView.findViewById(R.id.acceptBtn);
        acceptBtn.setAlpha(0.0f);
        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseCamera();
                Activity activity = getActivity();
                if(activity instanceof Post){
                    Post post = (Post)activity;
                    post.acceptedImage(image);
                } else if(activity instanceof NewAccount) {
                    NewAccount newAccount = (NewAccount)activity;
                    newAccount.acceptedImage(image);
                }
            }
        });

        flashBtn = (ImageButton)rootView.findViewById(R.id.flashBtn);
        if(getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            flashBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    flashState = (flashState < 2) ? (flashState + 1) : 0;
                    updateFlashState(flashState);
                }
            });
        } else {
            flashBtn.setEnabled(false);
            flashBtn.setAlpha(0.0f);
        }

        galleryBtn = (ImageButton)rootView.findViewById(R.id.galleryBtn);
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User.makeToast("Transition to prompt gallery here");
            }
        });

        cameraFacingBtn = (ImageButton)rootView.findViewById(R.id.cameraFacingBtn);
        if(getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)){
            cameraFacingBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!cameraReleased){ mCamera.stopPreview(); }
                    mCamera.release();
                    cameraReleased = true;
                    cameraFrontFacing = !cameraFrontFacing;
                    loadCamera();
                }
            });
        } else {
            cameraFacingBtn.setEnabled(false);
            cameraFacingBtn.setAlpha(0.0f);
        }

        return rootView;
    }

    private void updateCameraUI(boolean active){
        if(active){
            galleryBtn.setEnabled(true);
            flashBtn.setEnabled(true);
            cameraFacingBtn.setEnabled(true);
            captureButton.setEnabled(true);

            retakeBtn.setAlpha(0.0f);
            acceptBtn.setAlpha(0.0f);
            retakeBtn.setVisibility(View.GONE);
            acceptBtn.setVisibility(View.GONE);
//            ObjectAnimator.ofFloat(retakeBtn, "alpha", 1f, 0f).setDuration(ANIM_SPEED).start();
//            ObjectAnimator.ofFloat(acceptBtn, "alpha", 1f, 0f).setDuration(ANIM_SPEED).start();
            captureButton.setImageResource(R.drawable.post_button_record_on_56dp);
            switch(flashState){
                case 0:
                    flashBtn.setImageResource(R.drawable.ic_flash_auto_white_24dp);
                    break;
                case 1:
                    flashBtn.setImageResource(R.drawable.ic_flash_off_white_24dp);
                    break;
                case 2:
                    flashBtn.setImageResource(R.drawable.ic_flash_on_white_24dp);
                    break;
                default:
                    flashBtn.setImageResource(R.drawable.ic_flash_auto_white_24dp);
                    break;
            }
            galleryBtn.setImageResource(R.drawable.ic_camera_roll_white_24dp);
            cameraFacingBtn.setImageResource(R.drawable.ic_camera_front_white_24dp);
        } else {
            galleryBtn.setEnabled(false);
            flashBtn.setEnabled(false);
            cameraFacingBtn.setEnabled(false);
            captureButton.setEnabled(false);

            retakeBtn.setVisibility(View.VISIBLE);
            acceptBtn.setVisibility(View.VISIBLE);
            ObjectAnimator.ofFloat(retakeBtn, "alpha", 0f, 1f).setDuration(ANIM_SPEED).start();
            ObjectAnimator.ofFloat(acceptBtn, "alpha", 0f, 1f).setDuration(ANIM_SPEED).start();
            captureButton.setImageResource(R.drawable.post_button_record_pressed_56dp);
            switch(flashState){
                case 0:
                    flashBtn.setImageResource(R.drawable.ic_flash_auto_grey600_24dp);
                    break;
                case 1:
                    flashBtn.setImageResource(R.drawable.ic_flash_off_grey600_24dp);
                    break;
                case 2:
                    flashBtn.setImageResource(R.drawable.ic_flash_on_grey600_24dp);
                    break;
                default:
                    flashBtn.setImageResource(R.drawable.ic_flash_auto_grey600_24dp);
                    break;
            }
            galleryBtn.setImageResource(R.drawable.ic_camera_roll_grey600_24dp);
            cameraFacingBtn.setImageResource(R.drawable.ic_camera_front_grey600_24dp);
        }
    }

    private void updateFlashState(int state){
        String flashMode;
        switch(state){
            case 0:
                flashBtn.setImageResource(R.drawable.ic_flash_auto_white_24dp);
                flashMode = android.hardware.Camera.Parameters.FLASH_MODE_AUTO;
                break;
            case 1:
                flashBtn.setImageResource(R.drawable.ic_flash_off_white_24dp);
                flashMode = android.hardware.Camera.Parameters.FLASH_MODE_OFF;
                break;
            case 2:
                flashBtn.setImageResource(R.drawable.ic_flash_on_white_24dp);
                flashMode = android.hardware.Camera.Parameters.FLASH_MODE_ON;
                break;
            default:
                flashState = 0;
                flashBtn.setImageResource(R.drawable.ic_flash_auto_white_24dp);
                flashMode = android.hardware.Camera.Parameters.FLASH_MODE_AUTO;
                break;
        }

        Log.d(User.LOGTAG, Integer.toString(state));
        android.hardware.Camera.Parameters params = mCamera.getParameters();
        params.setFlashMode(flashMode);
        mCamera.setParameters(params);
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        releaseCamera();
//    }

    public void releaseCamera(){
        try {
            if(mCamera != null){
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

//    @Override
//    protected void onResume(){
//        super.onResume();
//        try {
//            if(cameraReleased && mCamera != null){ loadCamera(); }
//        } catch (Exception e){ Log.d(User.LOGTAG, "Error starting camera preview: " + e.getMessage()); }
//    }

    private void loadCamera(){
        try {
            mCamera = getCameraInstance(cameraFrontFacing);
            CameraPreview tempPreview = mPreview;
            mPreview = new CameraPreview(getActivity(), mCamera);
            preview.addView(mPreview);
            cameraReleased = false;
            mCamera.startPreview();
            preview.removeView(tempPreview);
        } catch(Exception e) { e.printStackTrace(); }
    }

    public static android.hardware.Camera getCameraInstance(boolean cameraFrontFacing){
        android.hardware.Camera camera = null;
        try {
            if(cameraFrontFacing){
                camera = android.hardware.Camera.open(android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
            } else {
                camera = android.hardware.Camera.open(android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
            }
        } catch (RuntimeException e) { Log.d(User.LOGTAG, "Camera is not available: " + e.getMessage()); }
        return camera;
    }

    private class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private android.hardware.Camera mCamera;

        public CameraPreview(Context context, android.hardware.Camera camera) {
            super(context);
            mCamera = camera;

            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.i(User.LOGTAG, "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {}

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            if(mHolder.getSurface() == null) return;

            try {
                mCamera.stopPreview();
            }
            catch (Exception e){}

            try {
                mCamera.setPreviewDisplay(mHolder);

                android.hardware.Camera.Parameters params = mCamera.getParameters();

                List<Integer> pictureFormats = params.getSupportedPictureFormats();
                if(pictureFormats.contains(ImageFormat.JPEG)){
                    params.setPictureFormat(ImageFormat.JPEG);
                }

                List<String> focusModes = params.getSupportedFocusModes();
                if(focusModes.contains(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
                    params.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }

                mCamera.setParameters(params);
                mCamera.setDisplayOrientation(90);
                mCamera.startPreview();
            } catch (Exception e){
                Log.i(User.LOGTAG, "Error starting camera preview: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
