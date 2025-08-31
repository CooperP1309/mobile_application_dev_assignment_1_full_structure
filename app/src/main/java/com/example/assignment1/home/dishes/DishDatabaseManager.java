package com.example.assignment1.home.dishes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DishDatabaseManager {

    // defining of database metadata
    public static final String DB_NAME = "System";
    public static final String DB_TABLE = "Dishes";
    public static final int DB_VERSION = 1;

    // sql string query to create a table with 3 columns
    private static final String CREATE_TABLE = "CREATE TABLE " + DB_TABLE
            + " (DishID INTEGER PRIMARY KEY, DishName TEXT NOT NULL, DishType TEXT NOT NULL," +
            " Ingredients TEXT NOT NULL, Price REAL NOT NULL, Image BLOB);";

    // declaring of DB manager objects
    private SQLHelper helper;
    private SQLiteDatabase db;
    private Context context;

    // constructor injection of context
    public DishDatabaseManager(Context theContext) {
        context = theContext;
        helper = new SQLHelper(context);
    }

    public DishDatabaseManager openReadable() throws android.database.SQLException {
        helper = new SQLHelper(context);
        db = helper.getReadableDatabase();          // opening of database in readable mode
        return this;
    }

    public DishDatabaseManager openWritable() throws android.database.SQLException {
        helper = new SQLHelper(context);
        db = helper.getWritableDatabase();          // opening of database in writable mode
        return this;
    }

    public void close() {
        helper.close();                             // method to close database helper object
    }

    // BEGIN OF INSERT AND QUERY METHODS
    /*
    public void addRow(String dishName, String dishType, String ingredients
                       , Double price, byte[] image) {
        openWritable();

        ContentValues newDish = new ContentValues();
        newDish.put("DishName", dishName);
        newDish.put("DishType", dishType);
        newDish.put("Ingredients", ingredients);
        newDish.put("Price", price);
        newDish.put("Image", image);
        try {
            db.insertOrThrow(DB_TABLE, null, newDish);
            Log.i("DishDB", "Inserted new row");
        }
        catch (Exception e) {
            Log.e("Error in inserting rows ", e.toString());
            e.printStackTrace();
        }

        close();
    }
    */

    public void addRow(Dish dish) {
        openWritable();

        byte[] image = new byte[0];

        ContentValues newDish = new ContentValues();
        newDish.put("DishID", dish.getDishID());
        newDish.put("DishName", dish.getDishName());
        newDish.put("DishType", dish.getDishType());
        newDish.put("Ingredients", dish.getIngredients());
        newDish.put("Price", dish.getPrice());
        //newDish.put("Image", BitmapConvert.toByteArr(dish.getBitmap()));
        newDish.put("Image", image);
        try {
            db.insertOrThrow(DB_TABLE, null, newDish);
            Log.i("DishDB", "Inserted new row");
        }
        catch (Exception e) {
            Log.e("Error in inserting rows ", e.toString());
            e.printStackTrace();
        }

        close();
    }


    public String retrieveRows() {

        // string array for selecting fields of table
        String[] columns = new String[] {"DishID", "DishName", "DishType", "Ingredients", "Price","Image"};
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
                    + ", " + cursor.getString(2) + ", " + cursor.getString(3)
                    + ", " + cursor.getDouble(4) + ", " + cursor.getBlob(5) + "\n";

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

    public void updateRow(Dish dish) {

        openWritable();

        byte[] image = new byte[0];

        ContentValues updatedDish = new ContentValues();
        updatedDish.put("DishName", dish.getDishName());
        updatedDish.put("DishType", dish.getDishType());
        updatedDish.put("Ingredients", dish.getIngredients());
        updatedDish.put("Price", dish.getPrice());
        //newDish.put("Image", BitmapConvert.toByteArr(dish.getBitmap()));
        updatedDish.put("Image", image);

        // preparing selected dish ID for correct type use in SQL statement
        String[] selectedDish = {Integer.toString(dish.getDishID())};

        try {
            db.update(DB_TABLE, updatedDish, "DishID = ?", selectedDish);
            //db.insertOrThrow(DB_TABLE, null, updatedDish);
            Log.i("DishDB", "Updated row " + selectedDish[0]);
        }
        catch (Exception e) {
            Log.e("DishDB", e.toString());
            e.printStackTrace();
        }

        close();
    }


    public void deleteRows(int[] dishIDs) {
        String whereClause = SqlIdUtility.buildWhereClause(dishIDs);
        String[] whereArgs = SqlIdUtility.buildWhereArgs(dishIDs);

        openWritable();

        int rowsDeleted = db.delete(DB_TABLE, whereClause, whereArgs);

        Log.i("DishDB", "Deleted " + Integer.toString(rowsDeleted) + " rows");

        close();
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

            StringBuilder whereClause = new StringBuilder("DishID IN (");

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