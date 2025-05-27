package com.school.finalProject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "signup.db";
    private static final int VERSION = 10;

    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT, target_weight REAL)");
        db.execSQL("CREATE TABLE weight(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, date TEXT, weight REAL, FOREIGN KEY (user_id) REFERENCES users(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists users");
        db.execSQL("DROP TABLE IF EXISTS weight");
        onCreate(db);
    }

    //Method to insert user
    public boolean insertUser(String username, String password) {
        SQLiteDatabase myDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("username", username);
        contentValues.put("password", password);
        long userId = myDB.insert("users", null, contentValues);
        return userId != -1;
    }

    //Method to insert user's weight
    public boolean insertWeight(long userId, String date, double weight) {
        SQLiteDatabase myDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("user_id", userId);
        contentValues.put("date", date);
        contentValues.put("weight", weight);
        long result = myDB.insert("weight", null, contentValues);
        return result != -1;
    }

    //Method to check if the username is already used in the database
    public boolean usernameStatus(String username) {
        SQLiteDatabase myDB = this.getWritableDatabase();
        Cursor cursor = myDB.rawQuery("SELECT * FROM users WHERE username = ?", new String[]{username});
        if (cursor.getCount() > 0){
            return true;
        } else {
            return false;
        }
    }
    
    //Method to retrieve & verify user from Database
    public boolean verifyUser(String username, String password) {
        SQLiteDatabase myDB = this.getWritableDatabase();
        Cursor cursor = myDB.rawQuery("SELECT * FROM users WHERE username = ?  AND password = ?", new String[]{username, password});
        if (cursor.getCount() > 0){
            return true;
        } else {
            return false;
        }
    }

    //Method to get userID from the database
    public long getUserID(String username, String password) {
        SQLiteDatabase myDB = this.getReadableDatabase();
        Cursor cursor = myDB.rawQuery("Select id FROM users WHERE username = ? and password = ?", new String[]{username, password});
        if (cursor.moveToFirst()) {
            return cursor.getLong(0);
        }
        cursor.close();
        return -1;
    }

    //Method to retrieve the user's weight
    public Cursor getWeightByUserID(long userId) {
        SQLiteDatabase myDB = this.getReadableDatabase();
        return myDB.rawQuery("SELECT * FROM weight WHERE user_id = ? ORDER BY date ASC", new String[]{String.valueOf(userId)});
    }

    //Method to insert user specific target weight
    public boolean insertTargetWeight(long userId, double targetWeight) {
        SQLiteDatabase myDB = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("target_weight", targetWeight);
        int rowsAffected = myDB.update("users", values, "id = ?", new String[] {String.valueOf(userId)});
        return rowsAffected > 0;
    }

    //Method to retrieve user's target weight
    public double getTargetWeight(long userId) {
        SQLiteDatabase myBD = this.getReadableDatabase();
        Cursor cursor = myBD.rawQuery("SELECT target_weight FROM users WHERE id = ?", new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            return cursor.getLong(0);
        }
        cursor.close();
        return -1;
    }

    //Method to check if a weight entry has been added for the day
    public boolean hasTodayWeightEntry(long userId) {
        SQLiteDatabase myDB = this.getReadableDatabase();
        String currentDate = formatDate(System.currentTimeMillis());

        Cursor cursor = myDB.rawQuery("SELECT * FROM weight WHERE user_id = ? AND date = ?", new String[]{String.valueOf(userId), currentDate});

        boolean hasEntry = cursor.getCount() > 0; //if count is greater than zero, than an entry for today already exists
        cursor.close();
        return hasEntry;
    }

    //method to format date to month, day, year
    private String formatDate(long date) {
        SimpleDateFormat mdy = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        return mdy.format(new Date(date));
    }
}
