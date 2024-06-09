package com.example.notebook;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notebook.R;
import com.example.notebook.databinding.NoteBlockBinding;

import java.util.ArrayList;
import java.util.List;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> {
    private List<Noteblock> noteblocks = new ArrayList<>();;
    private Activity activity;
    private int user_id = 0;
    private final int EDIT_NOTE=1;

    public ContentAdapter(Activity a,int user){
        this.activity = a;
        this.user_id = user;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView time;
        public ImageButton button;
        public ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.note_title);
            time = v.findViewById(R.id.textView2);
            button = v.findViewById(R.id.delete_button);
        }
    }
    public class Noteblock{
        public String title,time;
        public long note_id;
        public Noteblock(String title,String time,long note_id){
            this.title = title;
            this.time = time;
            this.note_id = note_id;
        }
    }
    @NonNull
    @Override
    public ContentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_block, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContentAdapter.ViewHolder holder, int position) {
        Noteblock note = noteblocks.get(position);
        holder.title.setText(note.title);
        holder.time.setText(note.time);
        holder.itemView.setTag(note.note_id);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, text_editor.class);
                intent.putExtra("note_id", note.note_id);
                intent.putExtra("user_id",user_id);
                activity.startActivityForResult(intent,EDIT_NOTE);
            }
        });
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(activity)
                        .setTitle("Delete Note")
                        .setMessage("Are you sure you want to delete this note?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                DatabaseHelper databaseHelper = new DatabaseHelper(activity);
                                long note_id = (long) holder.itemView.getTag();
                                databaseHelper.deleteNote(note_id);
                                removeAt(holder.getAdapterPosition());
                                UploadManager uploadManager = new UploadManager(activity);
                                uploadManager.delete_note(note_id);

                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

    }


    @Override
    public int getItemCount() {
        return noteblocks.size();
    }

    // MyAdapter.java
    public void addItem(String title,String time,Long note_id ) {
        noteblocks.add(new Noteblock(title,time,note_id));
        notifyItemInserted(noteblocks.size() - 1);
    }
    public void removeAt(int position){
        noteblocks.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,noteblocks.size());
    }
    public void editNote(long note_id,String title){
        for(Noteblock note:noteblocks){
            if(note.note_id==note_id){
                note.title=title;
                notifyItemChanged(noteblocks.indexOf(note));
                break;
            }
        }
    }


}