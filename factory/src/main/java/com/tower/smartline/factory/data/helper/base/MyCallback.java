package com.tower.smartline.factory.data.helper.base;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tower.smartline.factory.R;
import com.tower.smartline.factory.data.IDataSource;
import com.tower.smartline.factory.model.response.base.ResponseModel;
import com.tower.smartline.factory.model.response.base.Responses;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 自定义Retrofit回调接口的基类
 *
 * @param <T> 网络请求返回Result的类型
 * @author zpsong-tower <pingzisong2012@gmail.com>
 * @since 2021/6/11 5:45
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class MyCallback<T> implements Callback<ResponseModel<T>> {
    private static final String TAG = MyCallback.class.getName();

    protected IDataSource.Callback mCallback;

    // 响应Body
    private ResponseModel mRsp;

    /**
     * 构造方法
     *
     * @param callback IDataSource.Callback App对成功与失败的回调
     *                 如为Null则只是没有回调，不影响网络通信逻辑
     */
    public MyCallback(@Nullable IDataSource.Callback callback) {
        mCallback = callback;
    }

    /**
     * 对判空和失败错误码做处理
     * 建议子类调用 {@link #getResultWithoutCallback()}
     * 或 {@link #getResultOrHandled()} 获得处理结果和所需数据
     *
     * @param call     Call
     * @param response Response
     */
    @Override
    public void onResponse(@NonNull Call<ResponseModel<T>> call, @NonNull Response<ResponseModel<T>> response) {
        // 打印错误码 错误说明 服务器响应时间 Result
        Log.i(TAG, "onResponse: " + response.body());

        if (Responses.isSuccess(response.body(), mCallback)) {
            // Responses未被截获 对mRsp赋值 子类调用是否处理过 将不为Null 需要子类处理
            mRsp = response.body();
        }
    }

    /**
     * 失败则直接进行失败回调 响应相关的Toast提示
     * 如无意外不需要复写
     *
     * @param call Call
     * @param t    Throwable
     */
    @Override
    public void onFailure(@NonNull Call<ResponseModel<T>> call, @NonNull Throwable t) {
        Log.w(TAG, "onFailure");
        if (mCallback == null) {
            Log.w(TAG, "onFailure: callback == null");
            return;
        }
        mCallback.onFailure(R.string.toast_net_network_exception);
    }

    /**
     * 返回一个需要子类继续处理ResponseModel.result 不关注回调是否为空
     *
     * @return ResponseModel 如为Null则父类已处理过，不需要继续处理
     */
    @Nullable
    protected final T getResultWithoutCallback() {
        if (mRsp == null) {
            return null;
        }
        return (T) mRsp.getResult();
    }

    /**
     * 返回一个需要子类继续处理ResponseModel.result 会对回调做判空校验
     *
     * @return ResponseModel 如为Null则父类已处理过，不需要继续处理
     */
    @Nullable
    protected final T getResultOrHandled() {
        if (mCallback == null) {
            return null;
        }
        return getResultWithoutCallback();
    }
}
