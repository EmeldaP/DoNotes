package com.example.machoene.donotes;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.List;

public class NoteLisstActivity extends AppCompatActivity {

    //private ArrayAdapter<NoteInfo> mAdapterNotes;

    private NoteRecyclerAdapter mNoteAda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_lisst);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NoteLisstActivity.this, NoteActivity.class));
            }
        });




        initializeDisplayContent();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mNoteAda.notifyDataSetChanged();
    }

    private void initializeDisplayContent() {

        //assostiating recycler with our layout manager
        final RecyclerView recyclerNotes = (RecyclerView) findViewById(R.id.rv_list_notes);
        final LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerNotes.setLayoutManager(manager);

        //instance of adapter
        List<NoteInfo> noteInfos  = DataManager.getInstance().getNotes();
        mNoteAda =new NoteRecyclerAdapter(this, noteInfos);

        recyclerNotes.setAdapter(mNoteAda);
    }



}
