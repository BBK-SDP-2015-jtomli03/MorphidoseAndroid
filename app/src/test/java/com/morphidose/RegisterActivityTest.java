package com.morphidose;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class RegisterActivityTest {
    public RegisterActivity registerActivity;
    public Prescription prescription;

    @Before
    public void setUp(){
        registerActivity = new RegisterActivity();
        prescription = new Prescription("", "", "", "", "", "");
    }

    @Test
    public void testIsPrescriptionEmptyIfEmpty(){
        boolean result = registerActivity.isPrescriptionEmpty(prescription);
        assertThat(result, is(equalTo(true)));
    }

    @Test
    public void testIsPrescriptionEmptyIfNotEmpty(){
        prescription.setPrescriber("Dr Smith");
        boolean result = registerActivity.isPrescriptionEmpty(prescription);
        assertThat(result, is(equalTo(false)));
    }
}
