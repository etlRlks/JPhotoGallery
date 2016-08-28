package com.jogue.photogallery.utils;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by jogue- on 2016/8/21.
 */
public class QueryPreference {
    private static final String PREF_SEARCH_QUERY = "searchQuery"; //查询的key
    private static final String PREF_LAST_RESULT_ID = "lastResultId";//存储最近读取的照片的id
    private static final String PREF_IS_ALARM_ON = "isAlarmOn"; //接收器的标识符

    /*
    获取存储的查询文本
     */
    public static String getStoredQuery(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_QUERY, null);
    }
    /*
    设置存储的查询文本
     */
    public static void setStoredQuery(Context context, String query) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit() //获取editor
                .putString(PREF_SEARCH_QUERY, query)
                .apply();
    }
    /*
    获取最后数据的id
     */
    public static String getLastResultId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_LAST_RESULT_ID, null);
    }
    /*
    设置最后数据的id
     */
    public static void setLastResultId(Context context, String lastResultId) {
        //1. 通过PreferenceManager获取SharedPreferences
        PreferenceManager.getDefaultSharedPreferences(context)
                //2. 通过SharedPreferences获取SharedPreferences.editor
                .edit()
                //3. 使用SharedPreferences.editor的putString
                .putString(PREF_LAST_RESULT_ID, lastResultId)
                //4. 提交: 对提交的结果不关系 使用apply(),如果需要确保提交成功使用commit()
                .apply();
    }
    /*
    检查alarm是否打开或关闭
     */
    public static boolean isAlarmOn(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_IS_ALARM_ON, false);
    }
    public static void setAlarmOn(Context context, boolean isOn) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_IS_ALARM_ON, isOn)
                .apply();
    }
}
