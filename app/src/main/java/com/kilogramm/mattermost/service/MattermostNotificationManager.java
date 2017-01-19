package com.kilogramm.mattermost.service;

import android.util.Log;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.Preference.PreferenceRepository;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.member.MembersRepository;
import com.kilogramm.mattermost.model.entity.post.PostRepository;
import com.kilogramm.mattermost.model.entity.userstatus.AllRemove;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatusRepository;
import com.kilogramm.mattermost.model.fromnet.ChannelWithMember;
import com.kilogramm.mattermost.model.websocket.WebSocketObj;
import com.kilogramm.mattermost.network.ServerMethod;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 23.09.2016.
 */
public class MattermostNotificationManager {

    private static final String TAG = "Websocket";

    private MattermostService service;

    public MattermostNotificationManager(MattermostService service) {
        this.service = service;
    }

    public void handleSocket(WebSocketObj webSocketObj) {
        switch (webSocketObj.getEvent()) {
            case WebSocketObj.EVENT_CHANNEL_VIEWED:
                requestGetChannel(webSocketObj.getData().getChannelId());
                break;
            case WebSocketObj.EVENT_POST_DELETED:
                PostRepository.remove(webSocketObj.getData().getPost());
                break;
            case WebSocketObj.EVENT_POST_EDITED:
                break;
            case WebSocketObj.EVENT_POSTED:
                String channelId = webSocketObj.getData().getPost().getChannelId();
                if (channelId.equals(MattermostPreference.getInstance().getLastChannelId())) {
                    requestUpdateLastViewedAt(webSocketObj.getChannelId());
                } else {
                    requestGetChannel(channelId);
                }
                break;
            case WebSocketObj.EVENT_STATUS_CHANGE:
                Log.d(TAG, "EVENT_STATUS_CHANGE: useid = " + webSocketObj.getUserId() + "\n" +
                        "status = " + webSocketObj.getData().getStatus());
                UserStatusRepository.add(new UserStatus(webSocketObj.getUserId(), webSocketObj.getData().getStatus()));
                //userRepository.updateUserStatus(webSocketObj.getUserId(), webSocketObj.getData().getStatus());
                break;
            case WebSocketObj.EVENT_TYPING:
                break;
            case WebSocketObj.ALL_USER_STATUS:
                Observable.just(webSocketObj)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(webSocketObj1 -> {
                            Log.d(TAG, "handleSocket: ALL_USER");
                            UserStatusRepository.remove(new AllRemove());
                            UserStatusRepository.add(Stream.of(webSocketObj1.getData().getStatusMap())
                                    .map(stringStringEntry -> new UserStatus(stringStringEntry.getKey(), stringStringEntry.getValue()))
                                    .collect(Collectors.toList()));
                           /* UserStatusRepository.add();
                            for (String s : webSocketObj1.getData().getStatusMap().keySet()) {
                                UserStatusRepository.add(new UserStatus(s, webSocketObj1.getData().getStatusMap().get(s)));
                            }*/
                        });
                break;
            case WebSocketObj.EVENT_PREFERENCE_CHANGED:
                PreferenceRepository.add(webSocketObj.getData().getPreference());
                break;
        }
    }

    private void requestUpdateLastViewedAt(String channelId) {
        MattermostApp.getSingleton()
                .getMattermostRetrofitService()
                .updatelastViewedAt(MattermostPreference.getInstance().getTeamId(), channelId);
    }

    private void requestGetChannel(String channelId) {
        ServerMethod.getInstance()
                .getChannel(MattermostPreference.getInstance().getTeamId(),
                        channelId)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<ChannelWithMember>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, e.getMessage());
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ChannelWithMember channelWithMember) {
                        Log.d(TAG, "onNext: Channel loaded after posted event");
                        ChannelRepository.add(channelWithMember.getChannel());
                        MembersRepository.add(channelWithMember.getMember());
                    }
                });
    }
}
