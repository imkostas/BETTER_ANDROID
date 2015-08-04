package com.astapley.thememe.better;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class SupportActivity extends Activity implements View.OnClickListener {

    private ArrayList<supportItem> supportItem;
    private ListView supportListView;
    private SupportListAdapter supportListViewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int touchPositionX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        //top bar initialization
        final TextView topBarTitle = (TextView)findViewById(R.id.topBarTitle);
        topBarTitle.setTypeface(User.RalewaySemiBold);
        topBarTitle.setText(getString(R.string.support_title));
        topBarTitle.setOnClickListener(this);

        supportItem = new ArrayList<>();
        supportListViewAdapter = new SupportListAdapter(this, new ArrayList<supportItem>());
        supportListView = (ListView) findViewById(R.id.supportListView);
        supportListView.setAdapter(supportListViewAdapter);
        supportListView.setDivider(null);
        supportListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:  //Send email
                        showContact();
                        break;
                    case 1:  //Rate
                        User.makeToast("Rate");
                        break;
                    case 2:  //Legal
                        showLegal(1);
                        break;
                    case 3:  //Legal
                        showLegal(0);
                        break;
                }
            }
        });
        populatesupportItems();  ///tobe replaced with getSupport()

    }


    @Override
    public void onBackPressed() {
        this.finish();
        overridePendingTransition(R.anim.slide_in_from_left_half, R.anim.slide_out_right);
    }

    private void showContact(){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{getResources().getString(R.string.email)});
        i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.email_subject));
        i.putExtra(Intent.EXTRA_TEXT, "");
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            alertDismiss(getResources().getString(R.string.email_no_client) + " " + getResources().getString(R.string.email), getResources().getString(R.string.alert_got_it));
        }
    }

    private void showLegal(int position){

        final String VIEW_PAGER_POSITION = "view_pager_position";
        Bundle bundle = new Bundle();
        bundle.putInt(VIEW_PAGER_POSITION, position);
        Intent intent = new Intent(getBaseContext(), LegalActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_left_half);
    }



    private void populatesupportItems(){

        supportItem.clear();
        supportListViewAdapter.clear();
        supportItem.add(new supportItem("Send us feedback"));
        supportItem.add(new supportItem("Rate BETTER"));
        supportItem.add(new supportItem("Privacy policy"));
        supportItem.add(new supportItem("Terms of service"));
        supportListViewAdapter.notifyDataSetChanged();
        supportListViewAdapter.addAll(supportItem);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.topBarTitle:
                onBackPressed();
                break;
        }
    }

    private static class supportItem {
        public String text;

        public supportItem(String text){
            this.text = text;

        }
    }

    public class SupportListAdapter extends ArrayAdapter<supportItem> {

        public SupportListAdapter(Context context, ArrayList<supportItem> itemList){
            super(context, 0, itemList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            supportItemHolder supportItemHolder;


            if(convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.item_support, parent, false);

                supportItemHolder = new supportItemHolder();
                supportItemHolder.text = (TextView)convertView.findViewById(R.id.support_text);
                supportItemHolder.next = (ImageView)convertView.findViewById(R.id.support_next);
                supportItemHolder.filler = (ImageView)convertView.findViewById(R.id.filler8top);
                supportItemHolder.line = (ImageView)convertView.findViewById(R.id.support_item_line);
                supportItemHolder.text.setTypeface(User.RalewaySemiBold);
                supportItemHolder.text.setTextColor(Color.parseColor("#212121"));


                convertView.setTag(supportItemHolder);
            } else {
                supportItemHolder = (supportItemHolder)convertView.getTag();
            }

            supportItem supportItem = getItem(position);
            if(position!=0)supportItemHolder.filler.setVisibility(View.GONE);
            supportItemHolder.text.setText(supportItem.text);



            return convertView;
        }

        private class supportItemHolder {
            TextView text;
            ImageView next;
            ImageView filler;
            ImageView line;

        }
    }


    private void getSupport(){
        String command = "getSupport";
        StringRequest strReq = new StringRequest(com.android.volley.Request.Method.POST,
                User.api_uri + command, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                supportItem.clear();
                try {
                    Log.d(User.LOGTAG, response);

                    JSONObject obj = new JSONObject(response);
                    JSONArray Support = obj.getJSONArray("Support");
                    for(int i = 0; i < Support.length(); i++){
                        JSONObject search = Support.getJSONObject(i);
                        supportItem.add(new supportItem(search.getString("text")));
                    }

                    supportListViewAdapter.clear();
                    supportListViewAdapter.addAll(supportItem);
                } catch (Exception e) {
                    alertDismiss(getString(R.string.support_alert_unavailable), getString(R.string.alert_got_it));
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    JSONObject obj = new JSONObject(new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers)));
                    Log.d(User.LOGTAG, obj.toString());
                    if(obj.has("alert")){
                        JSONObject alert = obj.getJSONObject("alert");
                        if(alert.has("message") && alert.has("leftAction")){
                            alertDismiss(alert.getString("message"), alert.getString("leftAction"));
                        } else { throw new Exception(); }
                    } else { throw new Exception(); }
                } catch (Exception e) {
                    alertDismiss(getString(R.string.support_alert_unavailable), getString(R.string.alert_got_it));
                    e.printStackTrace();
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("api_key", User.api_key);
                params.put("username", User.username);
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
}
