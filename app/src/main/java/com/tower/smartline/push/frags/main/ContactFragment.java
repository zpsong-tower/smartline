package com.tower.smartline.push.frags.main;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.tower.smartline.common.app.PresenterFragment;
import com.tower.smartline.common.widget.PortraitView;
import com.tower.smartline.common.widget.recycler.BaseRecyclerAdapter;
import com.tower.smartline.factory.model.db.UserEntity;
import com.tower.smartline.factory.presenter.homepage.ContactPresenter;
import com.tower.smartline.factory.presenter.homepage.IHomepageContract;
import com.tower.smartline.push.R;
import com.tower.smartline.push.activities.MessageActivity;
import com.tower.smartline.push.activities.PersonalActivity;
import com.tower.smartline.push.databinding.FragmentContactBinding;

import com.bumptech.glide.Glide;

/**
 * ContactFragment
 *
 * @author zpsong-tower <pingzisong2012@gmail.com>
 * @since 2021/4/23 0:35
 */
public class ContactFragment extends PresenterFragment<IHomepageContract.Presenter>
        implements IHomepageContract.View {
    private static final String TAG = ContactFragment.class.getName();

    private FragmentContactBinding mBinding;

    private BaseRecyclerAdapter<UserEntity> mAdapter;

    // 标识是否第一次初始化数据
    private boolean mIsFirst = true;

    // 标识是否第一次初始化数据成功 (已拿到数据)
    private volatile boolean mIsFirstSuccess = false;

    public ContactFragment() {
        // Required empty public constructor
    }

    @Override
    public IHomepageContract.Presenter initPresenter() {
        return new ContactPresenter(this);
    }

    @NonNull
    @Override
    protected View initBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        if (mBinding == null) {
            mBinding = FragmentContactBinding.inflate(inflater, container, false);
        }
        return mBinding.getRoot();
    }

    @Override
    protected void initWidget() {
        super.initWidget();

        // 初始化Recycler
        mBinding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        if (mAdapter == null) {
            mAdapter = new BaseRecyclerAdapter<UserEntity>() {
                @Override
                protected int getItemViewType(int position, UserEntity userEntity) {
                    return R.layout.cell_contact_list;
                }

                @NonNull
                @Override
                protected BaseRecyclerViewHolder<UserEntity> onCreateViewHolder(View root, int viewType) {
                    return new ContactViewHolder(root);
                }
            };
            mAdapter.setListener(new BaseRecyclerAdapter.AdapterListener<UserEntity>() {
                @Override
                public void onItemClick(@NonNull BaseRecyclerAdapter.BaseRecyclerViewHolder<UserEntity> holder, UserEntity userEntity) {
                    MessageActivity.show(requireContext(), userEntity);
                }

                @Override
                public void onItemLongClick(@NonNull BaseRecyclerAdapter.BaseRecyclerViewHolder<UserEntity> holder, UserEntity userEntity) {
                }
            });
        }
        mBinding.recycler.setAdapter(mAdapter);

        // 设置空布局
        setEmptyView(mBinding.empty, mBinding.recycler);
    }

    @Override
    protected void initData() {
        super.initData();
        if (mIsFirst) {
            // Fragment首次初始化时加载一次数据
            getPresenter().initData();
            mIsFirst = false;
        } else if (!mIsFirstSuccess) {
            // 重新切换到该Fragment 还未拿到数据 显示加载动画
            showLoading();
        } else {
            // 重新切换到该Fragment 刷新界面内容
            onAdapterDataChanged();
        }
    }

    @Override
    public void firstInitDataSuccess() {
        mIsFirstSuccess = true;
    }

    @Override
    public BaseRecyclerAdapter<UserEntity> getRecyclerAdapter() {
        return mAdapter;
    }

    @Override
    public void onAdapterDataChanged() {
        if (mAdapter == null) {
            Log.w(TAG, "onAdapterDataChanged: mAdapter == null");
            return;
        }
        hideLoading(mAdapter.getItemCount() > 0);
    }

    @Override
    protected void destroyBinding() {
        mBinding = null;
    }

    private class ContactViewHolder extends BaseRecyclerAdapter.BaseRecyclerViewHolder<UserEntity>
            implements View.OnClickListener {
        PortraitView mPortrait;

        TextView mName;

        TextView mDesc;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            mPortrait = itemView.findViewById(R.id.im_portrait);
            mName = itemView.findViewById(R.id.txt_name);
            mDesc = itemView.findViewById(R.id.txt_desc);
        }

        @Override
        protected void onBind(@NonNull UserEntity userEntity) {
            if (mPortrait == null || mName == null || mDesc == null) {
                return;
            }
            mPortrait.setup(Glide.with(ContactFragment.this), userEntity);
            mPortrait.setOnClickListener(this);
            mName.setText(userEntity.getName());
            mDesc.setText(userEntity.getDescription());
        }

        private void onPortraitClick() {
            Log.i(TAG, "onPortraitClick: itemId: " + getItemId());
            if (getData() != null) {
                PersonalActivity.show(requireContext(), getData().getId());
            }
        }

        @Override
        public void onClick(View v) {
            if (v == null) {
                return;
            }
            int id = v.getId();
            if (id == mPortrait.getId()) {
                // 头像点击
                onPortraitClick();
            } else {
                Log.w(TAG, "onClick: illegal param: " + id);
            }
        }
    }
}