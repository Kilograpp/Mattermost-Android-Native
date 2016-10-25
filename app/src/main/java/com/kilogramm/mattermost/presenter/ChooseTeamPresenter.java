package com.kilogramm.mattermost.presenter;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.RealmString;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.authorization.ChooseTeamActivity;

import io.realm.Realm;

/**
 * Created by ngers on 25.10.16.
 */

public class ChooseTeamPresenter extends BaseRxPresenter<ChooseTeamActivity> {


    public void choisTeam(String id) {
        MattermostPreference.getInstance().setTeamId(id);
        clearDataBaseAfterSwichTeam();
        clearPreferenceTeam();
        sendShowChooseTeam();
    }

    private void clearDataBaseAfterSwichTeam() {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            realm1.delete(Post.class);
            realm1.delete(Channel.class);
            realm1.delete(RealmString.class);
        });
    }

    private void clearPreferenceTeam() {
        MattermostPreference.getInstance().setTeamId(null);
        MattermostPreference.getInstance().setLastChannelId(null);
    }

    private void sendShowChooseTeam() {
        createTemplateObservable(new Object())
                .subscribe(split((chooseTeamActivity, o) -> chooseTeamActivity.showChatActivity()));
    }
}
