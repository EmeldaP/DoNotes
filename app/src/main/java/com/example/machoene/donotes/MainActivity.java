package com.example.machoene.donotes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.machoene.donotes.DoNotesDatabaseContract.CourseInfoEntry;
import com.example.machoene.donotes.DoNotesDatabaseContract.NoteInfoEntry;

import java.util.List;

import androidx.core.view.GravityCompat;
import androidx.core.widget.DrawerLayout;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import static com.example.machoene.donotes.NoteActivity.LOADER_NOTES;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NoteRecyclerAdapter mNoteAda;
    private RecyclerView mRecyclerItems;
    private LinearLayoutManager mManager;
    private CourseRecyclerAdapter courseRecyclerAdapter;
    private GridLayoutManager coursesLayoutManager;

    private DoNotesOpenHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbHelper = new DoNotesOpenHelper(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NoteActivity.class));

            }
        });
          //getting preferences from setting pref xml default setting
        PreferenceManager.setDefaultValues(this,R.xml.pref_general,false);
        PreferenceManager.setDefaultValues(this,R.xml.pref_notification,false);
        PreferenceManager.setDefaultValues(this,R.xml.pref_data_sync,false);



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initializeDisplayContent();
    }
    @Override
    protected void onDestroy(){
        mDbHelper.close();
        super.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //restartLoader ensure requery data after its initialize
        getLoaderManager().restartLoader(LOADER_NOTES,null,this);

              //each time app start or navigation setting has update it will save them
        updateNavHeader();
    }

    private void loadNotes() {
        SQLiteDatabase db  = mDbHelper.getReadableDatabase();
        final String[] noteColumns = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry._ID};

        //sorting by id and title double sorting
        String noteOrderBy = NoteInfoEntry.COLUMN_COURSE_ID + "," +
                NoteInfoEntry.COLUMN_NOTE_TITLE;
        final Cursor noteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns
                , null, null, null, null, noteOrderBy );

        mNoteAda.changeCursor(noteCursor);
    }

    private void updateNavHeader() {
        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        //to access textViews in navigation header
        View headerView = navigationView.getHeaderView(0);
        TextView textUserName = (TextView)headerView.findViewById(R.id.text_user_name);
        TextView textUserEmail = (TextView)headerView.findViewById(R.id.text_user_email);

              //access and display default text in nav view
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String userName = pref.getString("user_display_name","");
        String userEmail = pref.getString("user_email_adress", "");
        //set new textView names in nav setting
        textUserEmail.setText(userEmail);
        textUserName.setText(userName);
    }
    private void initializeDisplayContent() {

        DataManager.loadFromDatabase(mDbHelper);

        //associating recycler with our layout manager
        mRecyclerItems = (RecyclerView) findViewById(R.id.list_items);
        //associate adapter with layout manager
        mManager = new LinearLayoutManager(this);
                //layout manager for courses
        coursesLayoutManager = new GridLayoutManager(this,this,
                getResources().getInteger(R.integer.course_grid_span));


        //instance of noteInfo adapter
       // List<NoteInfo> noteInfos  = DataManager.getInstance().getNotes();
        mNoteAda =new NoteRecyclerAdapter(this, null);

        //instanciate courseInfo adapter for data
         List<CourseInfo> courseInfo = DataManager.getInstance().getCourses();
         courseRecyclerAdapter = new CourseRecyclerAdapter(this,courseInfo);



        displayNotes();
    }

    private void displayNotes() {
        mRecyclerItems.setLayoutManager(mManager);
        mRecyclerItems.setAdapter(mNoteAda);

        //connect database and disolay our notes info
       //SQLiteDatabase db = mDbHelper.getReadableDatabase();
             //display as selected item/menu
        selectNavigationMenuItem(R.id.nav_notes);

    }

    private void selectNavigationMenuItem(int id) {
        NavigationView nv = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = nv.getMenu();
              //select passes id
        menu.findItem(id).setChecked(true);
    }

    private void displayCourses(){
        mRecyclerItems.setLayoutManager(coursesLayoutManager);
        mRecyclerItems.setAdapter(courseRecyclerAdapter);

        selectNavigationMenuItem(R.id.nav_courses);


    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //intent straiight to settings
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_notes) {
            displayNotes();

        } else if (id == R.id.nav_courses) {
               displayCourses();

        } else if (id == R.id.nav_share) {
           // handleSelection("dont you think you've shared enough");
            handleShare();

        } else if (id == R.id.nav_send) {
            handleSelection("send");

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleShare() {
        View view = findViewById(R.id.list_items);
        Snackbar.make(view,"shareTo -" +
                PreferenceManager.getDefaultSharedPreferences(this).
                         getString("user_favourite_social" ,""),Snackbar.LENGTH_LONG).show();
    }

    private void handleSelection(String message) {
        //to display msg toast/snack bar
        View view = findViewById(R.id.list_items);
       Snackbar.make(view,message,Snackbar.LENGTH_LONG).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        if(id == LOADER_NOTES) {
            loader = new CursorLoader(this) {
                @Override
                public Cursor loadInBackground() {
                    SQLiteDatabase db = mDbHelper.getReadableDatabase();
                    final String[] noteColumns = {
                            NoteInfoEntry.getQName(NoteInfoEntry._ID),
                            NoteInfoEntry.COLUMN_NOTE_TITLE,
                            CourseInfoEntry.COLUMN_COURSE_TITLE
                           // NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID)
                    };
                    final String noteOrderBy = CourseInfoEntry.COLUMN_COURSE_TITLE +
                            "," + NoteInfoEntry.COLUMN_NOTE_TITLE;



                    //joining tables JOIN course_info  ON note_info.course_id= couse_onfo.corse_id
                    String tableWithJoin = NoteInfoEntry.TABLE_NAME  + "JOIN" +
                            CourseInfoEntry.TABLE_NAME + "ON" +
                            NoteInfoEntry.getQName( NoteInfoEntry.COLUMN_COURSE_ID )+ "=" +
                            CourseInfoEntry.getQName( CourseInfoEntry.COLUMN_COURSE_ID);

                    return db.query(tableWithJoin, noteColumns,
                            null, null, null, null, noteOrderBy);
                }
            };
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {
        if(loader.getId() == LOADER_NOTES)  {
            mNoteAda.changeCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        if(loader.getId() == LOADER_NOTES)  {
            mNoteAda.changeCursor(null);
        }

    }
}
