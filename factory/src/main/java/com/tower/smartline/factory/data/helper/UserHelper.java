package com.tower.smartline.factory.data.helper;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tower.smartline.factory.data.IDataSource;
import com.tower.smartline.factory.model.api.user.UpdateInfoModel;
import com.tower.smartline.factory.model.db.UserEntity;
import com.tower.smartline.factory.model.db.UserEntity_Table;
import com.tower.smartline.factory.model.response.UserCard;
import com.tower.smartline.factory.model.response.base.ResponseModel;
import com.tower.smartline.factory.net.Network;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * UserHelper
 *
 * @author zpsong-tower <pingzisong2012@gmail.com>
 * @since 2021/7/6 3:43
 */
public class UserHelper {
    private static final String TAG = UserHelper.class.getName();

    /**
     * 用户个人信息更新
     *
     * @param model    UpdateInfoModel
     * @param callback 回调
     */
    public static void update(UpdateInfoModel model, IDataSource.Callback<UserCard> callback) {
        Log.i(TAG, "update: start");
        if (model == null) {
            Log.w(TAG, "update: model == null");
            return;
        }
        Network.remote()
                .userUpdate(model)
                .enqueue(new MyCallback<UserCard>(callback) {
                    @Override
                    public void onResponse(@NonNull Call<ResponseModel<UserCard>> call,
                                           @NonNull Response<ResponseModel<UserCard>> response) {
                        super.onResponse(call, response);
                        UserCard result = getResultOrHandled();
                        if (result == null) {
                            return;
                        }
                        DbHelper.save(UserEntity.class, result.toUserEntity());
                        callback.onSuccess(result);
                    }
                });
    }

    /**
     * 根据用户名模糊搜索
     *
     * @param name     被关注者的用户Id
     * @param callback 回调
     * @return Call 当前接口的调度者
     */
    @SuppressWarnings("rawtypes")
    public static Call search(@Nullable String name, IDataSource.Callback<List<UserCard>> callback) {
        Log.i(TAG, "search: start");
        if (TextUtils.isEmpty(name)) {
            name = "";
        }
        Call<ResponseModel<List<UserCard>>> call = Network.remote().userSearch(name);
        call.enqueue(new MyCallback<List<UserCard>>(callback) {
            @Override
            public void onResponse(@NonNull Call<ResponseModel<List<UserCard>>> call,
                                   @NonNull Response<ResponseModel<List<UserCard>>> response) {
                super.onResponse(call, response);
                List<UserCard> result = getResultOrHandled();
                if (result == null) {
                    return;
                }
                callback.onSuccess(result);
            }
        });
        return call;
    }

    /**
     * 关注 (加好友)
     *
     * @param id       被关注者的用户Id
     * @param callback 回调
     */
    public static void follow(String id, IDataSource.Callback<UserCard> callback) {
        Log.i(TAG, "follow: start");
        if (TextUtils.isEmpty(id)) {
            Log.w(TAG, "follow: id == null");
            return;
        }
        Network.remote()
                .userFollow(id)
                .enqueue(new MyCallback<UserCard>(callback) {
                    @Override
                    public void onResponse(@NonNull Call<ResponseModel<UserCard>> call,
                                           @NonNull Response<ResponseModel<UserCard>> response) {
                        super.onResponse(call, response);
                        UserCard result = getResultOrHandled();
                        if (result == null) {
                            return;
                        }

                        // 保存并通知联系人列表刷新
                        DbHelper.save(UserEntity.class, result.toUserEntity());
                        callback.onSuccess(result);
                    }
                });
    }

    /**
     * 获取联系人列表
     *
     * @param callback 回调
     */
    public static void getContacts(IDataSource.Callback<List<UserEntity>> callback) {
        Log.i(TAG, "getContacts: start");
        Network.remote().userContacts().enqueue(
                new MyCallback<List<UserCard>>(callback) {
                    @Override
                    public void onResponse(@NonNull Call<ResponseModel<List<UserCard>>> call, @NonNull Response<ResponseModel<List<UserCard>>> response) {
                        super.onResponse(call, response);
                        List<UserCard> result = getResultOrHandled();
                        if (result == null) {
                            return;
                        }

                        // TODO 数据库处理 待完善
                        List<UserEntity> entities = new ArrayList<>();
                        for (UserCard card : result) {
                            entities.add(card.toUserEntity());
                        }

                        callback.onSuccess(entities);
                    }
                }
        );
    }

    /**
     * 查询指定Id的用户信息 (网络)
     *
     * @param id       用户Id
     * @param callback 回调
     */
    public static void info(String id, IDataSource.Callback<UserEntity> callback) {
        Log.i(TAG, "info: start");
        if (TextUtils.isEmpty(id)) {
            Log.w(TAG, "info: id == null");
            return;
        }
        Network.remote()
                .userInfo(id)
                .enqueue(new MyCallback<UserCard>(callback) {
                    @Override
                    public void onResponse(@NonNull Call<ResponseModel<UserCard>> call,
                                           @NonNull Response<ResponseModel<UserCard>> response) {
                        super.onResponse(call, response);
                        UserCard result = getResultOrHandled();
                        if (result == null) {
                            return;
                        }

                        UserEntity userEntity = result.toUserEntity();
                        DbHelper.save(UserEntity.class, userEntity);
                        callback.onSuccess(userEntity);
                    }
                });
    }

    /**
     * 查询指定Id的用户信息 (优先本地，其次网络)
     *
     * @param id 用户Id
     */
    public static void infoFirstOfLocal(String id, IDataSource.Callback<UserEntity> callback) {
        if (TextUtils.isEmpty(id) || callback == null) {
            Log.w(TAG, "infoFirstOfLocal: id == null || callback == null");
            return;
        }
        UserEntity userEntity = infoFromLocal(id);
        if (userEntity != null) {
            callback.onSuccess(userEntity);
        } else {
            info(id, callback);
        }
    }

    /**
     * 查询指定Id的用户信息 (本地)
     *
     * @param id 用户Id
     */
    @Nullable
    private static UserEntity infoFromLocal(String id) {
        return SQLite.select()
                .from(UserEntity.class)
                .where(UserEntity_Table.id.eq(id))
                .querySingle();
    }
}
