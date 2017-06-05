package org.gdg.frisbee.android.activity;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.WindowManager;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.onboarding.FirstStartActivity;
import org.gdg.frisbee.android.rule.AnimationAwareWonderTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class FirstStartActivityTest {
    @Rule
    public ActivityTestRule<FirstStartActivity> activityRule = new ActivityTestRule<>(FirstStartActivity.class);

    @Rule
    public AnimationAwareWonderTestRule animationRule = new AnimationAwareWonderTestRule();

    @Test
    public void activityStarts() {
        onView(withId(R.id.viewSwitcher)).check(matches(isDisplayed()));
    }
}
