package com.example.notebook;

import com.example.notebook.Note;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface APIEndPoint {
    @GET("notes/get_notes/{user_id}/")
    Call<List<Note>> getNotes(@Path("user_id") int user_id);

    @GET("notes/get_note/{note_id}/")
    Call<Note> getNote(@Path("note_id") long note_id);

    @GET("notes/get_note_content/{note_id}/")
    Call<NoteResponse> getContents(@Path("note_id") long note_id);

    @FormUrlEncoded
    @POST("notes/upload_note/")
    Call<ResponseBody> uploadNote(@Field("note_id") long noteId,
                                  @Field("user_id") int userId,
                                  @Field("title") String title,
                                  @Field("create_time") String time,
                                  @Field("version") long version);

    @Multipart
    @POST("notes/upload_content/")
    Call<ResponseBody> uploadContent(
            @Part("note_id") RequestBody noteId,
            @Part("content_id") RequestBody contentId,
            @Part("content_type") RequestBody contentType,
            @Part("text") RequestBody text,
            @Part("file_url") RequestBody fileUrl,
            @Part("position") RequestBody position,
            @Part("version") RequestBody version,
            @Part MultipartBody.Part file
    );

    @GET("notes/download_file/{content_id}/")
    Call<ResponseBody> downloadFile(@Path("content_id") long content_id);

    @POST("notes/delete_note/{note_id}/")
    Call<ResponseBody> deleteNote(@Path("note_id") long note_id);
    @POST("notes/delete_content/{content_id}/")
    Call<ResponseBody> deleteContent(@Path("content_id") long content_id);
}
