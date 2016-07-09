package com.morphidose;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.List;

/**
 * A utility class to provide utility methods for http connections.
 */
public class HttpUtility {
    private static HttpUtility httpUtility = new HttpUtility();
    private static final String BASE_URL = "http://localhost/patient/";
    private static final String URL_FOR_POST_USER = BASE_URL + "prescription";
    private static final String URL_FOR_POST_DOSES = BASE_URL + "doses";
    private RestTemplate restTemplate;

    private HttpUtility(){}

    public static HttpUtility getHttpUtility(){
        return httpUtility;
    }

    public boolean isConnectedToInternet(ConnectivityManager connectivityManager){
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public RestTemplate getRestTemplate(){
        if(restTemplate == null){
           restTemplate = new RestTemplate();
           restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        }
        return restTemplate;
    }

    public Prescription getUserPrescription(User user){
        try {
            return HttpUtility.getHttpUtility().getRestTemplate().postForObject(URL_FOR_POST_USER, user, Prescription.class);
        } catch (RestClientException e) {
            if(e.getMessage().equals("patient.notfound")){
                return null;
            }else{
                Log.e("RestClientException", " in HttpUtility.getUserPrescription. Error message; " + e.getMessage(), e);
            }
        }
        return null;
    }

    public Dose sendDoses(List<Dose> doses){
        try {
            return HttpUtility.getHttpUtility().getRestTemplate().postForObject(URL_FOR_POST_DOSES, doses, Dose.class);
        }catch(org.springframework.web.client.ResourceAccessException e){
            Log.e("HttpUtility.sendDoses", e.getMessage(), e);
            return null;
        }catch(RestClientException e){
            Log.e("HttpUtility.sendDoses", e.getMessage(), e);
            return null;
        }
    }
}
