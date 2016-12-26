package com.kilogramm.mattermost.ui;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by kepar on 15.11.16.
 */
public class ScrollAwareFabBehavior extends FloatingActionButton.Behavior {

    private boolean isEnabled = true;

    public ScrollAwareFabBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout,
                                       FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target,
                        nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child,
                               View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
                dyUnconsumed);
        if (dyConsumed < 0) {
            animateFabDown(child);
        } else if (dyConsumed > 20) {
            animateFabUp(child);
        }
    }

    public void animateFabDown(FloatingActionButton child) {
        if(isEnabled){
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            int fab_bottomMargin = layoutParams.bottomMargin;
            child.animate().translationY(child.getHeight() + fab_bottomMargin + 5).setInterpolator(new LinearInterpolator()).start();
        }
    }

    public void animateFabUp(FloatingActionButton child) {
        if(isEnabled){
//            child.show();
            child.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();

        }
    }

    public void lockBehavior(){
        isEnabled = false;
    }

    public void unlockBehavior(){
        isEnabled = true;
    }
}