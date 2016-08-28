package com.jogue.photogallery.holder;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.jogue.photogallery.bean.GalleryItem;
import com.jogue.photogallery.activity.PhotoPageActivity;
import com.jogue.photogallery.R;
import com.jogue.photogallery.utils.DeviceUtils;
import com.squareup.picasso.Picasso;

/**
 * 继承重写RecycleView.ViewHolder
 * Created by jogue- on 2016/8/26.
 */
public class PhotoHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener, View.OnLongClickListener {
    private ImageView mItemImageView;
    private GalleryItem mGalleryItem;
    private Context mContext;
    //构造函数初始化
    public PhotoHolder(View itemView,  Context context) {
        super(itemView);
        mContext = context;
        mItemImageView = (ImageView) itemView
                .findViewById(R.id.fragment_photo_gallery_image_view);//初始化
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    //绑定item
    public void bindGallery(GalleryItem galleryItem, Context context) {
        Picasso.with(mContext)
                .load(galleryItem.getUrl())
                .resize(DeviceUtils.dip2px(mContext, 250)
                        , DeviceUtils.dip2px(mContext, 250))
                .centerCrop()
                .placeholder(R.drawable.bill_up_close)
                .into(mItemImageView);
    }
    public void setDetailPhoto(final GalleryItem galleryItem, int position) {
            /*mItemImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getActivity(), DetailPhotoActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(PHOTO_TAG,galleryItem.getUrl());
                    i.putExtra("bundle",bundle);
                    startActivity(i);
                }
            });*/
    }

    public void bindGalleryItem(GalleryItem galleryItem) {
        mGalleryItem = galleryItem;
    }
    @Override
    public void onClick(View view) {
        Intent i = PhotoPageActivity.newIntent(mContext, mGalleryItem.getPhotoPageUri());
        mContext.startActivity(i);
    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }
}
