package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kilogramm.mattermost.view.menu.directList.MenuDirectListFragment;

import nucleus.presenter.Presenter;

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
}
