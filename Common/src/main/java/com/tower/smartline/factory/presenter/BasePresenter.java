package com.tower.smartline.factory.presenter;

import androidx.annotation.NonNull;

/**
 * MVP模式 Presenter抽象类
 *
 * @param <V> MVP模式中的View
 * @author zpsong-tower <pingzisong2012@gmail.com>
 * @since 2021/6/8 9:41
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class BasePresenter<V extends IBaseContract.View> implements IBaseContract.Presenter {
    private V mView;

    /**
     * 构造方法
     *
     * @param view Presenter需要绑定的View层实例
     */
    public BasePresenter(@NonNull V view) {
        this.mView = view;
    }

    @Override
    public void start() {
        if (mView != null) {
            mView.showLoading();
        }
    }

    @Override
    public void destroy() {
        if (mView != null) {
            mView.setPresenter(null);
        }
        mView = null;
    }

    /**
     * 获取绑定的View层实例
     *
     * @return View
     */
    protected final V getView() {
        return mView;
    }

    /**
     * 设置并绑定View层实例
     *
     * @param view View
     */
    protected final void setView(@NonNull V view) {
        mView = view;
        mView.setPresenter(this);
    }
}
