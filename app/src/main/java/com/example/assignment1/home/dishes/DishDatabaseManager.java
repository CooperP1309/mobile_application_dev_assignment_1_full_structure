package com.example.assignment1.home.dishes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;

public class DishDatabaseManager {

    // defining of database metadata
    public static final String DB_NAME = "System";
    public static final String DB_TABLE = "Dishes";
    public static final int DB_VERSION = 1;

    // sql string query to create a table with 3 columns
    private static final String CREATE_TABLE = "CREATE TABLE " + DB_TABLE
            + " (DishID INTEGER PRIMARY KEY, DishName TEXT NOT NULL, DishType TEXT NOT NULL," +
            " Ingredients TEXT NOT NULL, Price REAL NOT NULL, ImageUri TEXT DEFAULT NULL);";

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

    public void addRow(Dish dish) {
        openWritable();

        ContentValues newDish = new ContentValues();
        newDish.put("DishID", dish.getDishID());
        newDish.put("DishName", dish.getDishName());
        newDish.put("DishType", dish.getDishType());
        newDish.put("Ingredients", dish.getIngredients());
        newDish.put("Price", dish.getPrice());
        if (dish.getImage().equals("")) {       // handling of no selected image
            newDish.putNull("ImageUri");
        }
        else {
            newDish.put("ImageUri", dish.getImage());
        }

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


    public String retrieveRowsWithoutImage() {

        // string array for selecting fields of table
        String[] columns = new String[] {"DishID", "DishName", "DishType", "Ingredients", "Price"};
        openWritable();

        // declaring a new cursor object for our database table
        Cursor cursor = db.query(DB_TABLE, columns, null, null, null, null, "DishType");
        String tablerows = "";

        // moving cursor to the first record on table
        cursor.moveToFirst();

        // while not past the last record in the table
        while (cursor.isAfterLast() == false) {

            // append retrieved record details to result string
            tablerows = tablerows + cursor.getInt(0) + ". " + cursor.getString(1)
                    + ", " + cursor.getString(2) + ", " + cursor.getString(3)
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

    /*public String retrieveRowsWithoutImage() {

        // string array for selecting fields of table
        String[] columns = new String[] {"DishID", "DishName", "DishType", "Ingredients", "Price","ImageUri"};
        openWritable();

        // declaring a new cursor object for our database table
        Cursor cursor = db.query(DB_TABLE, columns, null, null, null, null, "DishType");
        String tablerows = "";

        // moving cursor to the first record on table
        cursor.moveToFirst();

        // while not past the last record in the table
        while (cursor.isAfterLast() == false) {

            // append retrieved record details to result string
            tablerows = tablerows + cursor.getInt(0) + ". " + cursor.getString(1)
                    + ", " + cursor.getString(2) + ", " + cursor.getString(3)
                    + ", " + cursor.getDouble(4) + ", " + cursor.getString(5) + "\n";

            // iterate to next cursor
            cursor.moveToNext();
        }

        // closing of cursor
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        close();
        return tablerows;
    }*/

    public void updateRow(Dish dish) {

        openWritable();

        ContentValues updatedDish = new ContentValues();
        updatedDish.put("DishName", dish.getDishName());
        updatedDish.put("DishType", dish.getDishType());
        updatedDish.put("Ingredients", dish.getIngredients());
        updatedDish.put("Price", dish.getPrice());
        if (dish.getImage().equals("")) {               // handling of no image selected
            updatedDish.putNull("ImageUri");
        }
        else {
            updatedDish.put("ImageUri", dish.getImage());
        }

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

    public String retrieveDishname(Integer dishId) {

        openReadable();

        // preparing selected dish ID for correct type use in SQL statement
        String[] selectedDish = {dishId.toString()};

        // actual querying of the db
        Cursor cursor = db.query(DB_TABLE, null, "DishID = ?", selectedDish, null, null, null);
        Log.i("DishDB", "Searched with ID: " + selectedDish[0]);

        cursor.moveToFirst();

        // append retrieved record detail to result string
        String retrievedDishName =  cursor.getString(1);

        // closing of cursor
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        close();

        return retrievedDishName;
    }

    public String retrieveImageUri(Integer dishId) {

        openReadable();

        // preparing selected dish ID for correct type use in SQL statement
        String[] selectedDish = {dishId.toString()};

        // actual querying of the db
        Cursor cursor = db.query(DB_TABLE, null, "DishID = ?", selectedDish, null, null, null);
        Log.i("DishDB", "Searched with ID: " + selectedDish[0]);

        cursor.moveToFirst();

        // append retrieved record detail to result string
        String imageUri =  cursor.getString(5);

        // closing of cursor
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        close();

        return imageUri;
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