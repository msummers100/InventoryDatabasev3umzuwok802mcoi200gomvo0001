package com.example.inventorydatabasev3;

/**
 * Created by Michael on 3/25/2017.
 */
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventorydatabasev3.data.InventoryContract.InventoryEntry;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class ProductDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PICK_IMAGE_REQUEST = 0;
    private boolean isGalleryPicture = false;
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.inventorydatabasev3";
    private static final int EXISTING_INVENTORY_LOADER = 0;

    private Uri mCurrentInventoryUri;
    private TextView mNameTextView;
    private TextView mPriceTextView;
    private TextView mQuantityTextView;
    private TextView mSupplierNameTextView;
    private TextView mSupplierEmailTextView;
    private ImageView mPhotoImageView;

    private String mName;
    private double mPrice;
    private int mQuantity;
    private Uri mUri;
    private String uriString;
    private Bitmap mBitmap;
    private String mSupplierName;
    private String mSupplierEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.product_details);
        setContentView(R.layout.activity_product_details);

        Intent intent = getIntent();
        mCurrentInventoryUri = intent.getData();

        // Find all relevant views that we will need to read user input from and store
        mNameTextView = (TextView) findViewById(R.id.product_name);
        mPriceTextView = (TextView) findViewById(R.id.product_price);
        mQuantityTextView = (TextView) findViewById(R.id.product_quantity);
        mSupplierNameTextView = (TextView) findViewById(R.id.supplier_name);
        mSupplierEmailTextView = (TextView) findViewById(R.id.supplier_email);
        mPhotoImageView = (ImageView)findViewById(R.id.product_detail_photo_view);

        // Initialize a loader to read the inventory data from the database
        // and display the current values in the editor
        getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);


        final Button incrementButton = (Button) findViewById(R.id.increase_button);
        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText incrementEditText = (EditText) findViewById(R.id.quantity_increment);
                String incrementString = incrementEditText.getText().toString().trim();
                if (!incrementString.isEmpty()) {
                    int increment = Integer.parseInt(incrementString);
                    int updatedQuantity = increment + mQuantity;
                    updateQuantity(updatedQuantity);
                }
            }
        });

        final Button decrementButton = (Button) findViewById(R.id.decrease_button);
        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText decrementTextView = (EditText) findViewById(R.id.quantity_decrement);
                String decrementString = decrementTextView.getText().toString().trim();
                if (!decrementString.isEmpty()) {
                    int decrement = Integer.parseInt(decrementString);
                    int updatedQuantity;
                    if (mQuantity > decrement) {
                        updatedQuantity = mQuantity - decrement;
                        updateQuantity(updatedQuantity);
                    } else {
                        Toast.makeText(getApplicationContext(), "Current quantity in stock is less than " + mQuantity, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });

        Button deleteButton = (Button) findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });

        Button orderButton = (Button) findViewById(R.id.order_button);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subject = "Order Request";
                String[] email = {mSupplierEmail};
                StringBuilder emailBody = new StringBuilder();
                emailBody.append("There are only ");
                emailBody.append(mQuantity);
                emailBody.append(" left for ");
                emailBody.append(mName);
                composeEmail(email, subject, emailBody);
            }
        });

    }



    private void composeEmail(String[] email, String subject, StringBuilder stringBuilder) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, email);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, (Serializable) stringBuilder);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this product?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                deleteProduct();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (mCurrentInventoryUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentInventoryUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, "Error with deleting product",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Product deleted",
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void updateQuantity(int updatedQuantity) {
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_NAME, mName);
        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, mPrice);
        values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, updatedQuantity);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME, mSupplierName);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL, mSupplierEmail);

        int rowsAffected = getContentResolver().update(mCurrentInventoryUri, values, null, null);

        if (rowsAffected == 0) {
            Toast.makeText(getApplicationContext(), "Error with updating quantity", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Quantity updated", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryEntry.COLUMN_INVENTORY_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_PHOTO,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL
        };
        return new CursorLoader(this, mCurrentInventoryUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
            int photoColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PHOTO);
            int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME);
            int supplierEmailColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL);

            mName = cursor.getString(nameColumnIndex);
            mPrice = cursor.getDouble(priceColumnIndex);
            mQuantity = cursor.getInt(quantityColumnIndex);
            mUri = Uri.parse(cursor.getString(photoColumnIndex));
            mSupplierName = cursor.getString(supplierNameColumnIndex);
            mSupplierEmail = cursor.getString(supplierEmailColumnIndex);

            mNameTextView.setText(mName);
            mPriceTextView.setText(String.valueOf(mPrice));
            mQuantityTextView.setText(String.valueOf(mQuantity));
            mSupplierNameTextView.setText(mSupplierName);
            mSupplierEmailTextView.setText(mSupplierEmail);
            mPhotoImageView.setImageBitmap(getBitmapFromUri(mUri));

        }
    }

    // Select photo image and set Bitmap
    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (resultCode == Activity.RESULT_OK) {

            if (resultData != null) {
                mUri = resultData.getData();

                mBitmap = getBitmapFromUri(mUri);
                mPhotoImageView.setImageBitmap(mBitmap);
                uriString = getShareableImageUri().toString();
                isGalleryPicture = true;
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = null;
            if (parcelFileDescriptor != null) {
                fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            }
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            if (parcelFileDescriptor != null) {
                parcelFileDescriptor.close();
            }
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

    public Uri getShareableImageUri() {
        Uri imageUri;

        if (isGalleryPicture) {
            String filename = getFilePath();
            saveBitmapToFile(getCacheDir(), filename, mBitmap, Bitmap.CompressFormat.JPEG, 100);
            File imageFile = new File(getCacheDir(), filename);

            imageUri = FileProvider.getUriForFile(
                    this, FILE_PROVIDER_AUTHORITY, imageFile);

        } else {
            imageUri = mUri;
        }

        return imageUri;

    }
    public String getFilePath() {
        Cursor returnCursor =
                getContentResolver().query(mUri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);

        if (returnCursor != null) {
            returnCursor.moveToFirst();
        }
        String fileName = null;
        if (returnCursor != null) {
            fileName = returnCursor.getString(returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        }
        if (returnCursor != null) {
            returnCursor.close();
        }
        return fileName;
    }


    public boolean saveBitmapToFile(File dir, String fileName, Bitmap bm,
                                    Bitmap.CompressFormat format, int quality) {
        File imageFile = new File(dir, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bm.compress(format, quality, fos);
            fos.close();

            return true;
        } catch (IOException e) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
