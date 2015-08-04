package com.astapley.thememe.better;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;

import java.io.File;
import java.io.IOException;

public class ImageUtils {
    public static final int SMALL_PROFILE_DP = 56, MEDIUM_PROFILE_DP = 116, LARGE_PROFILE_DP = 300;
    public static final String PROFILE_SMALL = "small", PROFILE_MEDIUM = "medium", PROFILE_LARGE = "large";

    public static Bitmap grayScaleLuminosity(Bitmap original){
        int width = original.getWidth();
        int height = original.getHeight();

        //Luminosity color matrix 5X4
        float[] matrix = new float[]{0.21f, 0.72f, 0.07f, 0, 0,
                0.21f, 0.72f, 0.07f, 0, 0,
                0.21f, 0.72f, 0.07f, 0, 0,
                0, 0, 0, 1, 0,};

        Bitmap grayScaled = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(grayScaled);
        Paint paint = new Paint();
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        paint.setColorFilter(filter);
        canvas.drawBitmap(original, 0, 0, paint);

        return grayScaled;
    }

    public static Bitmap scaleBitmap(int dp, Bitmap image){
        int width = image.getWidth();
        int height = image.getHeight();
        float xScale = ((float)dpToPx(dp))/width;
        float yScale = ((float)dpToPx(dp))/height;
        float scale = (xScale <= yScale) ? xScale : yScale;

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        return Bitmap.createBitmap(image, 0, 0, width, height, matrix, true);
    }

    public static int dpToPx(int dp) {
        return Math.round((float) dp * User.density);
    }

    public static int pxToDP(float px){ return Math.round(px/User.density); }

    public static int getImageOrientation(String imagePath) {
        int rotate = 0;
        try {
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rotate;
    }

    public static Bitmap formatImage(int dp, Bitmap image, int rotate){
        image = ImageUtils.scaleBitmap(dp, image);
        return rotateImage(image, rotate);
    }

    public static Bitmap rotateImage(Bitmap image, int rotate){
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, false);
        image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getWidth());
        return image;
    }

    public static class LruBitmapCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {
        public LruBitmapCache(int maxSize) {
            super(maxSize);
        }

        public LruBitmapCache(Context ctx) {
            this(getCacheSize(ctx));
        }

        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }

        @Override
        public Bitmap getBitmap(String url) {
            return get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            put(url, bitmap);
        }

        // Returns a cache size equal to approximately five screens worth of images.
        public static int getCacheSize(Context ctx) {
            final DisplayMetrics displayMetrics = ctx.getResources().
                    getDisplayMetrics();
            final int screenWidth = displayMetrics.widthPixels;
            final int screenHeight = displayMetrics.heightPixels;
            // 4 bytes per pixel
            final int screenBytes = screenWidth * screenHeight * 4;

            return screenBytes * 5;
        }
    }
}
