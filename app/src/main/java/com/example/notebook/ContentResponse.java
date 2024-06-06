package com.example.notebook;
import com.google.gson.annotations.SerializedName;
public class ContentResponse {
    @SerializedName("note_id")
    public long note_id;
    @SerializedName("content_id")
    public long content_id;
    @SerializedName("content_type")
    public int type;
    @SerializedName("text")
    public String text;
    @SerializedName("file")
    public String file;
    @SerializedName("file_url")
    public String file_url;
    @SerializedName("position")
    public int position;
    @SerializedName("version")
    public long version;
}
