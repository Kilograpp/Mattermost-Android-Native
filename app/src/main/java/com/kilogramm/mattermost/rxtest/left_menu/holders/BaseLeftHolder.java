package com.kilogramm.mattermost.rxtest.left_menu.holders;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.kilogramm.mattermost.R;


class BaseLeftHolder extends RecyclerView.ViewHolder {

    BaseLeftHolder(View itemView) {
        super(itemView);
    }

    void setTextWhite(Context context, TextView channelName, TextView unreadedMessage) {
        channelName.setTextColor(context.getResources().getColor(R.color.white));
        unreadedMessage.setTextColor(context.getResources().getColor(R.color.white));
    }

    void setTextBlack(Context context, TextView channelName, TextView unreadedMessage) {
        channelName.setTextColor(context.getResources().getColor(R.color.black));
        unreadedMessage.setTextColor(context.getResources().getColor(R.color.black));
    }

    void setDefault(Context context, TextView channelName, TextView unreadedMessage) {
        channelName.setTypeface(Typeface.DEFAULT);
        channelName.setTextSize(12f);
        channelName.setTextColor(context.getResources().getColor(R.color.very_light_grey));
        unreadedMessage.setTypeface(Typeface.DEFAULT);
        unreadedMessage.setTextSize(12f);
        unreadedMessage.setTextColor(context.getResources().getColor(R.color.very_light_grey));
    }

    void setBold(Context context, TextView channelName, TextView unreadedMessage) {
        channelName.setTypeface(null, Typeface.BOLD);
        channelName.setTextSize(15f);
        channelName.setTextColor(context.getResources().getColor(R.color.white));
        unreadedMessage.setTypeface(null, Typeface.BOLD);
        unreadedMessage.setTextSize(15f);
        unreadedMessage.setTextColor(context.getResources().getColor(R.color.white));
    }
}
