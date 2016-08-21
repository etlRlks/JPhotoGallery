package com.jogue.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jogue- on 2016/8/15.
 */
public class PhotoGalleryFragment extends Fragment{

    private static final String TAG = "PhotoGalleryFragment";
    private int lastFetchedPage = 1; //获取最后一页的页数

    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); //屏幕旋转时存储数据
        setHasOptionsMenu(true);
        updateItem();
//        new FecthItemTask().execute(); //执行AsyncTask

        Handler resposeHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(resposeHandler);
        mThumbnailDownloader.setmThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail) {
                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                target.bindDrawable(drawable);
            }
        });
        mThumbnailDownloader.start(); //开启线程
        //获取消息泵，从MessageQueue不断抽取Message执行
        //因此，一个MessageQueue需要一个Looper9
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit(); //终止线程进程
        Log.i(TAG, "Background thread destroyed");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery,container,false);
        mPhotoRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_view);
        //设置布局管理器
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                PhotoAdapter adapter = (PhotoAdapter) recyclerView.getAdapter();
                int lastPosition = adapter.getLastBoundPosition();
                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                int loadBufferPosition = 1; //当前加载页
                if (lastPosition >= adapter.getItemCount()
                        - layoutManager.getSpanCount() - loadBufferPosition) {
                    new FecthItemTask().execute(lastPosition + 1);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        setupAdapter();
        return v;
    }

    private void setupAdapter() {
        //判断fragment是否与Activity连接，因为fragment能独立于activity存在
        //但是一旦fragment接收了回调函数，说明已经与activity进行联系，没有activity就没有回调
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class FecthItemTask extends AsyncTask<Integer,Void,List<GalleryItem>> {

        /*
        在后台线程运行
         */
        @Override
        protected List<GalleryItem> doInBackground(Integer... params) {
            String query = "robot"; //测试
            //重构
            if (query == null) {
                return new FlickFetchr().fetchRecentPhotos();
            } else {
                return new FlickFetchr().searchPhotos(query);
            }
        }

        /*
        在主线程运行，在doInBackground后被调用
         */
        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            if (lastFetchedPage > 1) {
                mItems.addAll(galleryItems);
                mPhotoRecyclerView.getAdapter().notifyDataSetChanged();
            } else {
                mItems = galleryItems;
                setupAdapter();

            }
            lastFetchedPage++;
        }
    }

    /*
    继承重写RecycleView.ViewHolder
     */
    private class PhotoHolder extends RecyclerView.ViewHolder {

        private ImageView mItemImageView;

        //构造函数初始化
        public PhotoHolder(View itemView) {
            super(itemView);
            mItemImageView = (ImageView) itemView
            .findViewById(R.id.fragment_photo_gallery_image_view);//初始化
        }

        //绑定item
        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }

        public void bindGalleryItem(GalleryItem galleryItem) {
            //picasso
            Picasso.with(getActivity())
                    .load(galleryItem.getUrl())
                    .placeholder(R.drawable.bill_up_close)
                    .into(mItemImageView);
        }
    }
    /*
    继承重写RecycleView.Adapter
     */
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<GalleryItem> mGalleryItems;
        //创建一个变量，用于存储最后一项的位置
        private int lastBoundPosition;

        public int getLastBoundPosition() {
            return lastBoundPosition;
        }

        //构造函数
        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            Drawable placeHolder = getResources().getDrawable(R.drawable.bill_up_close);
            holder.bindDrawable(placeHolder);
            mThumbnailDownloader.queueThunbnail(holder, galleryItem.getUrl());
            lastBoundPosition = position; //绑定最后的位置
            Log.i(TAG, "Last bound position is " + Integer.toString(lastBoundPosition));

        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //加载menu布局
        inflater.inflate(R.menu.fragment_photo_gallery,menu);
        //获取menu中的item
        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        //通过item获取SearchView
        final SearchView searchView = (SearchView) searchItem.getActionView();
        //设置监听
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //用户提交的时候，回调该方法
            //返回true表示系统已经处理请求
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "QueryTextSubmit: " + query);
                updateItem();
                return true;
            }
            //搜索框文本改变的时候，回调此方法
            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "QueryTextChange: " + newText);
                return false;
            }
        });
    }

    private void updateItem() {
        new FecthItemTask().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
