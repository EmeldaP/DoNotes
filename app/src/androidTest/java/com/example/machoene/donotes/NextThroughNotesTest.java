package com.example.machoene.donotes;

import android.support.test.rule.ActivityTestRule;

import androidx.core.widget.DrawerLayout;

import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static android.support.test.espresso.Espresso.*;


/**
 * Created by machoene on 2019-07-24.
 */
public class NextThroughNotesTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule(MainActivity.class);

    @Test
    public void NextThroughNotes() {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions);
        onView(withId(R.id.nav_notes)).perform(NavigationViewActions.navigateTo(R.id.nav_notes));

        //reyclerView
        onView(withId(R.id.list_items)).perform(new android. support.test.espresso.ViewAction[]{RecyclerViewActions.actionOnItemAtPosition(0, click())});
        List<NoteInfo> notes = DataManager.getInstance().getNotes();

        for (int index = 0; index < notes.size(); index++) {

            NoteInfo note = notes.get(index);
            //checking note is right selected on spinner and text
            onView(withId(R.id.spinner_courses)).check(matches(withSpinnerText(note.getCourse().getTitle())));
            onView(withId(R.id.text_note_text)).check(matches(withText(note.getText())));
            onView(withId(R.id.text_note_title)).check(matches(withText(note.getTitle())));

                if(index < notes.size() -1 )
            onView(allOf(withId(R.id.action_next),isEnabled())).perform(click());
            }
            onView(withId(R.id.action_next)).check(matches(not(isEnabled())));
             pressBack();

    }
}