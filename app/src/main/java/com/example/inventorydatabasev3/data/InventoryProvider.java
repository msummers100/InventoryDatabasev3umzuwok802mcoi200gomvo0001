package com.example.inventorydatabasev3.data;

/**
 * Created by Michael on 3/25/2017.
 */
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.inventorydatabasev3.data.InventoryContract.InventoryEntry;

/**
 * {@link ContentProvider} for Inventory Database v3 app.
 */

public class InventoryProvider extends ContentProvider {
    /** URI matcher code for the content URI for the inventory table */
    private static final int INVENTORY = 100;
    /** URI matcher code for the content URI for a single product in the inventory table */
    private static final int INVENTORY_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    // Static initializer. This is run the first time anything is called from this class.
    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    /** Database helper object */
    private InventoryDbHelper mInventoryDbHelper;

    @Override
    public boolean onCreate() {
        mInventoryDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mInventoryDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // For the INVENTORY code, query the inventory table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the inventory table.
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertInventory(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertInventory(Uri uri, ContentValues contentValues) {

        String name = contentValues.getAsString(InventoryEntry.COLUMN_INVENTORY_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        Double price = contentValues.getAsDouble(InventoryEntry.COLUMN_INVENTORY_PRICE);
        if (price <= 0) {
            throw new IllegalArgumentException("Product requires a valid price");
        }

        Integer quantity = contentValues.getAsInteger(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
        if (quantity <= 0) {
            throw new IllegalArgumentException("Product requires a valid quantity");
        }

        String photoUriString = contentValues.getAsString(InventoryEntry.COLUMN_INVENTORY_PHOTO);
        if (photoUriString == null) {
            throw new IllegalArgumentException("Product requires an image");
        }

        String supplierName = contentValues.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Product requires a supplier name");
        }

        String supplierEmail = contentValues.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL);
        if (supplierEmail == null) {
            throw new IllegalArgumentException("Product requires a supplier email");
        }

        SQLiteDatabase database = mInventoryDbHelper.getWritableDatabase();
        long id = database.insert(InventoryEntry.TABLE_NAME, null, contentValues);

        if (id == -1) {
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted;
        SQLiteDatabase database = mInventoryDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateInventory(uri, contentValues, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInventory(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for" + uri);
        }

    }

    private int updateInventory(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        if (contentValues.containsKey(InventoryEntry.COLUMN_INVENTORY_NAME)) {
            String name = contentValues.getAsString(InventoryEntry.COLUMN_INVENTORY_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        if (contentValues.containsKey(InventoryEntry.COLUMN_INVENTORY_PRICE)) {
            Double price = contentValues.getAsDouble(InventoryEntry.COLUMN_INVENTORY_PRICE);
            if (price <= 0) {
                throw new IllegalArgumentException("Product requires a valid price");
            }
        }

        if (contentValues.containsKey(InventoryEntry.COLUMN_INVENTORY_QUANTITY)) {
            int quantity = contentValues.getAsInteger(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
            if (quantity <= 0) {
                throw new IllegalArgumentException("Product requires a valid quantity");
            }
        }

        if (contentValues.containsKey(InventoryEntry.COLUMN_INVENTORY_PHOTO)) {
            String photoUriString = contentValues.getAsString(InventoryEntry.COLUMN_INVENTORY_PHOTO);
            if (photoUriString == null) {
                throw new IllegalArgumentException("Product requires a valid quantity");
            }
        }


        if (contentValues.containsKey(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME)) {
            String supplierName = contentValues.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Product requires a supplier name");
            }
        }

        if (contentValues.containsKey(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL)) {
            String supplierEmail = contentValues.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL);
            if (supplierEmail == null) {
                throw new IllegalArgumentException("Product requires a supplier email");
            }
        }
        if (contentValues.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mInventoryDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
