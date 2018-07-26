package com.suraku.dev.extensions;

/**
 * Handles operations on various primitive types of un-casted objects
 */

import android.content.ContentValues;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class TypeParser
{
    public Object setObjectType(Field field, Object objValue)
    {
        // Null check
        if (objValue == null) {
            return null;
        }

        // Determine type to cast
        if (field.getType().isAssignableFrom(String.class)) {
            return objValue.toString();
        }
        else if (field.getType().isAssignableFrom(UUID.class)) {
            return UUID.fromString(objValue.toString());
        }
        else if (field.getType().isAssignableFrom(Long.TYPE)) {
            return Long.parseLong(objValue.toString());
        }
        else if (field.getType().isAssignableFrom(Integer.TYPE)) {
            return Integer.parseInt(objValue.toString());
        }
        else if (field.getType().isAssignableFrom(Calendar.class)) {
            Calendar ret = Calendar.getInstance();
            ret.setTimeInMillis(Long.parseLong(objValue.toString()));
            return ret;
        }
        else if (field.getType().isAssignableFrom(Date.class)) {
            return new Date(Long.parseLong(objValue.toString()));
        }
        else {
            Log.d("TypeParser", "SetObjectType: Missing datatype for fieldname: " + field.getName() + " and value: " + objValue);
        }

        // Default
        Log.d("TYPE_PARSER", "Missing parse for field: " + field.getName() + " and value: " + objValue.toString());
        return null;
    }

    public ContentValues insertContentValues(ContentValues values, String fieldName, Object fieldValue)
    {
        if (fieldValue instanceof String) {
            values.put(fieldName, (String) fieldValue);
        }
        else if (fieldValue instanceof UUID) {
            values.put(fieldName, fieldValue.toString());
        }
        else if (fieldValue instanceof Long) {
            values.put(fieldName, (Long) fieldValue);
        }
        else if (fieldValue instanceof Integer) {
            values.put(fieldName, (Integer) fieldValue);
        }
        else if (fieldValue instanceof Calendar) {
            values.put(fieldName, ((Calendar)fieldValue).getTimeInMillis());
        }
        else if (fieldValue instanceof Date) {
            values.put(fieldName, ((Date) fieldValue).getTime());
        }
        else if (fieldValue == null) {
            values.putNull(fieldName);
        }
        else {
            Log.d("TypeParser", "InsertContentValues: Missing datatype for fieldname: " + fieldName + " and value: " + fieldValue);
        }

        return values;
    }
}