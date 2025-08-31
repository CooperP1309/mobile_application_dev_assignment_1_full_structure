package com.example.assignment1.login;

// import all packages needed for DB implementation
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*
    Authenticator
    Is a simple database manager class that stores some logins on
    a SQLite db and checks if a login is in the db.
    It is very simplified and should never be used on a real
    project as it is prone to SQL injection! I think...
 */

public class Authenticator {

    // defining of database metadata
    public static final String DB_NAME = "Restaurant";
    public static final String DB_TABLE = "LoginInfo";
    public static final int DB_VERSION = 1;

    // sql string query to create a table with 3 columns
    private static final String CREATE_TABLE = "CREATE TABLE " + DB_TABLE
            + " (employeeID INTEGER PRIMARY KEY AUTOINCREMENT, Username TEXT, Password TEXT," +
            " AccessLevel INTEGER);";

    // declaring of DB manager objects
    private SQLHelper helper;
    private SQLiteDatabase db;
    private Context context;

    // constructor injection of context
    public Authenticator(Context theContext) {
        context = theContext;
    }

    public Authenticator openReadable() throws android.database.SQLException {
        helper = new SQLHelper(context);
        db = helper.getReadableDatabase();          // opening of database in readable mode
        return this;
    }

    public Authenticator openWritable() throws android.database.SQLException {
        helper = new SQLHelper(context);
        db = helper.getWritableDatabase();          // opening of database in writable mode
        return this;
    }

    public void close() {
        helper.close();                             // method to close database helper object
    }

    public void addRow(String username, String password, Integer access) {
        openWritable();

        ContentValues newEmployee = new ContentValues();
        newEmployee.put("Username", username);
        newEmployee.put("Password", password);
        newEmployee.put("AccessLevel", access);
        try {
            db.insertOrThrow(DB_TABLE, null, newEmployee);
        }
        catch (Exception e) {
            Log.e("Error in inserting rows ", e.toString());
            e.printStackTrace();
        }
        close();
    }

    public int checkLogin(String username, String password) {
        openReadable();

        // defining array of strings for easy referencing of table columns in query()
        String[] columns = new String[] {"employeeID", "Username", "Password", "AccessLevel"};

        // declaring a new cursor object for our database table
        Cursor cursor = db.query(DB_TABLE, columns, "Username = " + "\"" + username + "\""
                + " AND Password = " + "\"" + password + "\"", null, null, null, null);

        // if no records found, cursor.moveToFirst() will return false
        if (cursor.moveToFirst()) {
            return cursor.getInt(3);    // return found record's access level
        }

        return 0;
    }

    public class SQLHelper extends SQLiteOpenHelper {
        public SQLHelper (Context c) {
            super(c, DB_NAME, null, DB_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("Login table", "Upgrading database i.e. dropping table and recreating it");
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
            onCreate(db);
        }
    }
}
