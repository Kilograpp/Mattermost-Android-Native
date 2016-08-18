package com.kilogramm.mattermost.viewmodel;

import android.content.Context;
import android.databinding.BaseObservable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.Channel;

/**
 * Created by Evgeny on 03.08.2016.
 */
public class ItemDiretionProfileViewModel extends BaseObservable implements ViewModel {

    private Channel channel;
    private Context context;

    /**
     *  1 - online
     *  2 - offline
     *  3 - missing
     *  4 - refresh
     */

    public ItemDiretionProfileViewModel(Context context, Channel channel){
        this.context = context;
        this.channel = channel;
    }

    public String getChannelName(){
        return channel.getUsername();}

    public Drawable getStatusIconDrawable() {
        switch (channel.getStatus()){
            case Channel.ONLINE:
                return context.getResources().getDrawable(R.drawable.status_online_drawable);
            case Channel.OFFLINE:
                return context.getResources().getDrawable(R.drawable.status_offline_drawable);
            case Channel.AWAY:
                return context.getResources().getDrawable(R.drawable.status_away_drawable);
            case Channel.REFRESH:
                return context.getResources().getDrawable(R.drawable.status_refresh_drawable);
            default:
                return context.getResources().getDrawable(R.drawable.status_offline_drawable);
        }
    }

    @Override
    public void destroy() {
        context = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }

    public void setChannel(Channel channel) {
        this.channel = channel;
        notifyChange();
    }

}
