package com.kilogramm.mattermost.rxtest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.adapters.DownloadsListAdapter;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfoRepository;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by kepar on 27.01.17.
 */
@RequiresPresenter(DownloadsListPresenter.class)
public class DownloadsListActivity  extends BaseActivity<DownloadsListPresenter>{

    private RecyclerView mRecyclerView;

    private DownloadsListAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads_list);

        setupToolbar("Files", true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new DownloadsListAdapter(this,
                FileInfoRepository.getInstance().getDownloadedFiles());
        mRecyclerView.setAdapter(mAdapter);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, DownloadsListActivity.class);
//        starter.putExtra();
        context.startActivity(starter);
    }
}
