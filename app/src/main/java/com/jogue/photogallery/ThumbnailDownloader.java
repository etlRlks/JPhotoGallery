package com.jogue.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by jogue- on 2016/8/19.
 */
public class ThumbnailDownloader<T> extends HandlerThread{
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0; //标识的信息 设置参数what
    private Handler mRequestHandler; //
    private Handler mResposeHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    public ThumbnailDownloader(Handler resposeHandler) {
        super(TAG);
        mResposeHandler = resposeHandler;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: "
                    + mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setmThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        mThumbnailDownloadListener = listener;
    }

    private void handleRequest(final T target) {
        try {
            final String url = mRequestMap.get(target);
            //第一次检查mRequestMap
            if (url == null) {
                return;
            }
            byte[] bitmapBytes = new FlickFetchr().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory
                    .decodeByteArray(bitmapBytes, 0 ,bitmapBytes.length);
            Log.i(TAG, "Bitmap created");
            mResposeHandler.post(new Runnable() {
                /*
                主线程执行
                 */
                @Override
                public void run() {
                    //第二次检查mRequestMap
                    //因为recycleview要回收它里面的view
                    //也确保每个photoHolder获取到正确的图像
                    if (mRequestMap.get(target) != url) {
                        return;
                    }
                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Error download image", e);
        }
    }

    public void queueThunbnail(T target, String url) {
        Log.i(TAG, "Got a URL: " + url);
        if (url == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                    .sendToTarget();
        }

    }
    //当用户旋转时，清理queue
    public void clearQueue() {
        mResposeHandler.removeMessages(MESSAGE_DOWNLOAD);
    }
}
