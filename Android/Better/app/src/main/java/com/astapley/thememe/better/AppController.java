package com.astapley.thememe.better;

import android.app.Application;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.util.Locale;

public class AppController extends Application {
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;
    private static AppController instance;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    protected void init() {
        instance = this;
        Locale.setDefault(Locale.US);
        User.initUser(getApplicationContext());
        clearImageCache();
    }

    public static synchronized AppController getInstance() { return instance; }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) { requestQueue = Volley.newRequestQueue(getApplicationContext()); }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(tag);
        VolleyLog.d("Adding request to queue: %s", req.getUrl());
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() { return imageLoader; }

    public void clearImageCache(){ imageLoader = new ImageLoader(getRequestQueue(), new ImageUtils.LruBitmapCache(ImageUtils.LruBitmapCache.getCacheSize(instance))); }

    public void cancelPendingRequests(Object tag) { if(requestQueue != null)requestQueue.cancelAll(tag); }
}
