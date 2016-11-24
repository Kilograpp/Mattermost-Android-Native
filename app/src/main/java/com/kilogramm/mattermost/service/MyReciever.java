package com.kilogramm.mattermost.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by melkshake on 23.11.16.
 */

public class MyReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentService = new Intent(context, MattermostService.class);
        context.startService(intentService);
    }
}
