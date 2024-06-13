package com.example.notebook;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "users.db";
    private static final int DATABASE_VERSION = 4;

    private static final String TABLE_USER = "users",NOTE_TABLE_NAME="notes",CONTENT_TABLE_NAME="content",FOLDER_TABLE_NAME = "folders", FOLDER_NOTE_TABLE_NAME = "folder_notes";
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_SIGNATURE = "signature";
    private static final String COLUMN_IMAGE_URL = "image_url";

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_CURRENT_USER_ID = "currentUserId";

    private SharedPreferences sharedPreferences;
    // 笔记列表：用户，笔记id，笔记题目，创建时间
    private static final String COLUMN_NOTE_ID="note_id",COLUMN_TITLE="title",COLUMN_CREATE_TIME="create_time",COLUMN_VERSION="version";
    // 笔记内容：所属笔记id，内容id，内容，类型，位置
    private static final String COLUMN_CONTENT_ID="content_id",COLUMN_CONTENT="content",COLUMN_TYPE="type",COLUMN_POSITION="position";
    // 文件夹：文件夹id，用户id，文件夹名称
    private static final String COLUMN_FOLDER_ID = "folder_id", COLUMN_FOLDER_NAME = "folder_name";
    // 文件夹和笔记关系表
    private static final String COLUMN_FOLDER_NOTE_ID = "folder_note_id";
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    private APIEndPoint api;
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY ,"
                + COLUMN_USERNAME + " TEXT,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_SIGNATURE + " TEXT,"
                + COLUMN_IMAGE_URL + " TEXT,"
                + COLUMN_VERSION + " INTEGER" + ")";
        db.execSQL(CREATE_USERS_TABLE);
        // 笔记列表：用户id，笔记id，笔记题目，创建时间
        String CREATE_NOTE_TABLE = String.format("CREATE TABLE %s (%s INTEGER, %s INTEGER PRIMARY KEY, %s TEXT,%s TEXT,%s INTEGER)",
                NOTE_TABLE_NAME,COLUMN_USER_ID,COLUMN_NOTE_ID,COLUMN_TITLE,COLUMN_CREATE_TIME,COLUMN_VERSION);
        db.execSQL(CREATE_NOTE_TABLE);
        // 笔记内容：所属笔记id，内容id，内容，类型,位置
        String CREATE_CONTENT_TABLE = String.format("CREATE TABLE %s (%s INTEGER,%s INTEGER PRIMARY KEY, %s TEXT,%s INTEGER,%s INTEGER,%s INTEGER)",
                CONTENT_TABLE_NAME,COLUMN_NOTE_ID,COLUMN_CONTENT_ID,COLUMN_CONTENT,COLUMN_TYPE,COLUMN_POSITION,COLUMN_VERSION);
        db.execSQL(CREATE_CONTENT_TABLE);
        String CREATE_FOLDER_TABLE = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s INTEGER, %s TEXT)",
                FOLDER_TABLE_NAME, COLUMN_FOLDER_ID, COLUMN_USER_ID, COLUMN_FOLDER_NAME);
        db.execSQL(CREATE_FOLDER_TABLE);

        String CREATE_FOLDER_NOTE_TABLE = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s INTEGER, %s INTEGER)",
                FOLDER_NOTE_TABLE_NAME, COLUMN_FOLDER_NOTE_ID, COLUMN_FOLDER_ID, COLUMN_NOTE_ID);
        db.execSQL(CREATE_FOLDER_NOTE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            String CREATE_FOLDER_TABLE = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s INTEGER, %s TEXT)",
                    FOLDER_TABLE_NAME, COLUMN_FOLDER_ID, COLUMN_USER_ID, COLUMN_FOLDER_NAME);
            db.execSQL(CREATE_FOLDER_TABLE);

            String CREATE_FOLDER_NOTE_TABLE = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s INTEGER, %s INTEGER)",
                    FOLDER_NOTE_TABLE_NAME, COLUMN_FOLDER_NOTE_ID, COLUMN_FOLDER_ID, COLUMN_NOTE_ID);
            db.execSQL(CREATE_FOLDER_NOTE_TABLE);
        }
    }

    public int addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        // values.put(COLUMN_IMAGE_URL, "");
        values.put(COLUMN_SIGNATURE, "快来填写个性签名吧");
        values.put(COLUMN_VERSION,System.currentTimeMillis());
        long row = db.insert(TABLE_USER, null, values);
        db.close();
        return (int)row;
    }
    public void addUser(int user_id ,String username, String password,String signature,String image_uri,long version) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID,user_id);
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_IMAGE_URL, image_uri);
        values.put(COLUMN_SIGNATURE,signature);
        values.put(COLUMN_SIGNATURE, "快来填写个性签名吧");
        values.put(COLUMN_VERSION,version);
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

    public int getCurrentUserId() {
        return sharedPreferences.getInt(KEY_CURRENT_USER_ID, -1);
    }
    public User getUser(int user_id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER,null,COLUMN_USER_ID+"= ?",new String[]{String.valueOf(user_id)},null,null,null);
        User user = null;
        if(cursor.moveToFirst()){
            user = new User();
            user.user_id = user_id;
            user.username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
            user.password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
            user.signatrue = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SIGNATURE));
            user.image_url = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL));
            user.version = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VERSION)));
        }
        cursor.close();
        db.close();
        return user;
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
        values.put(COLUMN_VERSION,System.currentTimeMillis());

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
        values.put(COLUMN_VERSION,System.currentTimeMillis());
        db.update(TABLE_USER, values, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(currentUserId)});
        db.close();
    }
    public void updateUserImage(int user_id,String imageUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (imageUrl != null) {
            values.put(COLUMN_IMAGE_URL, imageUrl);
        }
        values.put(COLUMN_VERSION,System.currentTimeMillis());
        db.update(TABLE_USER, values, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(user_id)});
        db.close();
    }

    /*public int getNoteCount() {
        int currentUserId = getCurrentUserId();
        if (currentUserId == -1) {
            return 0; // 如果没有当前用户，返回 0
        }
        Log.d("getNoteCount","currentUserId:"+currentUserId);
        List<Note> userNotes = getNoteList(currentUserId);
        Log.d("getNoteCount", "Number of user notes: " + userNotes.size());
        return userNotes.size();
    }*/


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

    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};
        Cursor cursor = db.query(TABLE_USER, columns, selection, selectionArgs, null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exists;
    }
    public long addNote(int user,String title,String create_time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID,user);
        values.put(COLUMN_TITLE,title);
        values.put(COLUMN_CREATE_TIME,create_time);
        values.put(COLUMN_VERSION,System.currentTimeMillis());
        long row = db.insert(NOTE_TABLE_NAME,null,values);
        db.close();
        return row;
    }
    public long addNote(int user,String title,String create_time,long note_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID,user);
        values.put(COLUMN_TITLE,title);
        values.put(COLUMN_CREATE_TIME,create_time);
        values.put(COLUMN_VERSION,System.currentTimeMillis());
        values.put(COLUMN_NOTE_ID,note_id);
        long row = db.insert(NOTE_TABLE_NAME,null,values);
        db.close();
        return row;
    }
    public long addContent(long note,String content,int type,int position){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE_ID,note);
        values.put(COLUMN_CONTENT,content);
        values.put(COLUMN_TYPE,type);
        values.put(COLUMN_POSITION,position);
        values.put(COLUMN_VERSION,System.currentTimeMillis());
        long row = db.insert(CONTENT_TABLE_NAME,null,values);
        db.close();
        // 云端数据库
        return row;
    }
    public long addContent(long note,String content,int type,int position,long content_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE_ID,note);
        values.put(COLUMN_CONTENT,content);
        values.put(COLUMN_TYPE,type);
        values.put(COLUMN_POSITION,position);
        values.put(COLUMN_VERSION,System.currentTimeMillis());
        values.put(COLUMN_CONTENT_ID,content_id);
        long row = db.insert(CONTENT_TABLE_NAME,null,values);
        db.close();
        // 云端数据库
        return row;
    }

    public List<Note> getNoteList(int user){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(NOTE_TABLE_NAME,null,COLUMN_USER_ID+"= ?",new String[]{String.valueOf(user)},null,null,null);
        List<Note> notes = new ArrayList<>();
        while(cursor.moveToNext()){
            Note note = new Note();
            note.note_id = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID)));
            note.title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
            note.create_time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATE_TIME));
            note.version = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VERSION)));;
            note.user_id = user;
            notes.add(note);
        }
        cursor.close();
        db.close();
        Log.d("getNoteList", String.valueOf(notes.size()));
        return notes;
    }
    public Note getNote(long note_id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(NOTE_TABLE_NAME,null,COLUMN_NOTE_ID+"= ?",new String[]{String.valueOf(note_id)},null,null,null);
        Note note = null;
        if(cursor.moveToFirst()){
            note = new Note();
            note.note_id = note_id;
            note.title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
            note.create_time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATE_TIME));
            note.user_id = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));;
            note.version = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VERSION)));;
        }
        cursor.close();
        db.close();
        return note;
    }

    public List<Content> getContentList(long note_id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(CONTENT_TABLE_NAME,null,COLUMN_NOTE_ID+"= ?",new String[]{String.valueOf(note_id)},null,null,COLUMN_POSITION);
        List<Content> contents = new ArrayList<>();
        while(cursor.moveToNext()){
            Content content = new Content();
            content.content_id = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT_ID)));
            content.content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));
            content.type = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
            content.position = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POSITION)));
            content.version =  Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VERSION)));;
            content.note_id = note_id;
            contents.add(content);
        }
        cursor.close();
        db.close();
        return contents;
    }
    public Content getContent(long content_id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(CONTENT_TABLE_NAME,null,COLUMN_CONTENT_ID+"= ?",new String[]{String.valueOf(content_id)},null,null,null);
        Content content = null;
        if(cursor.moveToFirst()){
            content = new Content();
            content.content_id = content_id;
            content.content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));
            content.type = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
            content.position = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
            content.note_id = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID)));
            content.version = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VERSION)));;
        }
        return content;
    }

    public void updateContent(long content_id,String content){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        Log.d("database", String.valueOf(content_id));
        Log.d("database",content);
        values.put(COLUMN_CONTENT,content);
        values.put(COLUMN_VERSION,System.currentTimeMillis());
        db.update(
                CONTENT_TABLE_NAME,
                values,
                COLUMN_CONTENT_ID + "= ?",
                new String[] {String.valueOf(content_id)});
        db.close();
    }
    public void updateContentPosition(long content_id,int position,int before_pos,boolean change_other){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        long version = System.currentTimeMillis();
        if(change_other) {
            // before>after, after<=pos<before 的pos+1
            if (before_pos > position) {
                String sql = "UPDATE " + CONTENT_TABLE_NAME + " SET " + COLUMN_POSITION + " = " + COLUMN_POSITION + " + 1 WHERE " + COLUMN_POSITION + " >= ? AND " + COLUMN_POSITION + " < ?";
                db.execSQL(sql, new String[]{String.valueOf(position), String.valueOf(before_pos)});
                sql = "UPDATE " + CONTENT_TABLE_NAME + " SET " + COLUMN_VERSION + " = " + String.valueOf(version) + " WHERE " + COLUMN_POSITION + " >= ? AND " + COLUMN_POSITION + " < ?";
                db.execSQL(sql, new String[]{String.valueOf(position), String.valueOf(before_pos)});
            }
            // before<after, before<pos<=after 的pos-1
            else {
                String sql = "UPDATE " + CONTENT_TABLE_NAME + " SET " + COLUMN_POSITION + " = " + COLUMN_POSITION + " - 1 WHERE " + COLUMN_POSITION + " > ? AND " + COLUMN_POSITION + " <= ?";
                db.execSQL(sql, new String[]{String.valueOf(before_pos), String.valueOf(position)});
                sql = "UPDATE " + CONTENT_TABLE_NAME + " SET " + COLUMN_VERSION + " = " + String.valueOf(version) + " WHERE " + COLUMN_POSITION + " > ? AND " + COLUMN_POSITION + " <= ?";
                db.execSQL(sql, new String[]{String.valueOf(before_pos), String.valueOf(position)});
            }
        }
        String sql = "UPDATE " + CONTENT_TABLE_NAME + " SET " + COLUMN_POSITION + " = " + String.valueOf(position) +" WHERE " + COLUMN_CONTENT_ID + " = ?";
        db.execSQL(sql, new String[] {String.valueOf(content_id)});
        sql = "UPDATE " + CONTENT_TABLE_NAME + " SET " +COLUMN_VERSION + " = "+ String.valueOf(version)+" WHERE " + COLUMN_CONTENT_ID + " = ?";
        db.execSQL(sql, new String[] {String.valueOf(content_id)});
        db.close();
    }
    public void updateTitle(long note_id,String title){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE,title);
        values.put(COLUMN_VERSION,System.currentTimeMillis());
        db.update(
                NOTE_TABLE_NAME,
                values,
                COLUMN_NOTE_ID + "= ?",
                new String[] {String.valueOf(note_id)});
        db.close();
    }
    public void deleteContent(long content_id){
        SQLiteDatabase db = getWritableDatabase();
        Content content = getContent(content_id);
        // 更新后面content的位置序号
        String sql = "UPDATE " + CONTENT_TABLE_NAME + " SET " + COLUMN_POSITION + " = " + COLUMN_POSITION + " - 1 WHERE " + COLUMN_POSITION + " > ? ";
        db.execSQL(sql, new String[]{String.valueOf(content.position)});
        sql = "UPDATE " + CONTENT_TABLE_NAME + " SET " + COLUMN_VERSION + " = " + String.valueOf(System.currentTimeMillis()) + " WHERE " + COLUMN_POSITION + " > ? ";
        db.delete(CONTENT_TABLE_NAME,COLUMN_CONTENT_ID +"= ?",new String[] {String.valueOf(content_id)} );
        db.execSQL(sql, new String[]{String.valueOf(content.position)});
        db.close();
    }
    public void deleteNote(long note_id){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(NOTE_TABLE_NAME,COLUMN_NOTE_ID +"= ?",new String[] {String.valueOf(note_id)} );
        db.delete(CONTENT_TABLE_NAME,COLUMN_NOTE_ID +"= ?",new String[] {String.valueOf(note_id)} );
        db.close();
    }
    public void updateNoteVersion(long note_id){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_VERSION,System.currentTimeMillis());
        db.update(
                NOTE_TABLE_NAME,
                values,
                COLUMN_NOTE_ID + "= ?",
                new String[] {String.valueOf(note_id)});
        db.close();
    }


    public void printAllNotes() {
        SQLiteDatabase db = this.getReadableDatabase();

        // SQL 查询语句，获取所有笔记的标题和内容
        String query = "SELECT " + NOTE_TABLE_NAME + "." + COLUMN_NOTE_ID + ", "
                + NOTE_TABLE_NAME + "." + COLUMN_TITLE + ", "
                + CONTENT_TABLE_NAME + "." + COLUMN_CONTENT
                + " FROM " + NOTE_TABLE_NAME
                + " LEFT JOIN " + CONTENT_TABLE_NAME
                + " ON " + NOTE_TABLE_NAME + "." + COLUMN_NOTE_ID + " = " + CONTENT_TABLE_NAME + "." + COLUMN_NOTE_ID;

        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            long noteId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
            String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));

            Log.d("printAllNotes", "Note ID: " + noteId + ", Title: " + title + ", Content: " + content);
        }

        cursor.close();
        db.close();
    }


    /*public List<Note> searchNotes(String keyword) {
        // 调用 printAllNotes 方法输出当前所有笔记的标题和内容
        printAllNotes();

        List<Note> notes = new ArrayList<>();
        int currentUserId = getCurrentUserId();
        if (currentUserId == -1) {
            Log.d("searchNotes", "currentUserId=-1");
            return notes; // 如果没有当前用户，返回空列表
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String keywordPattern = "%" + keyword + "%";
        Log.d("searchNotes", "keywordPattern: " + keywordPattern);

        // 查询标题匹配的笔记
        String titleQuery = "SELECT * FROM " + NOTE_TABLE_NAME + " WHERE " + COLUMN_USER_ID + "=? AND " + COLUMN_TITLE + " LIKE ?";
        Cursor titleCursor = db.rawQuery(titleQuery, new String[]{String.valueOf(currentUserId), keywordPattern});

        // 添加调试信息，输出标题匹配的内容
        Log.d("searchNotes", "Title Matching results count: " + titleCursor.getCount());

        while (titleCursor.moveToNext()) {
            Note note = new Note();
            note.note_id = titleCursor.getLong(titleCursor.getColumnIndexOrThrow(COLUMN_NOTE_ID));
            note.title = titleCursor.getString(titleCursor.getColumnIndexOrThrow(COLUMN_TITLE));
            note.create_time = titleCursor.getString(titleCursor.getColumnIndexOrThrow(COLUMN_CREATE_TIME));
            note.user_id = titleCursor.getInt(titleCursor.getColumnIndexOrThrow(COLUMN_USER_ID));
            note.version = titleCursor.getLong(titleCursor.getColumnIndexOrThrow(COLUMN_VERSION));

            Log.d("searchNotes", "Matched Title: " + note.title);
            Log.d("searchNotes", note.toString());
            notes.add(note);
        }
        titleCursor.close();

        // 查询内容匹配的笔记
        String contentQuery = "SELECT DISTINCT " + NOTE_TABLE_NAME + ".* FROM " + NOTE_TABLE_NAME + " INNER JOIN " + CONTENT_TABLE_NAME
                + " ON " + NOTE_TABLE_NAME + "." + COLUMN_NOTE_ID + " = " + CONTENT_TABLE_NAME + "." + COLUMN_NOTE_ID
                + " WHERE " + NOTE_TABLE_NAME + "." + COLUMN_USER_ID + "=? AND " + CONTENT_TABLE_NAME + "." + COLUMN_CONTENT + " LIKE ?";
        Cursor contentCursor = db.rawQuery(contentQuery, new String[]{String.valueOf(currentUserId), keywordPattern});

        // 添加调试信息，输出内容匹配的内容
        Log.d("searchNotes", "Content Matching results count: " + contentCursor.getCount());

        while (contentCursor.moveToNext()) {
            Note note = new Note();
            note.note_id = contentCursor.getLong(contentCursor.getColumnIndexOrThrow(COLUMN_NOTE_ID));
            note.title = contentCursor.getString(contentCursor.getColumnIndexOrThrow(COLUMN_TITLE));
            note.create_time = contentCursor.getString(contentCursor.getColumnIndexOrThrow(COLUMN_CREATE_TIME));
            note.user_id = contentCursor.getInt(contentCursor.getColumnIndexOrThrow(COLUMN_USER_ID));
            note.version = contentCursor.getLong(contentCursor.getColumnIndexOrThrow(COLUMN_VERSION));

            Log.d("searchNotes", "Matched Content for Note ID: " + note.note_id);

            // 检查内容是否匹配并输出匹配的内容
            Cursor matchedContentCursor = db.rawQuery("SELECT " + COLUMN_CONTENT + " FROM " + CONTENT_TABLE_NAME + " WHERE " + COLUMN_NOTE_ID + " = ? AND " + COLUMN_CONTENT + " LIKE ?", new String[]{String.valueOf(note.note_id), keywordPattern});
            while (matchedContentCursor.moveToNext()) {
                String matchedContent = matchedContentCursor.getString(matchedContentCursor.getColumnIndexOrThrow(COLUMN_CONTENT));
                Log.d("searchNotes", "Matched Content: " + matchedContent);
            }
            matchedContentCursor.close();

            Log.d("searchNotes", note.toString());
            notes.add(note);
        }
        contentCursor.close();

        db.close();
        return notes;
    }*/

    public List<Note> searchNotes(String keyword) {
        List<Note> notes = new ArrayList<>();
        int currentUserId = getCurrentUserId();
        if (currentUserId == -1) {
            Log.d("searchNotes", "No current user found, returning empty list");
            return notes; // 如果没有当前用户，返回空列表
        }

        Log.d("searchNotes", "Current user ID: " + currentUserId);

        List<Note> userNotes = getNoteList(currentUserId);
        Log.d("searchNotes", "Number of user notes: " + userNotes.size());

        for (Note note : userNotes) {
            Log.d("searchNotes", "Processing note: " + note.title);
            List<Content> contents = getContentList(note.note_id);
            Log.d("searchNotes", "Number of contents in note: " + contents.size());
            for (Content content : contents) {
                Log.d("searchNotes", "Processing content in note: " + content.content);
                if (content.content.contains(keyword) || note.title.contains(keyword)) {
                    notes.add(note);
                    Log.d("searchNotes", "Match found for keyword: " + keyword);
                    break;
                }
            }
        }

        return notes;
    }

    public long addFolder(String folderName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, getCurrentUserId());
        values.put(COLUMN_FOLDER_NAME, folderName);
        long row = db.insert(FOLDER_TABLE_NAME, null, values);
        db.close();
        return row;
    }

    public List<String> getAllFolders() {
        List<String> folders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_FOLDER_NAME};
        String selection = COLUMN_USER_ID + "=?";
        String[] selectionArgs = {String.valueOf(getCurrentUserId())};
        Cursor cursor = db.query(FOLDER_TABLE_NAME, columns, selection, selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            String folderName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FOLDER_NAME));
            folders.add(folderName);
        }

        cursor.close();
        db.close();
        return folders;
    }

    public long addNoteToFolder(long noteId, long folderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FOLDER_ID, folderId);
        values.put(COLUMN_NOTE_ID, noteId);
        long row = db.insert(FOLDER_NOTE_TABLE_NAME, null, values);
        Log.d("addNoteToFolder","folder="+folderId+",note="+noteId);
        db.close();
        return row;
    }

    public List<Long> getNotesInFolder(long folderId) {
        List<Long> noteIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_NOTE_ID};
        String selection = COLUMN_FOLDER_ID + "=?";
        String[] selectionArgs = {String.valueOf(folderId)};
        Cursor cursor = db.query(FOLDER_NOTE_TABLE_NAME, columns, selection, selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            long noteId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID));
            noteIds.add(noteId);
        }

        cursor.close();
        db.close();
        return noteIds;
    }

    // 添加获取文件夹ID的方法
    public long getFolderIdByName(String folderName) {
        long folderId = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_FOLDER_ID};
        String selection = COLUMN_FOLDER_NAME + "=? AND " + COLUMN_USER_ID + "=?";
        String[] selectionArgs = {folderName, String.valueOf(getCurrentUserId())};
        Cursor cursor = db.query(FOLDER_TABLE_NAME, columns, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            folderId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FOLDER_ID));
        }

        cursor.close();
        db.close();
        return folderId;
    }

    public List<Long> getAllNoteIds() {
        List<Long> noteIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_NOTE_ID};
        Cursor cursor = db.query(NOTE_TABLE_NAME, columns, null, null, null, null, null);

        while (cursor.moveToNext()) {
            long noteId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID));
            noteIds.add(noteId);
        }

        cursor.close();
        db.close();
        return noteIds;
    }

    public List<ContentAdapter.Noteblock> getNotesByIds(List<Long> noteIds) {
        List<ContentAdapter.Noteblock> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        for (Long noteId : noteIds) {
            String selection = COLUMN_NOTE_ID + "=?";
            String[] selectionArgs = {String.valueOf(noteId)};
            Cursor cursor = db.query(NOTE_TABLE_NAME, null, selection, selectionArgs, null, null, null);

            if (cursor.moveToFirst()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATE_TIME));
                notes.add(new ContentAdapter.Noteblock(title, time, noteId));
            }

            cursor.close();
        }
        db.close();
        return notes;
    }
}
