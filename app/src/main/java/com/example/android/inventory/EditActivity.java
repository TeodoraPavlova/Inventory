package com.example.android.inventory;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract;
import com.squareup.picasso.Picasso;

import java.io.File;

public class EditActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri currentUri;

    private EditText productEditName;
    private EditText productEditQuantity;
    private EditText productEditPrice;
    private ImageView mailButton;

    private Button increaseQuantityButton;
    private Button decreaseQuantityButton;
    private int quantityHolderVariable;

    private boolean changedFields = false;

    private final static int REQUESTED_INT_PHOTO = 77;
    private final static int REQUESTED_INT_STORAGE = 777;
    private ImageView productImage;
    private String currentPhoto="no image";

    private static final int EXISTING_PRODUCTS_LOADER = 0;

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            changedFields = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        productImage = (ImageView) findViewById(R.id.image);
        productEditName= (EditText) findViewById(R.id.edit_name);
        productEditPrice=(EditText) findViewById(R.id.edit_price);
        productEditQuantity = (EditText) findViewById(R.id.edit_quantity);
        increaseQuantityButton = (Button) findViewById(R.id.button_increase);
        decreaseQuantityButton = (Button) findViewById(R.id.button_decrease);

        productEditName.setOnTouchListener(touchListener);
        productEditQuantity.setOnTouchListener(touchListener);
        productEditPrice.setOnTouchListener(touchListener);
        productImage.setOnTouchListener(touchListener);
        increaseQuantityButton.setOnTouchListener(touchListener);
        decreaseQuantityButton.setOnTouchListener(touchListener);


        Intent intent = getIntent();
        currentUri = intent.getData();

        if (currentUri != null) {
            setTitle(R.string.edit_label);
            getLoaderManager().initLoader(EXISTING_PRODUCTS_LOADER, null, this);
        } else {
            setTitle(R.string.add_label);
            invalidateOptionsMenu();
        }

        mailButton = (ImageView) findViewById(R.id.button_mail);

        updateView();
    }

    private void updateView() {

        productEditName = (EditText) findViewById(R.id.edit_name);
        productEditQuantity = (EditText) findViewById(R.id.edit_quantity);
        productEditPrice = (EditText) findViewById(R.id.edit_price);

        if (currentUri != null) {
            mailButton.setVisibility(View.VISIBLE);
            mailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String productName=String.valueOf(productEditName.getText().toString());
                    String price = String.valueOf(productEditPrice.getText().toString());
                    String emailMessage ="Product name:"+productName+"\n"
                            +"Price: "+price;
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setData(Uri.parse("mailto:"));
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Your order");
                    intent.putExtra(Intent.EXTRA_TEXT, emailMessage);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });

            increaseQuantityButton = (Button) findViewById(R.id.button_increase);
            increaseQuantityButton.setOnTouchListener(touchListener);
            increaseQuantityButton.setVisibility(View.VISIBLE);
            increaseQuantityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(productEditQuantity.getText()) &&
                            Integer.valueOf(productEditQuantity.getText().toString()) > 0) {
                        quantityHolderVariable = Integer.valueOf(productEditQuantity.getText().toString());
                        increaseQuantityButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                quantityHolderVariable++;
                                productEditQuantity.setText(String.valueOf(quantityHolderVariable));
                            }
                        });
                    }
                }
            });

            decreaseQuantityButton = (Button) findViewById(R.id.button_decrease);
            decreaseQuantityButton.setOnTouchListener(touchListener);
            decreaseQuantityButton.setVisibility(View.VISIBLE);
            decreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(productEditQuantity.getText()) &&
                            Integer.valueOf(productEditQuantity.getText().toString()) > 0) {
                            quantityHolderVariable = Integer.valueOf(productEditQuantity.getText().toString());
                            decreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (quantityHolderVariable > 0) {
                                        quantityHolderVariable--;
                                        productEditQuantity.setText(String.valueOf(quantityHolderVariable));
                                    }
                                }
                            });

                    }
                }
            });

        productImage = (ImageView) findViewById(R.id.image);
        productImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               checkPermissionForImage(v);
            }

        });
    }
    }



    public void checkPermissionForImage(View view){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED){
                importImage();
            }else{
                String[] requestPermission = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(requestPermission,REQUESTED_INT_STORAGE);
            }
        }else{
            importImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUESTED_INT_STORAGE && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED){
            importImage();
        }
    }

    private void importImage() {
        File directory = Environment.
                getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        String imgDirectory = directory.getPath();
        Uri picture = Uri.parse(imgDirectory);

        Intent selectImage = new Intent(Intent.ACTION_PICK);
        selectImage.setDataAndType(picture, "image/*");
        startActivityForResult(selectImage, REQUESTED_INT_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUESTED_INT_PHOTO && resultCode==RESULT_OK && data!=null) {
            Uri photoUri = data.getData();
            currentPhoto = photoUri.toString();
            Picasso.with(EditActivity.this).load(photoUri)
                    .placeholder(R.drawable.add_image).fit().into(productImage);
        }
        }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete_product);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_product:
                if (changedFields) {
                    saveOrUpdate();
                }
                return true;
            case R.id.delete_product:
                deleteConfirmation();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!changedFields) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButton =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        unsavedChanges(discardButton);
    }

    private void saveOrUpdate() {
        String name = productEditName.getText().toString().trim();
        String price = productEditPrice.getText().toString().trim();
        String quantity = productEditQuantity.getText().toString().trim();

        ContentValues contentValues = new ContentValues();
        contentValues.put
                (InventoryContract.ProductEntry.PRODUCT_IMAGE, currentPhoto);
        contentValues.put
                (InventoryContract.ProductEntry.PRODUCT_NAME, name);
        contentValues.put
                (InventoryContract.ProductEntry.PRODUCT_PRICE, price);
        contentValues.put
                (InventoryContract.ProductEntry.PRODUCT_QUANTITY, quantity);

        productImage = (ImageView) findViewById(R.id.image);
        productImage.setOnTouchListener(touchListener);
        productImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionForImage(v);
            }

        });

        if (currentPhoto.equals("no image")|| name.isEmpty() || price.isEmpty() || quantity.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }
        productImage = (ImageView) findViewById(R.id.image);
        if (currentUri == null) {
            Uri insertedItem = getContentResolver().insert
                    (InventoryContract.ProductEntry.CONTENT_URI, contentValues);
            if (insertedItem == null) {
                Toast.makeText(this, "Error inserting product.", Toast.LENGTH_SHORT).show();
            }
        } else {
            int update = getContentResolver().update
                    (currentUri, contentValues, null, null);

            if (update == 0) {
                Toast.makeText(this, "Error updating product.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Updating product successful.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }
            finish();
        }

    private void deleteProduct() {
        if (currentUri != null) {
            int rowsDeleted = getContentResolver().delete(currentUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this,"An error occupied please try again.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,"Delete successful.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
                finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void unsavedChanges
    (DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Leave without saving?");
        builder.setPositiveButton("Yes", discardButtonClickListener);
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryContract.ProductEntry._ID,
                InventoryContract.ProductEntry.PRODUCT_IMAGE,
                InventoryContract.ProductEntry.PRODUCT_NAME,
                InventoryContract.ProductEntry.PRODUCT_PRICE,
                InventoryContract.ProductEntry.PRODUCT_QUANTITY};

        return new CursorLoader(EditActivity.this,currentUri,
                projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()){
            int imgIndex = data.getColumnIndex
                    (InventoryContract.ProductEntry.PRODUCT_IMAGE);
            int nameIndex = data.getColumnIndex
                    (InventoryContract.ProductEntry.PRODUCT_NAME);
            int priceIndex = data.getColumnIndex
                    (InventoryContract.ProductEntry.PRODUCT_PRICE);
            int quantityIndex = data.getColumnIndex
                    (InventoryContract.ProductEntry.PRODUCT_QUANTITY);

            String name = data.getString(nameIndex);
            productEditName.setText(name);
            double price = data.getDouble(priceIndex);
            productEditPrice.setText(String.valueOf(price));
            int quantity = data.getInt(quantityIndex);
            productEditQuantity.setText(String.valueOf(quantity));
            currentPhoto = data.getString(imgIndex);

            Picasso.with(EditActivity.this).load(currentPhoto)
                    .placeholder(R.drawable.add_image).fit().into(productImage);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productEditName.getText().clear();
        productEditName.setText("");
        productEditPrice.getText().clear();
        productEditPrice.setText("");
        productEditQuantity.getText().clear();
        productEditQuantity.setText("");
    }

}