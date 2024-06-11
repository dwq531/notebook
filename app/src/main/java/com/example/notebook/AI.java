package com.example.notebook;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AI {
    private OkHttpClient HTTP_CLIENT;
    private final DatabaseHelper databaseHelper;
    private final Context context;
    private String access_token;
    private final String api_key = "OTftVmUYdFJtcbJLVtgwspjR";
    private final String secret_key = "OJS6iSK2W6l1DCPMczJxs2J5lwQJWHk5";

    AI(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://aip.baidubce.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        HTTP_CLIENT = new OkHttpClient().newBuilder().build();
        this.context = context;
        databaseHelper = new DatabaseHelper(context);
    }

    public void getAccessToken(AICallBack callback) {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + api_key + "&client_secret=" + secret_key);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        HTTP_CLIENT.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("AccessToken", "Body: " + responseBody);
                        JSONObject jsonObject = new JSONObject(responseBody);
                        access_token = jsonObject.getString("access_token");
                        Log.d("AccessToken", "Access Token: " + access_token);
                        callback.execute();  // 执行传入的回调函数
                    } catch (Exception e) {
                        Log.e("ParsingError", "Failed to parse response: " + e.getMessage());
                    }
                } else {
                    Log.e("AccessToken", "Error: " + response.toString());
                }
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("AccessToken", "Failure: " + e.toString());
            }
        });
    }

    public void ask_AI(String prompt,AIUpdate callBack) throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("role", "user");
        jsonObject.put("content", prompt);

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"messages\":[" + jsonObject.toString() + "]}");
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions?access_token=" + access_token)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        HTTP_CLIENT.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.d("ask_AI", e.toString());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("ask_AI", responseBody);
                        JSONObject json_feedback = new JSONObject(responseBody);
                        String re = json_feedback.getString("result");
                        callBack.update(re);
                        Log.d("ask_AI", re);
                    } catch (JSONException e) {
                        Log.d("ask_AI", e.toString());
                    }
                }
            }
        });
    }

    public void generate_title(long note_id,AIUpdate callBack) {
        List<Content> contents = databaseHelper.getContentList(note_id);
        String prompt = "请生成以下内容的标题，只要标题别多废话：\n";
        for (Content content : contents) {
            if (content.type == 0) {
                prompt += content.content;
            }
        }

        String finalPrompt = prompt;
        getAccessToken(() -> {
            try {
                ask_AI(finalPrompt,callBack);
            } catch (Exception e) {
                Log.e("ask_AI", e.getMessage(), e);
            }
        });
    }
    public void generate_summary(long note_id,AIUpdate callBack){
        List<Content> contents = databaseHelper.getContentList(note_id);
        String prompt = "请生成以下内容的一小段总结：\n";
        for (Content content : contents) {
            if (content.type == 0) {
                prompt += content.content;
            }
        }

        String finalPrompt = prompt;
        getAccessToken(() -> {
            try {
                ask_AI(finalPrompt,callBack);
            } catch (Exception e) {
                Log.e("ask_AI", e.getMessage(), e);
            }
        });
    }
}
