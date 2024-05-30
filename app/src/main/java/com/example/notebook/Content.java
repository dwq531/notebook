package com.example.notebook;

public class Content {
    public long note_id;
    public long content_id;
    public String content;
    public int type;
    public int position;
    public enum Type{
        TEXT,
        IMAGE,
        AUDIO
    }
}
