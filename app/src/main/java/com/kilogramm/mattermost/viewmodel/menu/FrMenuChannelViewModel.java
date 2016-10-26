package com.kilogramm.mattermost.viewmodel.menu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.kilogramm.mattermost.view.addchat.AddExistingChannelsActivity;
import com.kilogramm.mattermost.view.menu.channelList.MenuChannelListFragment;
import com.kilogramm.mattermost.viewmodel.ViewModel;

/**
 * Created by Evgeny on 24.08.2016.
 */
public class FrMenuChannelViewModel implements ViewModel {

    private Context context;

    public FrMenuChannelViewModel(Context context) {
        this.context = context;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    }
}
