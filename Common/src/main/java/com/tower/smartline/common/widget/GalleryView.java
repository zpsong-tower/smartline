package com.tower.smartline.common.widget;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tower.smartline.common.R;
import com.tower.smartline.common.widget.recycler.MyRecyclerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 图片选择器View
 *
 * @author zpsong-tower <pingzisong2012@gmail.com>
 * @since 2021/4/25 15:22
 */
public class GalleryView extends RecyclerView {
    private static final int LOADER_ID = 0x0100;

    private static final int COLUMNS_NUM = 4;

    // 最大选中图片数量
    private static final int MAX_IMAGE_COUNT = 3;

    // 最小图片文件大小，小于10Kb过滤
    private static final int MIN_IMAGE_FILE_SIZE = 10 * 1024;

    private RecyclerAdapter mAdapter = new RecyclerAdapter();

    private LoaderCallback mLoaderCallback = new LoaderCallback();

    private List<Image> mSelectedImages = new LinkedList<>();

    private SelectedChangeListener mListener;

    public GalleryView(@NonNull Context context) {
        super(context);
        init();
    }

    public GalleryView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GalleryView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayoutManager(new GridLayoutManager(getContext(), COLUMNS_NUM));
        mAdapter.setListener(new MyRecyclerAdapter.AdapterListener<Image>() {
            @Override
            public void onItemClick(@NonNull MyRecyclerAdapter.MyViewHolder<Image> holder, Image image) {
                // 更新对应Cell的状态，若达到最大选中数量则不刷新界面
                if (onItemSelectClick(image)) {
                    holder.updateData(image);
                }
            }

            @Override
            public void onItemLongClick(@NonNull MyRecyclerAdapter.MyViewHolder<Image> holder, Image image) {
            }
        });
        setAdapter(mAdapter);
    }

    /**
     * 初始化方法
     *
     * @param loaderManager Loader管理器
     * @param listener      选择变化监听器
     * @return 返回LOADER_ID，可用于销毁Loader
     */
    public int setup(LoaderManager loaderManager, SelectedChangeListener listener) {
        mListener = listener;
        loaderManager.initLoader(LOADER_ID, null, mLoaderCallback);
        return LOADER_ID;
    }

    /**
     * Cell点击的具体逻辑
     *
     * @param image 被点击的图片数据
     * @return True，数据更改，需要刷新；False，达到最大选中数量，不刷新
     */
    private boolean onItemSelectClick(Image image) {
        if (image == null) {
            return false;
        }
        if (mSelectedImages.contains(image)) {
            mSelectedImages.remove(image);
            image.mIsSelect = false;
        } else if (mSelectedImages.size() >= MAX_IMAGE_COUNT) {
            if (getResources() != null) {
                String str = String.format(getResources()
                        .getString(R.string.label_gallery_select_max_size), MAX_IMAGE_COUNT);

                // TODO 后期统一处理Toast，归一化
                Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
            }
            return false;
        } else {
            mSelectedImages.add(image);
            image.mIsSelect = true;
        }
        notifySelectChanged();
        return true;
    }

    /**
     * 得到选中的图片的全部地址
     *
     * @return 包含选中的图片全部地址的String数组
     */
    private String[] getSelectedPath() {
        String[] paths = new String[mSelectedImages.size()];
        int index = 0;
        for (Image image : mSelectedImages) {
            paths[index++] = image.mPath;
        }
        return paths;
    }

    /**
     * 清空选中的图片
     */
    private void clear() {
        for (Image image : mSelectedImages) {
            image.mIsSelect = false;
        }
        mSelectedImages.clear();
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 通知选中状态改变
     */
    private void notifySelectChanged() {
        if (mListener != null) {
            mListener.onSelectedCountChanged(mSelectedImages.size());
        }
    }

    /**
     * 通知Adapter数据更改的方法
     *
     * @param images 新的数据
     */
    private void updateSource(List<Image> images) {
        mAdapter.replace(images);
    }


    private class RecyclerAdapter extends MyRecyclerAdapter<Image> {
        @Override
        protected int getItemViewType(int position, Image image) {
            return R.layout.cell_gallery;
        }

        @Override
        protected MyViewHolder<Image> onCreateViewHolder(View root, int viewType) {
            return new ViewHolder(root);
        }
    }

    private class ViewHolder extends MyRecyclerAdapter.MyViewHolder<Image> {
        private ImageView mPic;

        private View mShade;

        private CheckBox mSelected;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mPic = itemView.findViewById(R.id.im_image);
            mShade = itemView.findViewById(R.id.view_shade);
            mSelected = itemView.findViewById(R.id.cb_select);
        }

        @Override
        protected void onBind(Image image) {
            Glide.with(getContext())
                    .load(image.mPath) // 加载路径
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // 本地图片不使用缓存
                    .centerCrop() // 居中剪切
                    .placeholder(R.color.grey_200) // 加载时填充颜色
                    .into(mPic);
            mShade.setVisibility(image.mIsSelect ? VISIBLE : INVISIBLE);
            mSelected.setChecked(image.mIsSelect);
            mSelected.setVisibility(VISIBLE);
        }
    }

    /**
     * 每张图片的数据结构
     */
    private static class Image {
        // Id
        private int mId;

        // 路径
        private String mPath;

        // 创建日期
        private long mDate;

        // 是否选中
        private boolean mIsSelect;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Image image = (Image) o;
            return mPath != null ? mPath.equals(image.mPath) : image.mPath == null;
        }

        @Override
        public int hashCode() {
            return mPath != null ? mPath.hashCode() : 0;
        }
    }

    /**
     * 用于实际数据加载的Loader Callback
     */
    private class LoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {
        private final String[] IMAGE_PROJECTION = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED,
        };

        /**
         * 创建一个Loader
         *
         * @param id   必须为本类规定的LOADER_ID
         * @param args Bundle参数
         * @return CursorLoader
         */
        @NonNull
        @Override
        public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
            return new CursorLoader(getContext(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null,
                    null,
                    null,
                    IMAGE_PROJECTION[2] + " DESC"); // 按时间倒序查询
        }

        /**
         * 当Loader加载完成时
         *
         * @param loader Loader
         * @param data   游标数据
         */
        @Override
        public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
            List<Image> images = new ArrayList<>();
            if (data != null) {
                int count = data.getCount();
                if (count > 0) {
                    // 移动游标到开始
                    data.moveToFirst();

                    // 得到对应的列的Index坐标
                    int indexId = data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]);
                    int indexPath = data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]);
                    int indexDate = data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]);

                    // 循环读取，直到没有下一条数据
                    do {
                        int id = data.getInt(indexId);
                        String path = data.getString(indexPath);
                        long dateTime = data.getLong(indexDate);
                        File file = new File(path);
                        if (!file.exists() || file.length() < MIN_IMAGE_FILE_SIZE) {
                            // 如果没有图片，或者图片大小太小，则跳过
                            continue;
                        }

                        // 添加一条新的数据
                        Image image = new Image();
                        image.mId = id;
                        image.mPath = path;
                        image.mDate = dateTime;
                        images.add(image);
                    } while (data.moveToNext());
                }
            }
            updateSource(images);
        }

        /**
         * 当Loader销毁或重置
         *
         * @param loader Loader
         */
        @Override
        public void onLoaderReset(@NonNull Loader<Cursor> loader) {
            // 界面清空
            updateSource(null);
        }
    }

    /**
     * 选择发生变化时的对外监听器
     */
    public interface SelectedChangeListener {
        /**
         * 当选择图片数目发生变化
         *
         * @param count 已选择图片的数目
         */
        void onSelectedCountChanged(int count);
    }
}
