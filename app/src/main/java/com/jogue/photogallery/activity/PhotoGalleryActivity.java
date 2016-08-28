package com.jogue.photogallery.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.jogue.photogallery.fragment.PhotoGalleryFragment;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, PhotoGalleryActivity.class);
    }


}
