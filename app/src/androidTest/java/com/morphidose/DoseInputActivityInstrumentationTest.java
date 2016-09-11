package com.morphidose;


import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(MockitoJUnitRunner.class)
@MediumTest
public class DoseInputActivityInstrumentationTest {
    private static WifiManager wifiManager;

    @Mock
    public MorphidoseDbHelper mDbHelper;

    public static boolean wifiConnected(){
        return wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
    }

    @Rule
    public ActivityTestRule<DoseInputActivity> mDoseInputActivityTestRule =
            new ActivityTestRule<DoseInputActivity>(DoseInputActivity.class);

    @BeforeClass
    public static void setUp() throws Exception {
        //wifiManager = (WifiManager) InstrumentationRegistry.getTargetContext().getSystemService(Context.WIFI_SERVICE);
        when(mDbHelper.getString(R.string.hello_word))
                .thenReturn(FAKE_STRING);
        ClassUnderTest myObjectUnderTest = new ClassUnderTest(mMockContext);
    }
//
//    @AfterClass
//    public static void tearDown() throws Exception {
//        if (!wifiConnected()) {
//            wifiManager.setWifiEnabled(true);
//        }
//    }
//
//    @Before
//    public static void setUp() throws Exception {
//        if (!wifiConnected()) {
//            wifiManager.setWifiEnabled(true);
//        }
//    }

//    @Test
//    public void noWifiStartsSetUpActivity() throws Exception {
//        onView(withText(R.string.welcome)).check(matches(isDisplayed()));
//    }


}

//can view breakthrough dose
//can view regular dose
//can submit doses offline
//can submit doses online
