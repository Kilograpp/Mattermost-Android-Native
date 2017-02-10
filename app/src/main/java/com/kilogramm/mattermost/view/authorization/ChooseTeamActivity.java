package com.kilogramm.mattermost.view.authorization;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.view.View;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.database.repository.TeamsRepository;
import com.kilogramm.mattermost.databinding.ActivityChooseTeamBinding;
import com.kilogramm.mattermost.presenter.ChooseTeamPresenter;
import com.kilogramm.mattermost.rxtest.GeneralRxActivity;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by Jeniks on 27.07.2016.
 */
@RequiresPresenter(ChooseTeamPresenter.class)
public class ChooseTeamActivity extends BaseActivity<ChooseTeamPresenter> implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DB_TEAMS_LIST = 1;

    private ActivityChooseTeamBinding binding;

    TeamListCursorAdapter teamListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_choose_team);
        initView();
        getLoaderManager().initLoader(DB_TEAMS_LIST, null, this);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new TeamsCursorLoader(getApplicationContext());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        teamListAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }


    private void initView() {
        setupToolbar(getString(R.string.choose_team), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);

        teamListAdapter = new TeamListCursorAdapter(this, null,this::setTeam);

        String siteName = MattermostPreference.getInstance().getSiteName();
        if (siteName != null && siteName.length() > 0)
            binding.siteName.setText(siteName);
        else
            binding.siteName.setVisibility(View.GONE);
        binding.timeList.setAdapter(teamListAdapter);
        binding.timeList.setLayoutManager(new LinearLayoutManager(this));

    }

    void setTeam(String id) {
        getPresenter().chooseTeam(id);
    }

    public void showChatActivity() {
        GeneralRxActivity.start(this,
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, ChooseTeamActivity.class);
        context.startActivity(starter);
    }

    static class TeamsCursorLoader extends CursorLoader {

        public TeamsCursorLoader(Context context) {
            super(context);
        }

        @Override
        public Cursor loadInBackground() {
            return TeamsRepository.getAllTeams();
        }
    }
}
