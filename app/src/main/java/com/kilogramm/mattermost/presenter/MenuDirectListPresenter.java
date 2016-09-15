package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.entity.User;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.view.menu.directList.MenuDirectListFragment;

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

public class MenuDirectListPresenter extends Presenter<MenuDirectListFragment> {

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onTakeView(MenuDirectListFragment menuDirectListFragment) {
        super.onTakeView(menuDirectListFragment);
    }

    public void onMoreClick() {
        getView().goToDirectListActivity();
    }

    /*private void showDMDialog(RealmList<User> users) {
        AlertDialog.Builder directMessagesDialog = new AlertDialog.Builder(context);
        StringBuilder stringBuilder = new StringBuilder();
        for (User user : users) {
            stringBuilder.append(user.getUsername() + "\n");
        }
        directMessagesDialog.setMessage(stringBuilder.toString()).show();
    }*/
}
