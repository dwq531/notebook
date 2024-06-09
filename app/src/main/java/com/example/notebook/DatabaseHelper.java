package com.example.notebook;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "users.db";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_USER = "users";
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_SIGNATURE = "signature";
    private static final String COLUMN_IMAGE_URL = "image_url";

    private static final String TABLE_NOTE = "notes";
    private static final String COLUMN_NOTE_ID = "id";
    private static final String COLUMN_NOTE_USER_ID = "user_id";
    private static final String COLUMN_NOTE_CONTENT = "content";

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_CURRENT_USER_ID = "currentUserId";

    private SharedPreferences sharedPreferences;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_SIGNATURE + " TEXT,"
                + COLUMN_IMAGE_URL + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTE + "("
                + COLUMN_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NOTE_USER_ID + " INTEGER,"
                + COLUMN_NOTE_CONTENT + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_NOTE_USER_ID + ") REFERENCES " + TABLE_USER + "(" + COLUMN_USER_ID + "))";
        db.execSQL(CREATE_NOTES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_USER + " ADD COLUMN " + COLUMN_IMAGE_URL + " TEXT");
        }
    }

    public void addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        // values.put(COLUMN_IMAGE_URL, "");
        values.put(COLUMN_SIGNATURE, "快来填写个性签名吧");
        db.insert(TABLE_USER, null, values);
        db.close();
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};
        Cursor cursor = db.query(TABLE_USER, columns, selection, selectionArgs, null, null, null);

        boolean isValidUser = false;
        if (cursor.moveToFirst()) {
            int currentUserId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(KEY_CURRENT_USER_ID, currentUserId);
            editor.apply();
            isValidUser = true;
        }

        cursor.close();
        db.close();
        Log.d("checkUser", "currentUserId: " + getCurrentUserId());
        return isValidUser;
    }

    private int getCurrentUserId() {
        return sharedPreferences.getInt(KEY_CURRENT_USER_ID, -1);
    }

    public String getCurrentUsername() {
        int currentUserId = getCurrentUserId();
        if (currentUserId == -1) {
            Log.d("getCurrentUsername", "currentUserId is -1");
            return null;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_USERNAME + " FROM " + TABLE_USER + " WHERE " + COLUMN_USER_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(currentUserId)});
        String username = null;

        if (cursor.moveToFirst()) {
            username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
        }

        cursor.close();
        db.close();
        Log.d("getCurrentUsername", "username: " + username);
        return username;
    }

    public String getCurrentUserSignature() {
        int currentUserId = getCurrentUserId();
        if (currentUserId == -1) {
            Log.d("getCurrentUserSignature", "currentUserId is -1");
            return null;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_SIGNATURE + " FROM " + TABLE_USER + " WHERE " + COLUMN_USER_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(currentUserId)});
        String signature = null;

        if (cursor.moveToFirst()) {
            signature = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SIGNATURE));
        }

        cursor.close();
        db.close();
        Log.d("getCurrentUserSignature", "signature: " + signature);
        return signature;
    }

    public String getCurrentUserPassword() {
        int currentUserId = getCurrentUserId();
        if (currentUserId == -1) {
            Log.d("getCurrentUserPassword", "currentUserId is -1");
            return null;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_PASSWORD + " FROM " + TABLE_USER + " WHERE " + COLUMN_USER_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(currentUserId)});
        String password = null;

        if (cursor.moveToFirst()) {
            password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
        }

        cursor.close();
        db.close();
        Log.d("getCurrentUserPassword", "password: " + password);
        return password;
    }

    public String getCurrentUserImageUrl() {
        int currentUserId = getCurrentUserId();
        if (currentUserId == -1) {
            Log.d("getCurrentUserImageUrl", "currentUserId is -1");
            return null;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_IMAGE_URL + " FROM " + TABLE_USER + " WHERE " + COLUMN_USER_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(currentUserId)});
        String imageUrl = null;

        if (cursor.moveToFirst()) {
            imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL));
        }

        cursor.close();
        db.close();
        Log.d("getCurrentUserImageUrl", "imageUrl: " + imageUrl);
        return imageUrl;
    }

    public void updateUserInfo(String username, String password, String signature) {
        int currentUserId = getCurrentUserId();
        if (currentUserId == -1) return;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (username != null && !username.isEmpty()) {
            values.put(COLUMN_USERNAME, username);
        }
        if (password != null && !password.isEmpty()) {
            values.put(COLUMN_PASSWORD, password);
        }
        if (signature != null) {
            values.put(COLUMN_SIGNATURE, signature);
        }

        db.update(TABLE_USER, values, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(currentUserId)});
        db.close();
    }

    public void updateUserImage(String imageUrl) {
        int currentUserId = getCurrentUserId();
        if (currentUserId == -1) return;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (imageUrl != null) {
            values.put(COLUMN_IMAGE_URL, imageUrl);
        }
        db.update(TABLE_USER, values, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(currentUserId)});
        db.close();
    }

    public int getNoteCount() {
        return 0; // 实现获取笔记数量的逻辑
    }

    public void logoutCurrentUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_CURRENT_USER_ID);
        editor.apply();
    }

    public void printAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USER;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
                String username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
                String password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
                String signature = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SIGNATURE));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL));

                Log.d("DatabaseHelper", "ID: " + id + ", Username: " + username + ", Password: " + password
                        + ", Signature: " + signature + ", Image URL: " + imageUrl);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }
}
