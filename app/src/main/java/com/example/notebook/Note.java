package com.example.notebook;

public class Note {
    public long note_id;
    public int user_id;
    public String title;
    public String create_time;
    public Note(long note_id,int user_id,String title,String create_time){
        this.note_id=note_id;
        this.user_id=user_id;
        this.title=title;
        this.create_time=create_time;
    }
    public Note(){}
}
