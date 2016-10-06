package com.kilogramm.mattermost.presenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.InitObject;
import com.kilogramm.mattermost.model.entity.LicenseCfg;
import com.kilogramm.mattermost.model.entity.NotifyProps;
import com.kilogramm.mattermost.model.entity.RealmString;
import com.kilogramm.mattermost.model.entity.SaveData;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.entity.ThemeProps;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelByIdSpecification;
import com.kilogramm.mattermost.model.entity.channel.ChannelByTypeSpecification;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.error.HttpError;
import com.kilogramm.mattermost.model.fromnet.ChannelsWithMembers;
import com.kilogramm.mattermost.model.fromnet.LogoutData;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.network.MattermostHttpSubscriber;
import com.kilogramm.mattermost.view.authorization.MainActivity;
import com.kilogramm.mattermost.view.menu.GeneralActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import nucleus.presenter.Presenter;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by kraftu on 14.09.16.
 */
public class GeneralPresenter extends Presenter<GeneralActivity> {
    private static final String TAG = "GeneralPresenter";

    Realm realm;
    Subscription subscription;
    private UserRepository userRepository;
    private ChannelRepository channelRepository;
    private String teamId;

    private boolean setDialogFragment = false;
    private SaveData mSaveData;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        realm = Realm.getDefaultInstance();
        userRepository = new UserRepository();
        channelRepository = new ChannelRepository();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onTakeView(GeneralActivity generalActivity) {
        super.onTakeView(generalActivity);
        teamId = realm.where(Team.class).findFirst().getId();
        loadDirectProfiles();
        //loadChannels(realm.where(Team.class).findFirst().getId());
    }

    private void loadDirectProfiles() {
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        MattermostApp application = MattermostApp.getSingleton();
        ApiMethod service = application.getMattermostRetrofitService();
        subscription = service.getDirectProfile()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MattermostHttpSubscriber<Map<String, User>>() {
                    @Override
                    public void onErrorMattermost(HttpError httpError, Throwable e) {
                        getView().showErrorText(httpError.toString());
                    }

                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "complete load channels");
                        loadChannels(teamId);
                    }

