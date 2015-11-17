package com.morphidose;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to provide utility methods for http connections.
 */
public class HttpUtility {
    private static HttpUtility httpUtility = new HttpUtility();
    private static final String URL_FOR_POST_USER = "http://10.240.14.81:9000/patient/prescription";
    private static final String URL_FOR_POST_DOSES = "http://10.240.14.81:9000/patient/doses";
    private RestTemplate restTemplate;

    private HttpUtility(){};

    public static HttpUtility getHttpUtility(){
        return httpUtility;
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

    public Prescription getUserPrescription(User user){
        try {
            //String hospitalNumber = '{"hospitalNumber":"' + params[0] + '"}';
            //HttpHeaders requestHeaders = new HttpHeaders();
            //requestHeaders.setContentType(new MediaType("application", "json"));
            //HttpEntity<String> requestEntity = new HttpEntity<String>(user, requestHeaders);
            //final String url = "http://192.168.1.69:9000/patient/prescription";
            return HttpUtility.getHttpUtility().getRestTemplate().postForObject(URL_FOR_POST_USER, user, Prescription.class);
        } catch (RestClientException e) {
            if(e.getMessage() == "patient.notfound"){
                return null;
            }else{
                Log.e("MainActivity", e.getMessage(), e);
            }
        }
        return null;
    }

    public Dose sendDoses(List<Dose> doses){
        //jsonMapper.configure(Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        try {
            //String hospitalNumber = '{"hospitalNumber":"' + params[0] + '"}';
            //HttpHeaders requestHeaders = new HttpHeaders();
            //requestHeaders.setContentType(new MediaType("application", "json"));
            //HttpEntity<String> requestEntity = new HttpEntity<String>(user, requestHeaders);
            //final String url = "http://192.168.1.69:9000/patient/prescription";

//            List<String> listToSend = new ArrayList<String>();
//            String dateInSeconds = "";
//            //****** NEED TO RESOLVE BELOW CODE AS SAYS ARRAYLIST EMPTY -> below code now tested with toast ****///
//            listToSend.add(doses.get(0).getHospitalNumber());
//            for(Dose dose: doses){
//                dateInSeconds = String.valueOf(dose.getDate().getTime());
//                listToSend.add(dateInSeconds.substring(0, dateInSeconds.length() - 3));
//            }
            //listToSend.add("A059ES21");
            //listToSend.add("1446291391");


            //return HttpUtility.getHttpUtility().getRestTemplate().postForObject(URL_FOR_POST_DOSES, doses, Dose.class);
            return HttpUtility.getHttpUtility().getRestTemplate().postForObject(URL_FOR_POST_DOSES, doses, Dose.class);
        } catch (RestClientException e) {
            if(e.getMessage() == "patient.notfound"){
                return null;
            }else{
                Log.e("sendDoses", e.getMessage(), e);
            }
        }
        return null;
    }

}
