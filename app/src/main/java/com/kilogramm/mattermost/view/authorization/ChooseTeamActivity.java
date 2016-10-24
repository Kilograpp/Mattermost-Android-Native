package com.kilogramm.mattermost.view.authorization;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityChooseTeamBinding;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.rxtest.GeneralRxActivity;
import com.kilogramm.mattermost.view.BaseActivity;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Jeniks on 27.07.2016.
 */
public class ChooseTeamActivity extends BaseActivity {

    private ActivityChooseTeamBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_choose_team);
        initView();
    }

    private void initView() {
        setupToolbar(getString(R.string.title_forgot_password), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);

        Realm realm = Realm.getDefaultInstance();
        RealmResults<Team> teams = realm.where(Team.class).findAll();

        TeamListAdapter teamListAdapter = new TeamListAdapter(this, teams, id -> showChatActivity(id));

        binding.timeList.setAdapter(teamListAdapter);
        binding.timeList.setLayoutManager(new LinearLayoutManager(this));
        realm.close();
    }

    public void showChatActivity(String id) {
        MattermostPreference.getInstance().setTeamId(id);
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