                    @Override
                    public void onNext(Map<String, User> stringUserMap) {
                        // channelRepository.add(channelsWithMembers.getChannels());
                        RealmList<User> users = new RealmList<>();
                        users.addAll(stringUserMap.values());
                        users.add(new User("materMostAll", "all", "Notifies everyone in the channel, use in Town Square to notify the whole team"));
                        users.add(new User("materMostChannel", "channel", "Notifies everyone in the channel"));
                        userRepository.add(users);
                    }
                });

    }

    private void loadChannels(String teamId) {
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        MattermostApp application = MattermostApp.getSingleton();
        ApiMethod service = application.getMattermostRetrofitService();
        subscription = service.getChannelsTeam(teamId)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MattermostHttpSubscriber<ChannelsWithMembers>() {
                    @Override
                    public void onCompleted() {
                        if (setDialogFragment) {
                            setSelectedDirect(mSaveData.getUser_id(), mSaveData.getName());
                            Log.d(TAG, "Must open direct dialog");
                            setDialogFragment = false;
                        }
                        Log.d(TAG, "complete load channels");
                        // loadUsersTeam(teamId);
                    }

                    @Override
                    public void onErrorMattermost(HttpError httpError, Throwable e) {
                        getView().showErrorText(httpError.toString());
                    }

                    @Override
                    public void onNext(ChannelsWithMembers channelsWithMembers) {
                        channelRepository.prepareChannelAndAdd(channelsWithMembers.getChannels(),
                                MattermostPreference.getInstance().getMyUserId(), userRepository);
                    }
                });

    }

   /* private void loadUsersTeam(String teamId){
        if(subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        MattermostApp application = MattermostApp.getSingleton();
        ApiMethod service = application.getMattermostRetrofitService();
        subscription = service.getTeamUsers(teamId)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, User>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "complete load users");
                        Channel channel = channelRepository.query(new ChannelByTypeSpecification("O")).first();
                        if(channel!=null){
                            setSelectedChannel(channel.getId(),channel.getName());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Map<String, User> stringUserMap) {
                        userRepository.add(stringUserMap.values());
                    }
                });
    }*/

    public void save(SaveData saveData) {
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();

        MattermostApp application = MattermostApp.getSingleton();
        ApiMethod service = application.getMattermostRetrofitService();
        if (saveData != null) {
            List<SaveData> toSend = new ArrayList<>();
            toSend.add(saveData);
            subscription = service.save(toSend)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Boolean>() {
                        @Override
                        public void onCompleted() {
                            Realm realm = Realm.getDefaultInstance();
                            String teamId = realm.where(Team.class).findFirst().getId();
                            realm.close();
                            loadChannels(teamId);
                            setDialogFragment = true;
                            mSaveData = saveData;
                            Log.d(TAG, "mSaveData created");
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onNext(Boolean aBoolean) {
                            Log.d(TAG, "onNext");
                            if (!aBoolean) {
                                Log.d(TAG, "Save didn`t work out");
                            }
                        }
                    });
        }
    }

    @Override
    public void takeView(GeneralActivity generalActivity) {
        super.takeView(generalActivity);
        setSelectedLast(MattermostPreference.getInstance().getLastChannelId());
    }

    public void setSelectedLast(String id) {
        Channel channel;
        if (id != null) {
            channel = channelRepository.query(new ChannelByIdSpecification(id)).first();
            if (channel != null)
                switch (channel.getType()) {
                    case "O":
                        setSelectedChannel(channel.getId(), channel.getName());
                        break;
                    case "D":
                        setSelectedDirect(channel.getId(), channel.getUsername());
                        break;
                    case "P":
                        break;
                }
        } else {
            RealmResults<Channel> channels = channelRepository.query(new ChannelByTypeSpecification("O"));
            if (channels.size() != 0) {
                setSelectedChannel(channels.first().getId(), channels.first().getName());
            } else {
                channels.addChangeListener(element -> {
                    if (element.size() != 0)
                        setSelectedChannel(element.first().getId(), element.first().getName());
                });
            }
        }
    }

    public void setSelectedDirect(String itemId, String name) {
        /*String myId = realm.where(User.class).findFirst().getId();

        String channelId = realm.where(Channel.class)
                .equalTo("name", myId + "__" + itemId)
                .or()
                .equalTo("name", itemId + "__" + myId)
                .findFirst()
                .getId();*/
        if (getView() != null) {
            getView().setFragmentChat(itemId, name, false);
        }
        MattermostPreference.getInstance().setLastChannelId(itemId);
    }

    public void setSelectedChannel(String channelId, String name) {
        if (getView() != null) {
            getView().setFragmentChat(channelId, name, true);
        }
        MattermostPreference.getInstance().setLastChannelId(channelId);
    }

    public void logout() {
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        MattermostApp application = MattermostApp.getSingleton();
        ApiMethod service = application.getMattermostRetrofitService();
        subscription = service.logout(new Object())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<LogoutData>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "Complete logout");
                        clearDataBaseAfterLogout();
                        clearPreference();
                        MainActivity.start(getView(), Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "Error logout");
                    }

                    @Override
                    public void onNext(LogoutData logoutData) {
                    }
                });
    }

    private void clearPreference() {
        MattermostPreference.getInstance().setAuthToken(null);
    }

    private void clearDataBaseAfterLogout() {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            realm.delete(Post.class);
            realm.delete(Channel.class);
            realm.delete(InitObject.class);
            realm.delete(LicenseCfg.class);
            realm.delete(NotifyProps.class);
            realm.delete(RealmString.class);
            realm.delete(Team.class);
            realm.delete(InitObject.class);
            realm.delete(ThemeProps.class);
            realm.delete(User.class);
        });
        realm.close();
    }
}
