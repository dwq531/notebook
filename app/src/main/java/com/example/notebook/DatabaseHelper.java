package com.example.notebook;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

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
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "users",NOTE_TABLE_NAME="notes",CONTENT_TABLE_NAME="content";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    // 笔记列表：用户，笔记id，笔记题目，创建时间
    private static final String COLUMN_NOTE_ID="note_id",COLUMN_TITLE="title",COLUMN_CREATE_TIME="create_time",COLUMN_VERSION="version";
    // 笔记内容：所属笔记id，内容id，内容，类型，位置
    private static final String COLUMN_CONTENT_ID="content_id",COLUMN_CONTENT="content",COLUMN_TYPE="type",COLUMN_POSITION="position";
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    private APIEndPoint api;
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT,"
                + COLUMN_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);
        // 笔记列表：用户id，笔记id，笔记题目，创建时间
        String CREATE_NOTE_TABLE = String.format("CREATE TABLE %s (%s INTEGER, %s INTEGER PRIMARY KEY, %s TEXT,%s TEXT,%s INTEGER)",
                NOTE_TABLE_NAME,COLUMN_ID,COLUMN_NOTE_ID,COLUMN_TITLE,COLUMN_CREATE_TIME,COLUMN_VERSION);
        db.execSQL(CREATE_NOTE_TABLE);
        // 笔记内容：所属笔记id，内容id，内容，类型,位置
        String CREATE_CONTENT_TABLE = String.format("CREATE TABLE %s (%s INTEGER,%s INTEGER PRIMARY KEY, %s TEXT,%s INTEGER,%s INTEGER,%s INTEGER)",
                CONTENT_TABLE_NAME,COLUMN_NOTE_ID,COLUMN_CONTENT_ID,COLUMN_CONTENT,COLUMN_TYPE,COLUMN_POSITION,COLUMN_VERSION);
        db.execSQL(CREATE_CONTENT_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};
        Cursor cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count > 0;
    }
    public long addNote(int user,String title,String create_time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID,user);
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
        values.put(COLUMN_ID,user);
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
        Cursor cursor = db.query(NOTE_TABLE_NAME,null,COLUMN_ID+"= ?",new String[]{String.valueOf(user)},null,null,null);
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
            note.user_id = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));;
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
}
