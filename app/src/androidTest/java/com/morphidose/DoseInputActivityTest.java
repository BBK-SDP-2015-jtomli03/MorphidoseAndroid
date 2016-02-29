package com.morphidose;


import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class DoseInputActivityTest {
    //private static WifiManager wifiManager;

    @Rule
    public ActivityTestRule<DoseInputActivity> mDoseInputActivityTestRule =
            new ActivityTestRule<DoseInputActivity>(DoseInputActivity.class);
//
//    @BeforeClass
//    public static void setUp() throws Exception {
//        //wifiManager = (WifiManager) InstrumentationRegistry.getTargetContext().getSystemService(Context.WIFI_SERVICE);
//    }
//
//    @AfterClass
//    public static void tearDown() throws Exception {
////        if (!wifiConnected()) {
////            wifiManager.setWifiEnabled(true);
////        }
//    }

    @Test
    public void noWifiStartsSetUpActivity() throws Exception {
        onView(withText(R.string.welcome)).check(matches(isDisplayed()));
    }

//    @Test
//    public void checkGoToRegisterUserViewButtonIsNotDisplayedUnlessNetworkConnectivity() throws Exception {
//        if (wifiConnected()) {
//            wifiManager.setWifiEnabled(false);
//        }
//        Thread.sleep(1000);
//        onView(withId(R.id.GoToRegisterUserView)).check(matches(isDisplayed()));
//    }
//
//    @Test public void clickingGoToRegisterUserViewButtonStartsRegisterActivity() throws Exception {
//        if (!wifiConnected()) {
//            wifiManager.setWifiEnabled(true);
//        }
//        Thread.sleep(5000);
//        onView(withId(R.id.GoToRegisterUserView)).perform(click());
//        onView(withId(R.id.ButtonSubmitHospitalNumber)).check(matches(isDisplayed()));
//    }

//    public static boolean wifiConnected(){
//        return wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
//    }
}
