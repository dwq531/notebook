package com.example.notebook;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import com.example.notebook.APIEndPoint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class UploadManager {
    private APIEndPoint api;

    public UploadManager() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/")
                .build();

        api = retrofit.create(APIEndPoint.class);
    }

    public void uploadContent(long noteId, long contentId, int contentType, String text,int position, Uri fileuri,Context context) {
        File file = getFileFromUri(context,fileuri);
        if(file == null)
            return;
        RequestBody noteIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(noteId));
        RequestBody contentIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(contentId));
        RequestBody contentTypePart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(contentType));
        RequestBody textPart = RequestBody.create(MediaType.parse("text/plain"), text);
        RequestBody fileUrlPart = RequestBody.create(MediaType.parse("text/plain"), fileuri.toString());
        RequestBody positionPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(position));

        RequestBody fileRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), fileRequestBody);

        Call<ResponseBody> call = api.uploadContent(noteIdPart, contentIdPart, contentTypePart, textPart, fileUrlPart, positionPart, filePart);
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
    public static File getFileFromUri(Context context, Uri uri) {
        File file = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            String fileName = getFileName(contentResolver, uri);
            InputStream inputStream = contentResolver.openInputStream(uri);
            File tempFile = new File(context.getCacheDir(), fileName);

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
        }
        return file;
    }
    private static String getFileName(ContentResolver contentResolver, Uri uri) {
        String fileName = "";
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex != -1) {
                fileName = cursor.getString(nameIndex);
            }
            cursor.close();
        }
        return fileName;
    }
}
