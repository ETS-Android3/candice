package com.example.candice.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

//The following is the Flashcard Database which is created when making Flashcards
//Much of which is made through the documentation
//https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase
public class FlashcardDatabase {

    //Declaring the database
    DataBaseHelper dataBaseHelperFlashcard;

    //Initializing the above declarations
    //This is a new Database Helper
    public FlashcardDatabase(Context context) {
        dataBaseHelperFlashcard = new DataBaseHelper(context);
    }


    //Method below for inserting data into the Database
    public long insert(String Question, String Answer) {
        //Firstly check if the answers are empty or not so we don't insert empty values
        if (Question.isEmpty()) {
            return 0;
        }
        if (Answer.isEmpty()) {
            return 0;
        }

        //Grab the SQLiteDatabase as writeable
        SQLiteDatabase db = dataBaseHelperFlashcard.getWritableDatabase();

        //Putting the values into the database
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.QUESTION, Question);
        values.put(DataBaseHelper.ANSWER, Answer);

        //Once done we return as 1 and the data is inserted
        return db.insert(DataBaseHelper.TABLE_NAME, null, values);
    }


    //The following method returns the data from the Database
    public Cursor getData() {
        SQLiteDatabase db = dataBaseHelperFlashcard.getWritableDatabase();
        String[] column = {DataBaseHelper.UID, DataBaseHelper.QUESTION, DataBaseHelper.ANSWER};
        return db.query(DataBaseHelper.TABLE_NAME, column, null, null, null, null, DataBaseHelper.UID);
    }

    //Then of course, similar to the HistoryDatabase, if you delete the ID you're deleting the entire flashcard
    public boolean Delete(String ID) {
        SQLiteDatabase database = dataBaseHelperFlashcard.getWritableDatabase();
        return database.delete(DataBaseHelper.TABLE_NAME, DataBaseHelper.UID + "=?", new String[]{ID}) > 0;
    }


    // SQLiteDatabase helper class
    static class DataBaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "FlashcardDatabase";

        private static final String TABLE_NAME = "Flash_Table";

        private static final int TABLE_VERSION = 1;

        private static final String UID = "_id";

        private static final String QUESTION = "Question";

        private static final String ANSWER = "Answer";

        //Query for creating the table
        private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + UID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + QUESTION + " VARCHAR (255) ," + ANSWER + " VARCHAR (255) );";

        //And dropping the table if it exists
        private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

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
