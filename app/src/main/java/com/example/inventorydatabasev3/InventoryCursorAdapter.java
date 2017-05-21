package com.example.inventorydatabasev3;

/**
 * Created by Michael on 3/25/2017.
 */
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventorydatabasev3.data.InventoryContract.InventoryEntry;

public class InventoryCursorAdapter extends CursorAdapter {


    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.list_item_product_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.list_item_product_price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.list_item_product_quantity);
        ImageView photoImageView = (ImageView) view.findViewById(R.id.photo_view_item);
        Button trackerButton = (Button) view.findViewById(R.id.tracker_button);

        // Find the columns of inventory attributes that we're interested it
        final int rowId = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry._ID));
        String productName = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_INVENTORY_NAME));
        double productPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_INVENTORY_PRICE));
        final int[] productQuantity = {cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_INVENTORY_QUANTITY))};
        String photoUriString = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_INVENTORY_PHOTO));
        Uri photoUri = Uri.parse(photoUriString);

        // Set the views with the data
        nameTextView.setText(productName);
        priceTextView.setText(String.valueOf(productPrice));
        quantityTextView.setText(String.valueOf(productQuantity[0]));
        photoImageView.setImageBitmap(new AddProduct().getBitmapFromUri(photoUri));

        photoImageView.setImageURI(photoUri);

        trackerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (productQuantity[0] > 1) {
                    productQuantity[0] -= 1;
                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, productQuantity[0]);
                    Uri mCurrentInventoryUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, rowId);
                    int rowsAffected = context.getContentResolver().update(mCurrentInventoryUri, values, null, null);

                    if (rowsAffected == 0) {
                        Toast.makeText(context.getApplicationContext(), "Error with sales tracker update", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context.getApplicationContext(), "Sales tracker updated", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
