package com.suraku.trafficalarm.data.storage;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Base data interface
 */

public interface IBaseStorageProvider<TModel>
{
    TModel find(UUID pk);
    List<TModel> findAll(Pair<String, Object>... args);
    List<TModel> findAll(List<Pair<String, Object>> args);
    int update(TModel model);
    int delete(TModel model);
}
