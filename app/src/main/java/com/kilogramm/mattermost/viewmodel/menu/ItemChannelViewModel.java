package com.kilogramm.mattermost.viewmodel.menu;

import android.content.Context;
import android.databinding.BaseObservable;
import android.os.Bundle;

import com.kilogramm.mattermost.model.entity.Channel;
import com.kilogramm.mattermost.viewmodel.ViewModel;

/**
 * Created by Evgeny on 03.08.2016.
 */
public class ItemChannelViewModel extends BaseObservable implements ViewModel {
    
    private Channel channel;
    private Context context;

    public ItemChannelViewModel(Context context, Channel channel){
        this.context = context;
        this.channel = channel;
    }


    public String getChannelName(){
        return "# " + channel.getDisplayName();
    }

    public String getUnreadedMessage(){
        Integer unreadedMessage = channel.getUnreadedMessage();
        if(unreadedMessage != 0) {
            return channel.getUnreadedMessage() + "";
        } else {
            return  "";
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

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
        notifyChange();
    }

}
