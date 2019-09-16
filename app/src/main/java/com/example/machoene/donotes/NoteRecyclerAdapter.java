package com.example.machoene.donotes;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.machoene.donotes.DoNotesDatabaseContract.CourseInfoEntry;
import com.example.machoene.donotes.DoNotesDatabaseContract.NoteInfoEntry;

import java.util.List;

/**
 * Created by machoene on 2019-07-24.
 */

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder>{

    //private final List<NoteInfo> mNotes;
    private final Context mContext;
    private Cursor mCursor;
    private final LayoutInflater mLayoutInflater;
    private int coursePos;
    private int mNoteTitlePos;
    private int mIdPos;

    public NoteRecyclerAdapter(Context context,  Cursor cursor) {

        //this.mNotes = mNotes;
        mCursor = cursor;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        populateColumnPosition();

    }

    private void populateColumnPosition() {
        if (mCursor == null)
            return;
        //get column index form mCursor
        coursePos = mCursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE);
        mNoteTitlePos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mIdPos = mCursor.getColumnIndex(NoteInfoEntry._ID);
    }
    private void changrCursor(Cursor cursor){
        if (mCursor != null)
            mCursor.close();
        mCursor = cursor;
        populateColumnPosition();
        notifyDataSetChanged();
    }

    private void notifyDataSetChanged() {
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //
       View itemV = mLayoutInflater.inflate(R.layout.list_note,parent,false);
        return new ViewHolder(itemV);
    }
       //to display data on specif position
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        String course = mCursor.getString(coursePos);
        String noteTitle  = mCursor.getString(mNoteTitlePos);
        int id = mCursor.getInt(mIdPos);

       // NoteInfo note = mNotes.get(position);
        holder.mTextCourse.setText(course);
        holder.mTextTitle.setText(noteTitle);
        holder.mId = id;
            }
             //return number of rows ina cursor
    @Override
    public int getItemCount() {
        return mCursor == null ? 0: mCursor.getCount();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        public final TextView mTextCourse;
        public final TextView mTextTitle;
        public  int mId;

        public ViewHolder(View itemView) {

            super(itemView);
            //ref for text in list note
            mTextCourse = (TextView)itemView.findViewById(R.id.text_course);
            mTextTitle = (TextView)itemView.findViewById(R.id.text_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext,NoteActivity.class);
                    intent.putExtra(NoteActivity.NOTE_POSITION,mId);
                    mContext.startActivity(intent);
                }
            });
        }
    }

}
