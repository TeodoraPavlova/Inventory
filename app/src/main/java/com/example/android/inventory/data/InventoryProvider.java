package com.example.android.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class InventoryProvider extends ContentProvider {

    public static final String LOG = InventoryProvider.class.getSimpleName();

    private static final int PRODUCTS = 100;

    private static final int PRODUCT_ID = 101;

    private static final int PRODUCT_SEARCH = 102;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,
                InventoryContract.PATH_INVENTORY,PRODUCTS);
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,
                InventoryContract.PATH_INVENTORY+"/#",PRODUCT_ID);
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,
                InventoryContract.PATH_INVENTORY+"/*",PRODUCT_SEARCH);
    }

    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projectDb,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        SQLiteDatabase sqLiteDatabase = mDbHelper.getReadableDatabase();

        Cursor cursor = null;

        int matchUri = uriMatcher.match(uri);
        switch (matchUri){
            case PRODUCTS:
                cursor = sqLiteDatabase.query(InventoryContract.ProductEntry.TABLE_NAME,
                        projectDb,null,null,null,null,sortOrder);
                break;
            case PRODUCT_ID:
                selection = InventoryContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = sqLiteDatabase.query(InventoryContract.ProductEntry.TABLE_NAME,
                        projectDb,selection,selectionArgs,null,null,sortOrder);
                break;
            case PRODUCT_SEARCH:
                cursor = sqLiteDatabase.query(InventoryContract.ProductEntry.TABLE_NAME,
                        projectDb,selection,null,null,null,sortOrder);
            default:
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int matchUri = uriMatcher.match(uri);
        switch (matchUri){
            case PRODUCTS:
                return InventoryContract.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return InventoryContract.ProductEntry.CONTENT_ITEM_TYPE;
            case PRODUCT_SEARCH:
                return InventoryContract.ProductEntry.CONTENT_LIST_TYPE;
            default:
                throw new IllegalStateException("Unknown URI"+uri+"with match"+matchUri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        int matchUri = uriMatcher.match(uri);
        switch (matchUri){
            case PRODUCTS:
                return insertProduct(uri,contentValues);
            default:
                throw new IllegalArgumentException("You cannot insert: "+uri);
        }
    }

    private Uri insertProduct(Uri uri,ContentValues contentValues){
        SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();
        long id = sqLiteDatabase.insert
                (InventoryContract.ProductEntry.TABLE_NAME,null,contentValues);
        if(id==-1){
            Log.e(LOG,"Error inserting row: "+uri);
            return null;
        }

        String productName = contentValues.getAsString
                (InventoryContract.ProductEntry.PRODUCT_NAME);
        if(productName == null){
            throw new IllegalArgumentException("You must enter name.");
        }

        Double productPrice = contentValues.getAsDouble
                (InventoryContract.ProductEntry.PRODUCT_PRICE);
        if(productPrice == null || productPrice<0){
            throw new IllegalArgumentException("You must enter price.");
        }

        getContext().getContentResolver().notifyChange(uri,null);

        return ContentUris.withAppendedId(uri,id);
    }

    private int updateProduct(Uri uri,ContentValues contentValues,
                              String selection,String[] selectionArgs){
        SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();

        if (contentValues.size()==0){
            return 0;
        }

        if(contentValues.containsKey(InventoryContract.ProductEntry.PRODUCT_NAME)){
            String productName = contentValues.getAsString
                    (InventoryContract.ProductEntry.PRODUCT_NAME);
            if(productName== null){
                return 0;
            }
        }

        if(contentValues.containsKey(InventoryContract.ProductEntry.PRODUCT_PRICE)){
            Double productPrice = contentValues.getAsDouble
                    (InventoryContract.ProductEntry.PRODUCT_PRICE);
            if(productPrice == null || productPrice == 0){
                return 0;
            }
        }

        int updateRows = sqLiteDatabase.update(InventoryContract.ProductEntry.TABLE_NAME,
                contentValues,selection,selectionArgs);
        if(updateRows !=0){
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return updateRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        final int matchUri = uriMatcher.match(uri);

        switch (matchUri){
            case PRODUCTS:
                getContext().getContentResolver().notifyChange(uri,null);
                return updateProduct(uri,contentValues,selection,selectionArgs);
            case PRODUCT_ID:
                selection = InventoryContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                getContext().getContentResolver().notifyChange(uri,null);
                return updateProduct(uri,contentValues,selection,selectionArgs);
            default:
                throw new IllegalArgumentException("Error updating: "+uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        final int matchUri = uriMatcher.match(uri);
        SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();
        int deleteRows;
        switch (matchUri){
            case PRODUCTS:
                deleteRows = sqLiteDatabase.delete(InventoryContract.ProductEntry.TABLE_NAME,
                        selection,selectionArgs);
                break;
            case PRODUCT_ID:
                selection = InventoryContract.ProductEntry._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                deleteRows = sqLiteDatabase.delete(InventoryContract.ProductEntry.TABLE_NAME,
                        selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Error deleting: "+uri);
        }

        if(deleteRows != 0){
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return deleteRows;
    }
}
