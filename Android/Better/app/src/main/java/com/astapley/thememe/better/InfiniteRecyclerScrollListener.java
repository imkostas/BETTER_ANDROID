package com.astapley.thememe.better;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

public abstract class InfiniteRecyclerScrollListener extends RecyclerView.OnScrollListener {
    private boolean isLoading = true;
    private int itemCount = 0, currentPage = 0, bufferItemCount = 2;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    private LinearLayoutManager mLinearLayoutManager;

    public InfiniteRecyclerScrollListener(LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
    }

    public abstract void loadMore(int current_page);

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();
        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mLinearLayoutManager.getItemCount();

        if (isLoading && totalItemCount > itemCount) {
            isLoading = false;
            itemCount = totalItemCount;
        } else if (!isLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + bufferItemCount)) {
            currentPage++;
            loadMore(currentPage);
            isLoading = true;
        }
    }
}
