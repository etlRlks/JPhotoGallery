/**
 * AnimationUtils.java [V 1..0.0]
 * classes : com.hb56.hps.android.utils.AnimationUtils
 * zhangyx Create at 2014-10-31 下午2:31:50
 */
package com.jogue.photogallery.utils;


import android.app.Activity;
import android.content.Context;

import com.jogue.photogallery.R;

/**
 * 自定义控件的动画效果
 * create by jogue- on 2016/8/27.
 */
public class AnimationUtil {

	/*
	  退出Activity的动画 : zoom 动画
	  @param context
	 */
	public static void finishActivityAnimation(Context context) {
		((Activity) context).finish();
		((Activity) context).overridePendingTransition(R.anim.zoom_enter,
				R.anim.zoom_exit);
	}

	/*
	  zoom 动画s
	  @param context
	 */
	public static void activityZoomAnimation(Context context) {
		((Activity) context).overridePendingTransition(R.anim.zoom_enter,
				R.anim.zoom_exit);
	}

}
