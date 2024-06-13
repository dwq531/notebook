package com.example.notebook;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {

    private List<String> folders;
    private String currentFolder;
    private int selectedPosition = -1;

    public FolderAdapter(List<String> folders, String currentFolder) {
        this.folders = folders;
        this.currentFolder = currentFolder;
        this.selectedPosition = folders.indexOf(currentFolder);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView folderName;
        public LinearLayout folderItem;
        public CheckBox folderCheckBox;

        public ViewHolder(View v) {
            super(v);
            folderName = v.findViewById(R.id.folderName);
            folderItem = v.findViewById(R.id.folderItem);
            folderCheckBox = v.findViewById(R.id.folderCheckBox);
        }
    }

    @NonNull
    @Override
    public FolderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderAdapter.ViewHolder holder, int position) {
        String folder = folders.get(position);
        holder.folderName.setText(folder);

        // 设置 CheckBox 的状态
        holder.folderCheckBox.setChecked(folder.equals(currentFolder));

        holder.folderItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition = holder.getAdapterPosition();
                currentFolder = folders.get(selectedPosition);
                notifyDataSetChanged();
            }
        });

        holder.folderCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition = holder.getAdapterPosition();
                currentFolder = folders.get(selectedPosition);
                notifyDataSetChanged();
            }
        });

        // 设置背景颜色
        if (selectedPosition == position) {
            holder.folderItem.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.folderItem.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    public String getSelectedFolder() {
        if (selectedPosition != -1) {
            return folders.get(selectedPosition);
        }
        return null;
    }

    // 更新文件夹列表并通知数据集改变
    public void updateFolders(List<String> newFolders) {
        this.folders = newFolders;
        notifyDataSetChanged();
    }
}
