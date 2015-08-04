package com.astapley.thememe.better;

import android.widget.AbsListView;

public abstract class InfiniteListViewScrollListener implements AbsListView.OnScrollListener {
    private boolean isLoading = true;
    private int itemCount = 0, currentPage = 0, bufferItemCount = 10;

    public InfiniteListViewScrollListener() {}

    public abstract void loadMore(int page, int totalItemsCount);

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {}

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(totalItemCount < itemCount) {
            this.itemCount = totalItemCount;
            if(totalItemCount == 0) this.isLoading = true;
        }

        if(isLoading && (totalItemCount > itemCount)) {
            isLoading = false;
            itemCount = totalItemCount;
        } else if(!isLoading && ((totalItemCount - visibleItemCount) <= (firstVisibleItem + bufferItemCount))){
            currentPage++;
            loadMore(currentPage, totalItemCount);
            isLoading = true;
        }
    }
}
