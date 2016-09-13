package com.kilogramm.mattermost.viewmodel.menu;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.entity.User;
import com.kilogramm.mattermost.model.entity.UsersForDM;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.viewmodel.ViewModel;

import io.realm.Realm;
import io.realm.RealmList;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 23.08.2016.
 */
public class FrMenuDirectViewModel implements ViewModel {
    private static final String TAG = "FrMenuDirectViewModel";

    private Realm realm;
    private Context context;
    private Subscription subscription;

    public FrMenuDirectViewModel(Context context) {
        this.context = context;
        this.realm = Realm.getDefaultInstance();
    }

    public void onMoreClick(View v) {
//        Toast.makeText(context, "click more", Toast.LENGTH_SHORT).show();
        Team teamId = realm.where(Team.class).findFirst();
        getProfilesForDirectMessage(teamId.getId());
    }

    private void getProfilesForDirectMessage(String teamId) {
        Log.d(TAG, "TEAM_ID " + teamId);

        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();

        MattermostApp application = MattermostApp.get(context);
        ApiMethod service = application.getMattermostRetrofitService();

        subscription = service.getProfilesForDMList(teamId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UsersForDM>() {
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
                    public void onNext(UsersForDM usersForDM) {
                        Log.d(TAG, "USERS_FOR_DM " + usersForDM);
                        realm.executeTransaction(realm1 -> {
                            RealmList<User> users = new RealmList<>();
                            for (User user : usersForDM.getDmList().values()) {
                                if (user.getUsername() != null && !user.getId().equals(teamId))
                                    users.add(user);
                            }
                            realm1.insertOrUpdate(users);
//
                            showDMDialog(users);
                            realm.close();
                        });
                    }
                });
    }

    private void showDMDialog(RealmList<User> users) {
        AlertDialog.Builder directMessagesDialog = new AlertDialog.Builder(context);
        StringBuilder stringBuilder = new StringBuilder();

//        Realm realm = Realm.getDefaultInstance();
//        RealmResults<UsersForDM> all = realm.where(UsersForDM.class).findAll();
//        for (int i = 0; i < users.size(); i++) {
//            stringBuilder.append(i + "). " + users.get(i).getDmList().get(i) + "\n");
//        }

        for (User user : users) {
            stringBuilder.append(user.getUsername() + "\n");
        }

        directMessagesDialog.setMessage(stringBuilder.toString()).show();

//        realm.close();
    }

    @Override
    public void destroy() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }
}
