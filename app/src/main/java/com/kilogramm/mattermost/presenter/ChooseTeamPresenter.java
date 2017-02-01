package com.kilogramm.mattermost.presenter;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.UserMember;
import com.kilogramm.mattermost.model.entity.RealmString;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.member.Member;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.authorization.ChooseTeamActivity;

import io.realm.Realm;

/**
 * Created by ngers on 25.10.16.
 */

public class ChooseTeamPresenter extends BaseRxPresenter<ChooseTeamActivity> {

    public void chooseTeam(String id) {
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
            realm.delete(Member.class);
            realm.delete(ExtraInfo.class);
            realm1.delete(RealmString.class);
            realm1.delete(UserMember.class);
            realm1.where(User.class)
                    .notEqualTo("id", MattermostPreference.getInstance().getMyUserId());
        });
    }

    private void clearPreferenceTeam() {
        MattermostPreference.getInstance().setLastChannelId(null);
    }

    private void sendShowChooseTeam() {
        createTemplateObservable(new Object())
                .subscribe(split((chooseTeamActivity, o) -> chooseTeamActivity.showChatActivity()));
    }
}
