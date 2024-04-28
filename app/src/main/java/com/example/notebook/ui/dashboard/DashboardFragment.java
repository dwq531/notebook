package com.example.notebook.ui.dashboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.notebook.R;
import com.example.notebook.databinding.FragmentDashboardBinding;
import com.example.notebook.text_editor;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    Button addButton,searchButton;
    private final int ADD_NOTE = 0;

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
        return root;
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NOTE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    String resultValue = data.getStringExtra("key");
                    // TODO
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