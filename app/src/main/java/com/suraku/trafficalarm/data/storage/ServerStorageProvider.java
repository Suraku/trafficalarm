package com.suraku.trafficalarm.data.storage;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Queries a servers database
 */

class ServerStorageProvider<TModel> implements IServerStorageProvider<TModel>
{
    private Context m_Context;

    ServerStorageProvider(Context context) {
        m_Context = context;
    }

    @Override
    public TModel find(UUID primaryKey)
    {
        return null;
    }

    @Override
    public List<TModel> findAll(Pair<String, Object>... args) {
        return null;
    }

    @Override
    public List<TModel> findAll(List<Pair<String, Object>> args) { return null; }

    @Override
    public int update(TModel model) {
        return 0;
    }

    @Override
    public int delete(TModel model) {
        return 0;
    }
}
