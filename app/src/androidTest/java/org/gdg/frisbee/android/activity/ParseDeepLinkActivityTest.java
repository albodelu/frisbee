package org.gdg.frisbee.android.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.WindowManager;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.rule.AnimationAwareWonderTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.any;

@RunWith(AndroidJUnit4.class)
public class ParseDeepLinkActivityTest {
    public static final Uri URL_GDGROUPS_EVENT = Uri.parse("https://gdgroups.org/events/6256487006994432");
    public static final Uri URL_GDGROUPS_EVENT_INVALID = Uri.parse("https://gdgroups.org/events/xyz");

    @Rule
    public ActivityTestRule<ParseDeepLinkActivity> activityRule = new ActivityTestRule<>(ParseDeepLinkActivity.class, false, false);

    @Rule
    public AnimationAwareWonderTestRule animationRule = new AnimationAwareWonderTestRule();

    @Test
    public void givenGdgeventsLinkWillOpensEvent() {
        final Intent intent = new Intent(Intent.ACTION_VIEW, URL_GDGROUPS_EVENT);
        activityRule.launchActivity(intent);
        onView(withId(R.id.title)).check(matches(withText("GDG DevFest Sivas")));
    }

    @Test
    public void givenInvalidGdgeventsLinkDoesNotFail() {
        final Intent intent = new Intent(Intent.ACTION_VIEW, URL_GDGROUPS_EVENT_INVALID);
        activityRule.launchActivity(intent);
        onView(withId(R.id.title)).check(matches(withText(any(String.class))));
    }
}
