package com.example.vsimplephotogallery;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "image_db";
    private static final String TABLE_NAME = "images";
    protected static final String COLUMN_NAME = "image";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COLUMN_NAME + " BLOB)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropTableQuery = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(dropTableQuery);
        onCreate(db);
    }

    public void insertImage(SQLiteDatabase db, byte[] imageByteArray) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, imageByteArray);
        db.insert(TABLE_NAME, null, contentValues);
    }

    public Cursor retrieveImages(SQLiteDatabase db) {
        String selectQuery = "SELECT " + COLUMN_NAME + " FROM " + TABLE_NAME;
        return db.rawQuery(selectQuery, null);
    }

    public boolean isTableExists() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?", new String[]{TABLE_NAME});
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    protected void dropTable() {
        SQLiteDatabase db = getWritableDatabase();
        String dropTableQuery = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(dropTableQuery);
        onCreate(db); // Recreate the table
        db.close();
    }

}

