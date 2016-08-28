package com.jogue.photogallery.bean;

import android.net.Uri;

/**
 * Created by jogue- on 2016/8/16.
 */
public class GalleryItem {

    private String mCaption;
    private String mId;
    private String mUrl;
    private String mOwner;//userId

    @Override
    public String toString() {
        return super.toString();
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getCaption() {

        return mCaption;
    }

    public void setCaption(String mCaption) {
        this.mCaption = mCaption;
    }

    public String getUrl() {

        return mUrl;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public String getOwner() {
        return mOwner;
    }

    public void setOwner(String Owner) {
        this.mOwner = Owner;
    }
    /*
    生成照片网页的网址
     */
    public Uri getPhotoPageUri() {
        return Uri.parse("http://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(mOwner)
                .appendPath(mId)
                .build();
    }
}
