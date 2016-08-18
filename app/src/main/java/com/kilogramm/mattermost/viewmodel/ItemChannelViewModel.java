package com.kilogramm.mattermost.viewmodel;

import android.content.Context;
import android.databinding.BaseObservable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.Channel;

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
        return channel.getDisplayName();}


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
