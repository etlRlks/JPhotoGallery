package com.jogue.photogallery.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.jogue.photogallery.R;
import com.jogue.photogallery.utils.AnimationUtil;

/**
 * Created by jogue- on 2016/8/27.
 */
public class SplashActivity extends Activity{
    private static final long DELAY_TIME = 3000L;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        redirectByTime();
    }

    private void redirectByTime() {
        //开启耗时线程
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, PhotoGalleryActivity.class));
                AnimationUtil.finishActivityAnimation(SplashActivity.this);
            }
        }, DELAY_TIME);
    }
}
