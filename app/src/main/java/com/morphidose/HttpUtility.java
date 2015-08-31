package com.morphidose;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * A utility class to provide utility methods for http connections.
 */
public class HttpUtility {
    private static HttpUtility httpUtility = new HttpUtility();
    private static final String MORPHIDOSE_URL = "http://192.168.1.69:9000/patient/prescription";
    private RestTemplate restTemplate;

    private HttpUtility(){};

    public static HttpUtility getHttpUtility(){
        return httpUtility;
    }

    public static String getUrl() {
        return MORPHIDOSE_URL;
    }

    public boolean isConnectedToInternet(ConnectivityManager cm){
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public RestTemplate getRestTemplate(){
        if(restTemplate == null){
           restTemplate = new RestTemplate();
           restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        }
        return restTemplate;
    }

}
