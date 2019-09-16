package com.example.machoene.donotes;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by machoene on 2019-07-24.
 */

public class CourseRecyclerAdapter extends RecyclerView.Adapter<CourseRecyclerAdapter.ViewHolder>{

    private final List<CourseInfo> mCourses;
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;

    public CourseRecyclerAdapter(Context context, List<CourseInfo> courses) {
        mCourses = courses;

        mContext = context;

        mLayoutInflater = LayoutInflater.from(mContext);

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //
       View itemV = mLayoutInflater.inflate(R.layout.item_course,parent,false);
        return new ViewHolder(itemV);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CourseInfo course = mCourses.get(position);
        holder.mTextCourse.setText(course.getTitle());


    }

    @Override
    public int getItemCount() {
        return mCourses.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        public final TextView mTextCourse;

        public int mCurrentPosition;

        public ViewHolder(View itemView) {
            super(itemView);
            //ref for text in list note
            mTextCourse = (TextView)itemView.findViewById(R.id.text_course);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   Snackbar.make(view, mCourses.get(mCurrentPosition).getTitle(),
                           Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

}
