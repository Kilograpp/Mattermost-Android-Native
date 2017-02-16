package com.kilogramm.mattermost.rxtest.left_menu.holders;

import android.content.Context;
import android.view.View;

import com.kilogramm.mattermost.rxtest.left_menu.direct.IDirect;

/**
 * Created by ivan on 13.02.17.
 */

public abstract class BaseDirectViewHolder extends BaseLeftHolder {
    public BaseDirectViewHolder(View itemView) {
        super(itemView);
    }

    public abstract <T extends IDirect> void bindTo (T iDirect, Context context);
}
