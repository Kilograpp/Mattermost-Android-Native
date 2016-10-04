package com.kilogramm.mattermost.ui;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.kilogramm.mattermost.R;

/**
 * Created by Evgeny on 21.09.2016.
 */
public class ChatPopupMenu extends PopupMenu implements PopupMenu.OnMenuItemClickListener{



    public ChatPopupMenu(Context context, View anchor) {
        super(context, anchor);
        initView();
    }

    public ChatPopupMenu(Context context, View anchor, int gravity) {
        super(context, anchor, gravity);
        initView();
    }

    public ChatPopupMenu(Context context, View anchor, int gravity, int popupStyleAttr, int popupStyleRes) {
        super(context, anchor, gravity, popupStyleAttr, popupStyleRes);
        initView();
    }

    private void initView() {
        inflate(R.menu.my_chat_item_popupmenu);
    }


    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.edit:

                break;
            case R.id.delete:
                break;
        }
        return false;
    }
}
