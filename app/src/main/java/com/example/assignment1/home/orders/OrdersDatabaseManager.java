package com.example.assignment1.home.orders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class OrdersDatabaseManager {
    // defining of database metadata
    public static final String DB_NAME = "Outgoing";
    public static final String DB_TABLE = "Orders";
    public static final int DB_VERSION = 1;

    // sql string query to create a table with 3 columns
    private static final String CREATE_TABLE = "CREATE TABLE " + DB_TABLE
            + " (OrderID INTEGER PRIMARY KEY, DiningOption TEXT NOT NULL, TableNo INTEGER," +
            " Dishes TEXT NOT NULL, Price REAL NOT NULL);";

    // declaring of DB manager objects
    private SQLHelper helper;
    private SQLiteDatabase db;
    private Context context;

    // constructor injection of context
    public OrdersDatabaseManager(Context theContext) {
        context = theContext;
        helper = new SQLHelper(context);
    }

    public OrdersDatabaseManager openReadable() throws android.database.SQLException {
        helper = new SQLHelper(context);
        db = helper.getReadableDatabase();          // opening of database in readable mode
        return this;
    }

    public OrdersDatabaseManager openWritable() throws android.database.SQLException {
        helper = new SQLHelper(context);
        db = helper.getWritableDatabase();          // opening of database in writable mode
        return this;
    }

    public void close() {
        helper.close();                             // method to close database helper object
    }

    public void addRow(Order order) {
        openWritable();

        ContentValues newOrder = new ContentValues();
        newOrder.put("OrderID", order.getOrderId());
        newOrder.put("DiningOption", order.getDiningOption());
        newOrder.put("TableNo", order.getTableNo());
        newOrder.put("Dishes", order.getDishes());
        newOrder.put("Price", order.getPrice());

        try {
            db.insertOrThrow(DB_TABLE, null, newOrder);
            Log.i("OrderDB", "Inserted new row");
        }
        catch (Exception e) {
            Log.e("OrderDB - ERROR", e.toString());
            e.printStackTrace();
        }

        close();
    }

    public String retrieveRows() {

        // string array for selecting fields of table
        String[] columns = new String[] {"OrderID", "DiningOption", "TableNo", "Dishes", "Price"};
        openWritable();

        // declaring a new cursor object for our database table
        Cursor cursor = db.query(DB_TABLE, columns, null, null, null, null, null);
        String tablerows = "";

        // moving cursor to the first record on table
        cursor.moveToFirst();

        // while not past the last record in the table
        while (cursor.isAfterLast() == false) {

            // append retrieved record details to result string
            tablerows = tablerows + cursor.getInt(0) + ". " + cursor.getString(1)
                    + ", " + cursor.getInt(2) + ", " + cursor.getString(3)
                    + ", " + cursor.getDouble(4) + "\n";

            // iterate to next cursor
            cursor.moveToNext();
        }

        // closing of cursor
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        close();
        return tablerows;
    }


    public void updateRow(Order order) {
        openWritable();

        ContentValues newOrder = new ContentValues();
        newOrder.put("OrderID", order.getOrderId());
        newOrder.put("DiningOption", order.getDiningOption());
        newOrder.put("TableNo", order.getTableNo());
        newOrder.put("Dishes", order.getDishes());
        newOrder.put("Price", order.getPrice());

        // preparing selected order ID for correct type use in SQL statement
        String[] selectedOrder = {Integer.toString(order.getOrderId())};

        try {
            db.update(DB_TABLE, newOrder, "OrderID = ?", selectedOrder);
            //db.insertOrThrow(DB_TABLE, null, updatedDish);
            Log.i("OrderDB", "Updated row " + selectedOrder[0]);
        }
        catch (Exception e) {
            Log.e("OrderDB - Error", e.toString());
            e.printStackTrace();
        }

        close();
    }

    public void deleteRows(int[] orderIDs) {
        String whereClause = SqlIdUtility.buildWhereClause(orderIDs);
        String[] whereArgs = SqlIdUtility.buildWhereArgs(orderIDs);

        openWritable();

        int rowsDeleted = db.delete(DB_TABLE, whereClause, whereArgs);

        Log.i("OrderDB", "Deleted " + Integer.toString(rowsDeleted) + " rows");

        close();
    }

    public String retrieveOrder(Integer orderId) {

        openReadable();

        // preparing selected dish ID for correct type use in SQL statement
        String[] selectedDish = {orderId.toString()};

        // actual querying of the db
        Cursor cursor = db.query(DB_TABLE, null, "OrderID = ?", selectedDish, null, null, null);
        Log.i("DishDB", "Searched with ID: " + selectedDish[0]);

        cursor.moveToFirst();

        // append retrieved record detail to result string
        String retrievedOrder = cursor.getInt(0) + ". " + cursor.getString(1)
                        + ", " + cursor.getInt(2) + ", " + cursor.getString(3)
                        + ", " + cursor.getDouble(4) + "\n";

        // closing of cursor
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        close();

        return retrievedOrder;
    }

    public class SQLHelper extends SQLiteOpenHelper {
        public SQLHelper (Context c) {
            super(c, DB_NAME, null, DB_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
            Log.w("Dishes table","SQL script: CREATE_TABLE has been executed");
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("Dishes table", "Upgrading database i.e. dropping table and recreating it");
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
            onCreate(db);
        }
    }

    public static class SqlIdUtility {
        public static String buildWhereClause(int[] IDs) {

            StringBuilder whereClause = new StringBuilder("OrderID IN (");

            // a '?' for each ID, whereClasue = "IN (?, ?, ?)"
            for (int i=0; i<IDs.length; i++) {
                whereClause.append("?");

                if (i < IDs.length-1){      // if not on the last index
                    whereClause.append(", ");
                }
            }
            whereClause.append(")");
            return whereClause.toString();
        }

        public static String[] buildWhereArgs(int[] IDs) {

            String[] whereArgs = new String[IDs.length];

            // converting each ID to a string
            for (int i=0; i< IDs.length; i++) {
                whereArgs[i] = Integer.toString(IDs[i]);
            }
            return whereArgs;
        }
    }
}
