package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";

    private static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String SQL_CREATE_INVENTORY_TABLE =  "CREATE TABLE " +
                InventoryContract.ProductEntry.TABLE_NAME + " ("
                + InventoryContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryContract.ProductEntry.PRODUCT_IMAGE + " TEXT NOT NULL DEFAULT 'no image', "
                + InventoryContract.ProductEntry.PRODUCT_NAME + " TEXT NOT NULL, "
                + InventoryContract.ProductEntry.PRODUCT_PRICE + " REAL NOT NULL, "
                + InventoryContract.ProductEntry.PRODUCT_QUANTITY + " INTEGER DEFAULT 0 ); ";

        sqLiteDatabase.execSQL(SQL_CREATE_INVENTORY_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldDb, int newDb) {
        sqLiteDatabase.execSQL
                ("DROP TABLE IF EXISTS "+ InventoryContract.ProductEntry.TABLE_NAME);
    }
}
