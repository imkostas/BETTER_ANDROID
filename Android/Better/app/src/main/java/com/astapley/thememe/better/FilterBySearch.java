package com.astapley.thememe.better;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.util.ArrayList;

public class FilterBySearch extends Fragment {
    private static ArrayList<HashtagItem> search_tags_list;
    private static ArrayList<UserItem> search_users_list;

    private EditText searchEditText;
    private Button tagsBtn, usersBtn;
    private SearchTagsAdapter searchTagsAdapter;
    private SearchUserAdapter searchUsersAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    public FilterBySearch() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.filter_by_search, container, false);

        search_tags_list = new ArrayList<>();
        search_users_list = new ArrayList<>();

        ImageView backBtn = (ImageView)rootView.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                ((FeedController)getActivity()).hideFilterBySearch(false);
            }
        });

        ImageView clearBtn = (ImageView)rootView.findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setTextColor(getResources().getColor(R.color.color_better));
                searchEditText.setText("");
                hideSoftKeyboard();
            }
        });

        //search bar
        searchEditText = (EditText)rootView.findViewById(R.id.searchEditText);
        searchEditText.setTextColor(getResources().getColor(R.color.color_better));
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchEditText.setTextColor(getResources().getColor(R.color.color_white));
            }
        });

        searchTagsAdapter = new SearchTagsAdapter(rootView.getContext(), new ArrayList<HashtagItem>());
        final ListView tagsListView = (ListView)rootView.findViewById(R.id.tagsListView);
        tagsListView.setDivider(null);
        tagsListView.setVisibility(View.VISIBLE);
        tagsListView.setAdapter(searchTagsAdapter);
        tagsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((FeedController)getActivity()).hideFilterBySearch(true);
            }
        });

        searchUsersAdapter = new SearchUserAdapter(rootView.getContext(), new ArrayList<UserItem>());
        final ListView usersListView = (ListView) rootView.findViewById(R.id.usersListView);
        usersListView.setDivider(null);
        usersListView.setVisibility(View.GONE);
        usersListView.setAdapter(searchUsersAdapter);
        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((FeedController)getActivity()).hideFilterBySearch(true);
            }
        });

        populateItems();

        tagsBtn = (Button) rootView.findViewById(R.id.tagsBtn);
        tagsBtn.setTextColor(getResources().getColor(R.color.color_almost_black));
        tagsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTagsAdapter.clear();
                searchTagsAdapter.addAll(search_tags_list);
                tagsListView.setVisibility(View.VISIBLE);
                usersListView.setVisibility(View.GONE);
                tagsBtn.setBackgroundColor(getResources().getColor(R.color.color_white));
                usersBtn.setBackgroundColor(getResources().getColor(R.color.color_gray_default));
            }
        });

        usersBtn = (Button) rootView.findViewById(R.id.usersBtn);
        usersBtn.setTextColor(getResources().getColor(R.color.color_almost_black));
        usersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUsersAdapter.clear();
                searchUsersAdapter.addAll(search_users_list);
                usersListView.setVisibility(View.VISIBLE);
                tagsListView.setVisibility(View.GONE);
                tagsBtn.setBackgroundColor(getResources().getColor(R.color.color_gray_default));
                usersBtn.setBackgroundColor(getResources().getColor(R.color.color_white));
            }
        });

        tagsBtn.setBackgroundColor(getResources().getColor(R.color.color_white));
        usersBtn.setBackgroundColor(getResources().getColor(R.color.color_gray_default));

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateItems();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return rootView;
    }

    private void hideSoftKeyboard(){
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
    }

    private void populateItems(){
        search_tags_list.clear();
        searchTagsAdapter.clear();
        search_tags_list.add(new HashtagItem("style"));
        search_tags_list.add(new HashtagItem("styling"));
        search_tags_list.add(new HashtagItem("styleexpert"));
        searchTagsAdapter.notifyDataSetChanged();
        searchTagsAdapter.addAll(search_tags_list);

        search_users_list.clear();
        searchUsersAdapter.clear();
        search_users_list.add(new UserItem("stylizer"));
        search_users_list.add(new UserItem("styleseoul"));
        search_users_list.add(new UserItem("stylestyle"));
        searchUsersAdapter.notifyDataSetChanged();
        searchUsersAdapter.addAll(search_users_list);
    }

    private class SearchUserAdapter extends ArrayAdapter<UserItem> {
        public SearchUserAdapter(Context context, ArrayList<UserItem> searchItems){ super(context, 0, searchItems); }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            UserHolder holder;

            if(convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.item_search_users, parent, false);

                holder = new UserHolder();
                holder.profileImageView = (ImageView) convertView.findViewById(R.id.profileImageView);
                holder.usernameTextView = (TextView)convertView.findViewById(R.id.usernameTextView);

                convertView.setTag(holder);
            } else holder = (UserHolder)convertView.getTag();

            UserItem searchUserItem = getItem(position);
            AppController.getInstance().getImageLoader().get("http://www.bradfordcityfc.co.uk/images/common/bg_player_profile_default_big.png", ImageLoader.getImageListener(holder.profileImageView, R.drawable.feed_button_profile_default_m_56dp, R.drawable.feed_button_profile_default_m_56dp));
            holder.usernameTextView.setText(searchUserItem.username);
            holder.usernameTextView.setTextColor(getResources().getColor(R.color.color_better_dark));
            holder.usernameTextView.setTypeface(User.RalewayMedium);
            holder.usernameTextView.setTypeface(User.RalewayMedium);

            return convertView;
        }

        private class UserHolder {
            private ImageView profileImageView;
            private TextView usernameTextView;
        }
    }

    private class SearchTagsAdapter extends ArrayAdapter<HashtagItem> {
        public SearchTagsAdapter(Context context, ArrayList<HashtagItem> searchItems){ super(context, 0, searchItems); }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TagHolder holder;

            if(convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.item_search_tags, parent, false);

                holder = new TagHolder();
                holder.hashTextView = (TextView) convertView.findViewById(R.id.hashTextView);
                holder.tagTextView = (TextView)convertView.findViewById(R.id.tagsTextView);

                convertView.setTag(holder);
            } else holder = (TagHolder)convertView.getTag();

            HashtagItem searchTagItem = getItem(position);
            holder.hashTextView.setTypeface(User.RalewayMedium);
            holder.tagTextView.setText(searchTagItem.name);
            holder.tagTextView.setTypeface(User.RalewayMedium);

            return convertView;
        }

        private class TagHolder {
            private TextView hashTextView;
            private TextView tagTextView;
        }
    }

    public class UserItem {
        public String username;

        public UserItem(String username){
            this.username = username;
        }
    }

    public class HashtagItem {
        public String name;

        public HashtagItem(String name){
            this.name = name;
        }
    }
}