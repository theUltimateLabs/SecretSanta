package com.theultimatelabs.secretsanta;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

public class URLFetch extends AsyncTask<String, Void, String> { 
	 final String TAG = "URLFetch";
	 private String mHost;
	 
	 public URLFetch(String host) {
		 mHost = host;
	 }
	 
     protected String doInBackground(String... args) {
    	 
    	 if(args.length == 0){
    		 Log.e(TAG,"No Arguments");
    	 }
    	 String url = mHost + (String)args[0];
    	 StringEntity data = null;
    	 if (args.length >= 2) {
    		 try {
		    	 //Log.i(TAG,"Contents:"+data.toString());
    			Log.i(TAG,"Contents"+args[1]);
				data = new StringEntity(args[1]);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
    	 }
         HttpClient client = new DefaultHttpClient();
         
	     try {
	    	 HttpRequestBase request;
	    	 if (data==null) {
		    	 request = new HttpGet(new URI(url));
		     }
		     else {
		    	 request = new HttpPut(new URI(url));
		    	 ((HttpPut)request).setEntity(data);
		     }
	    	 Log.i(TAG,"Sending request to "+url);
	     	HttpResponse response = client.execute(request);
	     	if (response.getStatusLine().getStatusCode() == 200) {
		     	InputStream contentStream = response.getEntity().getContent();
		     	int contentLength = (int) response.getEntity().getContentLength();
		     	if (contentLength < 0) contentLength = 4096  ;
		     			
		     	byte[] contentBytes = new byte[contentLength];
		     	
		     	int realLength = Math.min(contentStream.read(contentBytes),contentLength);
		     	
		     	String contentString = new String(Arrays.copyOfRange(contentBytes,0,realLength),"UTF-8");
		     	Log.v(TAG,contentString);
		     	return contentString;
	     	}
	     	else {
	     		Log.e(TAG,response.getStatusLine().toString());
	     		Log.e(TAG,response.getStatusLine().getReasonPhrase());
	     	}
	     	return null;
	    } catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	     
	     return null;
	   
     }

     protected void onProgressUpdate(Integer... progress) {
         //setProgressPercent(progress[0]);
     }

     protected void onPostExecute(String result) {
    	 Log.i(TAG,"RESULT:"+result);
     }
}
