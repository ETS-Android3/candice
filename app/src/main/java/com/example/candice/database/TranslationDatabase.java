package com.example.candice.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

//This database is made for Translations
//https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase
public class TranslationDatabase {

    // Database object
    static DataBaseHelper helper;

    // Transaction Database constructor
    public TranslationDatabase(Context context) {
        helper = new DataBaseHelper(context);
    }

    // Insert the values set in MainFragment into the database
    public long insert(String sourceLanguage, String targetLanguage, String sourceText, String targetText) {

        //Make sure the incoming values are empty or not

        if (sourceText.isEmpty()) {
            return 0;
        }
        if (targetText.isEmpty()) {
            return 0;
        }
        if (sourceLanguage.isEmpty()) {
            return 0;
        }
        if (targetLanguage.isEmpty()) {
            return 0;
        }

        //If not the setup a new database (writeable of course)
        SQLiteDatabase db = helper.getWritableDatabase();

        //Along with some new ContentValues
        ContentValues values = new ContentValues();

        //Put the values into their respective columns
        values.put(DataBaseHelper.SOURCE_LANGUAGE, sourceLanguage);
        values.put(DataBaseHelper.TARGET_LANGUAGE, targetLanguage);
        values.put(DataBaseHelper.SOURCE_TEXT, sourceText);
        values.put(DataBaseHelper.TARGET_TEXT, targetText);
        return db.insert(DataBaseHelper.TABLE_NAME, null, values);
    }


    // deleteItem specific data from the database
    public boolean deleteItem(String ID) {
        SQLiteDatabase database = helper.getWritableDatabase();
        return database.delete(DataBaseHelper.TABLE_NAME, DataBaseHelper.UID + "=?", new String[]{ID}) > 0;
    }

    //This method gets all the data from the database
    public Cursor getData() {
        SQLiteDatabase db = helper.getWritableDatabase();
        String[] column = {DataBaseHelper.UID, DataBaseHelper.SOURCE_LANGUAGE, DataBaseHelper.TARGET_LANGUAGE, DataBaseHelper.SOURCE_TEXT, DataBaseHelper.TARGET_TEXT};
        return db.query(DataBaseHelper.TABLE_NAME, column, null, null, null, null, DataBaseHelper.UID);
    }

    public static boolean deleteAll() {
        SQLiteDatabase database = helper.getWritableDatabase();
        return database.delete(DataBaseHelper.TABLE_NAME, null, null) > 0;
    }


    //The method for creating the database itself
    public static class DataBaseHelper extends SQLiteOpenHelper {

        //The name of the database
        private static final String DATABASE_NAME = "HistoryDatabase";

        //Name of the table
        private static final String TABLE_NAME = "History_Table";

        //Version
        private static final int TABLE_VERSION = 1;

        //ID
        private static final String UID = "_id";

        //Source Languages (i.e. "English")
        private static final String SOURCE_LANGUAGE = "SourceLanguage";

        //Target Language (i.e. "French")
        private static final String TARGET_LANGUAGE = "TargetLanguage";

        //Source text (i.e. "Yes")
        private static final String SOURCE_TEXT = "SourceText";

        //Target text (i.e. "Oui")
        private static final String TARGET_TEXT = "TargetText";

        //Finally creating the table itself
        private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + UID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + SOURCE_LANGUAGE + " VARCHAR (255) ," + TARGET_LANGUAGE + " VARCHAR (255) ," +
                "" + SOURCE_TEXT + " VARCHAR (255)," + TARGET_TEXT + " VARCHAR (255) );";
        private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        //Providing the context to the Database
        Context context;

        //The following creates a database
        public DataBaseHelper(@Nullable Context context) {
            super(context, DATABASE_NAME, null, TABLE_VERSION);
            this.context = context;
            Log.e("Database", "Table Constructed");
        }


        //Creating the table
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
            Log.e("Database", "Table Created");
        }

        //Upgrading the database, such as a new column is added or data is changed
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DROP_TABLE);
            db.execSQL(CREATE_TABLE);
            Log.e("Database", "Table Upgraded");
        }
    }
}
