package com.example.mooood;

import android.app.Activity;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import android.view.View;
import android.widget.EditText;
import java.util.Calendar;
import com.robotium.solo.Solo;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * This test case is for
 *
 * US 02.01.01
 * As a participant, I want to express the reason why for a mood event using a brief textual
 * explanation (no more than 20 characters or 3 words).
 *
 */

@RunWith(AndroidJUnit4.class)
public class ReasonMoodEventTest {

    private Solo solo;
    private Calendar calendar;

    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class, true, true);

    /**
     * Runs before all tests and creates solo instance.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
    }

    /**
     * Gets the Activity
     * @throws Exception
     */
    @Test
    public void start() throws Exception {
        Activity activity = rule.getActivity();
    }

    @Test
    public void checkReason(){
        //go to Login
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        //enter username and password
        solo.enterText((EditText)solo.getView(R.id.activity_main_et__username), "eesayas");
        solo.waitForText("eesayas",1,2000);

        solo.enterText((EditText)solo.getView(R.id.activity_main_et__password), "lol");
        solo.waitForText("lol",1,2000);

        solo.clickOnView(solo.getView(R.id.activity_main_btn_submit));

        //go to UserFeedActivity (MoodEvent history)
        solo.waitForActivity(UserFeedActivity.class);

        //go to CreateEventActivity
        solo.clickOnView(solo.getView(R.id.fab));
        solo.waitForActivity(CreateEventActivity.class);

        //selects the first emoticon (default: "HAPPY")
        solo.clickOnView(solo.getView(R.id.select_emoticon_btn));

        //more than 20 characters
        solo.enterText((EditText) solo.getView(R.id.reason), "supercalifragilisticexpialidocious");
        solo.waitForText("supercalifragilisticexpialidocious", 1, 2000);

        solo.assertCurrentActivity("Wrong Activity", CreateEventActivity.class); //no redirect

        //more than 3 characters
        solo.clearEditText((EditText) solo.getView(R.id.reason));
        solo.enterText((EditText) solo.getView(R.id.reason), "a b c d");
        solo.waitForText("a b c d", 1, 2000);

        solo.assertCurrentActivity("Wrong Activity", CreateEventActivity.class); //no redirect

        //proper input
        solo.clearEditText((EditText) solo.getView(R.id.reason));
        solo.enterText((EditText) solo.getView(R.id.reason), "correct");
        solo.waitForText("correct", 1, 2000);

        //submit
        solo.clickOnView(solo.getView(R.id.submit_button));

        //item was successfully submitted
        solo.waitForActivity(UserFeedActivity.class);
        solo.assertCurrentActivity("Wrong Activity", UserFeedActivity.class);

        solo.sleep(2000); //for visual

        //delete created mood event for a clear db
        solo.scrollToTop();
        onView(withId(R.id.posts_list)).perform(RecyclerViewActions.actionOnItemAtPosition(0, swipeLeft()));
        onView(withId(R.id.posts_list)).perform(RecyclerViewActions.actionOnItemAtPosition(0, new ClickOnDeleteBtn()));
    }

    /**
     * Closes the activity after each test
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    /**
     * This is for clicking the child view (aka the delete btn) of the selected MoodEvent
     */
    public class ClickOnDeleteBtn implements ViewAction {

        ViewAction click = click();

        @Override
        public Matcher<View> getConstraints() {
            return null;
        }

        @Override
        public String getDescription() {
            return "Click on a child view with specified id.";
        }

        @Override
        public void perform(UiController uiController, View view) {
            click.perform(uiController, view.findViewById(R.id.menu));
        }
    }

}