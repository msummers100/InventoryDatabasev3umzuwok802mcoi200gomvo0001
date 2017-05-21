package com.example.inventorydatabasev3.data;

/**
 * Created by Michael on 3/25/2017.
 */
import android.net.Uri;
import android.provider.BaseColumns;

public final class InventoryContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private InventoryContract() {}

    public static final String CONTENT_AUTHORITY = "com.example.inventorydatabasev3";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY = "inventory";

    public static final class InventoryEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_INVENTORY);
        public static final String TABLE_NAME = "inventory";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_INVENTORY_NAME = "name";
        public static final String COLUMN_INVENTORY_PRICE = "price";
        public static final String COLUMN_INVENTORY_QUANTITY = "quantity";
        public static final String COLUMN_INVENTORY_PHOTO = "photo";
        public static final String COLUMN_INVENTORY_SUPPLIER_NAME = "supplier";
        public static final String COLUMN_INVENTORY_SUPPLIER_EMAIL = "email";
    }
}
