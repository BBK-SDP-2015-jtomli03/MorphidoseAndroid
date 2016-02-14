package com.morphidose;

import android.test.suitebuilder.annotation.SmallTest;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SmallTest
public class DoseInputActivityTest{
    public static DoseInputActivity doseInputActivity;

    @BeforeClass
    public static void setUp(){
        doseInputActivity = new DoseInputActivity();
    }

    @Test
    public void testGetFormulationForTablet(){
        String result = doseInputActivity.getFormulation("MST MR Tablet");
        assertThat(result, is(equalTo("tablet")));
    }

    @Test
    public void testGetFormulationForTabletLowerCase(){
        String result = doseInputActivity.getFormulation("MST MR tablet");
        assertThat(result, is(equalTo("tablet")));
    }

    @Test
    public void testGetFormulationForCapsule(){
        String result = doseInputActivity.getFormulation("Zomorph MR Capsule");
        assertThat(result, is(equalTo("capsule")));
    }

    @Test
    public void testGetFormulationReturnsNullIfNoRelevantFormulation(){
        String result = doseInputActivity.getFormulation("Zomorph MR Caps");
        assertThat(result, is(equalTo(null)));
    }

    @Test
    public void testgetOramorphDoseForAWholeNumber(){
        String result = doseInputActivity.getOramorphDose("20mg");
        assertThat(result, is(equalTo("Take ONE 10ml dose when required for breakthrough pain")));
    }

    @Test
    public void testgetOramorphDoseForAFractionalNumber(){
        String result = doseInputActivity.getOramorphDose("5mg");
        assertThat(result, is(equalTo("Take ONE 2.5ml dose when required for breakthrough pain")));
    }

    @Test
    public void testgetOramorphDoseForAFractionalString(){
        String result = doseInputActivity.getOramorphDose("5.0mg");
        assertThat(result, is(equalTo("Take ONE 2.5ml dose when required for breakthrough pain")));
    }
}
