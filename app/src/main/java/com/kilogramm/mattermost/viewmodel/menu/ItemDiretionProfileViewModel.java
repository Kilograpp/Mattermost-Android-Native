package com.kilogramm.mattermost.viewmodel.menu;

import android.content.Context;
import android.databinding.BaseObservable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.viewmodel.ViewModel;

/**
 * Created by Evgeny on 03.08.2016.
 */
public class ItemDiretionProfileViewModel extends BaseObservable implements ViewModel {

    public static final String TAG = "ItemDiretion";

    private Channel channel;
    private Context context;

    public ItemDiretionProfileViewModel(Context context, Channel channel){
        this.context = context;
        this.channel = channel;
    }

    public String getChannelName(){
        return channel.getUser().getUsername();}

    /*public Drawable getStatusIconDrawable() {
        switch (channel.getUser().getStatus()){
            case User.ONLINE:
                return context.getResources().getDrawable(R.drawable.status_online_drawable);
            case User.OFFLINE:
                return context.getResources().getDrawable(R.drawable.status_offline_drawable);
            case User.AWAY:
                return context.getResources().getDrawable(R.drawable.status_away_drawable);
            case User.REFRESH:
                return context.getResources().getDrawable(R.drawable.status_refresh_drawable);
            default:
                return context.getResources().getDrawable(R.drawable.status_offline_drawable);
        }
    }*/

    public String getUnreadedMessage(){
        /*Integer unreadedMessage = channel.getUnreadedMessage();
        if(unreadedMessage != 0) {
            return channel.getUnreadedMessage() + "";
        } else {
            return  "";
        }*/
        return "";
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
