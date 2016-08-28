package com.jogue.photogallery.receiver;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.jogue.photogallery.utils.FlickFetchr;
import com.jogue.photogallery.bean.GalleryItem;
import com.jogue.photogallery.activity.PhotoGalleryActivity;
import com.jogue.photogallery.utils.QueryPreference;
import com.jogue.photogallery.R;

import java.util.List;

/**
 * Created by jogue- on 2016/8/21.
 */
public class PollService extends IntentService{
    private static final String TAG = "PollService";
    private static final long POLL_INTEVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    public static final String ACTION_SHOW_NOTIFICATION = "com.jogue.photogallery.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE = "com.jogue.photogallery.PRIVATE";//权限标识，私有的权限
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = PollService.newIntent(context);
        //构建PendingIntent,用于启动service
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        //获取AlarmManager
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        if (isOn) {
            //设置重复闹钟
            /*
            1. 第一个参数是闹钟的类型，
               表示闹钟在手机睡眠状态下不可用，
               该状态下闹钟使用相对时间（相对于系统启动开始），状态值为3，相对时间
            2.  第二个是闹钟的第一次执行时间，相对时间
            3. 第三个是间隔时间
            4.  绑定闹钟的执行动作，比如发送一个广播、给出提示等等
                   这里通过启动服务来实现闹钟提示pi
             */
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME
            , SystemClock.elapsedRealtime(), POLL_INTEVAL, pi);
        } else {
            //取消闹钟
            alarmManager.cancel(pi);
            pi.cancel();
        }
        QueryPreference.setAlarmOn(context, isOn);
    }

    /*
    检验pendingIntent是否存在
     */
    public static boolean isServiceAlarmOn(Context context) {
        Intent i = PollService.newIntent(context);
        //PendingIntent.FLAG_NO_CREATE表示
        //如果PendingIntent不存在，返回null
        PendingIntent pi = PendingIntent.getService(context, 0, i,
                PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    public PollService() {
        super(TAG);
    }

    //响应意图
    @Override
    protected void onHandleIntent(Intent intent) {
        //后台需要检测网络是否连接着
        if (!isNetworkAvailableAndConnected()) {
            return;
        }
        //获取搜索结果
        String query = QueryPreference.getStoredQuery(this);
        //获取最后结果的id
        String lastResultId = QueryPreference.getLastResultId(this);
        //声明集合
        List<GalleryItem> items;
        //将最新的结果设置进FlickFetchr
        if (query == null) {
            items = new FlickFetchr().fetchRecentPhotos();
        } else {
            items = new FlickFetchr().searchPhotos(query);
        }
        if (items.size() == 0) {
            return;
        }
        //如果有结果，获取第一个
        String resultId = items.get(0).getId();
        //判断是否和最后的结果id不同
        if (resultId.equals(lastResultId)) {
            Log.i(TAG, "Got an old result: " + resultId);
        } else {
            Log.i(TAG, "Got a new Result: " + resultId);

        //通知
        Resources resources = getResources();
        Intent i = PhotoGalleryActivity.newIntent(this);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setTicker(resources.getString(R.string.new_picture_title)) //显示提示语
                .setSmallIcon(android.R.drawable.ic_menu_report_image) //显示图标
                .setContentTitle(resources.getString(R.string.new_picture_text))//显示内容标题
                .setContentText(resources.getString(R.string.new_picture_text))//内容
                .setContentIntent(pi) //点击意图
                .setAutoCancel(true) //单击面板自动取消
                .build();
        //获取NotificationManagerCompat 实例
        /*NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, notification); //传递notification，0是标识符
                                                //如果不同通知传递了相同的标识符，会替换最后的一个通知
        sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION), PREM_PRIVATE); //发送广播*/
            showBackgroundNotification(0, notification);
        }
        //如果不同，保存结果
        QueryPreference.setLastResultId(this, resultId);
    }

    private void showBackgroundNotification(int requestCode, Notification notification) {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestCode);
        i.putExtra(NOTIFICATION, notification);
        //1. 结果接受者
        //2. 运行结果接收者的handler
        //3. 请求码的初始值
        //4. 结果数据
        //5. 额外的结果
        sendOrderedBroadcast(i, PERM_PRIVATE, null,null,
                Activity.RESULT_OK, null, null);
    }

    /*
    检查网络连接状态
     */
    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo()
                .isConnected();
        return isNetworkConnected;
    }
}
