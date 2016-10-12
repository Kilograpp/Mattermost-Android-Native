package com.kilogramm.mattermost.rxtest;

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
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.entity.ThemeProps;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.network.ApiMethod;

import java.util.Map;

import icepick.State;
import io.realm.Realm;
import io.realm.RealmList;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 05.10.2016.
 */
public class GeneralRxPresenter extends BaseRxPresenter<GeneralRxActivity> {
    public static final String TAG = "GeneralPresenter";

    private static final int REQUEST_DIRECT_PROFILE = 1;
    private static final int REQUEST_LOAD_CHANNELS = 2;
    private static final int REQUEST_USER_TEAM = 3;
    private static final int REQUEST_LOGOUT = 4;

    Realm realm;
    private UserRepository userRepository;
    private ChannelRepository channelRepository;

    @State String teamId;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        realm = Realm.getDefaultInstance();
        teamId = realm.where(Team.class).findFirst().getId();
        userRepository = new UserRepository();
        channelRepository = new ChannelRepository();
        MattermostApp application = MattermostApp.getSingleton();
        ApiMethod service = application.getMattermostRetrofitService();
        initRequest(service);

    }

    private void initRequest(ApiMethod service) {

        restartableFirst(REQUEST_DIRECT_PROFILE, () -> {
            return service.getDirectProfile()
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread());
        },(generalRxActivity, stringUserMap) -> {
            RealmList<User> users = new RealmList<>();
            users.addAll(stringUserMap.values());
            users.add(new User("materMostAll","all","Notifies everyone in the channel, use in Town Square to notify the whole team"));
            users.add(new User("materMostChannel","channel","Notifies everyone in the channel"));
            userRepository.add(users);
            requestLoadChannels();
        },(generalRxActivity1, throwable) -> {
            sendShowError(throwable.toString());
        });

        restartableFirst(REQUEST_LOAD_CHANNELS, () -> {
            return service.getChannelsTeam(teamId)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread());
        },(generalRxActivity, channelsWithMembers) -> {
            channelRepository.prepareChannelAndAdd(channelsWithMembers.getChannels(),
                    MattermostPreference.getInstance().getMyUserId(),userRepository);
            requestUserTeam();
        },(generalRxActivity1, throwable) -> {
            sendShowError(throwable.toString());
        });

        restartableFirst(REQUEST_USER_TEAM, () -> {
           return  service.getTeamUsers(teamId)
                   .subscribeOn(Schedulers.computation())
                   .observeOn(AndroidSchedulers.mainThread());
        },(generalRxActivity, stringUserMap) -> {
            userRepository.add(stringUserMap.values());
        },(generalRxActivity1, throwable) -> {
            throwable.printStackTrace();
        });

        restartableFirst(REQUEST_LOGOUT,() -> {
            return service.logout(new Object())
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread());
        },(generalRxActivity, logoutData) -> {
            Log.d(TAG, "Complete logout");
            clearDataBaseAfterLogout();
            clearPreference();
            sendShowMainRxActivity();
        },(generalRxActivity1, throwable) -> {
            throwable.printStackTrace();
            Log.d(TAG, "Error logout");
        });
    }

    public void requestLoadChannels(){
        start(REQUEST_LOAD_CHANNELS);
    }

    public void requestDirectProfile(){
        start(REQUEST_DIRECT_PROFILE);
    }

    public void requestUserTeam(){
        start(REQUEST_USER_TEAM);
    }

    public void requestLogout(){
        start(REQUEST_LOGOUT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onTakeView(GeneralRxActivity generalRxActivity) {
        super.onTakeView(generalRxActivity);
        requestDirectProfile();
        //loadChannels(realm.where(Team.class).findFirst().getId());
    }

//    private void loadDirectProfiles(){
//        if(subscription != null && !subscription.isUnsubscribed())
//            subscription.unsubscribe();
//        MattermostApp application = MattermostApp.getSingleton();
//        ApiMethod service = application.getMattermostRetrofitService();
//        subscription = service.getDirectProfile()
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new MattermostHttpSubscriber<Map<String, User>>() {
//                    @Override
//                    public void onErrorMattermost(HttpError httpError, Throwable e) {
//                        getView().showErrorText(httpError.toString());
//                    }
//
//                    @Override
//                    public void onCompleted() {
//                        Log.d(TAG, "complete load channels");
//                        loadChannels(teamId);
//                    }
//
//                    @Override
//                    public void onNext(Map<String, User> stringUserMap) {
//                        // channelRepository.add(channelsWithMembers.getChannels());
//                        RealmList<User> users = new RealmList<>();
//                        users.addAll(stringUserMap.values());
//                        users.add(new User("materMostAll","all","Notifies everyone in the channel, use in Town Square to notify the whole team"));
//                        users.add(new User("materMostChannel","channel","Notifies everyone in the channel"));
//                        userRepository.add(users);
//                    }
//                });
//
//    }

//    private void loadChannels(String teamId){
//        if(subscription != null && !subscription.isUnsubscribed())
//            subscription.unsubscribe();
//        MattermostApp application = MattermostApp.getSingleton();
//        ApiMethod service = application.getMattermostRetrofitService();
//        subscription = service.getChannelsTeam(teamId)
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new MattermostHttpSubscriber<ChannelsWithMembers>() {
//                    @Override
//                    public void onCompleted() {
//                        Log.d(TAG, "complete load channels");
//                        loadUsersTeam(teamId);
//                    }
//
//                    @Override
//                    public void onErrorMattermost(HttpError httpError, Throwable e) {
//                        getView().showErrorText(httpError.toString());
//                    }
//
//                    @Override
//                    public void onNext(ChannelsWithMembers channelsWithMembers) {
//                        channelRepository.prepareChannelAndAdd(channelsWithMembers.getChannels(),
//                                MattermostPreference.getInstance().getMyUserId(),userRepository);
//                    }
//                });
//
//    }

//    private void loadUsersTeam(String teamId){
//        if(subscription != null && !subscription.isUnsubscribed())
//            subscription.unsubscribe();
//        MattermostApp application = MattermostApp.getSingleton();
//        ApiMethod service = application.getMattermostRetrofitService();
//        subscription = service.getTeamUsers(teamId)
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Map<String, User>>() {
//                    @Override
//                    public void onCompleted() {
//                        Log.d(TAG, "complete load users");
//                        Channel channel = channelRepository.query(new ChannelByTypeSpecification("O")).first();
//                        if(channel!=null){
//                            setSelectedChannel(channel.getId(),channel.getName());
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onNext(Map<String, User> stringUserMap) {
//                        userRepository.add(stringUserMap.values());
//                    }
//                });
//    }

    public void setSelectedDirect(String itemId,String name){
//        String myId = realm.where(User.class).findFirst().getId();
//
//        String channelId = realm.where(Channel.class)
//                .equalTo("name", myId + "__" + itemId)
//                .or()
//                .equalTo("name", itemId + "__" + myId)
//                .findFirst()
//                .getId();
        sendSetFragmentChat(itemId,name,false);
    }

    public void setSelectedChannel(String channelId,String name){
        sendSetFragmentChat(channelId,name,true);
    }

//    public void logout() {
//        if(subscription != null && !subscription.isUnsubscribed())
//            subscription.unsubscribe();
//        MattermostApp application = MattermostApp.getSingleton();
//        ApiMethod service = application.getMattermostRetrofitService();
//        subscription = service.logout(new Object())
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<LogoutData>() {
//                    @Override
//                    public void onCompleted() {
//                        Log.d(TAG, "Complete logout");
//                        clearDataBaseAfterLogout();
//                        clearPreference();
//                        MainActivity.start(getView(), Intent.FLAG_ACTIVITY_NEW_TASK |
//                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                        Log.d(TAG, "Error logout");
//                    }
//                    @Override
//                    public void onNext(LogoutData logoutData) { }
//                });
//    }

    private void clearPreference() {
        MattermostPreference.getInstance().setAuthToken(null);
    }

    private void clearDataBaseAfterLogout(){
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


    //to view methods

    private void sendShowError(String error){
        Observable.just(error)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(deliverFirst())
                .subscribe(split(GeneralRxActivity::showErrorText));
    }

    private void sendShowMainRxActivity(){
        Observable.just(new Object())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(deliverFirst())
                .subscribe(split((mainRxAcivity,o) -> mainRxAcivity.showMainRxActivity()));

    }

    private void sendSetFragmentChat(String channelId, String name, Boolean isChannel) {
        Observable.just(new OpenChatObject(channelId, name, isChannel))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(deliverFirst())
                .subscribe(split((generalRxActivity1, openChatObject)
                        -> generalRxActivity1.setFragmentChat(openChatObject.getChannelId(),name,isChannel)));

    }



    public static class OpenChatObject {
        private String channelId;
        private String name;
        private Boolean isChannel;

        public OpenChatObject(String channelId, String name, Boolean isChannel) {
            this.channelId = channelId;
            this.name = name;
            this.isChannel = isChannel;
        }

        public String getChannelId() {
            return channelId;
        }
        public String getName() {
            return name;
        }
        public Boolean getChannel() {
            return isChannel;
        }
    }
}
