package com.example.notebook;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NoteResponse {
    @SerializedName("contents")
    public List<ContentResponse> contents;
    @SerializedName("note")
    public Note note;
}
