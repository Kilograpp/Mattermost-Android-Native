package com.kilogramm.mattermost.view.fragments;

import android.os.Bundle;
import android.view.View;

import com.kilogramm.mattermost.view.BaseActivity;

import icepick.Icepick;
import nucleus.presenter.Presenter;
import nucleus.view.NucleusFragment;

/**
 * Created by Evgeny on 19.08.2016.
 */
public abstract class BaseFragment<P extends Presenter> extends NucleusFragment<P> {

    protected void setupToolbar(String activityTitle, String channelName,
                                View.OnClickListener listener1, View.OnClickListener listener2) {
        ((BaseActivity) getActivity()).setupChannelToolbar(
                activityTitle, channelName, listener1, listener2);
    }

    protected void setupTypingText(String text) {
        if ((getActivity()) != null)
            ((BaseActivity) getActivity()).setupTypingText(text);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Icepick.restoreInstanceState(this, bundle);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        Icepick.saveInstanceState(this, bundle);
    }
}
