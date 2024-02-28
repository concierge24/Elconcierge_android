package com.trava.utilities;

import android.content.Context;
import android.graphics.PointF;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

public class SnappingLinearLayoutManager extends LinearLayoutManager {
 
    public SnappingLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    } 
 
    @Override 
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
                                       int position) {
        RecyclerView.SmoothScroller smoothScroller = new RecyclerView.SmoothScroller() {
            @Override
            protected void onStart() {

            }

            @Override
            protected void onStop() {

            }

            @Override
            protected void onSeekTargetStep(int dx, int dy, @NonNull RecyclerView.State state, @NonNull Action action) {

            }

            @Override
            protected void onTargetFound(@NonNull View targetView, @NonNull RecyclerView.State state, @NonNull Action action) {

            }
        };
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    } 
 
    private class TopSnappedSmoothScroller extends LinearSmoothScroller {
        public TopSnappedSmoothScroller(Context context) {
            super(context);
 
        } 
 
        @Override 
        public PointF computeScrollVectorForPosition(int targetPosition) {
            return SnappingLinearLayoutManager.this
                    .computeScrollVectorForPosition(targetPosition);
        } 
 
        @Override 
        protected int getVerticalSnapPreference() { 
            return SNAP_TO_START; 
        } 
    } 
} 