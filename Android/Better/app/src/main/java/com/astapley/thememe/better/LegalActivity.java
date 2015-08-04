package com.astapley.thememe.better;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class LegalActivity extends FragmentActivity implements View.OnClickListener{
    private static final String VIEW_PAGER_POSITION = "view_pager_position";

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal);

        Bundle bundle = getIntent().getExtras();
        int position = bundle.getInt(VIEW_PAGER_POSITION);

        //top bar initialization
        final TextView topBarTitle = (TextView)findViewById(R.id.topBarTitle);
        topBarTitle.setTypeface(User.RalewaySemiBold);
        switch(position){
            case 0:
                topBarTitle.setText(getString(R.string.terms_of_service_text));
                break;
            case 1:
                topBarTitle.setText(getString(R.string.privacy_policy_text));
                break;
        }
        topBarTitle.setOnClickListener(this);

        final TextView privacyPolicyTextView = (TextView)findViewById(R.id.privacyPolicyTextView);
        final TextView termsOfServiceTextView = (TextView)findViewById(R.id.termsOfServiceTextView);

        termsOfServiceTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
                topBarTitle.setText(getString(R.string.terms_of_service_text));
                termsOfServiceTextView.setTextColor(getResources().getColor(R.color.color_almost_black));
                termsOfServiceTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.terms_legal_icon_dk_grey, 0, 0);
                privacyPolicyTextView.setTextColor(getResources().getColor(R.color.color_light_gray));
                privacyPolicyTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.terms_legal_icon_light_grey, 0, 0);
            }
        });

        privacyPolicyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
                topBarTitle.setText(getString(R.string.privacy_policy_text));
                termsOfServiceTextView.setTextColor(getResources().getColor(R.color.color_light_gray));
                termsOfServiceTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.terms_legal_icon_light_grey, 0, 0);
                privacyPolicyTextView.setTextColor(getResources().getColor(R.color.color_almost_black));
                privacyPolicyTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.terms_legal_icon_dk_grey, 0, 0);
            }
        });

        SectionsPagerAdapter viewPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager)findViewById(R.id.pager);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                switch(position){
                    case 0:
                        topBarTitle.setText(getString(R.string.terms_of_service_text));
                        termsOfServiceTextView.setTextColor(getResources().getColor(R.color.color_almost_black));
                        termsOfServiceTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.terms_legal_icon_dk_grey, 0, 0);
                        privacyPolicyTextView.setTextColor(getResources().getColor(R.color.color_light_gray));
                        privacyPolicyTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.terms_legal_icon_light_grey, 0, 0);
                        break;
                    case 1:
                        topBarTitle.setText(getString(R.string.privacy_policy_text));
                        termsOfServiceTextView.setTextColor(getResources().getColor(R.color.color_light_gray));
                        termsOfServiceTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.terms_legal_icon_light_grey, 0, 0);
                        privacyPolicyTextView.setTextColor(getResources().getColor(R.color.color_almost_black));
                        privacyPolicyTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.terms_legal_icon_dk_grey, 0, 0);
                        break;
                }
            }
        });

        viewPager.setCurrentItem(position);
        switch(position){
            case 0:
                topBarTitle.setText(getString(R.string.terms_of_service_text));
                termsOfServiceTextView.setTextColor(getResources().getColor(R.color.color_almost_black));
                termsOfServiceTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.terms_legal_icon_dk_grey, 0, 0);
                privacyPolicyTextView.setTextColor(getResources().getColor(R.color.color_light_gray));
                privacyPolicyTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.terms_legal_icon_light_grey, 0, 0);
                break;
            case 1:
                topBarTitle.setText(getString(R.string.privacy_policy_text));
                termsOfServiceTextView.setTextColor(getResources().getColor(R.color.color_light_gray));
                termsOfServiceTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.terms_legal_icon_light_grey, 0, 0);
                privacyPolicyTextView.setTextColor(getResources().getColor(R.color.color_almost_black));
                privacyPolicyTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.terms_legal_icon_dk_grey, 0, 0);
                break;
        }

        // Set typeface
        termsOfServiceTextView.setTypeface(User.RalewayMedium);
        privacyPolicyTextView.setTypeface(User.RalewayMedium);
    }

    @Override
    public void onBackPressed() {
        this.finish();
        overridePendingTransition(R.anim.slide_in_from_left_half, R.anim.slide_out_right);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.topBarTitle:
                onBackPressed();
                break;
        }
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public static class PlaceholderFragment extends Fragment {
        private WebView webView;

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int position) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(VIEW_PAGER_POSITION, position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_legal, container, false);

            Bundle bundle = this.getArguments();
            int position = bundle.getInt(VIEW_PAGER_POSITION);

            webView = (WebView)rootView.findViewById(R.id.webView);
            webView.getSettings().setLoadsImagesAutomatically(true);
            webView.setWebViewClient(new Browser());
            switch(position) {
                case 1:
                    webView.loadUrl("http://52.4.231.187/termsofservice.html");
                    break;
                case 2:
                    webView.loadUrl("http://52.4.231.187/privacyPolicy.html");
                    break;
            }

            return rootView;
        }

        private class Browser extends WebViewClient {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        }
    }
}