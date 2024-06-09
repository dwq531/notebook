package com.example.notebook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "users.db";
    private static final int DATABASE_VERSION = 3; // 更新版本号以反映结构的变化

    private static final String TABLE_USER = "users";
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_SIGNATURE = "signature";
    private static final String COLUMN_IMAGE_URL = "image_url"; // 新增头像 URL 字段

    private static final String TABLE_NOTE = "notes";
    private static final String COLUMN_NOTE_ID = "id";
    private static final String COLUMN_NOTE_USER_ID = "user_id";
    private static final String COLUMN_NOTE_CONTENT = "content";

    private int currentUserId = -1; // 用于存储当前用户的ID

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建用户表
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_SIGNATURE + " TEXT,"
                + COLUMN_IMAGE_URL + " TEXT" + ")"; // 新增头像 URL 字段
        db.execSQL(CREATE_USERS_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 当数据库版本增加时进行的操作
        if (oldVersion < 3) {
            // 从旧版本升级到新版本时添加新的 image_url 列
            db.execSQL("ALTER TABLE " + TABLE_USER + " ADD COLUMN " + COLUMN_IMAGE_URL + " TEXT");
        }
    }

    // 添加新用户
    public void addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        // 用户创建时头像 URL 默认为空
        values.put(COLUMN_IMAGE_URL, "drawable/default_profileimage.xml");
        // 用户创建时个性签名默认为空
        values.put(COLUMN_SIGNATURE, "");
        Log.d("adduser",username);
        db.insert(TABLE_USER, null, values);
        db.close();
    }

    // 检查用户是否存在
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};
        Cursor cursor = db.query(TABLE_USER, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        boolean isValidUser = count > 0;

        if (isValidUser && cursor.moveToFirst()) {
            currentUserId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
        }

        cursor.close();
        db.close();
        return isValidUser;
    }

    // 获取当前用户的用户名
    public String getCurrentUsername() {
        if (currentUserId == -1) return null;

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_USERNAME + " FROM " + TABLE_USER + " WHERE " + COLUMN_USER_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(currentUserId)});
        String username = null;

        if (cursor.moveToFirst()) {
            username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
        }
        Log.d("databaseHelper",username);
        cursor.close();
        db.close();
        return username;
    }

    // 获取当前用户的个性签名
    public String getCurrentUserSignature() {
        if (currentUserId == -1) return null;

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_SIGNATURE + " FROM " + TABLE_USER + " WHERE " + COLUMN_USER_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(currentUserId)});
        String signature = null;

        if (cursor.moveToFirst()) {
            signature = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SIGNATURE));
        }

        cursor.close();
        db.close();
        return signature;
    }

    // 获取当前用户的密码
    public String getCurrentUserPassword() {
        if (currentUserId == -1) return null;

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_PASSWORD + " FROM " + TABLE_USER + " WHERE " + COLUMN_USER_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(currentUserId)});
        String username = null;

        if (cursor.moveToFirst()) {
            username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
        }

        cursor.close();
        db.close();
        return username;
    }

    // 获取当前用户的头像 URL
    public String getCurrentUserImageUrl() {
        if (currentUserId == -1) return null;

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_IMAGE_URL + " FROM " + TABLE_USER + " WHERE " + COLUMN_USER_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(currentUserId)});
        String imageUrl = null;

        if (cursor.moveToFirst()) {
            imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL));
        }

        cursor.close();
        db.close();
        return imageUrl;
    }

    // 更新当前用户的信息
    public void updateUserInfo(String username, String password, String signature) {
        if (currentUserId == -1) return; // 未登录时不更新

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


    public void updateUserImage(String imageUrl)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if(imageUrl != null){
            values.put(COLUMN_IMAGE_URL, imageUrl);
        }
        db.update(TABLE_USER, values, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(currentUserId)});
        db.close();
    }
    // 获取当前用户的笔记数量
    public int getNoteCount() {
        // todo
        return 0;
    }

    // 注销当前用户
    public void logoutCurrentUser() {
        currentUserId = -1;
    }

    // 打印所有用户信息
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
