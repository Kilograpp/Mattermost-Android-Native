package com.kilogramm.mattermost.rxtest.left_menu.holders;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by ivan on 10.02.17.
 */

public class DirectItemDecoration extends RecyclerView.ItemDecoration {
    int verticalSpaceHeight;

    private final int[] ATTRS = new int[]{android.R.attr.listDivider};

    private Drawable divider;
    /**
     * Default divider will be used
     */
    public DirectItemDecoration(Context context){
        final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
        divider = styledAttributes.getDrawable(0);
        styledAttributes.recycle();
    }
    /**
     * Custom divider will be used
     */
    public DirectItemDecoration(Context context, int resId){
        divider = ContextCompat.getDrawable(context, resId);

    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + divider.getIntrinsicHeight();

            divider.setBounds(left, top, right, bottom);
            divider.draw(canvas);
        }
    }
}
