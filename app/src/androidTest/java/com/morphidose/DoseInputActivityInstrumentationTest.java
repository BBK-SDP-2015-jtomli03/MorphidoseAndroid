package com.morphidose;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import static org.hamcrest.Matchers.not;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;

@RunWith(AndroidJUnit4.class)
public class DoseInputActivityInstrumentationTest {
    private static WifiManager wifiManager;
    private Prescription prescription = new Prescription("Dr Bean", "Sat, 9 Jul 2016", "Morphgesic SR Tablets", "5mg", "Oramorph 10mg/5ml", "2.5mg");
    private User user = new User("A1234", prescription);
    public DoseInputActivity doseInputActivity;

    public static boolean wifiConnected(){
        return wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
    }

    @Rule
    public ActivityTestRule<DoseInputActivity> doseInputActivityTestRule =
            new ActivityTestRule<DoseInputActivity>(DoseInputActivity.class);

    @BeforeClass
    public static void setUp() throws Exception{
        wifiManager = (WifiManager) InstrumentationRegistry.getTargetContext().getSystemService(Context.WIFI_SERVICE);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (!wifiConnected()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    @Test
    public void displaysTheBreakthroughDoseButton() throws Exception {
        onView(withId(R.id.breakthrough_dose)).check(matches(isDisplayed()));
    }

    @Test
    public void displaysTheRegularDoseButtonText() throws Exception {
        onView(withId(R.id.regular_dose)).check(matches(isDisplayed()));
    }

    @Test
    public void displaysTheSubmitDoseButton() throws Exception {
        onView(withId(R.id.update_prescription)).check(matches(isDisplayed()));
    }

    @Test
    public void displaysTheSubmitDoseButtonText() throws Exception {
        onView(withId(R.id.submit)).check(matches(isDisplayed()));
    }

    @Test
    public void setBottomMessageDisplaysCorrectBottomMessageIfDosesInDatabase() throws Exception {
        doseInputActivity = doseInputActivityTestRule.getActivity();

        doseInputActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                doseInputActivity.setDosesInDatabase(true);
            }
        });
        onView(withText(R.string.doses_to_send)).check(matches(isDisplayed()));
    }

    @Test
    public void setBottomMessageDisplaysCorrectBottomMessageIfNoDosesInDatabase() throws Exception {
        doseInputActivity = doseInputActivityTestRule.getActivity();
        doseInputActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                doseInputActivity.setDosesInDatabase(false);
            }
        });
        onView(withText(R.string.all_doses_sent)).check(matches(isDisplayed()));
    }

    @Test
    public void displaysTheBreakthroughDoseOnClickingBreakthroughDoseButtonIfWifiEnabled() throws Exception {
        if (!wifiConnected()) {
            wifiManager.setWifiEnabled(true);
        }
        doseInputActivity = doseInputActivityTestRule.getActivity();
        doseInputActivity.setUser(user);
        onView(withId(R.id.breakthrough_dose)).perform(click());
        onView(withText("Oramorph 10mg/5ml Solution")).check(matches(isDisplayed()));
        onView(withText("Take ONE 1.25ml dose when required for breakthrough pain")).check(matches(isDisplayed()));
        onView(withText("(Prescribed on: Sat, 9 Jul 2016)")).check(matches(isDisplayed()));
    }

    @Test
    public void displaysTheRegularDoseOnClickingRegularDoseButtonIfWifiEnabled() throws Exception {
        if (!wifiConnected()) {
            wifiManager.setWifiEnabled(true);
        }
        doseInputActivity = doseInputActivityTestRule.getActivity();
        doseInputActivity.setUser(user);
        onView(withId(R.id.regular_dose)).perform(click());
        onView(withText("Morphgesic SR Tablets 5mg")).check(matches(isDisplayed()));
        onView(withText("Take ONE tablet TWICE a day")).check(matches(isDisplayed()));
        onView(withText("(Prescribed on: Sat, 9 Jul 2016)")).check(matches(isDisplayed()));
    }

    @Test
    public void displaysTheBreakthroughDoseOnClickingBreakthroughDoseButtonIfWifiDisabled() throws Exception {
        if (wifiConnected()) {
            wifiManager.setWifiEnabled(false);
        }
        doseInputActivity = doseInputActivityTestRule.getActivity();
        doseInputActivity.setUser(user);
        onView(withId(R.id.breakthrough_dose)).perform(click());
        onView(withText("Oramorph 10mg/5ml Solution")).check(matches(isDisplayed()));
        onView(withText("Take ONE 1.25ml dose when required for breakthrough pain")).check(matches(isDisplayed()));
        onView(withText("(Prescribed on: Sat, 9 Jul 2016)")).check(matches(isDisplayed()));
    }

    @Test
    public void displaysTheRegularDoseOnClickingRegularDoseButtonIfWifiDisabled() throws Exception {
        if (wifiConnected()) {
            wifiManager.setWifiEnabled(false);
        }
        doseInputActivity = doseInputActivityTestRule.getActivity();
        doseInputActivity.setUser(user);
        onView(withId(R.id.regular_dose)).perform(click());
        onView(withText("Morphgesic SR Tablets 5mg")).check(matches(isDisplayed()));
        onView(withText("Take ONE tablet TWICE a day")).check(matches(isDisplayed()));
        onView(withText("(Prescribed on: Sat, 9 Jul 2016)")).check(matches(isDisplayed()));
    }

    @Test
    public void displaysConfirmationMessageIfSubmitDoseButtonClickedIfWifiDisabled() throws Exception {
        if (wifiConnected()) {
            wifiManager.setWifiEnabled(false);
        }
        onView(withId(R.id.update_prescription)).perform(click());
        onView(withText(R.string.confirm))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void displaysConfirmationMessageIfSubmitDoseButtonClickedIfWifiEnabled() throws Exception {
        if (!wifiConnected()) {
            wifiManager.setWifiEnabled(true);
        }
        onView(withId(R.id.update_prescription)).perform(click());
        onView(withText(R.string.confirm))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void returnsToUserHomePageIfSubmitDoseButtonClickedFollowedByCancel() throws Exception {
        onView(withId(R.id.update_prescription)).perform(click());
        onView(withText(R.string.cancel)).perform(click());
        onView(withId(R.id.update_prescription)).check(matches(isDisplayed()));
    }

    @Test
    public void displaysSuccessfulMessageIfSubmitDoseButtonClickedFollowedByConfirm() throws Exception {
        onView(withId(R.id.update_prescription)).perform(click());
        onView(withText(R.string.confirm)).perform(click());
        onView(withText(R.string.dose_submitted_success)).inRoot(withDecorView(not(doseInputActivityTestRule.getActivity().getWindow().getDecorView()))).check(matches(isDisplayed()));

    }

    @Test
    public void returnsToUserHomePageIfSubmitDoseButtonClickedFollowedByConfirm() throws Exception {
        onView(withId(R.id.update_prescription)).perform(click());
        onView(withText(R.string.confirm)).perform(click());
        onView(withId(R.id.update_prescription)).check(matches(isDisplayed()));
    }

    @Test
    public void bottomMessageSetCorrectlyIfDoseSubmittedWithNoWifi() throws Exception {
        if (wifiConnected()) {
            wifiManager.setWifiEnabled(false);
        }
        onView(withId(R.id.update_prescription)).perform(click());
        onView(withText(R.string.confirm)).perform(click());
        onView(withText(R.string.doses_to_send)).check(matches(isDisplayed()));
    }

}

