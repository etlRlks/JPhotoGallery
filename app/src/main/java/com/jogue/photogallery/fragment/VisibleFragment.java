package com.jogue.photogallery.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.jogue.photogallery.receiver.PollService;

/**
 * Created by jogue- on 2016/8/24.
 */
public abstract class VisibleFragment extends Fragment{
    private static final String TAG = "VIsibleFragment";

    /*
    注册接收者
     */
    @Override
    public void onStart() {
        super.onStart();
        //动态IntentFilter，通过调用addCategory(String)
        // , addAction(String), addDataPath(String)配置过滤器

        //<intent-filter>
        //    <action
        //      android:name="com.bignerdranch.android.photogallery.SHOW_NOTIFICATION"/>
        //</intent-filter>
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, filter,PollService.PERM_PRIVATE,null);
    }

    /*
    注销接收者
     */
    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mOnShowNotification);
    }

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "canceling notification");
            setResultCode(Activity.RESULT_CANCELED);
        }
    };
}
