package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kilogramm.mattermost.model.entity.Channel;
import com.kilogramm.mattermost.model.entity.User;
import com.kilogramm.mattermost.network.websocket.WebSocketService;
import com.kilogramm.mattermost.view.menu.GeneralActivity;
import com.neovisionaries.ws.client.WebSocketException;

import io.realm.Realm;
import nucleus.presenter.Presenter;
/**
 * Created by kraftu on 14.09.16.
 */
public class GeneralPresenter extends Presenter<GeneralActivity> {

    Realm realm;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onTakeView(GeneralActivity generalActivity) {
        super.onTakeView(generalActivity);

        Channel channel = realm.where(Channel.class).equalTo("type", "O").findFirst();

        if(channel!=null)setSelectedChannel(channel.getId(),channel.getName());

        // WebSocketService.with(getApplicationContext()).run();


        //loadChannels(realm.where(Team.class).findFirst().getId());
    }

    private void loadChannels(String teamId){
        /*if(subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        MattermostApp application = MattermostApp.get(context);
        ApiMethod service = application.getMattermostRetrofitService();
        subscription = service.getChannelsTeam(teamId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ChannelsWithMembers>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "Complete load channel");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "Error");
                    }

                    @Override
                    public void onNext(ChannelsWithMembers channelsWithMembers) {
                        realm.executeTransaction(realm1 -> {
                            realm1.insertOrUpdate(channelsWithMembers.getChannels());
                            RealmList<User> users = new RealmList<User>();
                            users.addAll(channelsWithMembers.getMembers().values());
                            realm1.insertOrUpdate(users);
                        });
                        Log.d(TAG, "save in data base");
                    }
                });
                */

    }

    public void setSelectedDirect(String itemId,String name){
        String myId = realm.where(User.class).findFirst().getId();

        String channelId = realm.where(Channel.class)
                .equalTo("name", myId + "__" + itemId)
                .or()
                .equalTo("name", itemId + "__" + myId)
                .findFirst()
                .getId();
        getView().setFragmentChat(channelId,name,false);
    }

    public void setSelectedChannel(String channelId,String name){
        getView().setFragmentChat(channelId,name,true);
    }
}
