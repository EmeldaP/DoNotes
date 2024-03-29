package com.example.machoene.donotes;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.example.machoene.donotes.DoNotesDatabaseContract.CourseInfoEntry;
import com.example.machoene.donotes.DoNotesDatabaseContract.NoteInfoEntry;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    public static final String NOTE_POSITION = "com.example.machone.takenotes.NOTE_POSITION";
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.example.machone.takenotes.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.example.machone.takenotes.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.example.machone.takenotes.ORIGINAL_NOTE_TEXT";
    public static final int LOADER_COURSES = 1;
    public static final int POSITION_NOT_SET = -1;
    public static final int LOADER_NOTES = 0;
    private NoteInfo mNote;
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNotePosition;
    private boolean mIsCancelling;
    private String mOriginalNoteCourseId;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;
    private DoNotesOpenHelper doNote;
    private DoNotesOpenHelper mDbOpenHelper;
    private Cursor mNoteCursor;
    private int mCourseIdPos;
    private int mCourseTextPos;
    private int mNoteTitlePos;
    private int mNoteTextPos;
    private int mNotePosition;
    private int mNoteID;
    private SimpleCursorAdapter simpleCursorAdapter;
    private boolean coursesQueryFinished;
    private boolean mNotesQueryFinished;

    @Override
    public void onDestroy(){
        mDbOpenHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbOpenHelper= new DoNotesOpenHelper(this);
        mSpinnerCourses = (Spinner) findViewById(R.id.spinner_courses);

       // List<CourseInfo> courseInfos = DataManager.getInstance().getCourses();
            //populating spinner for SQLItedatabase
        simpleCursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
                new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[]{android.R.id.text1},0);
        simpleCursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(simpleCursorAdapter);

        getLoaderManager().iniLOader(LOADER_COURSES,null,this);

        readDisplayStateValues();
        if(savedInstanceState == null) {
            saveOriginalNoteValues();
        } else {
            restoreOriginalNoteValues(savedInstanceState);
        }

        mTextNoteTitle = (EditText) findViewById(R.id.text_note_title);
        mTextNoteText = (EditText) findViewById(R.id.text_note_text);

        if(!mIsNewNote)
            getLoaderManager().iniLoader(LOADER_NOTES, null,this);

    }

    private void loadCourseData() {
        //gettting/connecting to database
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        String[] courseColumn = {CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID};
        //query  rows
        Cursor cursor = db.query(CourseInfoEntry.TABLE_NAME, courseColumn,
                null,null,null,null,
                CourseInfoEntry.COLUMN_COURSE_TITLE);
        //assosiating adapter n cursors
        simpleCursorAdapter.changeCursor(cursor);
    }

    //query for particular note
    private void loadNoteData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        String  selection = NoteInfoEntry._ID +  "= ? ";

        String [] selectionArgs = {Integer.toString(mNoteID)};

        String [] noteColumns = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.SQL_CREATE_TABLE
        };
        mNoteCursor = db.query(NoteInfoEntry.TABLE_NAME,noteColumns,selection,selectionArgs,
                null,null,null);

        //position of column for cursor of notes
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);

        mNoteCursor.moveToNext();
        displayNote();



    }

    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        mOriginalNoteCourseId = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
    }

    private void saveOriginalNoteValues() {
        if(mIsNewNote)
            return;
        mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mOriginalNoteTitle = mNote.getTitle();
        mOriginalNoteText = mNote.getText();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsCancelling) {
            if(mIsNewNote) {
               deleteNoteFromDatabase();
            } else {
                storePreviousNoteValues();
            }
        } else {
            saveNote();
        }
    }

    private void deleteNoteFromDatabase() {
       final String selection = NoteInfoEntry._ID + "=?";
      final   String[] selectionargs = {Integer.toString(mNoteID)};

        @SuppressLint("StaticFieldLeak") AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
                db.delete(NoteInfoEntry.TABLE_NAME,selection,selectionargs);

                return null;
            }
        };
        task.execute();
       }

    private void storePreviousNoteValues() {

        CourseInfo course = DataManager.getInstance().getCourse(mOriginalNoteCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mOriginalNoteTitle);
        mNote.setText(mOriginalNoteText);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_NOTE_COURSE_ID, mOriginalNoteCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE, mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, mOriginalNoteText);
    }

    private void saveNote() {
           String courseId = selectedCourseId();
        String noteTitle = mTextNoteTitle.getText().toString();
        String  noteText = mTextNoteText.getText().toString();
        //updating note
        saveNoteToDatabase(courseId,noteTitle,noteText);
    }

    private String selectedCourseId() {
        //spinner position
        int selectPosition = mSpinnerCourses.getSelectedItemPosition();
        //course id correspondence
        Cursor cursor = simpleCursorAdapter.getCursor();
        cursor.moveToPosition(selectPosition);

        //index column
        int courseIdPos  = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        String  courseId = cursor.getString(courseIdPos);
        return courseId;
    }

    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText){
        //identify which note to update
        String selection = NoteInfoEntry._ID + " = ?";
        String [] selcetionArgs  = {Integer.toString(mNoteID)};
           //provide id to columns
        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID,courseId);
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE,noteTitle);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT,noteText);

        //connect to the database
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        db.update(NoteInfoEntry.TABLE_NAME,values,selection,selcetionArgs);
    }

    private void  displayNote() {
        String courseId  = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);

        //List<CourseInfo> courses = DataManager.getInstance().getCourses();
        //CourseInfo courseInfo = DataManager.getInstance().getCourse(courseId);
        int courseIndex = getIndexOfCourseId(courseId);

        spinnerCourses.setSelection(courseIndex);
        textNoteTitle.setText(mNote.getTitle());
        textNoteText.setText(mNote.getText());


    }

    private int getIndexOfCourseId(String courseId) {
        Cursor cursor = simpleCursorAdapter.getCursor();
        //check which course id matches the selected notes
        int courseIdPos =cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseRowIndex = 0;

        boolean more = cursor.moveToFirst();
        while (more){
            String cursorCourseId = cursor.getString(courseIdPos);
            if (courseId.equals(cursorCourseId));
               break;

            courseRowIndex++;
            more = cursor.moveToNext();
        }
        return courseRowIndex;
    }


    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mNoteID = intent.getIntExtra(NOTE_ID,ID_NOT_SET);
        mIsNewNote = mNoteID == ID_NOT_SET;
        if(mIsNewNote) {
            createNewNote();
        }
            Log.i(Tag,"mNoteId" + mNoteID);
