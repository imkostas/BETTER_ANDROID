package com.astapley.thememe.better;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class VolleyRequests {

    private void getRequest(){
        String url = User.api_uri + "user?api_key=" + User.api_key + "&user_id=2";
        StringRequest strReq = new StringRequest(com.android.volley.Request.Method.GET,
                url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    Log.d(User.LOGTAG, obj.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    Log.i(User.LOGTAG, new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers)));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        AppController.getInstance().addToRequestQueue(strReq, "Log In");
    }

    private void postRequest(){
        StringRequest strReq = new StringRequest(com.android.volley.Request.Method.POST,
                User.api_uri + "user", new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    Log.d(User.LOGTAG, obj.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    Log.i(User.LOGTAG, new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers)));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("api_key", User.api_key);
                params.put("user_id", "2");
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, "Log In");
    }

    private void putRequest(){
        StringRequest strReq = new StringRequest(com.android.volley.Request.Method.PUT,
                User.api_uri + "user", new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    Log.d(User.LOGTAG, obj.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    Log.i(User.LOGTAG, new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers)));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("api_key", User.api_key);
                params.put("user_id", "2");
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, "Log In");
    }

    private void deleteRequest(){
        String url = User.api_uri + "user?api_key=" + User.api_key + "&user_id=2";
        StringRequest strReq = new StringRequest(com.android.volley.Request.Method.DELETE,
                url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    Log.d(User.LOGTAG, obj.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    Log.i(User.LOGTAG, new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers)));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        AppController.getInstance().addToRequestQueue(strReq, "Log In");
    }

}
