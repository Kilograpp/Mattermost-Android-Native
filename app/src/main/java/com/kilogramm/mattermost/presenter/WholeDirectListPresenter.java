package com.kilogramm.mattermost.presenter;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.Channel;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.entity.User;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.view.direct.WholeDirectListActivity;

import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;
import nucleus.presenter.Presenter;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by melkshake on 14.09.16.
 */
public class WholeDirectListPresenter extends Presenter<WholeDirectListActivity> {
    private static final String TAG = "WholeDirListPresenter";

    private MattermostApp mMattermostApp;
    private Subscription mSubscription;
    private Realm realm;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);

        mMattermostApp = MattermostApp.getSingleton();
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onTakeView(WholeDirectListActivity wholeDirectListActivity) {
        super.onTakeView(wholeDirectListActivity);
    }

    public void getProfilesForDirectMessage() {
        if (mSubscription != null && !mSubscription.isUnsubscribed())
            mSubscription.unsubscribe();

        Realm realm = Realm.getDefaultInstance();
        Team team = realm.where(Team.class).findFirst();
        RealmList<User> users = new RealmList<>();

        ApiMethod service = mMattermostApp.getMattermostRetrofitService();
        mSubscription = service.getProfilesForDMList(team.getId())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, User>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "Complete load profiles for direct messages list");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Error load profiles for direct messages list");
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Map<String, User> stringUserMap) {
                        Log.d(TAG, "onNext success");
                        realm.executeTransaction(realm1 -> {
                            for (User user : stringUserMap.values()) {
                                users.add(user);
                            }
                            realm1.insertOrUpdate(users);
                        });
                    }
                });
    }

//    public Drawable getStatusIconDrawable() {
//        switch (channel.getStatus()){
//            case Channel.ONLINE:
//                return context.getResources().getDrawable(R.drawable.status_online_drawable);
//            case Channel.OFFLINE:
//                return context.getResources().getDrawable(R.drawable.status_offline_drawable);
//            case Channel.AWAY:
//                return context.getResources().getDrawable(R.drawable.status_away_drawable);
//            case Channel.REFRESH:
//                return context.getResources().getDrawable(R.drawable.status_refresh_drawable);
//            default:
//                return context.getResources().getDrawable(R.drawable.status_offline_drawable);
//        }
//    }
}
