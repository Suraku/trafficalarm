package com.suraku.trafficalarm.data.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

import com.suraku.dev.sqlite.SQLiteModelHelper;
import com.suraku.trafficalarm.data.scripts.SQL_1;
import com.suraku.trafficalarm.data.scripts.SQL_2;

import java.util.List;
import java.util.UUID;

/**
 * Queries the local storage database
 */

class LocalStorageProvider<TModel> extends SQLiteOpenHelper implements ILocalStorageProvider<TModel>
{
    private static final String DATABASE_NAME = "trafficalert.db";
    private static final int DATABASE_VERSION = 2;
    private SQLiteModelHelper mHelper;
    private Context mContext;

    LocalStorageProvider(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    // ~ SQL Helper ~

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("APP", "LocalStorageProvider_onCreate");
        _executeSqlScripts(db, 0, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("APP", "LocalStorageProvider_onUpgrade");
        _executeSqlScripts(db, oldVersion, newVersion);
    }

    // ~ Provider implementation ~

    @Override
    public void setModelClass(Class type) {
        mHelper = new SQLiteModelHelper(this, type);
    }

    @Override
    public TModel find(UUID primaryKey)
    {
        return mHelper.Find(primaryKey);
    }

    @Override
    public List<TModel> findAll(Pair<String, Object>... args) {
        return mHelper.FindAll(args);
    }

    @Override
    public List<TModel> findAll(List<Pair<String, Object>> args) { return mHelper.FindAll(args); }

    @Override
    public int update(TModel model) {
        return mHelper.Update(model);
    }

    @Override
    public int delete(TModel model) {
        return mHelper.Delete(model);
    }

    // ~ SQL Scripts ~
    private static final String m_getCreateTableScript = "SHOW CREATE TABLE tablename";

    private void _executeSqlScripts(SQLiteDatabase db, int oldVersion, int newVersion) {
        int i = 1;
        if (oldVersion > 0) i = oldVersion + 1;

        for (; i <= newVersion; i++) {
            switch (i) {
                case 1:
                    _executeScripts(db, new SQL_1().getScripts());
                    break;
                case 2:
                    _executeScripts(db, new SQL_2().getScripts());
                    break;
            }
        }
        //db.close();
    }

    private void _executeScripts(SQLiteDatabase db, String[] scripts) {
        for (String script : scripts) {
            db.execSQL(script);
        }
    }
}
