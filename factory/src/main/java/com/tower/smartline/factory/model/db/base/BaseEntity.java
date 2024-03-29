package com.tower.smartline.factory.model.db.base;

import com.tower.smartline.factory.utils.DiffUiDataCallback;

import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * 数据库Entity基础类
 *
 * @param <E> Entity类本身
 * @author zpsong-tower <pingzisong2012@gmail.com>
 * @since 2021/7/25 14:38
 */
public abstract class BaseEntity<E> extends BaseModel
        implements DiffUiDataCallback.UiDataDiffer<E> {
}
