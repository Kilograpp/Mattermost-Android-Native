package com.kilogramm.mattermost.rxtest;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by kepar on 27.01.17.
 */
@RequiresPresenter(DownloadsListPresenter.class)
public class DownloadsListActivity  extends BaseActivity<DownloadsListPresenter>{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads_list);
    }
}
