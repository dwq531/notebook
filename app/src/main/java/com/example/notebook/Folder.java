package com.example.notebook;

public class Folder {
    public String folder_name;
    public int folder_id;
    public int user_id;
    public long version;
    public Folder(String folder_name,int folder_id,int user_id,long version){
        this.folder_id=folder_id;
        this.folder_name=folder_name;
        this.user_id=user_id;
        this.version=version;
    }
}
