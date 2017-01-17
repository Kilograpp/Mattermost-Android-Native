package com.kilogramm.mattermost.rxtest.left_menu;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;

/**
 * Created by Evgeny on 17.01.2017.
 */

public class LeftMenuLayoutManager extends LinearLayoutManager {


    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }

    public LeftMenuLayoutManager(Context context) {
        super(context);
    }

    public LeftMenuLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public LeftMenuLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
