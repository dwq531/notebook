package com.example.notebook;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AIRequest {
    List<Map<String,String>> messages;
    AIRequest(String prompt){
        messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        Log.d("AIRequest",messages.toString());
    }
}
