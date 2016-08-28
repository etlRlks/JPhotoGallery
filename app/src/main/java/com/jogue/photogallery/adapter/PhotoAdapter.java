package com.jogue.photogallery.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jogue.photogallery.bean.GalleryItem;
import com.jogue.photogallery.holder.PhotoHolder;
import com.jogue.photogallery.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 继承重写RecycleView.Adapter
 * Created by jogue- on 2016/8/26.
 */

public class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
    @BindView(R.id.fragment_photo_gallery_image_view)
    private static final String TAG = "PhotoAdapter";
    private List<GalleryItem> mGalleryItems;
    //创建一个变量，用于存储最后一项的位置
    private int lastBoundPosition;
    List<Integer> heights;
    private Context mContext;
    private PhotoHolder photoHolder;

    public int getLastBoundPosition() {
        return lastBoundPosition;
    }

    //构造函数
    public PhotoAdapter(List<GalleryItem> galleryItems, Context context) {
        mGalleryItems = galleryItems;
        mContext = context;
        getRandomHeight(this.mGalleryItems);
    }

    //得到随机item的高度
    private void getRandomHeight(List<GalleryItem> galleryItems) {
        heights = new ArrayList<>();
        for (int i = 0; i < galleryItems.size(); i++) {
            heights.add((int) (160 + Math.random() * 400));
        }
    }

    @Override
    public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.gallery_item, parent, false);
        photoHolder = new PhotoHolder(view, mContext);
        return photoHolder;
    }

    @Override

    public void onBindViewHolder(PhotoHolder holder, int position) {
        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();//得到item的LayoutParams布局参数
        params.height = heights.get(position);//把随机的高度赋予item布局
        Log.i("ppppppposition", position + "");
        holder.itemView.setLayoutParams(params);//把params设置给item布局

        GalleryItem galleryItem = mGalleryItems.get(position);
        holder.bindGallery(galleryItem, mContext);
        holder.bindGalleryItem(galleryItem);
        holder.setDetailPhoto(galleryItem,position);
        lastBoundPosition = position; //绑定最后的位置
        Log.i(TAG, "Last bound position is " + Integer.toString(lastBoundPosition));

    }

    @Override
    public int getItemCount() {
        return mGalleryItems.size();
    }
}
