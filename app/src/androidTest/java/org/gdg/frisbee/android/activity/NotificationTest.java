package org.gdg.frisbee.android.activity;

import android.app.Notification;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;

import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.chapter.MainActivity;
import org.gdg.frisbee.android.eventseries.NotificationHandler;
import org.gdg.frisbee.android.eventseries.TaggedEventSeries;
import org.gdg.frisbee.android.rule.AnimationAwareWonderTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;

@RunWith(AndroidJUnit4.class)
public class NotificationTest {
    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);
    private UiDevice mDevice;

    @Rule
    public AnimationAwareWonderTestRule animationRule = new AnimationAwareWonderTestRule();

    @Before
    public void unlockScreen() {
        final MainActivity activity = activityRule.getActivity();
        Runnable wakeUpDevice = new Runnable() {
            public void run() {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        };
        activity.runOnUiThread(wakeUpDevice);
    }

    @Before
    public void setUp() throws Exception {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    @After
    public void tearDown() throws Exception {
        onView(isRoot()).perform(swipeUp());
    }

    @Test
    public void testNotificationForEventSeries() throws InterruptedException {
        NotificationManagerCompat nm = NotificationManagerCompat.from(activityRule.getActivity());

        int i = 0;
        for (TaggedEventSeries series : App.from(InstrumentationRegistry.getTargetContext()).currentTaggedEventSeries()) {
            Notification notification = new NotificationHandler(activityRule.getActivity(), series).createNotification();
            nm.notify(i, notification);

            mDevice.openNotification();
            Thread.sleep(2000);
            mDevice.findObject(By.textContains(activityRule.getActivity().getString(series.getTitleResId())))
                .click();
            Thread.sleep(2000);
            onView(allOf(
                withText(series.getTitleResId()),
                isDescendantOfA(isAssignableFrom(Toolbar.class)))
            ).check(matches(isDisplayed()));
        }
    }
}
