package com.example.notebook;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.example.notebook.APIEndPoint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UploadManager {
    private final APIEndPoint api;
    private final DatabaseHelper databaseHelper ;
    private final Context context;

    public UploadManager(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://183.172.155.18:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(APIEndPoint.class);
        this.context = context;
        databaseHelper = new DatabaseHelper(context);
    }
    private String getRealPathFromURI(Uri uri) {
        String filePath = null;
        if ("content".equals(uri.getScheme())) {
            String[] projection = { MediaStore.Images.Media.DATA };
            try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    filePath = cursor.getString(columnIndex);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equals(uri.getScheme())) {
            filePath = uri.getPath();
        }
        return filePath;
    }
    public File getFileFromUri(Uri uri,String ext) {
        File file = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(uri);
            File tempFile = new File(context.getCacheDir(), System.currentTimeMillis()+ext);

            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                int read;
                byte[] buffer = new byte[8 * 1024];
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
            }
            file = tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return file;
    }
    public void uploadContent(long noteId, long contentId, int contentType, String text,int position, Uri fileuri,long version) {
        File file = null;
        if(contentType!=0)
        {
            String ext=".jpg";
            if(contentType==2)
                ext=".3gp";
            file = getFileFromUri(fileuri,ext);
            if(file==null)
                file = new File(fileuri.getPath());
            if(file == null )
                return;
        }
        RequestBody noteIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(noteId));
        RequestBody contentIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(contentId));
        RequestBody contentTypePart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(contentType));
        RequestBody textPart = RequestBody.create(MediaType.parse("text/plain"), text);
        RequestBody fileUrlPart = null;
        RequestBody positionPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(position));
        RequestBody versionPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(version));
        MultipartBody.Part filePart = null;
        if(contentType!=0)
        {
            fileUrlPart = RequestBody.create(MediaType.parse("text/plain"), fileuri.toString());
            RequestBody fileRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            filePart = MultipartBody.Part.createFormData("file", file.getName(), fileRequestBody);
        }


        Call<ResponseBody> call = api.uploadContent(noteIdPart, contentIdPart, contentTypePart, textPart, fileUrlPart, positionPart, versionPart,filePart);
        call.enqueue(new Callback<ResponseBody>() {
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // 请求成功，处理响应
                    Log.d("API", "Response: " + response.body().toString());
                } else {
                    Log.d("API", "Error: " + response.errorBody().toString());
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 请求失败，处理错误
                Log.d("API","Failure: " + t.getMessage());
            }
        });
    }

    public void addNote(long note_id){
        Note note = databaseHelper.getNote(note_id);
        Call<ResponseBody> call = api.uploadNote(note_id,note.user_id,note.title,note.create_time,note.version);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // 请求成功，处理响应
                    Log.d("API","Response: " + response.body().toString());
                }
                else{
                    Log.d("API","Error: " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 请求失败，处理错误
                Log.d("API","Failure: " + t.getMessage());
            }
        });
    }

    public void uploadNote(long note_id){
        addNote(note_id);
        List<Content> contents = databaseHelper.getContentList(note_id);
        for(Content content:contents){
            if(content.type == 0)
                uploadContent(content.note_id,content.content_id,content.type,content.content,content.position,null,content.version);
            else
                uploadContent(content.note_id,content.content_id,content.type,"",content.position,Uri.parse(content.content) ,content.version);

        }
    }
    public void getNotes(int user_id){
        Call<List<Note>> call = api.getNotes(user_id);
        call.enqueue(new Callback<List<Note>>() {
            @Override
            public void onResponse(Call<List<Note>> call, Response<List<Note>> response) {
                if (response.isSuccessful()) {
                    for(Note note:response.body()){
                        Note localnote = databaseHelper.getNote(note.note_id);
                        if(localnote!=null){
                            if(localnote.version< note.version){
                                syncContentList(note.note_id);
                            }
                            else if(localnote.version>note.version){
                                uploadNote(localnote.note_id);
                            }
                        }else{
                            syncContentList(note.note_id);

                        }
                    }
                    Log.d("API","Response: " + response.body().toString());
                    Log.d("uploadmanager",databaseHelper.getNoteList(user_id).toString());
                }
                else{
                    Log.d("API","Error: " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<List<Note>> call, Throwable t) {
                // 请求失败，处理错误
                Log.d("API","Failure: " + t.getMessage());
            }
        });
    }
    public void syncContentList(long note_id){
        Call<NoteResponse> call = api.getContents(note_id);
        call.enqueue(new Callback<NoteResponse>() {
            @Override
            public void onResponse(Call<NoteResponse> call, Response<NoteResponse> response) {

                if (response.isSuccessful()) {
                    Note note = response.body().note;
                    Note localnote = databaseHelper.getNote(note_id);
                    List<ContentResponse> contentResponses = response.body().contents;
                    for(ContentResponse content: contentResponses){
                        Content localctt = databaseHelper.getContent(content.content_id);
                        if(localctt!=null){
                            // 本地版本比云端版本旧
                            if(localctt.version< content.version){
                                if(content.type==0)
                                    databaseHelper.updateContent(content.content_id, content.text);
                                else
                                    databaseHelper.updateContent(content.content_id,content.file_url);
                                databaseHelper.updateContentPosition(content.content_id, content.position, content.position, false);
                            }
                            // 本地版本新 todo
                            else{
                                uploadContent(note_id,localctt.content_id,localctt.type,localctt.content,localctt.position, Uri.parse(localctt.content),localctt.version);
                            }
                        }
                        // 本地没有
                        else{
                            // 本地note版本旧，下载到本地
                            if(localnote==null || localnote.version<note.version){
                                if(content.type!=0){
                                    databaseHelper.addContent(note_id,content.file_url, content.type, content.position,content.content_id);
                                    downloadFile(content.content_id, Uri.parse(content.file_url),(content.type==1));
                                }
                                else{
                                    databaseHelper.addContent(note_id,content.text, content.type, content.position,content.content_id);
                                }
                            }
                            // 本地note版本新，删除云端版本 todo
                            else{
                                delete_content(content.content_id);
                            }
                        }
                    }
                    if(localnote ==null)
                        databaseHelper.addNote(note.user_id,note.title,note.create_time,note.note_id);
                    else
                        databaseHelper.updateTitle(note_id, note.title);
                    Log.d("uploadManager", "get content successful");
                } else {
                    Log.d("uploadManager", "Server contact failed");
                }
            }

            @Override
            public void onFailure(Call<NoteResponse> call, Throwable t) {
                Log.e("uploadManager", "Error get content: " + t.getMessage());
            }
        });
    }
    public void downloadFile(long content_id, Uri localPath,boolean isImg) {
        Call<ResponseBody> call = api.downloadFile(content_id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Uri fileuri = writeResponseBodyToDisk(response.body(), localPath,isImg);
                    databaseHelper.updateContent(content_id,fileuri.toString());
                    Log.d("uploadManager", "File download was successful"+fileuri);
                } else {
                    Log.d("uploadManager", "Server contact failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("uploadManager", "Error downloading file: " + t.getMessage());
            }
        });
    }
    private Uri writeResponseBodyToDisk(ResponseBody body, Uri localPath,boolean isImg) {

        File file = new File(localPath.getPath());
        File storageDir;
        try{
            if(isImg)
            {
                storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                String name = file.getName();
                file = File.createTempFile(
                        name,  /* 前缀 */
                        ".jpg",         /* 后缀 */
                        storageDir      /* 目录 */
                );
                Log.d("writeResponseBodyToDisk",storageDir.getPath());
            }

            else {
                storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
                file = File.createTempFile(
                        file.getName(),  /* 前缀 */
                        ".3gp",         /* 后缀 */
                        storageDir      /* 目录 */
                );
            }
        }catch (IOException e){
            return null;
        }

        try (InputStream inputStream = body.byteStream();
             OutputStream outputStream = new FileOutputStream(file)) {

            byte[] fileReader = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(fileReader)) != -1) {
                outputStream.write(fileReader, 0, bytesRead);
            }
            outputStream.flush();
            String authority = context.getPackageName() + ".fileProvider";
            Uri uri = FileProvider.getUriForFile(context, authority, file);
            Log.d("writeResponseBodyToDisk", "File written to: " + uri);
            return uri;
        } catch (IOException e) {
            Log.e("writeResponseBodyToDisk", "Failed to write file to disk", e);
            return null;
        }
    }
    public void delete_note(long note_id){
        Call<ResponseBody> call = api.deleteNote(note_id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // 请求成功，处理响应
                    Log.d("API","Response: " + response.body().toString());
                }
                else{
                    Log.d("API","Error: " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 请求失败，处理错误
                Log.d("API","Failure: " + t.getMessage());
            }
        });
    }

    public void delete_content(long content_id){
        Call<ResponseBody> call = api.deleteContent(content_id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // 请求成功，处理响应
                    Log.d("API","Response: " + response.body().toString());
                }
                else{
                    Log.d("API","Error: " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 请求失败，处理错误
                Log.d("API","Failure: " + t.getMessage());
            }
        });


    }
    public void upload_user(int user_id){
        User user = databaseHelper.getUser(user_id);
        RequestBody userIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(user_id));
        RequestBody passwordPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(user.password));
        RequestBody usernamePart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(user.username));
        RequestBody signatruePart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(user.signatrue));
        RequestBody urlPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(user.image_url));
        RequestBody versionPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(user.version));
        File file = null;
        MultipartBody.Part filePart = null;
        if(user.image_url!=null) {
            file = getFileFromUri(Uri.parse(user.image_url), ".jpg");
            RequestBody fileRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            filePart = MultipartBody.Part.createFormData("image", file.getName(), fileRequestBody);
        }

        Call<ResponseBody> call = api.uploadUser(userIdPart,passwordPart,usernamePart,signatruePart,urlPart,versionPart,filePart);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("API","Response: " + response.body().toString());
                }
                else{
                    Log.d("API","Error: " + response.errorBody().toString());
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 请求失败，处理错误
                Log.d("API","Failure: " + t.getMessage());
            }
        });
    }
    public void get_user(String username){
        Call<User> call = api.get_user_by_name(username);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    if(user == null)
                        return;
                    User localUser = databaseHelper.getUser(user.user_id);
                    if(localUser==null ){
                        databaseHelper.addUser(user.user_id,user.username,user.password,user.signatrue,user.image_url,user.version);
                        downloadImg(user.user_id);
                    }
                    Log.d("API","Response: " + response.body().toString());
                }
                else{
                    Log.d("API","Error: " + response.errorBody().toString());
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // 请求失败，处理错误
                Log.d("API","Failure: " + t.getMessage());
            }
        });
    }
    public void downloadImg(int user_id){
        Call<ResponseBody> call = api.downloadImage(user_id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    File storageDir;
                    File file;
                    Uri fileuri=null;
                    try {
                        storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                        String name = "avator_"+user_id+"_"+System.currentTimeMillis();
                        file = File.createTempFile(
                                name,  /* 前缀 */
                                ".jpg",         /* 后缀 */
                                storageDir      /* 目录 */
                        );
                        try (InputStream inputStream = response.body().byteStream();
                             OutputStream outputStream = new FileOutputStream(file)) {
                            byte[] fileReader = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(fileReader)) != -1) {
                                outputStream.write(fileReader, 0, bytesRead);
                            }
                            outputStream.flush();
                            String authority = context.getPackageName() + ".fileProvider";
                            fileuri = FileProvider.getUriForFile(context, authority, file);
                            Log.d("writeResponseBodyToDisk", "File written to: " + fileuri);
                        } catch (IOException e) {
                            Log.e("writeResponseBodyToDisk", "Failed to write file to disk", e);
                        }
                    }catch (IOException e){
                        Log.d("uploadManager", "IOException:"+e);
                        return;
                    }
                    databaseHelper.updateUserImage(user_id,fileuri.toString());
                    Log.d("uploadManager", "File download was successful:"+fileuri);
                } else {
                    Log.d("uploadManager", "Server contact failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("uploadManager", "Error downloading file: " + t.getMessage());
            }
        });
    }
}
