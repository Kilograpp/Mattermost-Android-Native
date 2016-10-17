package com.kilogramm.mattermost.viewmodel.menu;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.kilogramm.mattermost.viewmodel.ViewModel;

/**
 * Created by Ngers on 24.08.2016.
 */
public class FrMenuPrivateViewModel implements ViewModel {

    private Context context;

    public FrMenuPrivateViewModel(Context context) {
        this.context = context;
    }

    public void onMoreClick(View v){
        Toast.makeText(context, "click more", Toast.LENGTH_SHORT).show();
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
