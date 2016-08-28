package com.jogue.photogallery.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jogue.photogallery.utils.QueryPreference;

/**
 * 简易广播类
 * Created by jogue- on 2016/8/22.
 */
public class StartupReceiver extends BroadcastReceiver{
    private static final String TAG = "StartupReceiver";

    //当收到intent时，调用该方法
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Receiver broadcast intent: " + intent.getAction());
        boolean isOn = QueryPreference.isAlarmOn(context);
        PollService.setServiceAlarm(context, isOn);
    }
}
