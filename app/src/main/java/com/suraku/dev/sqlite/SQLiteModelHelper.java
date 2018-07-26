package com.suraku.dev.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

import com.suraku.dev.extensions.TypeParser;
import com.suraku.trafficalarm.Helper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * SQLite helper library
 */

public class SQLiteModelHelper
{
    private String m_TableName;
    private String m_PrimaryKeyName;

    private Field[] m_ModelFields;
    private Class m_ModelClass;
    private SQLiteOpenHelper m_dbHelper;

    public void setTableName(String val) {
        this.m_TableName = val;
    }
    public void setPrimaryKeyName(String val) {
        this.m_PrimaryKeyName = val;
    }

    public SQLiteModelHelper(SQLiteOpenHelper dbHelper, Class modelType)
    {
        // Set the helper
        m_dbHelper = dbHelper;
        m_ModelClass = modelType;

        // Get table name from the class
        int modelNameStartIndex = modelType.getName().lastIndexOf(".");
        m_TableName = modelType.getName().substring(modelNameStartIndex + 1).replace("Model", "").toLowerCase();

        Field[] array_allModelFields = modelType.getDeclaredFields();
        List<Field> allModelFields = new ArrayList<>();

        // Discover each model field
        for (Field field : array_allModelFields) {
            Annotation metadata = field.getAnnotation(SQLiteModelMetadata.class);
            if (metadata == null) {
                continue;
            }

            allModelFields.add(field);
            String fieldName = field.getName();

            // Find the models primary key
            String priKey = fieldName.substring(fieldName.length() - 2, fieldName.length());
            if (priKey.equals("PK")) {
                m_PrimaryKeyName = fieldName;
            }
        }

        m_ModelFields = allModelFields.toArray(new Field[allModelFields.size()]);
    }

    public <TModel> int Update(TModel model)
    {
        SQLiteDatabase db = m_dbHelper.getWritableDatabase();
        String primaryKeyValue = "";

        ContentValues values = new ContentValues();
        for (Field field : m_ModelFields) {
            String fieldName = field.getName();
            field.setAccessible(true);

            // Parse and set the values
            TypeParser typeParser = new TypeParser();
            try {
                Object fieldValue = field.get(model);
                values = typeParser.insertContentValues(values, fieldName, fieldValue);

                // Get the primary key whilst we're searching for all values for later.
                if (fieldName.equals(m_PrimaryKeyName)) {
                    primaryKeyValue = field.get(model).toString();
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        // Try find an existing record and update
        int i = db.update(m_TableName,
                values,
                m_PrimaryKeyName + " == ?",
                new String[] { primaryKeyValue }
        );

        // Add new entry if doesn't previously exist
        if (i == 0) {
            i = (int) db.insert(m_TableName, null, values);
        }

        // Close, log and returns number of updated rows.
        db.close();
        Log.d("updateModel()", model.toString());
        return i;
    }

    public <TModel> int Delete(TModel model)
    {
        SQLiteDatabase db = m_dbHelper.getWritableDatabase();
        String primaryKeyValue;

        // Find the primary key
        try {
            Field priKeyField = model.getClass().getDeclaredField(m_PrimaryKeyName);
            priKeyField.setAccessible(true);
            primaryKeyValue = priKeyField.get(model).toString();

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        int i = db.delete(m_TableName,
                m_PrimaryKeyName + " == ?",
                new String[] { primaryKeyValue }
        );

        // Close, log and returns number of deleted rows.
        db.close();
        Log.d("deleteModel()", model.toString());
        return i;
    }

    public <TModel> TModel Find(UUID primaryKey)
    {
        List<TModel> items = this.FindAll(
                new Pair<String, Object>(m_PrimaryKeyName + " == ?", primaryKey));

        if (items.size() > 0) {
            Log.d("Find() successful", primaryKey.toString());
            return items.get(0);
        } else {
            if (primaryKey != null) {
                Log.d("Find() failure", "PK: " + primaryKey.toString() + " and Name: " + m_PrimaryKeyName);
            }
            return null;
        }
    }

    public <TModel> List<TModel> FindAll(Pair<String, Object>... args)
    {
        List<Pair<String, Object>> tmpList = new ArrayList<>();
        for (Pair<String, Object> pair : args) {
            tmpList.add(pair);
        }

        return FindAll(tmpList);
    }

    public <TModel> List<TModel> FindAll(List<Pair<String, Object>> args)
    {
        List<TModel> contentList = new ArrayList<>();
        SQLiteDatabase db = m_dbHelper.getReadableDatabase();

        String selectionQuery = null;
        List<String> selectionArgs = null;

        if (args != null && args.size() > 0) {
            selectionQuery = "";
            selectionArgs = new ArrayList<>();

            // Example: String = "Name = ? AND Content = ?", String[] { MyName, null }
            for (int i = 0; i < args.size(); i++) {
                Pair<String, Object> pair = args.get(i);
                String pairFirst = pair.first;

                if (!pairFirst.endsWith("== ?") && !pairFirst.endsWith("!= ?")) {
                    pairFirst += " == ?";
                }

                if (pair.second != null) {
                    selectionArgs.add(pair.second.toString());
                } else {
                    if (pairFirst.contains("== ?")) {
                        pairFirst = pairFirst.replace("== ?", "IS NULL");
                    } else {
                        pairFirst = pairFirst.replace("!= ?", "IS NOT NULL");
                    }
                }

                if (i == args.size() - 1) {
                    selectionQuery += pairFirst;
                } else {
                    selectionQuery += pairFirst + " AND ";
                }
            }
        }

        // Execute the query to gather together our data we want.
        Cursor cursor = db.query(
                m_TableName,
                null,
                selectionQuery,
                (selectionArgs == null) ? null : selectionArgs.toArray(new String[selectionArgs.size()]),
                null, null, null, null
        );

        // Return an empty list if we have no results.
        if (cursor.getCount() <= 0) {
            db.close();
            return contentList;
        }
        cursor.moveToFirst();

        TypeParser typeParser = new TypeParser();
        do {
            try {
                @SuppressWarnings("unchecked")
                TModel model = (TModel)m_ModelClass.newInstance();
                HashMap<String, String> rowValues = new HashMap<>();

                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    rowValues.put(cursor.getColumnName(i).toLowerCase(), cursor.getString(i));
                }

                for (Field field : m_ModelFields) {
                    field.setAccessible(true);

                    // Get the value
                    String strVal = rowValues.get(field.getName().toLowerCase());
                    Object val = typeParser.setObjectType(field, strVal);

                    // Set the value with its correct type
                    Method setMethod = Helper.findMethodCaseInsensitive(m_ModelClass, "set" + field.getName());
                    try {
                        setMethod.invoke(model, val);
                    } catch (Exception e) {
                        field.set(model, val);
                    }
                }
                // Add to result
                contentList.add(model);

            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        } while (cursor.moveToNext());

        // Log and return the result.
        Log.d("findAll()", contentList.toString());
        cursor.close();
        db.close();
        return contentList;
    }

}
