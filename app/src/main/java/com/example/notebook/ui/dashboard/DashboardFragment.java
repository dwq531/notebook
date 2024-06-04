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

import com.example.notebook.Content;
import com.example.notebook.ContentAdapter;
import com.example.notebook.DatabaseHelper;
import com.example.notebook.Note;
import com.example.notebook.R;
import com.example.notebook.databinding.FragmentDashboardBinding;
import com.example.notebook.text_editor;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    Button addButton,searchButton;
    private final int ADD_NOTE = 0,EDIT_NOTE=1;
    private RecyclerView recyclerView;
    private ContentAdapter adapter;
    private DatabaseHelper databaseHelper;
    private int user_id = 0;//todo:user
    private final static int NOTEID =2;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        addButton = root.findViewById(R.id.but_add);
        databaseHelper = new DatabaseHelper(root.getContext());
        recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        adapter = new ContentAdapter(getActivity(),0);
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
                databaseHelper.addContent(note_id,"",0,0);
                Log.d("text_editor", String.valueOf(note_id));
                intent.putExtra("note_id", note_id);
                intent.putExtra("user_id",user_id);
                startActivityForResult(intent,ADD_NOTE);
            }
        });
        updateNotes();
        return root;
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
                    adapter.addItem(title,time,note_id);
                }
            }
        }
        else if(requestCode == EDIT_NOTE){

            // todo
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateNotes(){
        List<Note> notes = databaseHelper.getNoteList(user_id);
        for(Note note:notes){
            adapter.addItem(note.title,note.create_time,note.note_id);
        }
    }
}