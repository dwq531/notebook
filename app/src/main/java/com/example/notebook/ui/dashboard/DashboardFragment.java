package com.example.notebook.ui.dashboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.notebook.R;
import com.example.notebook.databinding.FragmentDashboardBinding;
import com.example.notebook.text_editor;

import org.w3c.dom.Text;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    Button addButton,searchButton;
    private final int ADD_NOTE = 0;
    private LinearLayout noteList;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        addButton = root.findViewById(R.id.but_add);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到新建编辑文本页面
                Intent intent = new Intent(getActivity(), text_editor.class);
                startActivityForResult(intent,ADD_NOTE);
            }
        });
        noteList = root.findViewById(R.id.noteList);
        View example_note = getLayoutInflater().inflate(R.layout.note_block, noteList, false);
        noteList.addView(example_note);
        View example_note2 = getLayoutInflater().inflate(R.layout.note_block, noteList, false);
        noteList.addView(example_note2);

        return root;
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NOTE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    String title = data.getStringExtra("title");
                    String time = data.getStringExtra("time");
                    // 新增note block
                    View note_block = getLayoutInflater().inflate(R.layout.note_block, noteList, false);
                    TextView titleView = note_block.findViewById(R.id.note_title);
                    TextView timeView = note_block.findViewById(R.id.textView2);
                    titleView.setText(title);
                    timeView.setText(time);
                    noteList.addView(note_block);
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}