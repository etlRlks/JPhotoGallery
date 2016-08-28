package com.jogue.photogallery.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.jogue.photogallery.R;
import com.jogue.photogallery.adapter.PhotoAdapter;
import com.jogue.photogallery.bean.GalleryItem;
import com.jogue.photogallery.receiver.PollService;
import com.jogue.photogallery.utils.FlickFetchr;
import com.jogue.photogallery.utils.QueryPreference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jogue- on 2016/8/15.
 */
public class PhotoGalleryFragment extends VisibleFragment {

    private static final String TAG = "PhotoGalleryFragment";
    private static final String PHOTO_TAG = "PHOTO";
    private static final long DELAY_TIME = 1000;
    private int lastFetchedPage = 1; //获取最后一页的页数
    private int[] lastVisibleItem;
    private int currentScrollState = 0;
    private int lastVisibleItemPosition;
    private int visibleItemCount ;
    private int totalItemCount  ;
    private RecyclerView mPhotoRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private PhotoAdapter mPhotoAdapter ;
    private List<GalleryItem> mItems = new ArrayList<>();

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); //屏幕旋转时存储数据
        setHasOptionsMenu(true);
        updateItem();
        Log.i(TAG, "Background thread started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Background thread destroyed");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mPhotoRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_view);
        //设置布局管理器
        final StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        mPhotoRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                currentScrollState = newState;
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                visibleItemCount = layoutManager.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                if (visibleItemCount > 0 && currentScrollState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItemPosition >= totalItemCount - 1) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            new UpdateTask().execute();
                        }
                    }, DELAY_TIME);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (lastVisibleItem == null) {
                    lastVisibleItem = new int[staggeredGridLayoutManager.getSpanCount()];
                }
                staggeredGridLayoutManager.findLastVisibleItemPositions(lastVisibleItem);
                lastVisibleItemPosition = findMax(lastVisibleItem);
            }
        });
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new UpdateTask().execute();
            }

        });
        setupAdapter();
        return v;
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }


    private void setupAdapter() {
        //判断fragment是否与Activity连接，因为fragment能独立于activity存在
        //但是一旦fragment接收了回调函数，说明已经与activity进行联系，没有activity就没有回调
        if (isAdded()) {
            mPhotoAdapter = new PhotoAdapter(mItems, getActivity());
            mPhotoRecyclerView.setAdapter(mPhotoAdapter);
        }
    }

    private class FecthItemTask extends AsyncTask<Integer, Void, List<GalleryItem>> {
        private String mQuery;

        public FecthItemTask(String query) {
            mQuery = query;
        }

        public FecthItemTask() {
        }

        /*
        在后台线程运行
         */
        @Override
        protected List<GalleryItem> doInBackground(Integer... params) {
            //重构
            if (mQuery == null) {
                return new FlickFetchr().fetchRecentPhotos();
            } else {
                return new FlickFetchr().searchPhotos(mQuery);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //加载menu布局
        inflater.inflate(R.menu.fragment_photo_gallery, menu);
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
                QueryPreference.setStoredQuery(getActivity(), query);
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
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = QueryPreference.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });
        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        //判断轮询率
        if (PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    private void updateItem() {
        String query = QueryPreference.getStoredQuery(getActivity());
        new FecthItemTask(query).execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //清空历史记录
            case R.id.menu_item_clear:
                QueryPreference.setStoredQuery(getActivity(), null);
                updateItem();
                return true;
            case R.id.menu_item_toggle_polling:
                //如果false，证明返回的结果是ture，pi存在
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    刷新
     */
    private class UpdateTask extends AsyncTask<Void,Void,List<GalleryItem>> {
        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {
            return new FlickFetchr().fetchRecentPhotos();
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            for(GalleryItem g :galleryItems){
                mItems.add(0,g);
            }
            mPhotoAdapter.notifyDataSetChanged();
        }

    }
}
