package com.example.inventorydatabasev3;

/**
 * Created by Michael on 3/25/2017.
 */

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.inventorydatabasev3.data.InventoryContract.InventoryEntry;

import java.io.FileDescriptor;
import java.io.IOException;

public class AddProduct extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 1;
    private EditText productNameEditText;
    private EditText productPriceEditText;
    private EditText productQuantityEditText;
    private EditText productSupplierNameEditText;

    private EditText productSupplierEmailEditText;
    private ImageView mImageView;
    private Uri mPhotoUri;
    private static final int PICK_IMAGE_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.add_a_product);
        setContentView(R.layout.activity_add_a_product);

        productNameEditText = (EditText) findViewById(R.id.product_name_text_view);
        productPriceEditText = (EditText) findViewById(R.id.product_price_text_view);
        productQuantityEditText = (EditText) findViewById(R.id.product_quantity_text_view);
        productSupplierNameEditText = (EditText) findViewById(R.id.supplier_name_text_view);
        productSupplierEmailEditText = (EditText) findViewById(R.id.supplier_email_text_view);

        mImageView = (ImageView) findViewById(R.id.upload_photo_image_view);

        Button addPhotoButton = (Button) findViewById(R.id.upload_a_photo);
        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestStoragePermission();
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                mPhotoUri = resultData.getData();
                mImageView.setImageURI(mPhotoUri);

            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
            assert parcelFileDescriptor != null;
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    private void saveInventory() {
        String nameString = productNameEditText.getText().toString().trim();
        String priceString = productPriceEditText.getText().toString().trim();

        double price;
        if (priceString.isEmpty()) {
            price = 0;
        } else {
            price = Double.parseDouble(priceString);
        }

        String quantityString = productQuantityEditText.getText().toString().trim();
        int quantity;
        if (quantityString.isEmpty()) {
            quantity = 0;
        } else {
            quantity = Integer.parseInt(quantityString);
        }

        String supplierNameString = productSupplierNameEditText.getText().toString().trim();
        String supplierEmailString = productSupplierEmailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nameString) ||
                price == 0 ||
                quantity == 0 ||
                TextUtils.isEmpty(mPhotoUri.toString()) ||
                TextUtils.isEmpty(supplierNameString) ||
                TextUtils.isEmpty(supplierEmailString)) {
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and product attributes are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_NAME, nameString);
        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, price);
        values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantity);
        values.put(InventoryEntry.COLUMN_INVENTORY_PHOTO, mPhotoUri.toString());
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME, supplierNameString);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL, supplierEmailString);

        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

        if (newUri == null) {
            Toast.makeText(this, getString(R.string.insert_product_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.insert_product_succeeded),
                    Toast.LENGTH_SHORT).show();
        }

        finish();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_product, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveInventory();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}