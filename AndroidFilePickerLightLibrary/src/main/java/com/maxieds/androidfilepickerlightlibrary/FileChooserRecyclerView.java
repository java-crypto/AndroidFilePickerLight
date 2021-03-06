/*
        This program (the AndroidFilePickerLight library) is free software written by
        Maxie Dion Schmidt: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        The complete license provided with source distributions of this library is
        available at the following link:
        https://github.com/maxieds/AndroidFilePickerLight
*/

package com.maxieds.androidfilepickerlightlibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FileChooserRecyclerView extends RecyclerView {

    private static final String LOGTAG = FileChooserRecyclerView.class.getSimpleName();

    public FileChooserRecyclerView(Context layoutCtx) {
        super(layoutCtx);
        setupRecyclerViewLayout();
    }

    public FileChooserRecyclerView(Context context, AttributeSet attrSet) {
        super(context, attrSet);
        setupRecyclerViewLayout();
    }

    public FileChooserRecyclerView(Context context, AttributeSet attrSet, int defStyle) {
        super(context, attrSet, defStyle);
        setupRecyclerViewLayout();
    }

    public void setupRecyclerViewLayout() {

        setHasFixedSize(true);
        setNestedScrollingEnabled(false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        setLayoutParams(layoutParams);
        FileChooserRecyclerView.LayoutManager rvLayoutManager = new FileChooserRecyclerView.LayoutManager(getContext());
        setLayoutManager((FileChooserRecyclerView.LayoutManager) rvLayoutManager);
        addItemDecoration(
                new FileChooserRecyclerView.CustomDividerItemDecoration(
                        R.drawable.rview_file_item_divider,
                        DisplayFragments.getInstance().getFileItemLayoutStylizer()
                )
        );
        getItemAnimator().setChangeDuration(0);

    }

    // We want it to move when flung and be responsive, but keep an approximately
    // constant rate of movement through the items:
    public static final int DEFAULT_FLING_VELOCITY_DAMPENAT = 500;

    private static int FLING_VELOCITY_DAMPENAT = DEFAULT_FLING_VELOCITY_DAMPENAT;
    public static void setFlingVelocityDampenAtThreshold(int nextThreshold) {
        FLING_VELOCITY_DAMPENAT = nextThreshold;
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        if(Math.abs(velocityY) <= FLING_VELOCITY_DAMPENAT) {
            return super.fling(0, velocityY);
        }
        int scaledVelocityY = FLING_VELOCITY_DAMPENAT + (int) ((velocityY - FLING_VELOCITY_DAMPENAT) * Math.exp(-Math.pow(velocityY - FLING_VELOCITY_DAMPENAT, 0.25)));
        return super.fling(0, scaledVelocityY);
    }

    public interface RecyclerViewSlidingContextWindow {

        void setWeightBufferSize(int size);
        int getWeightBufferSize();

        int getActiveCountToBalanceTop();
        int getTopBufferPosition();
        int getActiveCountToBalanceBottom();
        int getBottomBufferPosition();

        int getLayoutVisibleDisplaySize();
        int getLayoutFirstVisibleItemIndex();
        int getLayoutLastVisibleItemIndex();
        int getActiveLayoutItemsCount();

    }

    /* See: https://developer.android.com/reference/androidx/recyclerview/widget/LinearSnapHelper */
    public static class LayoutManager extends LinearLayoutManager {

        private static LayoutManager localStaticInst = null;
        public static LayoutManager getInstance() { return localStaticInst; }

        public LayoutManager(Context layoutCtx) {
            super(layoutCtx);
            setOrientation(LinearLayoutManager.VERTICAL);
            setAutoMeasureEnabled(true);
            setReverseLayout(false);
            setStackFromEnd(true);
            setSmoothScrollbarEnabled(true);
            localStaticInst = this;
        }

        @Override
        public boolean isAutoMeasureEnabled() {
            return true;
        }

        /*
        public static final float SCROLLER_MILLISECONDS_PER_INCH = 16.0f; // larger values slow it down, 25.0 ~ default behavior
        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, State state, int position) {
            scrollInvokingRV = recyclerView;
            linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
                @Override
                protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                    return SCROLLER_MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
                }
            };
            linearSmoothScroller.setTargetPosition(position);
            startSmoothScroll(linearSmoothScroller);
        }
        */

    }

    public static class CustomDividerItemDecoration extends RecyclerView.ItemDecoration {

        private static final int[] DIVIDER_DEFAULT_ATTRS = new int[]{
                android.R.attr.listDivider,
                android.R.attr.verticalDivider,
                android.R.attr.horizontalDivider
        };
        private Drawable listingsDivider;

        public static final int LIST_DIVIDER_STYLE_INDEX = 0;
        public static final int DEFAULT_DIVIDER_STYLE_INDEX = 1;

        public CustomDividerItemDecoration(Context ctx, int dividerTypeIndex, boolean dividerTypeIsVertical) {
            final TypedArray styledDefaultAttributes = ctx.obtainStyledAttributes(DIVIDER_DEFAULT_ATTRS);
            if(dividerTypeIndex != LIST_DIVIDER_STYLE_INDEX) {
                dividerTypeIndex = DEFAULT_DIVIDER_STYLE_INDEX + (dividerTypeIsVertical ? 0 : 1);
            }
            listingsDivider = styledDefaultAttributes.getDrawable(dividerTypeIndex);
            styledDefaultAttributes.recycle();
        }

        public CustomDividerItemDecoration(int resId, CustomThemeBuilder.FileItemLayoutStylizer layoutStylizer) {
            if(layoutStylizer != null) {
                Drawable baseDivider = DisplayUtils.getDrawableFromResource(resId);
                layoutStylizer.applyStyleToLayoutDivider(baseDivider);
                listingsDivider = baseDivider;
            }
            else {
                listingsDivider = DisplayUtils.getDrawableFromResource(resId);
            }
        }

        public static void setMarginAdjustments(int leftAdjust, int topAdjust, int rightAdjust, int bottomAdjust) {
            MARGIN_RIGHT_ADJUST = rightAdjust;
            MARGIN_LEFT_ADJUST = leftAdjust;
            MARGIN_TOP_ADJUST = topAdjust;
            MARGIN_BOTTOM_ADJUST = bottomAdjust;
        }

        private static int MARGIN_RIGHT_ADJUST = 35;
        private static int MARGIN_LEFT_ADJUST = 35;
        private static int MARGIN_TOP_ADJUST = 0;
        private static int MARGIN_BOTTOM_ADJUST = 0;

        @Override
        public void onDraw(Canvas displayCanvas, RecyclerView parentContainerView, RecyclerView.State rvState) {
            int leftMargin = parentContainerView.getPaddingLeft() + MARGIN_LEFT_ADJUST;
            int rightMargin = parentContainerView.getWidth() - parentContainerView.getPaddingRight() - MARGIN_RIGHT_ADJUST;
            for (int i = 0; i < parentContainerView.getChildCount(); i++) {
                View childView = parentContainerView.getChildAt(i);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) childView.getLayoutParams();
                int topMargin = childView.getBottom() + params.bottomMargin + MARGIN_TOP_ADJUST;
                int bottomMargin = topMargin + listingsDivider.getIntrinsicHeight() + MARGIN_BOTTOM_ADJUST;
                listingsDivider.setBounds(leftMargin, topMargin, rightMargin, bottomMargin);
                listingsDivider.draw(displayCanvas);
            }
        }

    }

}
