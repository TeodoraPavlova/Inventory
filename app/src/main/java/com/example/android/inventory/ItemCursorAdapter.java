package com.example.android.inventory;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventory.data.InventoryContract;
import com.squareup.picasso.Picasso;

public class ItemCursorAdapter extends CursorAdapter {

    public ItemCursorAdapter(Context context, Cursor cursor){
        super(context,cursor,0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_product,viewGroup,false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        ImageView itemImage = (ImageView) view.findViewById(R.id.item_product_image);
        int imgIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.PRODUCT_IMAGE);
        TextView itemName = (TextView) view.findViewById(R.id.item_product_name);
        int nameIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.PRODUCT_NAME);
        TextView itemPrice = (TextView) view.findViewById(R.id.item_product_price);
        int priceIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.PRODUCT_PRICE);
        final TextView itemQuantity = (TextView) view.findViewById(R.id.item_product_quantity);
        int quantityIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.PRODUCT_QUANTITY);

        int idNumber = cursor.getInt(cursor.getColumnIndex(InventoryContract.ProductEntry._ID));
        Uri productImg = Uri.parse(cursor.getString(imgIndex));
        final String productName = cursor.getString(nameIndex);
        final double productPrice = cursor.getDouble(priceIndex);
        final int productQuantity = cursor.getInt(quantityIndex);

        final Uri uri = ContentUris.withAppendedId(InventoryContract.ProductEntry.CONTENT_URI,
                cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.ProductEntry._ID)));

        ImageView saleButton = (ImageView) view.findViewById(R.id.item_product_sale);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(productQuantity>0){
                    int updatedQuantity = productQuantity -1;
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(InventoryContract.ProductEntry.PRODUCT_QUANTITY,updatedQuantity);
                    context.getContentResolver().update(uri,contentValues,null,null);
                }
            }
        });

        Picasso.with(context).load(productImg).placeholder(R.drawable.add_image)
                .fit().into(itemImage);
        itemName.setText(productName);
        itemPrice.setText(String.valueOf(productPrice));
        itemQuantity.setText(String.valueOf(productQuantity));

    }


}
