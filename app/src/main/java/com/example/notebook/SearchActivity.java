package com.example.notebook;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notebook.ContentAdapter;
import com.example.notebook.DatabaseHelper;
import com.example.notebook.R;
import com.example.notebook.databinding.ActivitySearchBinding;

import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private DatabaseHelper databaseHelper;
    private ContentAdapter adapter;
    private RecyclerView recyclerView;
    private EditText searchInput;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        searchInput = findViewById(R.id.search_input);
        searchButton = findViewById(R.id.search_button);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseHelper = new DatabaseHelper(this);
        adapter = new ContentAdapter(this, 0,null);
        recyclerView.setAdapter(adapter);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchInput.getText().toString();
                searchNotes(query);
            }
        });
    }

    private void searchNotes(String query) {
        List<Note> notes = databaseHelper.searchNotes(query);
        // 如果没有匹配到任何内容，则显示 toast 弹窗
        if (notes.isEmpty()) {
            Toast Toast = null;
            Toast.makeText(SearchActivity.this, "未搜索到相关内容", Toast.LENGTH_SHORT).show();
        }
        adapter.clearItems();
        for (Note note : notes) {
            adapter.addItem(note.title, note.create_time, note.note_id);
            adapter.keyword = query;
        }
    }
}
