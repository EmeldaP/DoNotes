package com.example.machoene.donotes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.machoene.donotes.DoNotesDatabaseContract.CourseInfoEntry;
import com.example.machoene.donotes.DoNotesDatabaseContract.NoteInfoEntry;

import androidx.annotation.Nullable;

public class DoNotesOpenHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "DONotes.db";
    public static final int DATABASE_VERSION = 2;
    public DoNotesOpenHelper(@Nullable Context context) {

        super(context,DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create empty tables
        db.execSQL(CourseInfoEntry.SQL_CREATE_TABLE);
        db.execSQL(NoteInfoEntry.SQL_CREATE_TABLE);
        //create index
        db.execSQL(CourseInfoEntry.SQL_CREATE_INDEX1);
        db.execSQL(NoteInfoEntry.SQL_CREATE_INDEX1);

        //worker to fill tables
        DatabaseDataWorker worker = new DatabaseDataWorker(db);
        worker.insertCourses();
        worker.insertSampleNotes();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 2){
            db.execSQL(NoteInfoEntry.SQL_CREATE_INDEX1);
            db.execSQL(CourseInfoEntry.SQL_CREATE_INDEX1);
        }


    }
}
