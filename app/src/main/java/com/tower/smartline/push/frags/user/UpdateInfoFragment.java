package com.tower.smartline.push.frags.user;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.tower.smartline.common.app.MyApplication;
import com.tower.smartline.common.app.PresenterFragment;
import com.tower.smartline.factory.presenter.user.IUpdateInfoContract;
import com.tower.smartline.factory.presenter.user.UpdateInfoPresenter;
import com.tower.smartline.push.R;
import com.tower.smartline.push.activities.MainActivity;
import com.tower.smartline.push.databinding.FragmentUpdateInfoBinding;
import com.tower.smartline.push.frags.media.GalleryFragment;

import com.bumptech.glide.Glide;

import com.yalantis.ucrop.UCrop;

import static android.app.Activity.RESULT_OK;

/**
 * 用户更新信息Fragment
 *
 * @author zpsong-tower <pingzisong2012@gmail.com>
 * @since 2021/4/30 7:10
 */
public class UpdateInfoFragment extends PresenterFragment<IUpdateInfoContract.Presenter>
        implements IUpdateInfoContract.View, View.OnClickListener {
    private static final String TAG = UpdateInfoFragment.class.getName();

    // 图片压缩质量值[0-100]
    private static final int COMPRESSION_QUALITY_VALUE = 96;

    // 图片裁剪边界的纵横比
    private static final int ASPECT_RATIO_VALUE = 1;

    // 图片裁剪最大大小像素值
    private static final int MAX_RESULT_SIZE = 520;

    // 性别背景选择器 男用颜色 蓝色
    private static final int BACK_SEL_MALE = 0;

    // 性别背景选择器 女用颜色 粉色
    private static final int BACK_SEL_FEMALE = 1;

    private FragmentUpdateInfoBinding mBinding;

    // 头像图片本地资源标识
    private Uri mPortraitUri;

    private boolean mIsMale = true;

    private boolean mIsDefault = true;

    public UpdateInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public IUpdateInfoContract.Presenter initPresenter() {
        return new UpdateInfoPresenter(this);
    }

    @NonNull
    @Override
    protected View initBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        if (mBinding == null) {
            mBinding = FragmentUpdateInfoBinding.inflate(inflater, container, false);
        }
        return mBinding.getRoot();
    }

    @Override
    protected void initWidget() {
        super.initWidget();

        // 点击监听初始化
        mBinding.imPortrait.setOnClickListener(this);
        mBinding.imSex.setOnClickListener(this);
        mBinding.btnSubmit.setOnClickListener(this);
    }

    private void onPortraitClick() {
        Log.i(TAG, "onPortraitClick");
        new GalleryFragment().setListener((uri -> {
            UCrop.Options options = new UCrop.Options();
            options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
            options.setCompressionQuality(COMPRESSION_QUALITY_VALUE);
            UCrop.of(uri, Uri.fromFile(MyApplication.getPortraitTmpFile()))
                    .withAspectRatio(ASPECT_RATIO_VALUE, ASPECT_RATIO_VALUE) // 纵横比
                    .withMaxResultSize(MAX_RESULT_SIZE, MAX_RESULT_SIZE) // 最大尺寸
                    .withOptions(options) // 相关参数
                    .start(requireActivity());
        })).show(getChildFragmentManager(), GalleryFragment.class.getName());
    }

    private void onSexClick() {
        Log.i(TAG, "onSexClick");
        Drawable drawable;
        if (mIsMale) {
            // 切换为女性模式
            drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_sex_female);
            mBinding.imSex.getBackground().setLevel(BACK_SEL_FEMALE);
            if (mIsDefault) {
                mBinding.imPortrait.setImageResource(R.drawable.default_portrait_female);
            }
        } else {
            // 切换为男性模式
            drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_sex_male);
            mBinding.imSex.getBackground().setLevel(BACK_SEL_MALE);
            if (mIsDefault) {
                mBinding.imPortrait.setImageResource(R.drawable.default_portrait_male);
            }
        }
        mBinding.imSex.setImageDrawable(drawable);
        mIsMale = !mIsMale;
    }

    private void onSubmitClick() {
        Log.i(TAG, "onSubmitClick");
        String desc = mBinding.editDesc.getText().toString();
        getPresenter().update(mPortraitUri, desc, mIsMale);
    }

    @Override
    public void submitSuccess() {
        MainActivity.show(requireContext());
        requireActivity().finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 处理UCrop回调
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP && data != null) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                mIsDefault = false;
                loadPortrait(resultUri);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Log.e(TAG, "onActivityResult: UCrop RESULT_ERROR");
            MyApplication.showToast(R.string.toast_common_unknown_error);
        }
    }

    /**
     * 加载Uri到当前头像中
     *
     * @param uri 头像裁剪后的资源标志
     */
    private void loadPortrait(Uri uri) {
        mPortraitUri = uri;
        Glide.with(this)
                .asBitmap()
                .load(uri)
                .centerCrop()
                .into(mBinding.imPortrait);
    }

    @Override
    public void showError(int str) {
        // 显示错误提示 界面恢复操作
        super.showError(str);
        hideLoading();
    }

    @Override
    public void showLoading() {
        // Loading 界面不可操作
        super.showLoading();
        mBinding.loading.start();
        mBinding.btnSubmit.setEnabled(false);
        mBinding.imPortrait.setEnabled(false);
        mBinding.editDesc.setEnabled(false);
        mBinding.imSex.setEnabled(false);
    }

    @Override
    public void hideLoading() {
        super.hideLoading();
        mBinding.loading.stop();
        mBinding.btnSubmit.setEnabled(true);
        mBinding.imPortrait.setEnabled(true);
        mBinding.editDesc.setEnabled(true);
        mBinding.imSex.setEnabled(true);
    }

    @Override
    protected void destroyBinding() {
        mBinding = null;
    }

    @Override
    public void onClick(View v) {
        if (v == null) {
            return;
        }
        int id = v.getId();
        if (id == mBinding.imPortrait.getId()) {
            // 头像点击
            onPortraitClick();
        } else if (id == mBinding.imSex.getId()) {
            // 性别点击
            onSexClick();
        } else if (id == mBinding.btnSubmit.getId()) {
            // 提交点击
            onSubmitClick();
        } else {
            Log.w(TAG, "onClick: illegal param: " + id);
        }
    }
}