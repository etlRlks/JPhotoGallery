package com.jogue.photogallery.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

import com.jogue.photogallery.fragment.PhotoPageFragment;

/**
 * Created by jogue- on 2016/8/25.
 */
public class PhotoPageActivity extends SingleFragmentActivity {

    private PhotoPageFragment photoPageFragment;

    public static Intent newIntent(Context context, Uri photoPageUri) {
        Intent i = new Intent(context, PhotoPageActivity.class);
        i.setData(photoPageUri);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        photoPageFragment = PhotoPageFragment.newInstance(getIntent().getData());
        return photoPageFragment;
    }

    @Override
    public void onBackPressed() {
        if (photoPageFragment.webViewCanGoBack()) {
            photoPageFragment.webViewGoBack();
        } else {
            super.onBackPressed();
        }
    }
}
