package com.suraku.trafficalarm.data.storage;

/**
 * Interface
 */

public interface ILocalStorageProvider<TModel> extends IBaseStorageProvider<TModel>
{
    void setModelClass(Class type);
}
