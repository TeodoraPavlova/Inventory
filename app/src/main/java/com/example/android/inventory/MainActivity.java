package com.example.android.inventory;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    ItemCursorAdapter mAdapter;
    private static final int LOADER_ID=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportLoaderManager().initLoader(LOADER_ID,null,this);

        FloatingActionButton add = (FloatingActionButton) findViewById(R.id.add_new_product);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,EditActivity.class);
                startActivity(intent);
            }
        });

        ListView productsList = (ListView) findViewById(R.id.products);

        RelativeLayout empty = (RelativeLayout) findViewById(R.id.empty_view);
        productsList.setEmptyView(empty);

        mAdapter = new ItemCursorAdapter(this,null);
        productsList.setAdapter(mAdapter);

        productsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this,EditActivity.class);
                Uri currentItem = ContentUris.withAppendedId(
                        InventoryContract.ProductEntry.CONTENT_URI,l);
                intent.setData(currentItem);
                startActivity(intent);
            }
        });
    }

    private void deleteItemDialog() {
        final AlertDialog.Builder alertDeletion = new AlertDialog.Builder(MainActivity.this);
        alertDeletion.setMessage("Are you sure you want to make those changes?");
        alertDeletion.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteAll();
                Toast.makeText(MainActivity.this,
                        "All items successfully deleted.",Toast.LENGTH_SHORT).show();
            }
        });
        alertDeletion.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();

            }
        });
        final AlertDialog alertDialog = alertDeletion.create();
        alertDialog.show();
    }

    private void deleteAll(){
        int deleteAll = getContentResolver().delete
                (InventoryContract.ProductEntry.CONTENT_URI,null,null);
        if(deleteAll>0){
            Toast.makeText(MainActivity.this,"Items deletion successful.",Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete_products:
                deleteItemDialog();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ID:
            String[] projection = {
                    InventoryContract.ProductEntry._ID,
                    InventoryContract.ProductEntry.PRODUCT_IMAGE,
                    InventoryContract.ProductEntry.PRODUCT_NAME,
                    InventoryContract.ProductEntry.PRODUCT_PRICE,
                    InventoryContract.ProductEntry.PRODUCT_QUANTITY};

            return new CursorLoader(MainActivity.this,
                    InventoryContract.ProductEntry.CONTENT_URI,
                    projection, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
