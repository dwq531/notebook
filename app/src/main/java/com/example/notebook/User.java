package com.example.notebook;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("user_id")
    public int user_id;
    @SerializedName("password")
    public String password;
    @SerializedName("username")
    public String username;
    @SerializedName("signatrue")
    public String signatrue;
    @SerializedName("image_url")
    public String image_url;
    @SerializedName("version")
    public long version;
}
