package com.example.inventorydatabasev3.data;

/**
 * Created by Michael on 3/25/2017.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.inventorydatabasev3.data.InventoryContract.InventoryEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory";
    private static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_INVENTORY_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_INVENTORY_PRICE + " DOUBLE NOT NULL, "
                + InventoryEntry.COLUMN_INVENTORY_QUANTITY + " INTEGER NOT NULL, "
                + InventoryEntry.COLUMN_INVENTORY_PHOTO + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_INVENTORY_SUPPLIER_EMAIL + " TEXT NOT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS" + InventoryEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
