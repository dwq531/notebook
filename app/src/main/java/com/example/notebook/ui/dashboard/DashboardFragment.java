package com.example.notebook.ui.dashboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notebook.APIEndPoint;
import com.example.notebook.Content;
import com.example.notebook.ContentAdapter;
import com.example.notebook.DatabaseHelper;
import com.example.notebook.Note;
import com.example.notebook.R;
import com.example.notebook.SearchActivity;
import com.example.notebook.UploadManager;
import com.example.notebook.databinding.FragmentDashboardBinding;
import com.example.notebook.text_editor;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    Button addButton,searchButton;
    private RecyclerView recyclerView;
    private ContentAdapter adapter;
    private DatabaseHelper databaseHelper;
    private UploadManager uploadManager;
    private int user_id = 0;
    private final static int NOTEID =2;
    private final int ADD_NOTE = 0,EDIT_NOTE=1;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        uploadManager = new UploadManager(getContext());
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        addButton = root.findViewById(R.id.but_add);
        databaseHelper = new DatabaseHelper(root.getContext());
        user_id = databaseHelper.getCurrentUserId();;
        recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        adapter = new ContentAdapter(getActivity(),0,null);
        recyclerView.setAdapter(adapter);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到新建编辑文本页面
                Intent intent = new Intent(getActivity(), text_editor.class);
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String time = dateFormat.format(calendar.getTime());
                long note_id = databaseHelper.addNote(user_id,"新建笔记",time);
                uploadManager.addNote(note_id);
                databaseHelper.addContent(note_id,"",0,0);
                Log.d("text_editor", String.valueOf(note_id));
                intent.putExtra("note_id", note_id);
                intent.putExtra("user_id",user_id);
                startActivityForResult(intent,ADD_NOTE);
            }
        });

        searchButton = root.findViewById(R.id.but_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });


        updateNotes();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    public void clearNotes(){

    }
    public void updateNotes(){
        List<Note> notes = databaseHelper.getNoteList(user_id);
        Log.d("adapter",notes.toString());
        for(Note note:notes){
            adapter.addItem(note.title,note.create_time,note.note_id);
        }
    }
    public void addNoteItem(String title,String time,long note_id){
        adapter.addItem(title,time,note_id);
    }
    public void editNoteItem(String title,long note_id){
        adapter.editNote(note_id,title);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_NOTE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    String title = data.getStringExtra("title");
                    String time = data.getStringExtra("time");
                    Long note_id = data.getLongExtra("note_id",-1);
                    // 新增note block
                    addNoteItem(title,time,note_id);

                }
            }
        }
        else if(requestCode == EDIT_NOTE){
            if (resultCode == Activity.RESULT_OK && data != null){
                String title = data.getStringExtra("title");
                Long note_id = data.getLongExtra("note_id",-1);
                Log.d("notelist",title);
                editNoteItem(title,note_id);

            }
            // todo
        }
    }
}