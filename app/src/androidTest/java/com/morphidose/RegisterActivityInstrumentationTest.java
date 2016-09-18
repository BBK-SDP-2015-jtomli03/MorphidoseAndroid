package com.morphidose;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class RegisterActivityInstrumentationTest {
    private static WifiManager wifiManager;
    private static String hospitalNumber = "A051942";

    public static boolean wifiConnected(){
        return wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
    }

    @Rule
    public ActivityTestRule<RegisterActivity> registerActivityTestRule =
            new ActivityTestRule<RegisterActivity>(RegisterActivity.class);

    @BeforeClass
    public static void setUp() throws Exception {
        wifiManager = (WifiManager) InstrumentationRegistry.getTargetContext().getSystemService(Context.WIFI_SERVICE);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (!wifiConnected()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    @Test
    public void displaysTheEditTextField() throws Exception {
        if (!wifiConnected()) {
            wifiManager.setWifiEnabled(true);
        }
        onView(withId(R.id.EditTextHospitalNumber)).check(matches(isDisplayed()));
    }

    @Test public void cancelButtonOnSubmitHospitalNumberAlertReturnsToRegisterView() throws Exception {
        if (!wifiConnected()) {
            wifiManager.setWifiEnabled(true);
        }
        Thread.sleep(1000);
        onView(withId(R.id.EditTextHospitalNumber))
                .perform(typeText(hospitalNumber), closeSoftKeyboard());
        onView(withId(R.id.ButtonSubmitHospitalNumber)).perform(click());
        onView(withText(R.string.submit))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        onView(withText(R.string.cancel)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.EditTextHospitalNumber)).check(matches(isDisplayed()));
    }

    @Test public void displaysTheAlertDialogOnEnteringAHospitalNumberAndClickingSubmit() throws Exception {
        if (!wifiConnected()) {
            wifiManager.setWifiEnabled(true);
        }
        onView(withId(R.id.EditTextHospitalNumber))
                .perform(typeText(hospitalNumber), closeSoftKeyboard());
        onView(withId(R.id.ButtonSubmitHospitalNumber)).perform(click());
        onView(withText(R.string.submit))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        onView(withText(R.string.cancel)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.EditTextHospitalNumber)).check(matches(isDisplayed()));
    }

    @Test public void displaysTheNoWIFIAlertDialogIfNoWifi() throws Exception {
        if (wifiConnected()) {
            wifiManager.setWifiEnabled(false);
        }
        Thread.sleep(1000);
        onView(withId(R.id.EditTextHospitalNumber))
                .perform(typeText(hospitalNumber), closeSoftKeyboard());
        onView(withId(R.id.ButtonSubmitHospitalNumber)).perform(click());
        onView(withText(R.string.submit)).perform(click());
        onView(withText(R.string.no_wifi))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        onView(withText(R.string.ok)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.EditTextHospitalNumber)).check(matches(isDisplayed()));
    }

    @Test public void displaysTheUserNotFoundAlertBoxIfHospitalNumberDoesntExist() throws Exception {
        if (!wifiConnected()) {
            wifiManager.setWifiEnabled(true);
        }
        onView(withId(R.id.EditTextHospitalNumber))
                .perform(typeText(hospitalNumber), closeSoftKeyboard());
        Thread.sleep(3000);
        onView(withId(R.id.ButtonSubmitHospitalNumber)).perform(click());
        onView(withText(R.string.submit)).perform(click());
        onView(withText(R.string.user_not_found))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        onView(withText(R.string.ok)).perform(click());
        Thread.sleep(3000);
        onView(withId(R.id.EditTextHospitalNumber)).check(matches(isDisplayed()));
    }
}