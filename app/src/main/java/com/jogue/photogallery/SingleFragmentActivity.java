package com.jogue.photogallery;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

public abstract class SingleFragmentActivity extends AppCompatActivity {

    protected abstract Fragment createFragment();

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        // 1. Activity 通过FragmentManager管理Fragment
        FragmentManager fm = getSupportFragmentManager();
        // 2. 给它一个fragment去管理
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        //
        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()   //事务一般都是用来添加，删除，分离或替换fragment
                    .add(R.id.fragment_container, fragment)
                    .commit();

        }
    }
}
