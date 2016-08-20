package com.kilogramm.mattermost.view.fragments;

import android.support.v4.app.Fragment;
import android.view.View;

import com.kilogramm.mattermost.view.BaseActivity;

/**
 * Created by Evgeny on 19.08.2016.
 */
public class BaseFragment extends Fragment {

    protected void setupToolbar(String activityTitle,String channelName, View.OnClickListener listener){
        ((BaseActivity) getActivity()).setupChannelToolbar(activityTitle,channelName,listener);
    }


}
