package com.kilogramm.mattermost.view.authorization;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.view.View;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityChooseTeamBinding;
import com.kilogramm.mattermost.model.entity.team.Team;
import com.kilogramm.mattermost.model.entity.team.TeamRepository;
import com.kilogramm.mattermost.presenter.ChooseTeamPresenter;
import com.kilogramm.mattermost.rxtest.GeneralRxActivity;
import com.kilogramm.mattermost.view.BaseActivity;

import io.realm.RealmResults;
import nucleus.factory.RequiresPresenter;

/**
 * Created by Jeniks on 27.07.2016.
 */
@RequiresPresenter(ChooseTeamPresenter.class)
public class ChooseTeamActivity extends BaseActivity<ChooseTeamPresenter> {

    private ActivityChooseTeamBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_choose_team);
        initView();
    }

    private void initView() {
        setupToolbar(getString(R.string.choose_team), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);

        RealmResults<Team> teams = TeamRepository.query();
        TeamListAdapter teamListAdapter = new TeamListAdapter(this, teams, this::setTeam);
        String siteName = MattermostPreference.getInstance().getSiteName();
        if (siteName != null && siteName.length() > 0)
            binding.siteName.setText(siteName);
        else
            binding.siteName.setVisibility(View.GONE);
        binding.timeList.setAdapter(teamListAdapter);
        binding.timeList.setLayoutManager(new LinearLayoutManager(this));

    }

    void setTeam(String id){
        getPresenter().chooseTeam(id);
    }

    public void showChatActivity() {
        GeneralRxActivity.start(this,
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, ChooseTeamActivity.class);
        context.startActivity(starter);
    }


}