//            mNote = DataManager.getInstance().getNotes().get(mNoteID);
        }


    private void createNewNote() {

      ContentValues values = new ContentValues();
      values.put(NoteInfoEntry.COLUMN_COURSE_ID,"");
      values.put(NoteInfoEntry.COLUMN_NOTE_TITLE,"");
      values.put(NoteInfoEntry.COLUMN_NOTE_TEXT,"");
        //inserting new data
      SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
      //cast return value to int
      mNoteID= (int)db.insert(NoteInfoEntry.TABLE_NAME,null,values);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        } else if (id == R.id.action_cancel) {
            mIsCancelling = true;
            finish();
        }
        else if (id == R.id.action_next){
            moveNext();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item  = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size()-1;
        item.setEnabled(mNotePosition < lastNoteIndex);

        return super.onPrepareOptionsMenu(menu);
    }

    //move next to save new note
    private void moveNext() {

        saveNote();
        //next note

        ++mNotePosition;
        mNote = DataManager.getInstance().getNotes().get(mNotePosition);
        //save original

        saveOriginalNoteValues();
        displayNote();

        invalidateOptionsMenu();
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String text = "Checkout what I learned in the Pluralsight course \"" +
                course.getTitle() +"\"\n" + mTextNoteText.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }

    @NonNull
    @Override
    //to create loader
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = null;
        if (id == LOADER_NOTES)
            loader = createLoaderNotes();
        else if (id == LOADER_COURSES)
            loader = createLoaderCourses();
        return loader;
    }

    private CursorLoader createLoaderCourses() {
        coursesQueryFinished = false;
        return new CursorLoader(this){
            @Override
            public Cursor loadInBackground() {
                //gettting/connecting to database
                SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
                String[] courseColumn = {CourseInfoEntry.COLUMN_COURSE_TITLE,
                        CourseInfoEntry.COLUMN_COURSE_ID,
                        CourseInfoEntry._ID};
                //query  rows
                return db.query(CourseInfoEntry.TABLE_NAME, courseColumn,
                        null,null,null,null,
                        CourseInfoEntry.COLUMN_COURSE_TITLE);

            }
        };
    }

    private CursorLoader createLoaderNotes() {
        //when query start is false
        mNotesQueryFinished = false;
        return  new CursorLoader(this){
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

                String  selection = NoteInfoEntry._ID +  "= ? ";

                String [] selectionArgs = {Integer.toString(mNoteID)};

                String [] noteColumns = {
                        NoteInfoEntry.COLUMN_COURSE_ID,
                        NoteInfoEntry.COLUMN_NOTE_TITLE,
                        NoteInfoEntry.SQL_CREATE_TABLE
                };
                return db.query(NoteInfoEntry.TABLE_NAME,noteColumns,selection,selectionArgs,
                        null,null,null);

            }
        };
    }

    @Override
    //to load data when ready/ results
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_NOTES)
            loadFinishedNotes(data);
        else if (loader.getId() == LOADER_COURSES){
            simpleCursorAdapter.changeCursor(data);
            //query false when start query
            coursesQueryFinished = true;
            //then true when finished
            displayNoteWhenQueriesFinished();
        }
    }

    private void loadFinishedNotes( Cursor data) {
        mNoteCursor = data;
        //position of column for cursor of notes
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);

        mNoteCursor.moveToNext();
        //when query is finished
        mNotesQueryFinished = true;
        displayNoteWhenQueriesFinished();

    }

    private void displayNoteWhenQueriesFinished() {
        if (mNotesQueryFinished && coursesQueryFinished)
            displayNote();
    }

    @Override
    //for cleaning things up
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
         if (loader.getId() == LOADER_NOTES){
             if (mNoteCursor != null)
                 mNoteCursor.close();
             }else if (loader.getId() == LOADER_COURSES){
             simpleCursorAdapter.changeCursor(null);
         }
    }
}
